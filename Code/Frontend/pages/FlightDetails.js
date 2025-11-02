import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api";
import terminalImage from "../assets/terminal.png";
import "../styles/FlightDetails.css";

const FlightDetails = () => {
    const { flightId } = useParams();
    const [flight, setFlight] = useState(null);
    const [expanded, setExpanded] = useState(true);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        fetchFlight();
    }, [flightId]);

    const fetchFlight = async () => {
        try {
            setLoading(true);
            const res = await api.get(`/flights/${flightId}`);
            setFlight(res.data);
        } catch (e) {
            console.error(e);
            setError("Ошибка загрузки данных рейса");
        } finally {
            setLoading(false);
        }
    };

    const getBoardingTimeInfo = () => {
        if (!flight?.scheduledDeparture) return null;
        const boardingEnd = new Date(flight.scheduledDeparture);
        const now = new Date();
        const diffMinutes = Math.floor((boardingEnd - now) / 60000);

        if (diffMinutes <= 0) return { text: "Посадка закрыта", color: "red" };
        if (diffMinutes < 20) return { text: `Посадка заканчивается через ${diffMinutes} мин`, color: "red" };
        if (diffMinutes < 60) return { text: `Посадка заканчивается через ${diffMinutes} мин`, color: "orange" };
        return { text: `Посадка заканчивается через ${Math.floor(diffMinutes / 60)} ч ${diffMinutes % 60} мин`, color: "black" };
    };

    const addDelayToTime = (date, delayMinutes) => {
        if (!date || !delayMinutes) return date;
        return new Date(new Date(date).getTime() + delayMinutes * 60000);
    };

    if (loading) return <div className="loading">Загрузка...</div>;
    if (error) return <div className="error">{error}</div>;
    if (!flight) return <div className="error">Рейс не найден</div>;

    const cancelled = flight.status?.toLowerCase() === "cancelled";
    const arrived = flight.status?.toLowerCase() === "arrived";
    const delayed = flight.delayMinutes && flight.delayMinutes > 0;
    const boardingInfo = !cancelled && !arrived ? getBoardingTimeInfo() : null;

    const adjustedDeparture = arrived && delayed
        ? addDelayToTime(flight.scheduledDeparture, flight.delayMinutes)
        : flight.actualDeparture;

    const adjustedArrival = arrived && delayed
        ? addDelayToTime(flight.scheduledArrival, flight.delayMinutes)
        : flight.actualArrival;

    return (
        <div className="flight-container">
            {/* 🛫 Верхний блок */}
            <div className="flight-header">
                <h2>{flight.departureAirport?.name}</h2>
                <p>{flight.departureAirport?.iataCode} Elev. {flight.departureAirport?.altitude}ft</p>
            </div>

            {/* 📦 Контейнер с данными и картинкой */}
            <div className="details-layout">
                <div className="details-left">
                    {/* 🗺️ Схема терминала (на мобилке — будет тут) */}
                    <div className="terminal-mobile">
                        <img src={terminalImage} alt="Terminal map" className="terminal-image" />
                    </div>

                    {/* ✈️ Информация о рейсе */}
                    <div className="route-block">
                        <h3>{flight.arrivalAirport?.city} ({flight.flightNumber})</h3>
                        <button className="toggle-button" onClick={() => setExpanded(!expanded)}>
                            {expanded ? "▲" : "▼"}
                        </button>
                    </div>

                    {/* 📊 Таблица */}
                    {expanded && (
                        <div className="table">
                            {!cancelled ? (
                                <>
                                    <div className="row">
                                        <div className="cell with-border">
                                            <strong>По расписанию вылет:</strong>
                                            <div>{formatDate(flight.scheduledDeparture)}</div>
                                        </div>
                                        <div className="cell">
                                            <strong>Фактический вылет:</strong>
                                            <div>
                                                {arrived
                                                    ? delayed
                                                        ? formatDate(adjustedDeparture)
                                                        : "—"
                                                    : adjustedDeparture
                                                        ? formatDate(adjustedDeparture)
                                                        : "—"}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="row">
                                        <div className="cell with-border">
                                            <strong>По расписанию прибытие:</strong>
                                            <div>{formatDate(flight.scheduledArrival)}</div>
                                        </div>
                                        <div className="cell">
                                            <strong>Фактическое прибытие:</strong>
                                            <div>
                                                {arrived
                                                    ? delayed
                                                        ? formatDate(adjustedArrival)
                                                        : "—"
                                                    : adjustedArrival
                                                        ? formatDate(adjustedArrival)
                                                        : "—"}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="row">
                                        <div className="cell">
                                            <strong>Статус рейса:</strong>
                                            <div>{flight.status || "—"}</div>
                                            {delayed && (
                                                <div className="delay-text">
                                                    Задержка: {flight.delayMinutes} мин
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    <div className="row">
                                        <div className="cell with-border">
                                            <strong>Авиакомпания:</strong>
                                            <div>{flight.airline}</div>
                                        </div>
                                        <div className="cell">
                                            <strong>Рейс №:</strong>
                                            <div>{flight.flightNumber}</div>
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <div className="cancelled-block">🛑 Рейс отменён. Данные недоступны.</div>
                            )}
                        </div>
                    )}

                    {!cancelled && !arrived && (
                        <div className="gate-block">Посадка на рейс из гейта {flight.gate || "—"}</div>
                    )}

                    {!cancelled && !arrived && boardingInfo && (
                        <div className="boarding-time" style={{ color: boardingInfo.color }}>
                            {boardingInfo.text}
                        </div>
                    )}

                    {arrived && <div className="completed-footer">✈️ Посадка завершена — рейс прибыл.</div>}
                    {cancelled && <div className="cancelled-footer">✈️ Рейс отменён — посадка не производится.</div>}

                    <button className="back-button" onClick={() => navigate(-1)}>← Назад</button>
                </div>

                {/* 🖥️ Картинка сбоку (появляется только на ПК) */}
                <div className="details-right">
                    <img src={terminalImage} alt="Terminal map" className="terminal-image" />
                </div>
            </div>
        </div>
    );
};

const formatDate = (date) => {
    if (!date) return "—";
    return new Date(date).toLocaleString("ru-RU", {
        day: "2-digit",
        month: "short",
        hour: "2-digit",
        minute: "2-digit",
    });
};

export default FlightDetails;
