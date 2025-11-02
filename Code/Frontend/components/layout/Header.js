import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../../styles/Header.css';

const Header = () => {
    const { currentUser, logout } = useAuth();
    const navigate = useNavigate();

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isBurgerOpen, setIsBurgerOpen] = useState(false);
    const profileMenuRef = useRef(null);
    const burgerMenuRef = useRef(null);

    // 📍 состояние для города и ошибок геолокации
    const [city, setCity] = useState("Определение...");
    const [geoError, setGeoError] = useState("");

    // Закрытие меню при клике вне
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (profileMenuRef.current && !profileMenuRef.current.contains(event.target)) {
                setIsMenuOpen(false);
            }
            if (burgerMenuRef.current && !burgerMenuRef.current.contains(event.target)) {
                setIsBurgerOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // 📍 Геолокация и определение города
    useEffect(() => {
        if (!("geolocation" in navigator)) {
            setGeoError("Геолокация недоступна");
            setCity("Москва");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            async (pos) => {
                const { latitude, longitude } = pos.coords;
                try {
                    const res = await fetch(
                        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=10&addressdetails=1`
                    );
                    const data = await res.json();
                    const cityName =
                        data.address.city ||
                        data.address.town ||
                        data.address.village ||
                        data.address.state ||
                        "Неизвестно";
                    setCity(cityName);
                } catch (e) {
                    console.error("Ошибка при определении города:", e);
                    setCity("Неизвестно");
                }
            },
            () => {
                setGeoError("Не удалось определить местоположение");
                setCity("Москва");
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 20000 }
        );
    }, []);

    const handleLogout = () => {
        logout();
        navigate('/login');
        setIsMenuOpen(false);
    };

    const handleProfileClick = () => {
        navigate('/profile');
        setIsMenuOpen(false);
    };

    const handleMyFlightsClick = () => {
        navigate('/my-flights');
        setIsMenuOpen(false);
    };

    const handleHomeClick = () => {
        navigate('/');
        setIsBurgerOpen(false);
    };

    return (
        <header className="header">
            {/* 🍔 Бургер-меню */}
            <div className="burger-container" ref={burgerMenuRef}>
                <button className="burger-button" onClick={() => setIsBurgerOpen(!isBurgerOpen)}>
                    <div className="burger-icon">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                </button>

                {isBurgerOpen && (
                    <div className="burger-dropdown">
                        <button onClick={handleHomeClick} className="burger-item">
                            Главная
                        </button>
                        <div className="menu-divider"></div>
                        <button onClick={handleMyFlightsClick} className="burger-item">
                            Мои рейсы
                        </button>
                    </div>
                )}
            </div>

            {/* 📍 Местоположение */}
            <div className="location">
                <svg className="location-icon" viewBox="0 0 24 24" fill="#7EBFFF">
                    <path
                        d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                </svg>
                <span className="city-text">{city}</span>
            </div>

            {/* 👤 Профиль */}
            <div className="profile-container" ref={profileMenuRef}>
                <button
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                    className="profile-button"
                    aria-label="Меню профиля"
                >
                    <div className="profile-circle">
                        <svg className="profile-icon" viewBox="0 0 24 24" fill="#7EBFFF">
                            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4
                            1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8
                            1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                        </svg>
                    </div>
                </button>

                {isMenuOpen && (
                    <div className="dropdown-menu">
                        <div className="menu-header">
                            <div className="user-email">{currentUser?.email || 'Пользователь'}</div>
                            <div className="user-status">Активный</div>
                        </div>

                        <div className="menu-divider"></div>

                        <button onClick={handleProfileClick} className="menu-item">
                            Данные
                        </button>

                        <div className="menu-divider"></div>

                        <button onClick={handleLogout} className="menu-item logout">
                            Выход
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
