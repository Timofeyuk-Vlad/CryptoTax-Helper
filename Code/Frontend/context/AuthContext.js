import React, { createContext, useEffect, useState } from "react";
import axios from "axios";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            setLoading(false);
            return;
        }

        axios
            .get("http://localhost:8080/api/auth/me", {
                headers: { Authorization: `Bearer ${token}` },
            })
            .then((res) => setUser(res.data))
            .catch(() => {
                localStorage.removeItem("token");
            })
            .finally(() => setLoading(false));
    }, []);

    const logout = () => {
        localStorage.removeItem("token");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, setUser, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};
