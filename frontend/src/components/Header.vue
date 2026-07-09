<template>
  <header class="bg-blue-600 px-4 sm:px-8 py-4 text-white">
    <nav class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 class="text-xl font-bold">
        Whiteboard App
        </h1>

        <div class="flex flex-wrap items-center gap-2">
            <RouterLink
              v-if="auth.user?.role === 'ADMIN'"
              to="/admin"
              class="text-sm rounded bg-yellow-500 px-3 py-2 hover:bg-yellow-600"
            >
              管理者画面
            </RouterLink>

            <RouterLink
              v-if="auth.user"
              to="/boards"
              class="text-sm rounded bg-green-500 px-3 py-2 hover:bg-green-600"
            >
              ボード一覧
            </RouterLink>
        
            <button
              v-if="auth.user"
              class="text-sm rounded bg-red-500 px-3 py-2 hover:bg-red-600"
              @click="logout"
            >
              ログアウト
            </button>
        </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { authApi } from "@/api/auth";

const router = useRouter();
const auth = useAuthStore();

const logout = async () => {
  try {
    await authApi.logout();
  } finally {
    auth.clear();

    router.push("/login");
  }
};
</script>