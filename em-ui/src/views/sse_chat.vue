<template>
  <h1>aaa</h1>
  <input v-model="msg" />
  <button @click="test">发送</button>
  <button @click="stop">停</button>
  <div>{{ awnser }}</div>
</template>
<script setup>
import { ref } from "vue";

let msg = ref("你是什么职业");
const awnser = ref("等待回答");

const token =
  window.sessionStorage.getItem("zhaoshang_token") ||
  window.localStorage.getItem("zhaoshang_token");
const headers = {};
headers["hk_token"] = token;
headers["Content-Type"] = "application/json";

let controller = null;

async function test() {
  alert("ok");
  controller = new AbortController();

  const resp = await fetch("http://localhost:8080/ai/dashscope-postask", {
    method: "POST",
    headers,

    body: JSON.stringify({
      msg: encodeURIComponent(msg.value),
      userName: "admin",
      password: "123456",
    }),
    signal: controller.signal, //把中断信息绑到请求上,将来调用 controller.abort() 就能中断这次请求
  });

  const reader = resp.body.getReader();
  const decoder = new TextDecoder("utf-8");

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    let msg = decoder.decode(value, { stream: true });
    awnser.value += msg;
  }

  controller = null;
}

function stop() {
  controller?.abort();
}
</script>
