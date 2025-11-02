import React, { useState } from "react";
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
import { ReportsAPI } from "../api/api";

export default function Reports() {
    const [year, setYear] = useState(new Date().getFullYear());

    const download = async (fn, name) => {
        try {
            const res = await fn({ year });
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const a = document.createElement("a");
            a.href = url;
            a.download = name;
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            alert("❌ Не удалось сгенерировать отчёт. Проверьте подписку.");
        }
    };

    return (
        <>
            <Navbar />
            <Container
                sx={{
                    minHeight: "calc(100vh - 64px)", // чтобы не учитывал Navbar
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "flex-start", // вместо center
                    pt: 8, // отступ сверху ~64px
                    pb: 8, // отступ снизу
                }}
            >
                <Fade in timeout={400}>
                    <Paper
                        sx={{
                            p: 4,
                            width: "100%",
                            maxWidth: 520,
                            boxShadow: 4,
                            borderRadius: 3,
                        }}
                    >
                        <Typography
                            variant="h5"
                            gutterBottom
                            align="center"
                            sx={{ fontWeight: 600 }}
                        >
                            Отчёты
                        </Typography>

                        <Box sx={{ display: "grid", gap: 2, mt: 2 }}>
                            <TextField
                                label="Год"
                                type="number"
                                value={year}
                                onChange={(e) => setYear(e.target.value)}
                                inputProps={{ min: 2000, max: 2100 }}
                                fullWidth
                            />

                            <Button
                                variant="contained"
                                fullWidth
                                size="large"
                                onClick={() =>
                                    download(ReportsAPI.generatePdf, `tax-report-${year}.pdf`)
                                }
                            >
                                Скачать PDF
                            </Button>

                            <Button
                                variant="outlined"
                                fullWidth
                                size="large"
                                onClick={() =>
                                    download(ReportsAPI.generateExcel, `tax-report-${year}.xlsx`)
                                }
                            >
                                Скачать Excel
                            </Button>
                        </Box>
                    </Paper>
                </Fade>
            </Container>
        </>
    );
}
