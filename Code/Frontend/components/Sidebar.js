import React from "react";
import { List, ListItemButton, ListItemText, Drawer, Toolbar } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function Sidebar() {
    const navigate = useNavigate();
    return (
        <Drawer variant="permanent" sx={{ width: 220, [`& .MuiDrawer-paper`]: { width: 220 } }}>
            <Toolbar />
            <List>
                {[
                    { text: "Dashboard", to: "/dashboard" },
                    { text: "Tax Profile", to: "/tax-profile" },
                    { text: "Connections", to: "/connections" },
                    { text: "Transactions", to: "/transactions" },
                    { text: "Reports", to: "/reports" },
                    { text: "Tax Calculation", to: "/tax-calculation" },
                    { text: "Settings", to: "/settings" }
                ].map(item => (
                    <ListItemButton key={item.to} onClick={() => navigate(item.to)}>
                        <ListItemText primary={item.text} />
                    </ListItemButton>
                ))}
            </List>
        </Drawer>
    );
}
