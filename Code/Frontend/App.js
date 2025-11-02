import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Header from './components/layout/Header';
import Home from './pages/Home';
import Flights from './pages/Flights';
import Subscriptions from './pages/Subscriptions';
import Profile from './pages/Profile';
import ProfilePage from "./pages/ProfilePage";
import MyFlights from "./pages/MyFlights";
import LoginPage from './pages/LoginPage';
import FlightTimeCalc from "./pages/FlightTimeCalc";
import FlightDetails from './pages/FlightDetails';
import './styles/App.css';
import { subscribeUserToPush } from "./utils/pushManager"; // ✅ импортируем утилиту для пушей

/* =========================
   Layout с автоматическим Header
   ========================= */
const Layout = ({ children }) => {
    const location = useLocation();
    const showHeader = location.pathname !== '/login';
    return (
        <div className="App">
            {showHeader && <Header />}
            <main>{children}</main>
        </div>
    );
};

/* =========================
   Защита маршрутов
   ========================= */
const ProtectedRoute = ({ children }) => {
    const { isAuthenticated } = useAuth();

    // 📡 При входе в систему — подписываем пользователя на push
    useEffect(() => {
        if (isAuthenticated) {
            subscribeUserToPush().catch(console.error);
        }
    }, [isAuthenticated]);

    return isAuthenticated ? children : <Navigate to="/login" replace />;
};

/* =========================
   Основное приложение
   ========================= */
function App() {
    // 🛠️ Регистрируем Service Worker при старте приложения
    useEffect(() => {
        if ("serviceWorker" in navigator) {
            window.addEventListener("load", () => {
                navigator.serviceWorker
                    .register("/service-worker.js")
                    .then((reg) => console.log("✅ Service Worker зарегистрирован:", reg.scope))
                    .catch((err) => console.error("❌ Ошибка регистрации Service Worker:", err));
            });
        }
    }, []);

    return (
        <AuthProvider>
            <Router>
                <Routes>
                    {/* 🏠 Главная страница */}
                    <Route
                        path="/"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <Home />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 🔑 Страница логина */}
                    <Route path="/login" element={<LoginPage />} />

                    {/* ✈️ Список всех рейсов */}
                    <Route
                        path="/flights"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <Flights />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 📍 Детали рейса по ID */}
                    <Route
                        path="/flights/:flightId"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <FlightDetails />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 📍 Детали рейса по номеру */}
                    <Route
                        path="/flights/number/:flightNumber"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <FlightDetails />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* ✈️ Мои рейсы */}
                    <Route
                        path="/my-flights"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <MyFlights />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 🕒 Страница расчёта времени */}
                    <Route
                        path="/flights/:flightId/calculate"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <FlightTimeCalc />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 👤 Профиль */}
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <ProfilePage />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 📬 Подписки */}
                    <Route
                        path="/subscriptions"
                        element={
                            <ProtectedRoute>
                                <Layout>
                                    <Subscriptions />
                                </Layout>
                            </ProtectedRoute>
                        }
                    />

                    {/* 🌐 Фолбэк */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
