import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import airplaneImage from "../assets/plane.png";
import { useNavigate } from "react-router-dom";
import { subscribeUserToPush } from "../utils/pushManager";
import "../styles/Home.css";

const Home = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [recentFlights, setRecentFlights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const { currentUser } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        loadRecentViews();
    }, []);

    const loadRecentViews = async () => {
        if (!currentUser) return;
        try {
            setLoading(true);
            const response = await api.get("/api/flight-views/my/recent?limit=20");
            let views = Array.isArray(response.data) ? response.data : [];

            const uniqueMap = new Map();
            for (const view of views) {
                const flightId = view.flight?.id;
                if (!flightId) continue;
                const existing = uniqueMap.get(flightId);
                if (!existing || new Date(view.viewedAt) > new Date(existing.viewedAt)) {
                    uniqueMap.set(flightId, view);
                }
            }

            const uniqueSorted = Array.from(uniqueMap.values())
                .sort((a, b) => new Date(b.viewedAt) - new Date(a.viewedAt))
                .slice(0, 4);

            setRecentFlights(uniqueSorted);
        } catch (error) {
            console.error("Error loading recent views:", error);
            setError("Не удалось загрузить историю просмотров");
            setRecentFlights(getFallbackFlights());
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!searchQuery.trim()) {
            loadRecentViews();
            return;
        }

        try {
            setLoading(true);
            const response = await api.get(
                `/flights/search?query=${encodeURIComponent(searchQuery)}&userEmail=${encodeURIComponent(currentUser.email)}`
            );
            const flights = Array.isArray(response.data) ? response.data : [];

            const wrapped = flights.map((f) => ({
                id: f.id,
                flight: { ...f, subscribed: f.subscribed ?? false },
                viewCount: 0,
                viewedAt: null,
            }));

            setRecentFlights(wrapped);
        } catch (error) {
            console.error("Search error:", error);
            setError("Ошибка при поиске рейсов");
        } finally {
            setLoading(false);
        }
    };

    const toggleSubscription = async (flightId, currentStatus, flightNumber) => {
        try {
            if (currentStatus) {
                await api.post(`/api/subscriptions/unsubscribe?flightNumber=${flightNumber}`);
            } else {
                const subscription = await subscribeUserToPush();
                if (!subscription) {
                    alert("❌ Разрешите уведомления в браузере.");
                    return;
                }
                await api.post(`/api/subscriptions/subscribe`, {
                    flightId,
                    endpoint: subscription.endpoint,
                    p256dh: subscription.keys.p256dh,
                    auth: subscription.keys.auth,
                });
            }

            setRecentFlights((prev) =>
                prev.map((view) =>
                    view?.flight?.id === flightId
                        ? { ...view, flight: { ...view.flight, subscribed: !currentStatus } }
                        : view
                )
            );
        } catch (error) {
            console.error("Subscription error:", error);
            setError("Ошибка при изменении подписки");
        }
    };

    const getFallbackFlights = () => [
        {
            id: 1,
            flight: {
                id: 1,
                flightNumber: "SU1160",
                departureAirport: { city: "Москва", iataCode: "SVO" },
                arrivalAirport: { city: "Стамбул", iataCode: "IST" },
                subscribed: true,
            },
            viewedAt: new Date().toISOString(),
            viewCount: 1,
        },
    ];

    if (loading) {
        return (
            <div className="home-container">
                <div className="home-airplane-section">
                    <img src={airplaneImage} alt="Airplane" className="home-airplane-image" />
                </div>
                <div className="home-loading-text">Загрузка истории просмотров...</div>
            </div>
        );
    }

    return (
        <div className="home-container">
            <div className="home-airplane-section">
                <div className="home-airplane-container">
                    <img src={airplaneImage} alt="Airplane" className="home-airplane-image" />
                </div>
            </div>

            <div className="home-search-section">
                <form onSubmit={handleSearch} className="home-search-form">
                    <div className="home-search-container">
                        <input
                            type="text"
                            placeholder="Поиск рейсов по номеру, городу или аэропорту..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="home-search-input"
                        />
                        <button type="submit" className="home-search-button">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="white">
                                <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z" />
                            </svg>
                        </button>
                    </div>
                </form>
            </div>

            {error && (
                <div className="home-error-box">
                    {error}
                    <button onClick={() => setError("")} className="home-error-close">
                        ×
                    </button>
                </div>
            )}

            <div className="home-recent-section">
                <div className="home-section-header">
                    <h2 className="home-section-title">
                        {searchQuery ? "Результаты поиска" : "Недавно просмотренные рейсы"}
                    </h2>
                    <button onClick={loadRecentViews} className="home-refresh-button">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="#7EBFFF">
                            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z" />
                        </svg>
                    </button>
                </div>

                {recentFlights.length === 0 ? (
                    <div className="home-empty-state">
                        <h3>{searchQuery ? "Рейсы не найдены" : "История просмотров пуста"}</h3>
                        <p>{searchQuery ? "Попробуйте другой запрос" : "Начните поиск рейсов"}</p>
                    </div>
                ) : (
                    <div className="home-flights-list">
                        {recentFlights.map((viewHistory) => {
                            const flight = viewHistory?.flight;
                            if (!flight) return null;

                            return (
                                <div
                                    key={viewHistory.id}
                                    className="home-flight-item"
                                    onClick={() => navigate(`/flights/${flight.id}`)}
                                >
                                    <div className="home-flight-info">
                                        <div className="home-flight-header">
                                            <span className="home-flight-number">{flight?.flightNumber || "—"}</span>
                                        </div>
                                        <span className="home-flight-route">
                                            {flight?.departureAirport?.city} ({flight?.departureAirport?.iataCode}) →{" "}
                                            {flight?.arrivalAirport?.city} ({flight?.arrivalAirport?.iataCode})
                                        </span>
                                    </div>

                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            toggleSubscription(flight.id, flight?.subscribed, flight?.flightNumber);
                                        }}
                                        className="home-heart-button"
                                        aria-label={flight?.subscribed ? "Отписаться" : "Подписаться"}
                                    >
                                        {flight?.subscribed ? (
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="black">
                                                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                            </svg>
                                        ) : (
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="black" strokeWidth="2">
                                                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Home;
