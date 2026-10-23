<template>
  <el-container>
    <el-main>
      <el-alert
        title="AI 招商助手：基于 ToolCalling（函数调用），可查询/统计系统业务数据，也可登记企业、提交审批等写操作（写操作会先向你复述确认，回复“确认”才真正执行）"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />

      <div class="chat-toolbar">
        <span class="chat-id"
          >会话ID：{{ chatId }}（同一会话内多轮对话共享记忆）</span
        >
        <el-button size="small" @click="newSession">开启新会话</el-button>
      </div>

      <div ref="boxRef" class="chat-box">
        <div v-if="messages.length === 0" class="empty-tip">
          试试下面的快捷问题，或直接输入你的问题
        </div>

        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
          <div class="bubble">{{ m.content }}</div>
        </div>

        <div v-if="loading" class="msg ai">
          <div class="bubble thinking">
            正在思考并调用工具<span class="dots">...</span>
          </div>
        </div>
      </div>

      <div class="quick-bar">
        <el-tag
          v-for="q in samples"
          :key="q"
          class="sample-tag"
          :effect="loading ? 'plain' : 'light'"
          @click="ask(q)"
        >
          {{ q }}
        </el-tag>
      </div>

      <div class="input-bar">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="输入问题，Enter 发送；写操作回复“确认”后执行"
          :disabled="loading"
          @keyup.enter.exact="send"
        />
        <el-button
          type="primary"
          class="send-btn"
          :loading="loading"
          :disabled="!input.trim()"
          @click="send"
        >
          发送
        </el-button>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { nextTick, ref } from "vue";
import { ElMessage } from "element-plus";

const backendBase = "http://localhost:8080";

const samples = [
  "现在系统里有哪些外资企业？",
  "外资企业主要来自哪些国家？",
  "各年份招商项目的总投资额是多少？",
  "PRJ010 的审批到哪一步了？",
  "帮我登记一家美国的新能源企业，叫星辰能源，注册资本5000万元",
];

const chatId = ref(newChatId());
const messages = ref([]); // {role: 'user'|'ai', content: string}
const input = ref("");
const loading = ref(false);
const boxRef = ref(null);

function newChatId() {
  return "web-" + Date.now().toString(36) + Math.floor(Math.random() * 1000);
}

function newSession() {
  chatId.value = newChatId();
  messages.value = [];
  ElMessage({ message: "已开启新会话", type: "success" });
}

function ask(q) {
  if (loading.value) return;
  input.value = q;
  send();
}

async function scrollBottom() {
  await nextTick();
  if (boxRef.value) {
    boxRef.value.scrollTop = boxRef.value.scrollHeight;
  }
}

async function send() {
  const text = input.value.trim();
  if (!text || loading.value) return;

  messages.value.push({ role: "user", content: text });
  input.value = "";
  loading.value = true;
  scrollBottom();

  try {
    const params = new URLSearchParams({ msg: text, chatId: chatId.value });
    const token =
      window.sessionStorage.getItem("zhaoshang_token") ||
      window.localStorage.getItem("zhaoshang_token");

    const resp = await fetch(
      `${backendBase}/ai/tool/chat?${params.toString()}`,
      {
        headers: token ? { hk_token: token } : {},
      },
    );

    if (!resp.ok) {
      const msg = await resp.text();
      throw new Error(msg || `HTTP ${resp.status}`);
    }

    const data = await resp.json();
    messages.value.push({ role: "ai", content: data.answer || "（无回答）" });
  } catch (e) {
    messages.value.push({
      role: "ai",
      content: "调用失败：" + (e.message || "未知错误"),
    });
  } finally {
    loading.value = false;
    scrollBottom();
  }
}
</script>

<style scoped>
.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chat-id {
  font-size: 12px;
  color: #909399;
}

.chat-box {
  height: 420px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.empty-tip {
  text-align: center;
  color: #c0c4cc;
  margin-top: 160px;
  font-size: 14px;
}

.msg {
  display: flex;
  margin-bottom: 12px;
}

.msg.user {
  justify-content: flex-end;
}

.msg.ai {
  justify-content: flex-start;
}

.bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 10px;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.msg.ai .bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 2px;
}

.thinking {
  color: #909399 !important;
}

.quick-bar {
  margin: 10px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sample-tag {
  cursor: pointer;
}

.input-bar {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.send-btn {
  height: 56px;
  width: 96px;
}
</style>
