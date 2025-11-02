import React, { useEffect, useState } from "react";
import {
    Box,
    Button,
    Container,
    Paper,
    TextField,
    Typography,
    Fade,
} from "@mui/material";
import Navbar from "../components/Navbar";
import { ExchangeAPI } from "../api/api";

export default function ExchangeConnections() {
    const [list, setList] = useState([]);
    const [form, setForm] = useState({
        exchange: "BINANCE",
        apiKey: "",
        apiSecret: "",
    });
    const [msg, setMsg] = useState("");

    const load = () =>
        ExchangeAPI.list()
            .then((res) => setList(res.data))
            .catch(() => setList([]));

    useEffect(() => {
        load();
    }, []);

    const connect = async () => {
        setMsg("");
        try {
            await ExchangeAPI.connect(form);
            setMsg("✅ Биржа подключена");
            setForm({ exchange: "BINANCE", apiKey: "", apiSecret: "" });
            load();
        } catch {
            setMsg("❌ Ошибка подключения");
        }
    };

    const importFromExchange = async (id) => {
        setMsg("");
        try {
            await ExchangeAPI.importBinance(id);
            setMsg(`📦 Импорт с подключения #${id} запущен`);
        } catch {
            setMsg("Ошибка импорта");
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
                    flexDirection: "column",
                    gap: 3,
                }}
            >
                <Fade in timeout={400}>
                    <Paper sx={{ p: 4, width: "100%", maxWidth: 600, boxShadow: 4 }}>
                        <Typography variant="h5" gutterBottom align="center">
                            Подключённые биржи
                        </Typography>

                        <Box sx={{ mt: 2 }}>
                            {list.length ? (
                                list.map((c) => (
                                    <Box
                                        key={c.id}
                                        sx={{
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center",
                                            mb: 1,
                                        }}
                                    >
                                        <Typography>
                                            • {c.exchange} — {c.status || "OK"}
                                        </Typography>
                                        <Button
                                            variant="outlined"
                                            size="small"
                                            onClick={() => importFromExchange(c.id)}
                                        >
                                            Импортировать
                                        </Button>
                                    </Box>
                                ))
                            ) : (
                                <Typography color="text.secondary">
                                    Нет активных подключений
                                </Typography>
                            )}
                        </Box>

                        {msg && (
                            <Typography align="center" sx={{ mt: 2 }}>
                                {msg}
                            </Typography>
                        )}
                    </Paper>
                </Fade>

                <Fade in timeout={500}>
                    <Paper sx={{ p: 4, width: "100%", maxWidth: 600, boxShadow: 4 }}>
                        <Typography variant="h5" gutterBottom align="center">
                            Добавить подключение
                        </Typography>

                        <Box sx={{ display: "grid", gap: 2, mt: 2 }}>
                            <TextField
                                label="Биржа"
                                value={form.exchange}
                                onChange={(e) =>
                                    setForm({ ...form, exchange: e.target.value })
                                }
                            />
                            <TextField
                                label="API Key"
                                value={form.apiKey}
                                onChange={(e) =>
                                    setForm({ ...form, apiKey: e.target.value })
                                }
                            />
                            <TextField
                                label="API Secret"
                                value={form.apiSecret}
                                onChange={(e) =>
                                    setForm({ ...form, apiSecret: e.target.value })
                                }
                            />

                            <Button
                                variant="contained"
                                size="large"
                                onClick={connect}
                                fullWidth
                            >
                                Подключить
                            </Button>
                        </Box>
                    </Paper>
                </Fade>
            </Container>
        </>
    );
}
