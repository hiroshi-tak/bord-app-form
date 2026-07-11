import axios from "axios";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

export const api = axios.create({
    baseURL: "/",
    withCredentials: true,
});

let auth: ReturnType<typeof useAuthStore>;

api.interceptors.response.use(
    res => res,

    async (error) => {

        if (!auth) {
            auth = useAuthStore();
        }

        const originalRequest = error.config;

        if (
            (error.response?.status === 401 || error.response?.status === 403) &&
            !originalRequest._retry &&
            !error.config?.url?.includes("/api/auth/refresh")
        ) {
            originalRequest._retry = true;

            try {
                await api.post("/api/auth/refresh");

                return api.request(originalRequest);

            } catch {
                auth.clear();
                await router.push("/login");
                return Promise.reject(error);
            }
        }

        return Promise.reject(error);
    }
);
