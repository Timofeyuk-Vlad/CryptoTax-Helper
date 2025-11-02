import React, { useContext } from "react";
import { AppBar, Toolbar, Typography, Button } from "@mui/material";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Navbar() {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => { logout(); navigate("/login"); };

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" sx={{ flexGrow: 1 }}>
                    CryptoTax Helper
                </Typography>
                {user ? (
                    <>
                        <Typography sx={{ mr: 2 }}>{user.email}</Typography>
                        <Button color="inherit" onClick={() => navigate("/dashboard")}>Dashboard</Button>
                        <Button color="inherit" onClick={() => navigate("/tax-profile")}>Tax Profile</Button>
                        <Button color="inherit" onClick={() => navigate("/connections")}>Connections</Button>
                        <Button color="inherit" onClick={() => navigate("/transactions")}>Transactions</Button>
                        <Button color="inherit" onClick={() => navigate("/reports")}>Reports</Button>
                        <Button color="inherit" onClick={() => navigate("/tax-calculation")}>Tax Calc</Button>
                        <Button color="inherit" onClick={() => navigate("/settings")}>Settings</Button>
                        <Button color="inherit" onClick={handleLogout}>Logout</Button>
                    </>
                ) : (
                    <>
                        <Button color="inherit" onClick={() => navigate("/login")}>Login</Button>
                    </>
                )}
            </Toolbar>
        </AppBar>
    );
}
