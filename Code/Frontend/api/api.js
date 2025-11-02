import axios from "axios";

export const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_BASE_URL,
});

// Подставляем JWT из localStorage
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("jwt");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export default api;

// Примеры удобных вызовов:
export const AuthAPI = {
    login: (email, password) => api.post("/auth/login", { email, password }),
    register: (payload) => api.post("/auth/register", payload),
    me: () => api.get("/auth/me")
};

export const ProfileAPI = {
    get: () => api.get("/tax-profile"),
    save: (payload) => api.put("/tax-profile", payload)
};

export const ExchangeAPI = {
    list: () => api.get("/exchange/connections"),
    connect: (payload) => api.post("/exchange/connect", payload),
    importBinance: () => api.post("/exchange/import/binance")
};

export const TransactionsAPI = {
    list: (params) => api.get("/transactions", { params })
};

export const ReportsAPI = {
    generatePdf: (params) => api.get("/report/pdf", { params, responseType: "blob" }),
    generateExcel: (params) => api.get("/report/excel", { params, responseType: "blob" })
};

export const TaxAPI = {
    calculate: (payload) => api.post("/tax/calculate", payload)
};
