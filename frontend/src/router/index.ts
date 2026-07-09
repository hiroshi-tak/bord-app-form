import { createRouter, createWebHistory } from 'vue-router'

import DefaultLayout from "@/layouts/DefaultLayout.vue";
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import BoardListView from "@/views/BoardListView.vue";
import WhiteBoardView from "@/views/WhiteBoardView.vue";
import { useAuthStore } from "@/stores/auth";

const routes = [
  {
    path: "/",
    component: DefaultLayout,

    children: [

        { path: "/", redirect: "/login" },

        {
          path: "/login",
          name: "login",
          component: LoginView,
        },
        {
          path: "/register",
          name: "register",
          component: RegisterView,
        },
        {
          path: "/boards",
          name: "boards",
          component: BoardListView,
          meta: { requiresAuth: true },
        },
        {
          path: "/board/:id",
          name: "board",
          component: WhiteBoardView,
          props: true,
          meta: { requiresAuth: true },
        },
        {
          path: "/admin",
          name: "admin",
          component: () =>
            import("@/views/AdminView.vue"),
          meta: { requiresAuth: true },
        },
      
    ],
    
  },
  
];

const router = createRouter({

  history: createWebHistory(import.meta.env.BASE_URL),

  routes,

});

router.beforeEach(async (to) => {

  const requiresAuth = Boolean(to.meta.requiresAuth);

  if (!requiresAuth) return true;

  const auth = useAuthStore();

  await auth.fetchMe();

  if (auth.user) {
    return true;
  }

  return "/login";
});

export default router;