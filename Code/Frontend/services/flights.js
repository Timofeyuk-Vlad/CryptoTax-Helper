import api from "./api";

export const flightsService = {
    getById: (id) => api.get(`/flights/${id}`),
    search: (query) => api.get(`/flights/search?query=${encodeURIComponent(query)}`)
};
