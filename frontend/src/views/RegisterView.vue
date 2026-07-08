<template>
  <main class="mx-auto max-w-4xl p-8">
    <div class="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">

      <h1 class="mb-8 text-center text-3xl font-bold">
        ユーザー登録
      </h1>

      <form @submit.prevent="onSubmit" class="space-y-5">

        <div>
          <label class="mb-2 block text-sm font-medium text-gray-700">
            ユーザー名
          </label>
          <input
            v-model="username"
            type="text"
            class="w-full rounded-md border px-3 py-2"
          />
        </div>

        <div>
          <label class="mb-2 block text-sm font-medium text-gray-700">
            パスワード
          </label>
          <input
            v-model="password"
            type="password"
            class="w-full rounded-md border px-3 py-2"
          />
        </div>

        <div>
          <label class="mb-2 block text-sm font-medium text-gray-700">
            パスワード（確認）
          </label>
          <input
            v-model="confirmPassword"
            type="password"
            class="w-full rounded-md border px-3 py-2"
          />
        </div>

        <button
          type="submit"
          class="w-full rounded-md bg-green-600 py-2 text-white hover:bg-green-700"
        >
          登録
        </button>

        <p v-if="error" class="text-red-500 text-sm text-center mt-2">
          {{ error }}
        </p>

        <RouterLink to="/login" class="text-blue-600 block text-center mt-4">
          ログイン画面へ戻る
        </RouterLink>

      </form>

    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { register } from "@/api/auth";

const router = useRouter();

const username = ref("");
const password = ref("");
const confirmPassword = ref("");
const error = ref("");

const onSubmit = async () => {
  error.value = "";

  if (password.value !== confirmPassword.value) {
    error.value = "パスワードが一致しません";
    return;
  }

  try {
    await register(username.value, password.value);

    // 成功したらログインへ
    router.push("/login");

  } catch (e: any) {
    error.value =
      e?.response?.data || "登録に失敗しました";
  }
};
</script>