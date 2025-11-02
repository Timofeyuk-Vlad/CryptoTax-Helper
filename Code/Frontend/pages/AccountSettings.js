import React, { useContext, useEffect, useState } from "react";
import {
    Box,
    Button,
    Container,
    Paper,
    TextField,
    Typography,
    Alert,
    CircularProgress,
} from "@mui/material";
import axios from "axios";
import Navbar from "../components/Navbar";
import { AuthContext } from "../context/AuthContext";

export default function AccountSettings() {
    const { user } = useContext(AuthContext);
    const [pwd, setPwd] = useState({ old: "", next: "" });

    // --- 2FA state ---
    const [loading, setLoading] = useState(false);
    const [is2FAEnabled, setIs2FAEnabled] = useState(false);
    const [qrVisible, setQrVisible] = useState(false);
    const [qrCodeUrl, setQrCodeUrl] = useState(null);
    const [verificationCode, setVerificationCode] = useState("");
    const [message, setMessage] = useState("");

    const token = localStorage.getItem("token");

    useEffect(() => {
        fetch2FAStatus();
    }, []);

    const fetch2FAStatus = async () => {
        try {
            setLoading(true);
            const res = await axios.get("http://localhost:8080/api/auth/2fa/status", {
                headers: { Authorization: `Bearer ${token}` },
            });
            setIs2FAEnabled(res.data.is2faEnabled);
        } catch (err) {
            console.error(err);
            setMessage("Ошибка при загрузке статуса 2FA");
        } finally {
            setLoading(false);
        }
    };

    const enable2FA = async () => {
        try {
            setLoading(true);
            const res = await axios.post(
                "http://localhost:8080/api/auth/2fa/setup",
                {},
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setQrCodeUrl(res.data.qrCodeUrl);
            setQrVisible(true);
            setMessage("Отсканируйте QR-код в приложении Google Authenticator");
        } catch (err) {
            console.error(err);
            setMessage("Ошибка при настройке 2FA");
        } finally {
            setLoading(false);
        }
    };

    const verify2FA = async () => {
        try {
            setLoading(true);
            const res = await axios.post(
                "http://localhost:8080/api/auth/2fa/verify",
                { verificationCode },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setMessage(res.data.message);
            setIs2FAEnabled(true);
            setQrCodeUrl(null);
            setQrVisible(false);
            setVerificationCode("");
        } catch (err) {
            console.error(err);
            setMessage("Неверный код верификации");
        } finally {
            setLoading(false);
        }
    };

    const disable2FA = async () => {
        try {
            setLoading(true);
            const res = await axios.post(
                "http://localhost:8080/api/auth/2fa/disable",
                {},
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setMessage(res.data.message);
            setIs2FAEnabled(false);
            setQrVisible(false);
            setQrCodeUrl(null);
        } catch (err) {
            console.error(err);
            setMessage("Ошибка при отключении 2FA");
        } finally {
            setLoading(false);
        }
    };

    const changePassword = async () => {
        // TODO: подключить настоящий endpoint
        alert(`Смена пароля (заглушка): ${pwd.old} → ${pwd.next}`);
    };

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 4 }}>
                <Paper sx={{ p: 4, maxWidth: 600, mx: "auto", border: "2px solid black" }}>
                    <Typography variant="h5" gutterBottom>
                        Настройки аккаунта
                    </Typography>

                    {message && (
                        <Alert
                            severity={message.toLowerCase().includes("ошибка") ? "error" : "info"}
                            sx={{ mb: 2 }}
                        >
                            {message}
                        </Alert>
                    )}

                    <Typography sx={{ mb: 2 }}>Email: {user?.email}</Typography>

                    {/* ---- Смена пароля ---- */}
                    <Box sx={{ display: "grid", gap: 2, mb: 3 }}>
                        <TextField
                            label="Старый пароль"
                            type="password"
                            value={pwd.old}
                            onChange={(e) => setPwd({ ...pwd, old: e.target.value })}
                        />
                        <TextField
                            label="Новый пароль"
                            type="password"
                            value={pwd.next}
                            onChange={(e) => setPwd({ ...pwd, next: e.target.value })}
                        />
                        <Button variant="contained" onClick={changePassword}>
                            Сменить пароль
                        </Button>
                    </Box>

                    {/* ---- 2FA ---- */}
                    <Typography variant="h6" sx={{ mb: 2 }}>
                        Двухфакторная аутентификация
                    </Typography>

                    {loading ? (
                        <CircularProgress size={30} />
                    ) : (
                        <>
                            {!is2FAEnabled && !qrVisible && (
                                <Button
                                    variant="outlined"
                                    fullWidth
                                    onClick={enable2FA}
                                    sx={{ border: "2px solid black", color: "black" }}
                                >
                                    Включить 2FA
                                </Button>
                            )}

                            {is2FAEnabled && !qrVisible && (
                                <Button
                                    variant="outlined"
                                    color="error"
                                    fullWidth
                                    onClick={disable2FA}
                                    sx={{ border: "2px solid black", color: "black" }}
                                >
                                    2FA включена — Отключить
                                </Button>
                            )}

                            {qrVisible && qrCodeUrl && (
                                <Box sx={{ mt: 3, textAlign: "center" }}>
                                    <Typography>Отсканируйте этот QR-код:</Typography>
                                    <img
                                        src={qrCodeUrl}
                                        alt="QR Code"
                                        style={{ margin: "15px 0", border: "2px solid black" }}
                                    />
                                    <TextField
                                        label="Введите код из приложения"
                                        fullWidth
                                        value={verificationCode}
                                        onChange={(e) => setVerificationCode(e.target.value)}
                                        sx={{ mb: 2 }}
                                    />
                                    <Button
                                        variant="contained"
                                        fullWidth
                                        onClick={verify2FA}
                                        sx={{ border: "2px solid black" }}
                                    >
                                        Подтвердить
                                    </Button>
                                </Box>
                            )}
                        </>
                    )}
                </Paper>
            </Container>
        </>
    );
}
