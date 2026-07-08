<template>
    <main class="mx-auto max-w-4xl p-8">
        <div class="min-h-screen bg-gray-100 p-8">
            
            <h1 class="text-3xl font-bold mb-8">
                管理者画面
            </h1>

            <div
                v-if="error"
                class="mb-4 rounded bg-red-100 p-3 text-red-700"
                >
                {{ error }}
            </div>

            <!-- User管理 -->
            <section class="bg-white p-6 rounded shadow mb-8">
                <h2 class="text-xl font-bold mb-4">
                ユーザー管理
                </h2>

                <table class="w-full">

                    <thead>
                        <tr class="border-b">
                            <th class="text-left">
                                ID
                            </th>
                            <th class="text-left">
                                ユーザー名
                            </th>
                            <th class="text-left">
                                権限
                            </th>
                            <th>
                                操作
                            </th>
                        </tr>
                    </thead>
                
                    <tbody>
                        <tr
                            v-for="user in users"
                            :key="user.id"
                            class="border-b"
                        >
                            <td>
                                {{user.id}}
                            </td>
                            <td>
                                {{user.username}}
                            </td>
                            <td>
                                {{user.role}}
                            </td>
                            <td>
                            <button
                                    v-if="user.role !== 'ADMIN'"
                                    @click="deleteUser(user.id)"
                                    class="bg-red-500 text-white px-3 py-1 rounded"
                                >
                                    削除
                                </button>

                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>

            <!-- Board管理 -->
            <section class="bg-white p-6 rounded shadow">
                <h2 class="text-xl font-bold mb-4">
                    ボード管理
                </h2>


                <table class="w-full">
                    <thead>
                        <tr class="border-b">
                            <th>
                                ID
                            </th>
                            <th>
                                タイトル
                            </th>
                            <th>
                                所有者
                            </th>
                            <th>
                                操作
                            </th>
                        </tr>
                    </thead>
                
                    <tbody>
                        <tr
                        v-for="board in boards"
                        :key="board.id"
                        class="border-b"
                        >
                            <td>
                                {{board.id}}
                            </td>
                            <td>
                                {{board.title}}
                            </td>
                            <td>
                                {{board.ownerName}}
                            </td>
                            <td>
                                <button
                                    @click="deleteBoard(board.id)"
                                    class="bg-red-500 text-white px-3 py-1 rounded"
                                >
                                    削除
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </div>
    </main>
</template>

<script setup lang="ts">

import { ref, onMounted } from "vue";
import { api } from "@/lib/api";

interface User {

    id:number;
    username:string;
    role:string;

}

interface Board {

    id:number;
    title:string;
    ownerName:string;

}

const users = ref<User[]>([]);
const boards = ref<Board[]>([]);
const error = ref("");

const loadUsers = async()=>{

    try {
        const res = await api.get("/api/admin/users");
        users.value = res.data;
        error.value = "";
    } catch {
        error.value = "ユーザー一覧の取得に失敗しました。";
    }

};

const loadBoards = async()=>{

    try {
        const res = await api.get("/api/admin/boards");
        boards.value = res.data;
        error.value = "";
    } catch {
        error.value = "ボード一覧の取得に失敗しました。";
    }

};

const deleteUser = async(id:number)=>{

    if(!confirm("ユーザー削除しますか？"))
        return;

    try {
        await api.delete(`/api/admin/users/${id}`);
        await loadUsers();
        error.value = "";
    } catch {
        error.value = "ユーザーの削除に失敗しました。";
    }

};

const deleteBoard = async(id:number)=>{

    if(!confirm("ボード削除しますか？"))
        return;

    try {
        await api.delete(`/api/admin/boards/${id}`);
        await loadBoards();
        error.value = "";
    }catch {
        error.value = "ボードの削除に失敗しました。";
    }

};

onMounted(async()=>{

    await loadUsers();
    await loadBoards();

});

</script>