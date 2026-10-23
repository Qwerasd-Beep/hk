<template>
  <el-container>
    <el-main>
      <el-alert
        title="输入文本后调用后端 /ai/text2sound 接口生成语音，返回音频字节流并直接在网页中播放"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-form label-width="90px">
        <el-form-item label="文本">
          <el-input
            v-model="text"
            type="textarea"
            :rows="4"
            placeholder="请输入要转换成语音的文本"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="音色">
          <el-input
            v-model="voice"
            placeholder="例如：longhua"
            style="width: 260px"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="语速">
          <el-input-number
            v-model="speed"
            :min="0.5"
            :max="2"
            :step="0.1"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="格式">
          <el-select v-model="format" style="width: 160px" :disabled="loading">
            <el-option label="mp3" value="mp3" />
            <el-option label="wav" value="wav" />
            <el-option label="pcm" value="pcm" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!text.trim()"
            @click="generate"
          >
            生成语音
          </el-button>
          <el-button :disabled="loading" @click="clear">清空</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-card v-if="audioUrl">
        <template #header>
          <span>生成结果</span>
        </template>

        <audio :src="audioUrl" controls autoplay class="audio-player" />

        <div class="audio-actions">
          <el-link
            :href="audioUrl"
            :download="'ai-sound.' + format"
            type="primary"
          >
            下载音频
          </el-link>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { onBeforeUnmount, ref } from "vue";
import { ElMessage } from "element-plus";

const backendBase = "http://localhost:8080";

const text = ref("你好，这是一段由 AI 生成的语音。");
const voice = ref("longhua");
const speed = ref(1.0);
const format = ref("mp3");
const loading = ref(false);
const error = ref("");
const audioUrl = ref("");

async function generate() {
  if (!text.value.trim()) return;

  loading.value = true;
  error.value = "";

  try {
    const params = new URLSearchParams({
      text: text.value,
      voice: voice.value,
      speed: String(speed.value),
      format: format.value,
    });

    const token =
      window.sessionStorage.getItem("zhaoshang_token") ||
      window.localStorage.getItem("zhaoshang_token");

    const resp = await fetch(
      `${backendBase}/ai/text2sound?${params.toString()}`,
      {
        headers: token ? { hk_token: token } : {},
      },
    );

    if (!resp.ok) {
      const msg = await resp.text();
      throw new Error(msg || `HTTP ${resp.status}`);
    }

    const blob = await resp.blob();
    if (audioUrl.value) {
      URL.revokeObjectURL(audioUrl.value);
    }
    audioUrl.value = URL.createObjectURL(blob);

    ElMessage({
      message: "语音生成成功",
      type: "success",
    });
  } catch (e) {
    error.value = "生成失败：" + (e.message || "未知错误");
  } finally {
    loading.value = false;
  }
}

function clear() {
  error.value = "";
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value);
    audioUrl.value = "";
  }
}

onBeforeUnmount(() => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value);
  }
});
</script>

<style scoped>
.audio-player {
  width: 100%;
  max-width: 720px;
}

.audio-actions {
  margin-top: 12px;
}
</style>
