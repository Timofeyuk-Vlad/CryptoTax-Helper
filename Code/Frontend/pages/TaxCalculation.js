import React, { useState } from "react";
import { Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import Navbar from "../components/Navbar";
import { TaxAPI } from "../api/api";

export default function TaxCalculation() {
    const [payload, setPayload] = useState({ method: "FIFO", from: "", to: "" });
    const [result, setResult] = useState(null);
    const [msg, setMsg] = useState("");

    const calculate = async () => {
        setMsg(""); setResult(null);
        try {
            const res = await TaxAPI.calculate(payload);
            setResult(res.data);
        } catch {
            setMsg("Ошибка расчёта");
        }
    };

    const onChange = (k) => (e) => setPayload({ ...payload, [k]: e.target.value });

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Paper sx={{ p: 3, maxWidth: 640 }}>
                    <Typography variant="h6" gutterBottom>Расчёт налогов</Typography>
                    <Box sx={{ display: "grid", gap: 2 }}>
                        <TextField label="Метод" value={payload.method} onChange={onChange("method")} />
                        <TextField label="С (YYYY-MM-DD)" value={payload.from} onChange={onChange("from")} />
                        <TextField label="По (YYYY-MM-DD)" value={payload.to} onChange={onChange("to")} />
                        <Button variant="contained" onClick={calculate}>Рассчитать</Button>
                        {msg && <Typography color="error">{msg}</Typography>}
                        {result && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="subtitle1">Итог к уплате: {result.totalTax ?? "-"}</Typography>
                                <Typography variant="body2" color="text.secondary">Основание: {result.method || payload.method}</Typography>
                            </Box>
                        )}
                    </Box>
                </Paper>
            </Container>
        </>
    );
}
