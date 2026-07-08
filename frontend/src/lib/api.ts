import axios from "axios";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

export const api = axios.create({
    //baseURL: import.meta.env.VITE_API_URL,
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

        if (
            error.response?.status === 401 &&
            !error.config?.url?.includes("/api/auth/refresh")
        ) {
            try {
                await api.post("/api/auth/refresh");

                return api.request(error.config);

            } catch {
                auth.clear();
                await router.push("/login");
            }
        }

        return Promise.reject(error);
    }
);
