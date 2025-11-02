import api from './api';

export const authService = {
    login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        return response.data;
    },

    register: async ({ email, password, firstName, lastName, phoneNumber }) => {
        try {
            const response = await api.post('/auth/register', {
                email,
                password,
                firstName,
                lastName,
                phoneNumber
            });
            return response.data;
        } catch (err) {
            // Обрабатываем ошибку конфликта (повторный email)
            if (err.response && err.response.status === 409) {
                return { token: null, email, message: err.response.data.message || "User already exists" };
            }
            throw err;
        }
    },

    logout: () => {
        localStorage.removeItem('authToken');
    },

    getCurrentUser: () => {
        return localStorage.getItem('authToken');
    }
};
