import React, { useEffect, useState } from "react";
import { Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import Navbar from "../components/Navbar";
import { ExchangeAPI } from "../api/api";

export default function ExchangeConnections() {
    const [list, setList] = useState([]);
    const [form, setForm] = useState({ exchange: "BINANCE", apiKey: "", apiSecret: "" });
    const [msg, setMsg] = useState("");

    const load = () => ExchangeAPI.list().then(res => setList(res.data)).catch(() => setList([]));
    useEffect(() => { load(); }, []);

    const connect = async () => {
        setMsg("");
        try {
            await ExchangeAPI.connect(form);
            setMsg("Подключено");
            setForm({ exchange: "BINANCE", apiKey: "", apiSecret: "" });
            load();
        } catch {
            setMsg("Ошибка подключения");
        }
    };

    const importBinance = async () => {
        setMsg("");
        try {
            await ExchangeAPI.importBinance();
            setMsg("Импорт запущен");
        } catch {
            setMsg("Ошибка импорта");
        }
    };

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Paper sx={{ p: 3, mb: 2 }}>
                    <Typography variant="h6" gutterBottom>Подключённые биржи</Typography>
                    <Box sx={{ mb: 1 }}>
                        {list?.length ? list.map((c, i) => (
                            <Typography key={i}>• {c.exchange} — {c.status || "OK"}</Typography>
                        )) : <Typography color="text.secondary">Нет подключений</Typography>}
                    </Box>
                    <Button variant="outlined" onClick={importBinance}>Импортировать сделки (Binance демо)</Button>
                    {msg && <Typography sx={{ mt: 1 }}>{msg}</Typography>}
                </Paper>

                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>Добавить подключение</Typography>
                    <Box sx={{ display: "grid", gap: 2, maxWidth: 520 }}>
                        <TextField label="Биржа" value={form.exchange} onChange={(e) => setForm({ ...form, exchange: e.target.value })} />
                        <TextField label="API Key" value={form.apiKey} onChange={(e) => setForm({ ...form, apiKey: e.target.value })} />
                        <TextField label="API Secret" value={form.apiSecret} onChange={(e) => setForm({ ...form, apiSecret: e.target.value })} />
                        <Button variant="contained" onClick={connect}>Подключить</Button>
                    </Box>
                </Paper>
            </Container>
        </>
    );
}
