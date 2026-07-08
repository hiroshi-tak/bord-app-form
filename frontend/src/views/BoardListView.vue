<template>
  <main class="mx-auto max-w-4xl p-8">

    <h2 class="mb-6 text-2xl font-bold">
      ボード一覧
    </h2>

    <div
      v-if="error"
      class="mb-4 rounded bg-red-100 p-3 text-red-700"
    >
      {{ error }}
    </div>

    <input
        v-model="title"
        type="text"
        placeholder="ボード名"
        class="rounded border px-3 py-2"
    />

    <button
        @click="createBoard"
        class="rounded bg-green-600 px-5 py-3 text-white hover:bg-green-700"
        >
        ＋ 新しいボードを作成
    </button>

    <div class="space-y-5">

      <div
        v-for="board in boards"
        :key="board.id"
        class="rounded-lg bg-white p-6 shadow"
      >

        <h3 class="text-xl font-semibold">
          {{ board.title }}
        </h3>

        <p class="mt-2 text-gray-600">
          作成者：{{ board.ownerName }}
        </p>

        <div class="mt-4 text-right">

          <RouterLink
            :to="`/board/${board.id}`"
            class="rounded bg-blue-600 px-4 py-2 text-white"
          >
            開く
          </RouterLink>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { api } from "@/lib/api";

interface Board {
  id: number;
  title: string;
  ownerName: string;
}

const router = useRouter();
const title = ref("");
const error = ref("");
const boards = ref<Board[]>([]);

onMounted(async () => {
  try {
    const res = await api.get("/api/boards");
    boards.value = res.data;
  } catch (e) {
    router.push("/login");
  }
});

const createBoard = async () => {
  error.value = "";

  if (!title.value) {
    error.value = "ボード名を入力してください。";
    return;
  } 

  try {
    await api.post("/api/boards", {
      title: title.value,
    });

    alert("作成しました");

    title.value = "";

    const res = await api.get("/api/boards");
    boards.value = res.data;

  } catch (e: any) {
    error.value =
      e?.response?.data || "ボードの作成に失敗しました。";
  }
};
</script>