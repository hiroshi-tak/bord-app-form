<template>
  <main class="flex-1 p-6">
    <h2 class="mb-5 text-2xl font-bold">
      {{ board?.title }}
    </h2>

    <p class="mb-5 text-gray-500">
      作成者：{{ board?.ownerName }}
    </p>

    <div class="flex items-center gap-4">
      <span class="rounded bg-blue-500 px-3 py-1">
        参加人数：{{ memberCount }} / 5
      </span>
    </div>

    <!-- ツールバー -->
    <div class="mb-5 flex flex-wrap items-center gap-3 rounded-lg bg-white p-4 shadow">

      <input
        type="color"
        v-model="color"
        class="h-10 w-10 cursor-pointer rounded border"
      />

      <select
        v-model.number="lineWidth"
        class="rounded border px-3 py-2"
      >
        <option :value="2">2px</option>
        <option :value="4">4px</option>
        <option :value="6">6px</option>
        <option :value="8">8px</option>
        <option :value="10">10px</option>
        <option :value="12">12px</option>
        <option :value="16">16px</option>
        <option :value="20">20px</option>
        <option :value="24">24px</option>
      </select>

      <button
        @click="tool='pen'"
        class="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
      >
        ペン
      </button>

      <button
        @click="tool='eraser'"
        class="rounded bg-yellow-500 px-4 py-2 text-white hover:bg-yellow-600"
      >
        消しゴム
      </button>

      <button @click="undo">UNDO</button>
      <button @click="redo">REDO</button>

    </div>

    <!-- 描画エリア -->
    <div class="rounded-lg bg-white shadow">

  <div class="relative">

    <canvas
      ref="canvasRef"
      width="1200"
      height="700"
      class="w-full rounded border border-gray-300 bg-white"
      style="touch-action: none;"
      @contextmenu.prevent
      @pointerdown="startDraw"
      @pointermove="draw"
      @pointerup="stopDraw"
      @pointercancel="stopDraw"
    />

    <div
      v-for="(c, id) in cursors"
      :key="id"
      class="absolute w-3 h-3 bg-red-500 rounded-full pointer-events-none"
      :style="{
        left: (c.x / 1200 * 100) + '%',
        top: (c.y / 700 * 100) + '%',
        transform: 'translate(-50%, -50%)'
      }"
    />

  </div>

</div>

  </main>
</template>

<script setup lang="ts">
// Vue / Router / API
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "@/lib/api";
import { v4 as uuidv4 } from "uuid";

const route = useRoute();
const router = useRouter();

// Board / Stroke
interface Board {
  id: number;
  title: string;
  ownerName: string;
}

type Stroke = {
  points: { x: number; y: number }[];
  color: string;
  lineWidth: number;
  tool: "pen" | "eraser";
};

//ユーザー識別（リアルタイム共有用）
const userId = uuidv4();
const memberCount = ref(0);

// UI・描画状態
// ボード情報
const board = ref<Board | null>(null);

// Canvas DOM
const canvasRef = ref<HTMLCanvasElement | null>(null);

// 描画状態
const isDrawing = ref(false);
const color = ref("#000000");
const lineWidth = ref(2);
const tool = ref<"pen" | "eraser">("pen");

// 描画履歴（Undo / Redo）
const strokes = ref<Stroke[]>([]);
const redoStack = ref<Stroke[]>([]);

// 現在描画中のストローク
let currentStroke: Stroke | null = null;

// 描画ポイント数制限
const MAX_POINTS = 5000;

//Canvasレンダリング制御
let ctx: CanvasRenderingContext2D | null = null;

//再接続
let reconnectTimer: number | null = null;
let reconnectCount = 0;
const MAX_RECONNECT_COUNT = 5;
let manualClose = false;

// 再描画処理
const redraw = () => {
  if (!ctx || !canvasRef.value) return;

  ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height);

  // 背景リセット
  ctx.fillStyle = "#ffffff";
  ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height);

  // 全ストローク再描画
  for (const stroke of strokes.value) {
    drawStroke(stroke);
  }
};

// ストローク描画（1本分）
const drawStroke = (stroke: Stroke) => {
  if (!ctx) return;

  ctx.globalCompositeOperation = "source-over";
  ctx.lineWidth = stroke.lineWidth;

  if (stroke.tool === "eraser") {
    ctx.globalCompositeOperation = "destination-out";
    ctx.strokeStyle = "rgba(0,0,0,1)";
  } else {
    ctx.strokeStyle = stroke.color;
  }

  const points = stroke.points;
  if (!points || points.length === 0) return;

  ctx.beginPath();

  const first = points[0];
  if (!first) return;

  ctx.moveTo(first.x, first.y);

  for (let i = 1; i < points.length; i++) {
    const p = points[i];
    if (!p) continue;
    ctx.lineTo(p.x, p.y);
  }

  ctx.stroke();
};

