import { defineStore } from "pinia";
import { authApi } from "@/api/auth";
import type { User } from "@/api/auth";

export const useAuthStore = defineStore("auth", {
    state: () => ({
        user: null as User | null,
        checked: false,
    }),

    actions: {
        async fetchMe() {
            try {
                const res = await authApi.me();

                this.user = res.data;
                this.checked = true;

                return this.user;

            } catch {
                this.user = null;
                this.checked = true;
                return null;
            }
        },

        clear() {
            this.user = null;
            this.checked = true;
        }
    }
});

