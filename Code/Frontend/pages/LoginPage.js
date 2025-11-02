import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import airplaneImage from '../assets/plane.png';
import { subscribeUserToPush } from "../utils/pushManager";
import api from "../services/api";
import "../styles/LoginPage.css";

const LoginPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login, register } = useAuth();
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e?.preventDefault();
        if (!email || !password) {
            setError('Please enter email and password');
            return;
        }

        setLoading(true);
        setError('');
        try {
            await login(email, password);
            navigate('/');
        } catch {
            setError('Login failed. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    const handleSignUp = async (e) => {
        e?.preventDefault();
        if (!email || !password || !firstName || !lastName || !phoneNumber) {
            setError('Все поля обязательны для заполнения');
            return;
        }

        setLoading(true);
        setError('');
        try {
            const payload = { email, password, firstName, lastName, phoneNumber };
            const response = await register(payload);

            if (!response.token) {
                setError(response.message || 'Регистрация прошла неуспешно');
                return;
            }

            await login(email, password);
            navigate('/');
        } catch (error) {
            if (error.response?.data?.error) {
                setError(error.response.data.error);
            } else {
                setError(error.message || 'Ошибка регистрации. Попробуйте снова.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            {/* 🔝 Верхняя секция */}
            <div className="login-top">
                <div className="login-content">
                    <h1 className="welcome-text">Welcome to</h1>
                    <h1 className="app-name">LowFlightZone</h1>
                    <div className="login-airplane-container">
                        <img src={airplaneImage} alt="Airplane" className="airplane-image" />
                    </div>
                </div>

                <div className="arcs-container">
                    <div className="arc white1"></div>
                    <div className="arc blue1"></div>
                    <div className="arc white2"></div>
                    <div className="arc blue2"></div>
                    <div className="arc blue0"></div>
                </div>
            </div>

            {/* 🔻 Нижняя секция */}
            <div className="login-bottom">
                <div className="bottom-content">
                    {error && (
                        <div className="error-box">
                            <strong>Error:</strong> {error}
                            <button onClick={() => setError('')} className="error-clear">×</button>
                        </div>
                    )}

                    <div className="form-container">
                        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} disabled={loading} required />
                        <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} disabled={loading} required />
                        {!isLogin && (
                            <>
                                <input type="text" placeholder="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} disabled={loading} required />
                                <input type="text" placeholder="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} disabled={loading} required />
                                <input type="tel" placeholder="Phone Number" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} disabled={loading} required />
                            </>
                        )}
                    </div>

                    {isLogin ? (
                        <>
                            <button onClick={handleLogin} disabled={loading} className="primary-btn">
                                {loading ? 'Signing in...' : 'Log in'}
                            </button>
                            <button onClick={() => setIsLogin(false)} disabled={loading} className="secondary-btn">
                                Sign up
                            </button>
                        </>
                    ) : (
                        <>
                            <button onClick={handleSignUp} disabled={loading} className="primary-btn">
                                {loading ? 'Creating account...' : 'Sign up'}
                            </button>
                            <button onClick={() => setIsLogin(true)} disabled={loading} className="secondary-btn">
                                Log in
                            </button>
                        </>
                    )}

                    <button className="help-link">Not able to login? Try here</button>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
