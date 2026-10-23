<template>
  <el-container>
    <el-main>
      <el-alert
        title="生成后图片会下载保存到后端项目 uploads/ai-image 目录，页面使用项目内 URL 展示，避免临时签名地址过期"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-form label-width="90px">
        <el-form-item label="提示词">
          <el-input
            v-model="prompt"
            type="textarea"
            :rows="3"
            placeholder="请输入想要生成的图片内容，例如：一只可爱的橘猫在阳光下打盹"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item label="尺寸">
          <el-select v-model="size" style="width: 220px" :disabled="loading">
            <el-option
              v-for="item in sizeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="风格">
          <el-select v-model="style" style="width: 220px" :disabled="loading">
            <el-option
              v-for="item in styleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!prompt.trim()"
            @click="generate"
          >
            生成图片
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

      <el-card v-if="loading || imageUrls.length">
        <template #header>
          <span>{{ loading ? "生成中，请稍候..." : "生成结果" }}</span>
        </template>

        <el-skeleton v-if="loading" :rows="8" animated />

        <div v-else class="image-list">
          <div v-for="url in imageUrls" :key="url" class="image-item">
            <el-image
              :src="url"
              fit="contain"
              class="generated-image"
              :preview-src-list="imageUrls"
              preview-teleported
            />
            <div class="image-actions">
              <el-link :href="url" target="_blank" type="primary">
                在新窗口打开
              </el-link>
            </div>
          </div>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { doGet } from "@/utils/ajax";

const backendBase = "http://localhost:8080";

const prompt = ref("一只可爱的橘猫在阳光下打盹");
const size = ref("1024*1024");
const style = ref("auto");
const loading = ref(false);
const error = ref("");
const imageUrls = ref([]);

const sizeOptions = ["1024*1024", "720*1280", "1280*720"];

const styleOptions = [
  { label: "自动", value: "auto" },
  { label: "摄影", value: "photography" },
  { label: "人像", value: "portrait" },
  { label: "3D 卡通", value: "3d cartoon" },
  { label: "动漫", value: "anime" },
  { label: "油画", value: "oil painting" },
  { label: "水彩", value: "watercolor" },
  { label: "素描", value: "sketch" },
  { label: "中国画", value: "chinese painting" },
  { label: "扁平插画", value: "flat illustration" },
];

function toAbsoluteUrl(path) {
  return path.startsWith("http") ? path : backendBase + path;
}

async function generate() {
  if (!prompt.value.trim()) return;

  loading.value = true;
  error.value = "";
  imageUrls.value = [];

  try {
    const resp = await doGet("/ai/text2image-local", {
      prompt: prompt.value,
      size: size.value,
      style: style.value,
      n: 1,
    });

    const data = resp.data || {};
    const urls = data.urls || [];
    imageUrls.value = urls.map(toAbsoluteUrl);

    if (imageUrls.value.length === 0) {
      error.value = "生成失败：" + JSON.stringify(data.metadata || {});
    } else {
      ElMessage({
        message: "图片已生成并保存到项目 uploads/ai-image 目录",
        type: "success",
      });
    }
  } catch (e) {
    error.value =
      "请求失败：" + (e.response?.data?.message || e.message || "未知错误");
  } finally {
    loading.value = false;
  }
}

function clear() {
  error.value = "";
  imageUrls.value = [];
}
</script>

<style scoped>
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.image-item {
  width: 320px;
}

.generated-image {
  width: 320px;
  height: 320px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
}

.image-actions {
  margin-top: 8px;
}
</style>
