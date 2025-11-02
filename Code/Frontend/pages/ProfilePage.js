import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import "../styles/ProfilePage.css";
import airplaneImage from "../assets/plane.png"; // ✈️ подключаем картинку

const ProfilePage = () => {
    const { currentUser } = useAuth();
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        phoneNumber: "",
        password: ""
    });
    const [passwordChanged, setPasswordChanged] = useState(false);
    const [originalData, setOriginalData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState("");

    useEffect(() => {
        if (currentUser) {
            loadUserData();
        }
    }, [currentUser]);

    const loadUserData = async () => {
        try {
            const res = await api.get(`/api/users/${currentUser.id}`);
            const data = res.data;
            setFormData({ ...data, password: "••••••••" });
            setOriginalData({ ...data, password: "••••••••" });
        } catch (error) {
            console.error("Ошибка загрузки данных пользователя:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));

        if (name === "password") {
            setPasswordChanged(true);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const payload = { ...formData };
        if (!passwordChanged) {
            delete payload.password;
        }

        try {
            await api.put(`/api/users/${currentUser.id}`, payload);
            setMessage("✅ Данные успешно обновлены!");
            setOriginalData({ ...payload, password: "••••••••" });
            if (!passwordChanged) {
                setFormData({ ...payload, password: "••••••••" });
            }
            setPasswordChanged(false);
        } catch (error) {
            console.error("Ошибка обновления данных:", error);
            setMessage("❌ Не удалось обновить данные");
        }
    };

    const hasChanges =
        originalData && JSON.stringify(formData) !== JSON.stringify(originalData);

    if (loading) return <div className="loading">Загрузка профиля...</div>;

    return (
        <div className="profile-page">
            {/* ✈️ Самолёт (будет виден только на мобильных через CSS) */}
            <div className="profile-airplane-section">
                <img src={airplaneImage} alt="Airplane" className="airplane-image" />
            </div>

            <h2 className="profile-title">Мой профиль</h2>

            <form onSubmit={handleSubmit} className="profile-form">
                <div className="form-group">
                    <label>Имя</label>
                    <input
                        type="text"
                        name="firstName"
                        value={formData.firstName || ""}
                        onChange={handleChange}
                    />
                </div>

                <div className="form-group">
                    <label>Фамилия</label>
                    <input
                        type="text"
                        name="lastName"
                        value={formData.lastName || ""}
                        onChange={handleChange}
                    />
                </div>

                <div className="form-group">
                    <label>Email</label>
                    <input
                        type="email"
                        name="email"
                        value={formData.email || ""}
                        disabled
                    />
                </div>

                <div className="form-group">
                    <label>Телефон</label>
                    <input
                        type="text"
                        name="phoneNumber"
                        value={formData.phoneNumber || ""}
                        onChange={handleChange}
                    />
                </div>

                <div className="form-group">
                    <label>Пароль</label>
                    <input
                        type="password"
                        name="password"
                        value={formData.password || ""}
                        onChange={handleChange}
                    />
                </div>

                <button
                    type="submit"
                    className={`save-button ${hasChanges ? "active" : "inactive"}`}
                    disabled={!hasChanges}
                >
                    Сохранить изменения
                </button>

                {message && <div className="message">{message}</div>}
            </form>
        </div>
    );
};

export default ProfilePage;
