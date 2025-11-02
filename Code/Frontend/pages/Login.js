import React, { useState } from "react";
import {
    Box,
    Button,
    Container,
    Paper,
    TextField,
    Typography,
    Alert,
} from "@mui/material";
import axios from "axios";

export default function Login() {
    const [form, setForm] = useState({ email: "", password: "", code: "" });
    const [requires2FA, setRequires2FA] = useState(false);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const handleLogin = async () => {
        try {
            setLoading(true);
            setMessage("");

            const res = await axios.post("http://localhost:8080/api/auth/login", {
                email: form.email,
                password: form.password,
            });

            if (res.data.requires2fa) {
                setRequires2FA(true);
                setMessage("Введите код из приложения Google Authenticator");
            } else if (res.data.token) {
                localStorage.setItem("token", res.data.token);
                window.location.href = "/dashboard";
            }
        } catch (err) {
            setMessage(err.response?.data?.error || "Ошибка входа");
        } finally {
            setLoading(false);
        }
    };

    const handle2FALogin = async () => {
        try {
            setLoading(true);
            setMessage("");

            const res = await axios.post("http://localhost:8080/api/auth/login/2fa", {
                email: form.email,
                password: form.password,
                verificationCode: form.code,
            });

            if (res.data.token) {
                localStorage.setItem("token", res.data.token);
                window.location.href = "/dashboard";
            }
        } catch (err) {
            setMessage(err.response?.data?.error || "Неверный код 2FA");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container sx={{ mt: 10 }}>
            <Paper
                sx={{
                    maxWidth: 400,
                    mx: "auto",
                    p: 4,
                    border: "2px solid black",
                    textAlign: "center",
                }}
            >
                <Typography variant="h5" gutterBottom>
                    Вход в систему
                </Typography>

                {message && (
                    <Alert
                        severity={message.includes("Ошибка") ? "error" : "info"}
                        sx={{ mb: 2 }}
                    >
                        {message}
                    </Alert>
                )}

                <Box sx={{ display: "grid", gap: 2 }}>
                    <TextField
                        label="Email"
                        value={form.email}
                        onChange={(e) => setForm({ ...form, email: e.target.value })}
                        sx={{ input: { color: "black" } }}
                    />
                    <TextField
                        label="Пароль"
                        type="password"
                        value={form.password}
                        onChange={(e) => setForm({ ...form, password: e.target.value })}
                    />

                    {requires2FA && (
                        <TextField
                            label="Код 2FA"
                            value={form.code}
                            onChange={(e) => setForm({ ...form, code: e.target.value })}
                        />
                    )}

                    <Button
                        variant="contained"
                        onClick={requires2FA ? handle2FALogin : handleLogin}
                        disabled={loading}
                        sx={{
                            border: "2px solid black",
                            color: "black",
                            backgroundColor: "white",
                            "&:hover": { backgroundColor: "#f2f2f2" },
                        }}
                    >
                        {requires2FA ? "Подтвердить вход" : "Войти"}
                    </Button>

                    <Typography variant="body2" sx={{ mt: 2 }}>
                        Ещё нет аккаунта?{" "}
                        <a href="/register" style={{ color: "black" }}>
                            Регистрация
                        </a>
                    </Typography>
                </Box>
            </Paper>
        </Container>
    );
}
