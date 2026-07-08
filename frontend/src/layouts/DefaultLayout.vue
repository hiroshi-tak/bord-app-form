<template>

  <div class="min-h-screen bg-gray-100">

    <Header />

    <RouterView />

  </div>

</template>


<script setup lang="ts">

import Header from "@/components/Header.vue";

</script>