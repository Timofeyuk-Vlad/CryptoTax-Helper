import React, { useEffect, useState } from "react";
import {
    Box,
    Button,
    Card,
    CardContent,
    Container,
    Grid,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
    CircularProgress,
} from "@mui/material";
import Navbar from "../components/Navbar";
import { ProfileAPI, TransactionsAPI } from "../api/api";

export default function Dashboard() {
    const [profile, setProfile] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadData() {
            try {
                const [profileRes, txRes] = await Promise.all([
                    ProfileAPI.get(),
                    TransactionsAPI.list({ limit: 5 }),
                ]);
                setProfile(profileRes.data);
                setTransactions(txRes.data || []);
            } catch (e) {
                console.error(e);
                setError("Не удалось загрузить данные");
            } finally {
                setLoading(false);
            }
        }
        loadData();
    }, []);

    if (loading) {
        return (
            <>
                <Navbar />
                <Box sx={{ display: "flex", justifyContent: "center", mt: 5 }}>
                    <CircularProgress />
                </Box>
            </>
        );
    }

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Typography variant="h5" gutterBottom>
                    Панель управления
                </Typography>

                {/* === Профиль пользователя === */}
                <Paper sx={{ p: 2, mb: 3 }}>
                    {profile ? (
                        <>
                            <Typography variant="subtitle1">
                                👤 Страна: <b>{profile.country}</b>
                            </Typography>
                            <Typography variant="subtitle1">
                                ИНН: <b>{profile.taxIdentificationNumber || "—"}</b>
                            </Typography>
                            <Typography variant="subtitle2" sx={{ mt: 1 }}>
                                Профиль заполнен ✅
                            </Typography>
                        </>
                    ) : (
                        <>
                            <Typography color="error" variant="subtitle1">
                                ⚠️ Налоговый профиль не заполнен
                            </Typography>
                            <Button
                                href="/tax-profile"
                                variant="contained"
                                size="small"
                                sx={{ mt: 1 }}
                            >
                                Заполнить профиль
                            </Button>
                        </>
                    )}
                </Paper>

                {/* === Статистика === */}
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={4}>
                        <Card>
                            <CardContent>
                                <Typography color="textSecondary" gutterBottom>
                                    Всего транзакций
                                </Typography>
                                <Typography variant="h5">
                                    {transactions.length || 0}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={4}>
                        <Card>
                            <CardContent>
                                <Typography color="textSecondary" gutterBottom>
                                    Активов (по валютам)
                                </Typography>
                                <Typography variant="h5">
                                    {new Set(transactions.map((t) => t.baseAsset)).size || 0}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={4}>
                        <Card>
                            <CardContent>
                                <Typography color="textSecondary" gutterBottom>
                                    Годовой отчёт
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/reports"
                                    fullWidth
                                    sx={{ mt: 1 }}
                                >
                                    Перейти к отчётам
                                </Button>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>

                {/* === Последние операции === */}
                <Box sx={{ mt: 4 }}>
                    <Typography variant="h6" gutterBottom>
                        Последние операции
                    </Typography>

                    {transactions.length > 0 ? (
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Дата</TableCell>
                                    <TableCell>Тип</TableCell>
                                    <TableCell>Актив</TableCell>
                                    <TableCell>Количество</TableCell>
                                    <TableCell>Цена</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {transactions.slice(0, 5).map((tx) => (
                                    <TableRow key={tx.id}>
                                        <TableCell>
                                            {new Date(tx.timestamp).toLocaleDateString()}
                                        </TableCell>
                                        <TableCell>{tx.type}</TableCell>
                                        <TableCell>{tx.baseAsset}</TableCell>
                                        <TableCell>{tx.amount}</TableCell>
                                        <TableCell>{tx.price || "—"}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    ) : (
                        <Typography color="text.secondary">
                            Нет транзакций для отображения
                        </Typography>
                    )}
                </Box>

                {error && (
                    <Typography color="error" sx={{ mt: 2 }}>
                        {error}
                    </Typography>
                )}
            </Container>
        </>
    );
}
