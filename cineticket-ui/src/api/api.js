import axios from "axios";

export const api = axios.create({
    baseURL: "http://localhost:8080", // backend base URL
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const adminApi = axios.create({
    baseURL: "http://localhost:8080",
});

adminApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("adminToken");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
