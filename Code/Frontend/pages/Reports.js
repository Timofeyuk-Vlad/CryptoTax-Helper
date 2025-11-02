import React, { useState } from "react";
import { Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import Navbar from "../components/Navbar";
import { ReportsAPI } from "../api/api";

export default function Reports() {
    const [period, setPeriod] = useState({ from: "", to: "" });

    const download = async (fn, name) => {
        const res = await fn(period);
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const a = document.createElement("a");
        a.href = url; a.download = name; a.click();
        window.URL.revokeObjectURL(url);
    };

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Paper sx={{ p: 3, maxWidth: 520 }}>
                    <Typography variant="h6" gutterBottom>Отчёты</Typography>
                    <Box sx={{ display: "grid", gap: 2 }}>
                        <TextField label="С (YYYY-MM-DD)" value={period.from} onChange={(e) => setPeriod({ ...period, from: e.target.value })} />
                        <TextField label="По (YYYY-MM-DD)" value={period.to} onChange={(e) => setPeriod({ ...period, to: e.target.value })} />
                        <Button variant="contained" onClick={() => download(ReportsAPI.generatePdf, "report.pdf")}>PDF</Button>
                        <Button variant="outlined" onClick={() => download(ReportsAPI.generateExcel, "report.xlsx")}>Excel</Button>
                    </Box>
                </Paper>
            </Container>
        </>
    );
}
