import React, { useEffect, useState } from "react";
import { Box, Button, Container, MenuItem, Paper, TextField, Typography } from "@mui/material";
import Navbar from "../components/Navbar";
import { ProfileAPI } from "../api/api";

export default function TaxProfile() {
    const [form, setForm] = useState({ country: "RU", inn: "", benefits: "" });
    const [msg, setMsg] = useState("");

    useEffect(() => {
        ProfileAPI.get().then(res => setForm({ ...form, ...res.data })).catch(() => {});
        // eslint-disable-next-line
    }, []);

    const onChange = (k) => (e) => setForm({ ...form, [k]: e.target.value });

    const save = async () => {
        setMsg("");
        try {
            await ProfileAPI.save(form);
            setMsg("Профиль сохранён");
        } catch {
            setMsg("Ошибка сохранения");
        }
    };

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>Налоговый профиль</Typography>
                    <Box sx={{ display: "grid", gap: 2, maxWidth: 520 }}>
                        <TextField select label="Страна" value={form.country} onChange={onChange("country")}>
                            <MenuItem value="RU">Россия</MenuItem>
                            <MenuItem value="BY">Беларусь</MenuItem>
                        </TextField>
                        <TextField label="ИНН" value={form.inn || ""} onChange={onChange("inn")} />
                        <TextField label="Льготы (опционально)" value={form.benefits || ""} onChange={onChange("benefits")} />
                        <Button variant="contained" onClick={save}>Сохранить</Button>
                        {msg && <Typography>{msg}</Typography>}
                    </Box>
                </Paper>
            </Container>
        </>
    );
}
