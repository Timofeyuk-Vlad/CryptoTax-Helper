import axios from "axios";

export const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_BASE_URL,
});

// Подставляем JWT из localStorage
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
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
    get: () => api.get("/tax-profile"),       // OK — совпадает с @GetMapping
    save: (payload) => api.post("/tax-profile", payload) // ✅ было PUT, теперь POST
};

export const ExchangeAPI = {
    list: () => api.get("/exchange/connections"),
    connect: (payload) => api.post("/exchange/connect", payload),
    importBinance: (connectionId) => api.post(`/exchange/import/${connectionId}`)
};

export const TransactionsAPI = {
    list: (params) => api.get("/transactions", { params })
};

export const ReportsAPI = {
    generatePdf: ({ year }) =>
        api.post("/reports/tax/pdf", null, {
            params: { year },
            responseType: "blob",
        }),

    generateExcel: ({ year }) =>
        api.post("/reports/tax/excel", null, {
            params: { year },
            responseType: "blob",
        }),
};

export const TaxAPI = {
    calculate: (payload) => {
        const year = new Date().getFullYear();
        const { from, to } = payload;
        const params = new URLSearchParams({ year, from, to });
        return api.post(`/transactions/tax/fifo-detailed?${params.toString()}`, payload);
    }
};