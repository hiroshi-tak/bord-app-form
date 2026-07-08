import axios from "axios";
import { api } from "@/lib/api";

export interface User {
    username: string;
    role: string;
}

export const register = async (username: string, password: string) => {
    return await api.post("api/auth/register", {
        username,
        password,
    });
};

export const authApi = {
    login: (username: string, password: string) => {
        return api.post("/api/auth/login", {
            username,
            password,
        });
    },

    me: () => {
        return api.get("/api/me");
    },

    logout: () => {
        return api.post("/api/auth/logout");
    }

};