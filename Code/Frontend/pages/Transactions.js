import React, { useEffect, useState } from "react";
import { Container, Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import Navbar from "../components/Navbar";
import { TransactionsAPI } from "../api/api";

export default function Transactions() {
    const [rows, setRows] = useState([]);

    useEffect(() => {
        TransactionsAPI.list().then(res => setRows(res.data || [])).catch(() => setRows([]));
    }, []);

    return (
        <>
            <Navbar />
            <Container sx={{ mt: 3 }}>
                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>Транзакции</Typography>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>Дата</TableCell>
                                <TableCell>Тип</TableCell>
                                <TableCell>Тикер</TableCell>
                                <TableCell>Кол-во</TableCell>
                                <TableCell>Цена</TableCell>
                                <TableCell>Комиссия</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {rows.map((r, i) => (
                                <TableRow key={i}>
                                    <TableCell>{r.date || r.timestamp || "-"}</TableCell>
                                    <TableCell>{r.type}</TableCell>
                                    <TableCell>{r.symbol || r.asset}</TableCell>
                                    <TableCell>{r.amount}</TableCell>
                                    <TableCell>{r.price}</TableCell>
                                    <TableCell>{r.fee}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                    {!rows.length && <Typography color="text.secondary" sx={{ mt: 1 }}>Нет данных</Typography>}
                </Paper>
            </Container>
        </>
    );
}
