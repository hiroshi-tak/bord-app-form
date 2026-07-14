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

          <p
            v-if="errors.username"
            class="mt-1 text-sm text-red-500"
          >
            {{ errors.username }}
          </p>
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
          
          <p
            v-if="errors.password"
            class="mt-1 text-sm text-red-500"
          >
            {{ errors.password }}
          </p>

          <p
            v-if="errors.confirmPassword"
            class="mt-1 text-sm text-red-500"
          >
            {{ errors.confirmPassword }}
          </p>
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
const errors = ref<Record<string, string>>({});

const onSubmit = async () => {
  error.value = "";
  errors.value = {};

  if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = "パスワードが一致しません";
    return;
  }

  try {
    await register(username.value, password.value);

    alert("登録しました");
    await router.push("/login");

  } catch (e: any) {

    if (e.response?.status === 400 &&
      typeof e.response.data === "object"
    ) {
      errors.value = e.response.data;
    } else {
      error.value = e?.response?.data ?? "登録に失敗しました";
    }
  }
};
</script>