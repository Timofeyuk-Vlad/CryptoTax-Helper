import React, { useState } from "react";
import {
    Box,
    Button,
    Container,
    Paper,
    TextField,
    Typography,
    MenuItem,
    Fade,
} from "@mui/material";
import Navbar from "../components/Navbar";
import { TaxAPI } from "../api/api";

export default function TaxCalculation() {
    const [payload, setPayload] = useState({
        method: "FIFO",
        from: "",
        to: "",
        asset: "",
    });
    const [result, setResult] = useState(null);
    const [msg, setMsg] = useState("");

    const calculate = async () => {
        setMsg("");
        setResult(null);

        if (!payload.from || !payload.to) {
            setMsg("Please select both start and end dates (From / To).");
            return;
        }

        if (new Date(payload.from) > new Date(payload.to)) {
            setMsg("Start date cannot be later than end date.");
            return;
        }

        try {
            const res = await TaxAPI.calculate(payload);
            setResult(res.data);
        } catch (err) {
            console.error(err);
            setMsg("Calculation error");
        }
    };

    const onChange = (k) => (e) => setPayload({ ...payload, [k]: e.target.value });

    return (
        <>
            <Navbar />
            <Container
                sx={{
                    minHeight: "calc(100vh - 64px)", // учёт высоты Navbar
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "flex-start", // не по центру вертикали, чтобы не сжималось
                    pt: 6, // отступ сверху
                    pb: 6, // отступ снизу
                }}
            >
                <Fade in timeout={400}>
                    <Paper
                        sx={{
                            p: 4,
                            width: "100%",
                            maxWidth: 640,
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
                            Tax Calculation
                        </Typography>

                        <Box sx={{ display: "grid", gap: 2, mt: 2 }}>
                            <TextField
                                label="Method"
                                value={payload.method}
                                onChange={onChange("method")}
                                fullWidth
                            />

                            <TextField
                                select
                                label="Currency (asset)"
                                value={payload.asset}
                                onChange={onChange("asset")}
                                helperText="Select the asset to calculate tax for"
                                fullWidth
                            >
                                <MenuItem value="">All assets</MenuItem>
                                <MenuItem value="BTC">BTC (Bitcoin)</MenuItem>
                                <MenuItem value="ETH">ETH (Ethereum)</MenuItem>
                                <MenuItem value="BNB">BNB (Binance Coin)</MenuItem>
                                <MenuItem value="USDT">USDT (Tether)</MenuItem>
                                <MenuItem value="SOL">SOL (Solana)</MenuItem>
                            </TextField>

                            <TextField
                                label="From"
                                type="date"
                                value={payload.from}
                                onChange={onChange("from")}
                                InputLabelProps={{ shrink: true }}
                                fullWidth
                            />

                            <TextField
                                label="To"
                                type="date"
                                value={payload.to}
                                onChange={onChange("to")}
                                InputLabelProps={{ shrink: true }}
                                fullWidth
                            />

                            <Button variant="contained" size="large" onClick={calculate} fullWidth>
                                Calculate
                            </Button>

                            {msg && (
                                <Typography color="error" align="center" sx={{ mt: 1 }}>
                                    {msg}
                                </Typography>
                            )}

                            {result && (
                                <Box sx={{ mt: 3, textAlign: "center" }}>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                                        💰 Total tax to pay:&nbsp;
                                        {result.calculation?.fifoCalculation?.taxAmount
                                            ? `${result.calculation.fifoCalculation.taxAmount} ${result.calculation.fifoCalculation.currency || "RUB"}`
                                            : "-"}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary">
                                        Profit: {result.calculation?.fifoCalculation?.taxableProfit ?? "-"}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary">
                                        Method: {result.payload?.method || "FIFO"}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary">
                                        Country: {result.country}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary">
                                        Asset: {payload.asset || "ALL"}
                                    </Typography>
                                </Box>
                            )}
                        </Box>
                    </Paper>
                </Fade>
            </Container>
        </>
    );
}
