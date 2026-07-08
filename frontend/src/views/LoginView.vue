<template>
  <main class="mx-auto max-w-4xl p-8">
    <div class="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">

      <form @submit.prevent="login" class="space-y-5">
        <div>
          <label
            for="username"
            class="mb-2 block text-sm font-medium text-gray-700"
          >
            ユーザー名
          </label>

          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="ユーザー名を入力"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label
            for="password"
            class="mb-2 block text-sm font-medium text-gray-700"
          >
            パスワード
          </label>

          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="パスワードを入力"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <p
          v-if="error"
          class="text-center text-sm text-red-500"
        >
          {{ error }}
        </p>

        <button
          type="submit"
          class="w-full rounded-md bg-blue-600 py-2 text-white transition hover:bg-blue-700"
        >
          ログイン
        </button>

        <div class="text-center">
          <RouterLink
            to="/register"
            class="text-blue-600 hover:underline"
          >
            新規登録はこちら
          </RouterLink>
        </div>
      </form>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { authApi } from "@/api/auth";

const router = useRouter();
const username = ref("");
const password = ref("");
const error = ref("");
const auth = useAuthStore();

const login = async () => {
  error.value = "";

  try {
    await authApi.login(username.value, password.value);

    await auth.fetchMe();

    router.push("/boards");

  } catch (e: any) {
    error.value =
      e?.response?.data || "ログイン失敗";
  }
};
</script>