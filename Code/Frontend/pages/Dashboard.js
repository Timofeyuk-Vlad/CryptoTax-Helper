import React from "react";
import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import Navbar from "../components/Navbar";

export default function Dashboard() {
    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={4}>
                        <Paper sx={{ p: 2 }}><Typography variant="h6">Баланс / Профиль</Typography></Paper>
                    </Grid>
                    <Grid item xs={12} md={8}>
                        <Paper sx={{ p: 2 }}><Typography variant="h6">Сводка по транзакциям</Typography></Paper>
                    </Grid>
                    <Grid item xs={12}>
                        <Paper sx={{ p: 2 }}><Typography variant="h6">Уведомления</Typography></Paper>
                    </Grid>
                </Grid>
            </Container>
        </>
    );
}