// Pointer Events
const getPos = (e: PointerEvent) => {
  const canvas = canvasRef.value;
  if (!canvas) {
    return { x:0, y:0 };
  }

  const rect = canvas.getBoundingClientRect();

  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;

  return {
    x: (e.clientX - rect.left) * scaleX,
    y: (e.clientY - rect.top) * scaleY
  };
};

// 描画開始
const startDraw = (event: PointerEvent) => {
  if (!ctx || !canvasRef.value) return;

  canvasRef.value.setPointerCapture(event.pointerId);

  isDrawing.value = true;

  const { x, y } = getPos(event);

  currentStroke = {
    points: [{ x, y }],
    color: color.value,
    lineWidth: lineWidth.value,
    tool: tool.value
  };

  ctx.beginPath();

  ctx.lineWidth = lineWidth.value;

  if (tool.value === "eraser") {
    ctx.globalCompositeOperation = "destination-out";
    ctx.strokeStyle = "rgba(0,0,0,1)";
  } else {
    ctx.globalCompositeOperation = "source-over";
    ctx.strokeStyle = color.value;
  }

  ctx.moveTo(x, y);
};

// 描画中
const draw = (event: PointerEvent) => {
  if (!isDrawing.value || !ctx || !currentStroke) return;

  const { x, y } = getPos(event);

  // ポイント数制限
  if (currentStroke.points.length >= MAX_POINTS) {

    console.warn(
      "stroke point limit reached:",
      currentStroke.points.length
    );

    return;
  }

  const last =
    currentStroke.points[
      currentStroke.points.length - 1
    ];

  // 近すぎる点は保存しない
  if (
    last &&
    Math.abs(x - last.x) < 3 &&
    Math.abs(y - last.y) < 3
  ) {
    return;
  }

  currentStroke.points.push({ x, y });

  ctx.lineTo(x, y);
  ctx.stroke();

  sendCursor(event);
};

// 描画終了
const stopDraw = (event?: PointerEvent) => {
  if (!isDrawing.value) return;

  if (!ctx || !canvasRef.value || !currentStroke) return;

  isDrawing.value = false;

  if (currentStroke.points.length < 2) {
    currentStroke = null;
    return;
  }

  ctx.closePath();

  strokes.value.push(currentStroke);

  sendStroke(currentStroke); // サーバへ送信

  currentStroke = null;
  redoStack.value = [];

  if (event) {
    canvasRef.value.releasePointerCapture(event.pointerId);
  }
};

// Undo / Redo（履歴管理）
const undo = () => {
  if (strokes.value.length === 0) return;

  const stroke = strokes.value.pop();
  if (stroke) redoStack.value.push(stroke);

  redraw();

  sendUndo();
};

const redo = () => {
  if (redoStack.value.length === 0) return;

  const stroke = redoStack.value.pop();
  if (stroke) strokes.value.push(stroke);

  redraw();

  sendRedo();
};

// WebSocket（リアルタイム同期）
let ws: WebSocket | null = null;

