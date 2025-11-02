import React, { useEffect, useState } from "react";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import airplaneImage from "../assets/plane.png";
import "../styles/MyFlights.css";

const MyFlights = () => {
    const { currentUser } = useAuth();
    const [flights, setFlights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        if (currentUser) {
            loadSubscribedFlights();
        }
    }, [currentUser]);

    const loadSubscribedFlights = async () => {
        try {
            setLoading(true);
            const res = await api.get(`/api/subscriptions/user/${currentUser.email}`);
            const subscriptions = res.data || [];

            const flightsFull = await Promise.all(
                subscriptions.map(async (sub) => {
                    try {
                        const flightRes = await api.get(`/flights/${sub.flight.id}`);
                        return { ...flightRes.data, subscribed: true };
                    } catch (e) {
                        console.warn("Ошибка загрузки данных рейса:", e);
                        return null;
                    }
                })
            );

            setFlights(flightsFull.filter(Boolean));
        } catch (err) {
            console.error("Ошибка загрузки подписок:", err);
            setError("Не удалось загрузить подписки");
        } finally {
            setLoading(false);
        }
    };

    const toggleSubscription = async (flightId, currentStatus, flightNumber) => {
        try {
            if (currentStatus) {
                await api.post(`/api/subscriptions/unsubscribe?flightNumber=${flightNumber}`);
            } else {
                await api.post(`/api/subscriptions/subscribe?flightId=${flightId}`);
            }
            await loadSubscribedFlights();
        } catch (error) {
            console.error("Ошибка изменения подписки:", error);
            setError("Не удалось изменить подписку");
        }
    };

    if (loading) return <div className="loading">Загрузка...</div>;

    return (
        <div className="myflights-container">
            <div className="myflights-airplane-section">
                <div className="airplane-container">
                    <img src={airplaneImage} alt="Airplane" className="airplane-image" />
                </div>
            </div>

            <h2 className="page-title">Мои рейсы</h2>

            {error && <div className="error-message">{error}</div>}

            {flights.length === 0 ? (
                <div className="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="#7EBFFF">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                    </svg>
                    <h3>У вас нет подписанных рейсов</h3>
                    <p>Подпишитесь на интересующие вас рейсы, чтобы они появились здесь</p>
                </div>
            ) : (
                <div className="flights-list">
                    {flights.map((flight) => (
                        <div
                            key={flight.id}
                            className="flight-item"
                            onClick={() => navigate(`/flights/${flight.id}`)}
                        >
                            <div className="flight-info">
                                <div className="flight-header">
                                    <span className="flight-number">{flight.flightNumber || "—"}</span>
                                </div>
                                <span className="flight-route">
                                    {flight?.departureAirport?.city} ({flight?.departureAirport?.iataCode}) →{" "}
                                    {flight?.arrivalAirport?.city} ({flight?.arrivalAirport?.iataCode})
                                </span>
                            </div>

                            <div className="right-side">
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        toggleSubscription(flight.id, flight.subscribed, flight.flightNumber);
                                    }}
                                    className="heart-button"
                                    aria-label={flight.subscribed ? "Отписаться от рейса" : "Подписаться на рейс"}
                                >
                                    {flight.subscribed ? (
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="black" stroke="black" strokeWidth="1">
                                            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                        </svg>
                                    ) : (
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="black" strokeWidth="2">
                                            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                        </svg>
                                    )}
                                </button>

                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        navigate(`/flights/${flight.id}/calculate`);
                                    }}
                                    className="calc-button"
                                >
                                    Рассчитать время
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default MyFlights;
