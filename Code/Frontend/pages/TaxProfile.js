import React, { useEffect, useState } from "react";
import {
    Box,
    Button,
    Container,
    MenuItem,
    Paper,
    TextField,
    Typography,
    Fade,
} from "@mui/material";
import Navbar from "../components/Navbar";
import { ProfileAPI } from "../api/api";

export default function TaxProfile() {
    const [form, setForm] = useState({
        country: "RUSSIA",
        taxIdentificationNumber: "",
        expectedAnnualIncome: "",
        bankAccountNumber: "",
    });
    const [msg, setMsg] = useState("");

    useEffect(() => {
        ProfileAPI.get()
            .then((res) => setForm(res.data))
            .catch(() => setMsg("⚠️ Не удалось загрузить профиль"));
    }, []);

    const onChange = (k) => (e) =>
        setForm({ ...form, [k]: e.target.value });

    const save = async () => {
        setMsg("");
        try {
            await ProfileAPI.save(form);
            setMsg("✅ Профиль успешно сохранён");
        } catch {
            setMsg("❌ Ошибка при сохранении профиля");
        }
    };

    return (
        <>
            <Navbar />
            <Container
                sx={{
                    minHeight: "100vh",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                }}
            >
                <Fade in timeout={400}>
                    <Paper sx={{ p: 4, width: "100%", maxWidth: 540, boxShadow: 4 }}>
                        <Typography variant="h5" gutterBottom align="center">
                            Налоговый профиль
                        </Typography>

                        <Box sx={{ display: "grid", gap: 2, mt: 2 }}>
                            <TextField
                                select
                                label="Страна"
                                value={form.country}
                                onChange={onChange("country")}
                                fullWidth
                            >
                                <MenuItem value="RUSSIA">Россия</MenuItem>
                                <MenuItem value="BELARUS">Беларусь</MenuItem>
                            </TextField>

                            <TextField
                                label="ИНН / Налоговый номер"
                                value={form.taxIdentificationNumber}
                                onChange={onChange("taxIdentificationNumber")}
                                helperText="Введите ваш ИНН или идентификационный номер"
                                fullWidth
                            />

                            <TextField
                                label="Ожидаемый годовой доход"
                                type="number"
                                value={form.expectedAnnualIncome}
                                onChange={onChange("expectedAnnualIncome")}
                                helperText="Укажите доход за год (в местной валюте)"
                                fullWidth
                            />

                            <TextField
                                label="Банковский счёт"
                                value={form.bankAccountNumber}
                                onChange={onChange("bankAccountNumber")}
                                fullWidth
                            />

                            <Button
                                variant="contained"
                                size="large"
                                onClick={save}
                                fullWidth
                                sx={{ mt: 1 }}
                            >
                                Сохранить
                            </Button>
                        </Box>

                        {msg && (
                            <Typography
                                align="center"
                                sx={{ mt: 2 }}
                                color={
                                    msg.startsWith("✅") ? "success.main" : "error.main"
                                }
                            >
                                {msg}
                            </Typography>
                        )}
                    </Paper>
                </Fade>
            </Container>
        </>
    );
}
