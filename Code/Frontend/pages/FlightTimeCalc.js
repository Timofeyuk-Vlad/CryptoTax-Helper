import React, { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { MapContainer, TileLayer, Marker, useMap } from "react-leaflet";
import L from "leaflet";
import api from "../services/api";
import "../styles/FlightTimeCalc.css";

/* 📍 Иконки маркеров */
const userIcon = new L.Icon({
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41],
});

/* 📍 Автоподгон карты */
const FitBounds = ({ points }) => {
    const map = useMap();
    useEffect(() => {
        const valid = points.filter(Boolean);
        if (valid.length === 0) return;
        if (valid.length === 1) {
            map.setView(valid[0], 12);
        } else {
            map.fitBounds(valid, { padding: [40, 40] });
        }
    }, [points, map]);
    return null;
};

/* 📦 Основной компонент */
const FlightTimeCalc = () => {
    const { flightId } = useParams();
    const navigate = useNavigate();

    const [flight, setFlight] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [userPos, setUserPos] = useState(null);
    const [geoError, setGeoError] = useState("");

    const [airportParams] = useState({
        trafficMultiplier: 1.2,
        avgSpeedKmh: 32,
        checkinMin: 10,
        securityMin: 15,
        passportMin: 10,
        bufferMin: 20,
    });

    /* 📡 Загружаем данные рейса */
    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                const res = await api.get(`/flights/${flightId}`);
                setFlight(res.data);
            } catch (e) {
                console.error(e);
                setError("Не удалось загрузить данные рейса");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [flightId]);

    /* 📍 Определяем позицию пользователя */
    useEffect(() => {
        if (!("geolocation" in navigator)) {
            setGeoError("Геолокация недоступна, используется Москва.");
            setUserPos([55.751244, 37.618423]);
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (pos) => setUserPos([pos.coords.latitude, pos.coords.longitude]),
            () => {
                setGeoError("Не удалось определить местоположение. Используется Москва.");
                setUserPos([55.751244, 37.618423]);
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 20000 }
        );
    }, []);

    const airportCoords =
        (flight?.departureAirport?.latitude &&
            flight?.departureAirport?.longitude &&
            [flight.departureAirport.latitude, flight.departureAirport.longitude]) ||
        null;

    /* 📊 Статусы */
    const statusRaw = flight?.status || "";
    const statusLower = statusRaw.toLowerCase();
    const isCancelled = statusLower === "cancelled";
    const isDeparted = ["departed", "arrived", "landed"].includes(statusLower);
    const isDelayed = statusLower === "delayed";
    const isCritical = ["cancelled", "delayed", "diverted", "incident", "emergency"].includes(
        statusLower
    );

    const actualStr =
        isDelayed && flight?.estimatedDeparture
            ? `${formatTime(flight.estimatedDeparture)} ${
                flight?.delayMinutes ? `(+${flight.delayMinutes} мин)` : ""
            }`
            : flight?.actualDeparture
                ? formatTime(flight.actualDeparture)
                : "—";

    /* 🧠 Расчёт времени */
    const now = new Date();
    const { leaveBy, beAtGateBy, driveMinutes, procMinutesTotal } = useMemo(() => {
        if (isDeparted || isCancelled) return {};
        if (!airportCoords || !userPos || !flight?.scheduledDeparture) return {};

        const distKm = haversineKm(userPos, airportCoords);
        const baseDriveMin = (distKm / Math.max(airportParams.avgSpeedKmh, 5)) * 60;
        const driveMin = Math.ceil(baseDriveMin * airportParams.trafficMultiplier);
        const procMin =
            airportParams.checkinMin +
            airportParams.securityMin +
            airportParams.passportMin +
            airportParams.bufferMin;

        const gateTime = new Date(new Date(flight.scheduledDeparture).getTime() - minutes(30));
        const leave = new Date(gateTime.getTime() - minutes(driveMin + procMin));

        return { leaveBy: leave, beAtGateBy: gateTime, driveMinutes: driveMin, procMinutesTotal: procMin };
    }, [airportCoords, userPos, flight?.scheduledDeparture, airportParams, isDeparted, isCancelled]);

    if (loading) return <div className="loading">Загрузка…</div>;
    if (error) return <div className="error">{error}</div>;
    if (!flight) return <div className="error">Рейс не найден</div>;

    const titleLine = `${flight?.departureAirport?.city || ""} (${flight?.departureAirport?.iataCode || ""}) → ${
        flight?.arrivalAirport?.city || ""
    } (${flight?.arrivalAirport?.iataCode || ""}) (${flight?.flightNumber || ""})`;

    return (
        <div className="flighttime-container">
            {/* ✈️ Заголовок */}
            <div className="flighttime-header">
                <h2>{titleLine}</h2>
            </div>

            {/* 📦 Контент */}
            <div className="flighttime-layout">
                <div className="flighttime-left">
                    {/* Статус */}
                    <div className="flighttime-card">
                        <Row label="STATUS">
                            <div className="flighttime-status">
                                {isCritical && <span className="flighttime-dot" />}
                                <span>{statusRaw || "—"}</span>
                            </div>
                        </Row>
                        <Divider />
                        <Row label="SCHEDULED"><span>{formatTime(flight?.scheduledDeparture)}</span></Row>
                        <Divider />
                        <Row label="ACTUAL"><span>{actualStr}</span></Row>
                        <Divider />
                        <Row label="GATE"><span>{isCancelled ? "— (рейс отменён)" : flight?.gate || "—"}</span></Row>
                    </div>

                    {/* Рекомендации */}
                    <div className="flighttime-card">
                        <Row label="NOW"><span>{formatTime(now)}</span></Row>
                        <Divider />
                        {isCancelled || isDeparted ? (
                            <div style={{ padding: "0.75rem 0" }}>
                                <div>Ориентировочно выезжайте к <strong>—</strong></div>
                                <div style={{ color: "#6b7280" }}>(чтобы быть у гейта к <strong>—</strong>)</div>
                            </div>
                        ) : (
                            <div style={{ padding: "0.75rem 0" }}>
                                <div>Ориентировочно выезжайте к <strong>{leaveBy ? formatTime(leaveBy) : "—"}</strong></div>
                                <div style={{ color: "#6b7280" }}>(чтобы быть у гейта к <strong>{beAtGateBy ? formatTime(beAtGateBy) : "—"}</strong>)</div>
                                {driveMinutes && procMinutesTotal && (
                                    <div style={{ marginTop: 6, fontSize: 13, color: "#6b7280" }}>
                                        Дорога: ~{driveMinutes} мин • Процедуры в аэропорту: ~{procMinutesTotal} мин
                                    </div>
                                )}
                                {geoError && <div style={{ marginTop: 6, fontSize: 12, color: "#b91c1c" }}>{geoError}</div>}
                            </div>
                        )}
                    </div>

                    {/* 🖥️ Кнопка назад — только на ПК */}
                    <button className="flighttime-back-button desktop-only" onClick={() => navigate(-1)}>
                        ← Назад
                    </button>
                </div>

                {/* 🗺️ Карта */}
                <div className="flighttime-right">
                    <div className="flighttime-map-card">
                        <div className="flighttime-map-title">Местоположение и аэропорт</div>
                        <div className="flighttime-map-box">
                            <MapContainer
                                center={airportCoords || userPos || [55.751244, 37.618423]}
                                zoom={11}
                                style={{ height: "100%", width: "100%" }}
                                scrollWheelZoom
                            >
                                <TileLayer
                                    attribution="&copy; OpenStreetMap"
                                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                />
                                {userPos && <Marker position={userPos} icon={userIcon} />}
                                {airportCoords && <Marker position={airportCoords} icon={userIcon} />}
                                <FitBounds points={[userPos, airportCoords].filter(Boolean)} />
                            </MapContainer>
                        </div>
                    </div>

                    {/* 📱 Кнопка назад — только на мобильных */}
                    <button className="flighttime-back-button mobile-only" onClick={() => navigate(-1)}>
                        ← Назад
                    </button>
                </div>
            </div>
        </div>
    );
};

/* 🛠️ Утилиты */
const minutes = (n) => n * 60 * 1000;
const formatTime = (dateLike) =>
    dateLike
        ? new Date(dateLike).toLocaleTimeString("ru-RU", { hour: "2-digit", minute: "2-digit" })
        : "—";

const haversineKm = (a, b) => {
    if (!a || !b) return 0;
    const [lat1, lon1] = a;
    const [lat2, lon2] = b;
    const toRad = (x) => (x * Math.PI) / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const s1 =
        Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(s1), Math.sqrt(1 - s1));
    return R * c;
};

const Row = ({ label, children }) => (
    <div className="flighttime-row">
        <div className="flighttime-row-label">{label}</div>
        <div className="flighttime-row-value">{children}</div>
    </div>
);

const Divider = () => <div style={{ height: 1, background: "#e5e7eb", margin: "0.3rem 0" }} />;

export default FlightTimeCalc;
