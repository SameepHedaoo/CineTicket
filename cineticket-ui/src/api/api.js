import axios from "axios";
import { notifySuccess } from "../ui/notificationBus";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const isMutatingRequest = (config) => {
    const method = String(config?.method || "get").toLowerCase();
    return method !== "get" && method !== "head" && method !== "options";
};

const shouldShowSuccessToast = (config) => {
    if (!isMutatingRequest(config)) {
        return false;
    }
    return !config?.silentSuccessToast;
};

const successMessageForResponse = (response) => {
    const data = response?.data;
    if (typeof data === "string" && data.trim()) {
        return data.trim();
    }
    if (data && typeof data.message === "string" && data.message.trim()) {
        return data.message.trim();
    }

    const method = String(response?.config?.method || "post").toLowerCase();
    if (method === "delete") {
        return "Deleted successfully.";
    }
    if (method === "put" || method === "patch") {
        return "Updated successfully.";
    }
    return "Action completed successfully.";
};

const attachSuccessToastInterceptor = (client) => {
    client.interceptors.response.use(
        (response) => {
            if (shouldShowSuccessToast(response?.config)) {
                notifySuccess(successMessageForResponse(response));
            }
            return response;
        },
        (error) => {
            const status = error?.response?.status;
            // If JWT is missing/expired, force re-auth.
            if (status === 401) {
                localStorage.removeItem("token");
                localStorage.removeItem("adminToken");
                window.dispatchEvent(new Event("auth-changed"));
            }
            return Promise.reject(error);
        }
    );
};

export const api = axios.create({
    baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
attachSuccessToastInterceptor(api);

export const adminApi = axios.create({
    baseURL: API_BASE_URL,
});

adminApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("adminToken");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
attachSuccessToastInterceptor(adminApi);
