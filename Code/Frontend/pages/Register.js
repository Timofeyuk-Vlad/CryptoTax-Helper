import React, { useState } from "react";
import { Box, Button, Paper, TextField, Typography } from "@mui/material";
import { AuthAPI } from "../api/api";
import { useNavigate } from "react-router-dom";

export default function Register() {
    const [form, setForm] = useState({ email: "", password: "", confirmPassword: "", firstName: "", lastName: "" });
    const [err, setErr] = useState("");
    const [ok, setOk] = useState("");
    const navigate = useNavigate();

    const onChange = (k) => (e) => setForm({ ...form, [k]: e.target.value });

    const onSubmit = async (e) => {
        e.preventDefault();
        setErr(""); setOk("");
        try {
            await AuthAPI.register(form);
            setOk("Пользователь зарегистрирован. Теперь войдите.");
            setTimeout(() => navigate("/login"), 800);
        } catch (e) {
            const msg = e?.response?.data?.error || "Ошибка регистрации";
            setErr(msg);
        }
    };

    return (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 10 }}>
            <Paper sx={{ p: 4, width: 460 }}>
                <Typography variant="h5" gutterBottom>Регистрация</Typography>
                <Box component="form" onSubmit={onSubmit}>
                    <TextField label="Имя" fullWidth margin="normal" value={form.firstName} onChange={onChange("firstName")} />
                    <TextField label="Фамилия" fullWidth margin="normal" value={form.lastName} onChange={onChange("lastName")} />
                    <TextField label="Email" fullWidth margin="normal" value={form.email} onChange={onChange("email")} />
                    <TextField label="Пароль" type="password" fullWidth margin="normal" value={form.password} onChange={onChange("password")} />
                    <TextField label="Подтверждение пароля" type="password" fullWidth margin="normal" value={form.confirmPassword} onChange={onChange("confirmPassword")} />
                    {err && <Typography color="error">{err}</Typography>}
                    {ok && <Typography color="primary">{ok}</Typography>}
                    <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>Зарегистрироваться</Button>
                </Box>
            </Paper>
        </Box>
    );
}
