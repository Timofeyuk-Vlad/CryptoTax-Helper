import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import TaxProfile from "./pages/TaxProfile";
import ExchangeConnections from "./pages/ExchangeConnections";
import Transactions from "./pages/Transactions";
import Reports from "./pages/Reports";
import TaxCalculation from "./pages/TaxCalculation";
import AccountSettings from "./pages/AccountSettings";

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<Navigate to="/login" replace />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
                    <Route path="/tax-profile" element={<ProtectedRoute><TaxProfile /></ProtectedRoute>} />
                    <Route path="/connections" element={<ProtectedRoute><ExchangeConnections /></ProtectedRoute>} />
                    <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
                    <Route path="/reports" element={<ProtectedRoute><Reports /></ProtectedRoute>} />
                    <Route path="/tax-calculation" element={<ProtectedRoute><TaxCalculation /></ProtectedRoute>} />
                    <Route path="/settings" element={<ProtectedRoute><AccountSettings /></ProtectedRoute>} />
                    <Route path="/settings" element={<AccountSettings />} />

                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