// 接続
const connectWS = () => {
  manualClose = false;

  if (ws && ws.readyState === WebSocket.OPEN) return;
  if (ws && ws.readyState === WebSocket.CONNECTING) return;
/*
  const socket = new WebSocket(
    `${import.meta.env.VITE_WS_URL}/ws/board?boardId=${route.params.id}`
  );
*/
  const protocol =
  location.protocol === "https:" ? "wss" : "ws";

  const socket = new WebSocket(
    `${protocol}://${location.host}/ws/board?boardId=${route.params.id}`
  );

  ws = socket;

  socket.onopen = () => {
    console.log("WebSocket connected");

    reconnectCount = 0;

    socket.send(JSON.stringify({
      type: "join",
      boardId: route.params.id
    }));
  };

  socket.onerror = (e) => {
    console.error("WebSocket error", e);
  };

  socket.onclose = (event) => {
/*
    console.log(
      "WebSocket closed",
      "code:",
      event.code,
      "reason:",
      event.reason,
    );
*/

    ws = null;

    // 自分で閉じた場合
    if(manualClose){
      return;
    }

    if(event.code === 3001){

      alert(
        "ユーザーが削除されました"
      );

      router.push("/login");

      return;
    }

    // ボード存在確認
    if(event.code === 3002){

        alert(
          "ボードが削除されました"
        );

        router.push("/boards");

        return;
    }

    // 再接続
    if (reconnectCount >= MAX_RECONNECT_COUNT) {
      console.error(
        "WebSocket reconnect failed"
      );

      return;
    }

    reconnectCount++;

    const delay = reconnectCount * 2000;

    console.log(
      `Reconnect attempt ${reconnectCount} after ${delay}ms`
    );

    reconnectTimer = window.setTimeout(() => {
      connectWS();
    }, delay);
  };

  // 他ユーザーイベント受信
  socket.onmessage = (event) => {
    console.log("WS received:", event.data);

    const msg = JSON.parse(event.data);

    if (msg.type === "member:count") {
      memberCount.value = msg.count;
    }

    // ストローク追加
    if (msg.type === "stroke:add") {
      if (msg.userId === userId) return;
      
      strokes.value.push(msg.data);
      redraw();
    }

    // Undo同期
    if (msg.type === "stroke:undo") {

      if(msg.userId === userId) return;

      const stroke = strokes.value.pop();

      if (stroke) {
        redoStack.value.push(stroke);
      }

      redraw();
    }

    // Redo同期
    if (msg.type === "stroke:redo") {

      if(msg.userId === userId) return;

      const stroke = redoStack.value.pop();

      if (stroke) {
        strokes.value.push(stroke);
      }

      redraw();
    }

    // カーソル同期
    if (msg.type === "cursor:move") {
      cursors.value[msg.userId] = {
        x: msg.x,
        y: msg.y,
        lastSeen: Date.now()
      };
    }

    // カーソル削除
    if (msg.type === "cursor:leave") {
      const newCursors = {
        ...cursors.value
      };
      delete newCursors[msg.userId];
      cursors.value = newCursors;
    }
  };
};

// ストローク送信
const simplifyPoints = (
  points: {x:number, y:number}[],
  interval = 3
) => {

  return points.filter((_, index) => {
    return index % interval === 0;
  });

};

const sendStroke = (stroke: Stroke) => {

  if (!ws || ws.readyState !== WebSocket.OPEN) return;

  const sendData = {
    ...stroke,
    points: simplifyPoints(stroke.points, 3)
  };

  if (stroke.points.length > MAX_POINTS) {
    console.warn(
      "send cancelled",
      sendData.points.length
    );
    return;
  }

  const message = JSON.stringify({
    type: "stroke:add",
    boardId: route.params.id,
    userId,
    data: sendData
  });

  console.log(
    "stroke size:",
    message.length
  );

  ws.send(message);
};

// UNDO送信
const sendUndo = () => {

  if (!ws || ws.readyState !== WebSocket.OPEN) return;

  ws.send(JSON.stringify({
    type:"stroke:undo",
    boardId: route.params.id,
    userId
  }));
};

// REDO送信
const sendRedo = () => {

  if (!ws || ws.readyState !== WebSocket.OPEN) return;

  ws.send(JSON.stringify({
    type:"stroke:redo",
    boardId:route.params.id,
    userId     
  }));
};

// カーソル共有（マルチユーザー）
const cursors = ref<Record<string, {
  x: number;
  y: number;
  lastSeen: number;
}>>({});

// カーソル送信（60fps制限あり）
const sendCursor = (e: PointerEvent) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) return;

  const canvas = canvasRef.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();

  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;


  ws.send(JSON.stringify({
    type: "cursor:move",
    boardId: route.params.id,
    userId,
    x: (e.clientX - rect.left) * scaleX,
    y: (e.clientY - rect.top) * scaleY
  }));
};

// 初期化 / ライフサイクル
onMounted(async () => {

  // ボード取得
  const res = await api.get(`/api/boards/${route.params.id}`);
  board.value = res.data;

  // Canvas初期化
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext("2d");

    if (ctx) {
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height);

      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.lineWidth = lineWidth.value;
    }
  }
});

// WebSocket接続 + resize監視
onMounted(() => {
  connectWS();
  window.addEventListener("resize", () => {});
});

onUnmounted(() => {
  manualClose = true;

  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
  }
  
  if (ws) {
    manualClose = true;
    ws.close();
    ws = null;
  }

  window.removeEventListener("resize", () => {});
});

</script>