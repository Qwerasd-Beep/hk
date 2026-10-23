<template>
  <div class="login-page" :class="{ 'is-dark': appStore.resolvedTheme === 'dark' }">
    <div class="login-toolbar">
      <el-button class="login-toolbar__theme" text circle :aria-label="appStore.resolvedTheme === 'dark' ? '切换浅色模式' : '切换深色模式'" @click="toggleTheme">
        <el-icon :size="20"><Sunny v-if="appStore.resolvedTheme === 'light'" /><Moon v-else /></el-icon>
      </el-button>
    </div>
    <div class="login-layout">
      <section class="login-brand" aria-label="平台介绍">
        <div class="login-brand__header">
          <img :src="logo" class="login-brand__logo" alt="平台 Logo" />
          <div class="login-brand__identity"><span class="login-brand__name">招商管理平台</span></div>
        </div>
        <div class="login-brand__hero">
          <el-tag class="login-brand__tag" type="primary" effect="plain" round>
            <span class="login-brand__tag-dot" />Enterprise Ready
          </el-tag>
          <h1 class="login-brand__title">开箱即用的企业级中后台解决方案</h1>
          <div class="login-brand__features">
            <div class="login-brand__feature"><span class="login-brand__feature-mark"><span class="login-brand__feature-icon i-svg:security" /></span><span class="login-brand__feature-text">安全可靠</span></div>
            <div class="login-brand__feature"><span class="login-brand__feature-mark"><el-icon class="login-brand__feature-icon"><Clock /></el-icon></span><span class="login-brand__feature-text">高效稳定</span></div>
            <div class="login-brand__feature"><span class="login-brand__feature-mark"><span class="login-brand__feature-icon i-svg:flexible" /></span><span class="login-brand__feature-text">灵活扩展</span></div>
          </div>
        </div>
      </section>
      <main class="login-card">
        <div class="login-card__inner">
          <div class="login-card__form">
            <h2 class="login-card__title">欢迎回来</h2>
            <p class="login-card__desc">请完成身份验证后进入系统</p>
            <el-form ref="formRef" :model="user" :rules="rules" size="large" :validate-on-rule-change="false" @submit.prevent="login">
              <el-form-item prop="name">
                <el-input v-model="user.name" placeholder="账号" aria-label="账号" autocomplete="username" :prefix-icon="UserIcon" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="user.password" placeholder="密码" aria-label="密码" type="password" show-password autocomplete="current-password" :prefix-icon="LockIcon" />
              </el-form-item>
              <div class="login-card__options"><el-checkbox v-model="user.rememberMe">记住我</el-checkbox></div>
              <el-button class="login-card__submit" type="primary" size="large" native-type="submit" :loading="submitting">登录</el-button>
            </el-form>
          </div>
        </div>
        <footer class="login-footer">招商管理平台</footer>
      </main>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from "vue";
import { User as UserIcon, Lock as LockIcon } from "@element-plus/icons-vue";
import { useAppStore } from "@/stores";
import logo from "@/assets/images/logo.png";
const appStore = useAppStore();
function toggleTheme() {
  appStore.resolvedTheme = appStore.resolvedTheme === "dark" ? "light" : "dark";
}
import { ElMessage } from "element-plus";
import { doPost } from "@/utils/ajax";
import { clearToken } from "@/utils/common";
import { loginUser } from "@/composables/useLoginUser";
const formRef = ref();
const submitting = ref(false);
const user = reactive({name:"", password:"", rememberMe:false});
const rules = {name:[{required:true,message:"请输入账号",trigger:"blur"}], password:[{required:true,message:"请输入密码",trigger:"blur"}]};
async function login() {
  if (submitting.value || !await formRef.value.validate().catch(() => false)) return;
  submitting.value = true;
  try {
    const form = new FormData();
    Object.entries(user).forEach(([key,value]) => form.append(key,value));
    const {data} = await doPost("/login",form);
    if (data.code !== 20000 || !data.data) throw new Error(data.msg || "登录失败");
    clearToken();
    loginUser.value = null;
    (user.rememberMe ? localStorage : sessionStorage).setItem("zhaoshang_token",data.data);
    // 整页跳转清除上一账号的组件缓存和标签状态。
    location.replace("/dashboard");
  } catch(error) { ElMessage.error(error.message); }
  finally { submitting.value = false; }
}
</script>
<style lang="scss" scoped>
$text-primary: #273248;
$text-secondary: #667085;
$text-muted: #98a2b3;
$input-h: 44px;

.login-page {
  // 品牌色阶：从运行时主题色派生，切换主题色板（ArcoD/AntD/ElementD）时整页跟随
  --brand-strong: color-mix(in srgb, var(--el-color-primary), #000 15%);
  --brand-deep: color-mix(in srgb, var(--el-color-primary), #000 45%);

  position: relative;
  display: flex;
  min-height: 100vh;
  overflow: auto;
  /* 浅色渐变底色；桌面端被品牌区背景与白色卡片完全覆盖，窄屏两区透明后透出 */
  background: linear-gradient(180deg, #ffffff 0%, #dbeafe 100%);
}

.login-toolbar {
  position: fixed;
  top: 28px;
  right: 32px;
  z-index: 10;
  display: flex;
  gap: 12px;
  align-items: center;

  :deep(*) {
    cursor: pointer;
  }
}

.login-layout {
  display: flex;
  flex: 1;
  min-height: 100%;
}

.login-brand {
  position: relative;
  display: flex;
  flex: 0 0 65%;
  flex-direction: column;
  min-height: 100vh;
  padding: 28px 64px 48px;
  overflow: hidden;
  // 白云蓝海渐变（原 bg.svg）：白 → 主题色浅蓝，各 stop 按主题色混合比例派生
  background: linear-gradient(
    180deg,
    #fff 0%,
    color-mix(in srgb, var(--el-color-primary) 3%, #fff) 16%,
    color-mix(in srgb, var(--el-color-primary) 5%, #fff) 32%,
    color-mix(in srgb, var(--el-color-primary) 6%, #fff) 48%,
    color-mix(in srgb, var(--el-color-primary) 8%, #fff) 62%,
    color-mix(in srgb, var(--el-color-primary) 15%, #fff) 76%,
    color-mix(in srgb, var(--el-color-primary) 25%, #fff) 88%,
    color-mix(in srgb, var(--el-color-primary) 42%, #fff) 100%
  );
  animation: login-pane-in 0.36s ease-out both;

  &__header,
  &__hero {
    position: relative;
    z-index: 1;
  }

  &__header {
    display: flex;
    gap: 14px;
    align-items: center;
  }

  &__logo {
    box-sizing: border-box;
    width: 42px;
    height: 42px;
    padding: 5px;
    background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
    border: 1px solid color-mix(in srgb, var(--el-color-primary) 18%, transparent);
    border-radius: 12px;
  }

  &__identity {
    display: inline-flex;
    gap: 10px;
    align-items: center;
    min-width: 0;
  }

  &__name {
    font-size: 24px;
    font-weight: 600;
    line-height: 1;
    color: var(--brand-deep);
  }

  &__version {
    display: inline-flex;
    align-items: center;
    height: 22px;
    padding: 0 8px;
    font-size: 12px;
    font-weight: 600;
    line-height: 1;
    color: color-mix(in srgb, var(--brand-deep) 80%, transparent);
    background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
    border: 1px solid color-mix(in srgb, var(--el-color-primary) 16%, transparent);
    border-radius: 999px;
  }

  &__hero {
    display: flex;
    flex: 1;
    flex-direction: column;
    justify-content: center;
    width: min(760px, 100%);
    padding: 40px 0 60px;
  }

  &__tag {
    gap: 8px;
    // 去掉 main 包装层后 tag 成为 hero 的 flex 子项，防止被 stretch 拉满宽度
    align-self: flex-start;
    height: 32px;
    padding: 0 15px 0 13px;
    margin-bottom: 20px;
    font-size: 12px;
    font-weight: 700;
    color: var(--brand-strong);
    background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
    border-color: color-mix(in srgb, var(--el-color-primary) 16%, transparent);

    :deep(.el-tag__content) {
      display: inline-flex;
      gap: 8px;
      align-items: center;
    }
  }

  &__tag-dot {
    display: inline-block;
    flex-shrink: 0;
    width: 7px;
    height: 7px;
    background: var(--el-color-primary);
    border-radius: 50%;
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--el-color-primary) 16%, transparent);
  }

  &__title {
    margin: 0;
    font-size: 36px;
    font-weight: 800;
    line-height: 1.15;
    color: var(--brand-deep);
    letter-spacing: 0;
  }

  &__desc {
    max-width: 560px;
    margin: 0;
    font-size: 18px;
    line-height: 1.6;
    color: #475569;
  }

  &__features {
    display: inline-flex;
    align-items: center;
    width: fit-content;
    max-width: 100%;
    margin-top: 32px;
  }

  &__feature {
    position: relative;
    display: inline-flex;
    gap: 10px;
    align-items: center;
    height: 32px;
    padding: 0 15px;
    font-size: 14px;
    font-weight: 600;
    color: color-mix(in srgb, var(--brand-deep) 85%, transparent);
    background: transparent;

    &:first-child {
      padding-left: 0;
    }

    &:not(:last-child)::after {
      position: absolute;
      top: 7px;
      right: 0;
      width: 1px;
      height: 14px;
      content: "";
      background: color-mix(in srgb, var(--el-color-primary) 18%, transparent);
    }
  }

  &__feature-mark {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    color: var(--brand-strong);
    background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
    border: 1px solid color-mix(in srgb, var(--el-color-primary) 16%, transparent);
    border-radius: 6px;
  }

  &__feature-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    color: var(--brand-strong);
  }

  &__feature-text {
    line-height: 1;
    white-space: nowrap;
  }
}

.login-card {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 0 0 35%;
  flex-direction: column;
  align-items: center;
  padding: 0 0 32px;
  background: #fff;
  animation: login-pane-in 0.36s ease-out 0.04s both;

  &__inner {
    box-sizing: border-box;
    display: flex;
    flex: 1;
    flex-direction: column;
    justify-content: center;
    width: 100%;
    max-width: 430px;
    padding: 0 20px;
  }

  &__form {
    width: 100%;
  }

  &__title {
    margin: 0 0 10px;
    font-size: 28px;
    font-weight: 750;
    line-height: 1.1;
    color: $text-primary;
    letter-spacing: 0;
  }

  &__desc {
    margin: 10px 0 28px;
    font-size: 15px;
    color: $text-muted;
  }

  &__prefix-icon {
    display: inline-flex;
    width: 14px;
    height: 14px;
    color: var(--el-text-color-placeholder);
  }



  &__options {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 22px;
    font-size: 14px;
    color: $text-secondary;

    a {
      font-weight: 500;
      color: var(--el-color-primary);
      cursor: pointer;
      transition: opacity 0.15s;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  &__submit {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
    box-shadow: 0 12px 24px color-mix(in srgb, var(--el-color-primary) 18%, transparent);

    &:hover {
      box-shadow: 0 14px 28px color-mix(in srgb, var(--el-color-primary) 22%, transparent);
    }

    &:focus,
    &:focus-visible {
      outline: none;
    }
  }



  /* 半透明白底按钮，在渐变背景上保持可见；hover 时提实并加深投影 */

}

:deep(.el-form-item) {
  margin-bottom: 14px;
}

:deep(.el-input__wrapper) {
  height: $input-h;
}

.login-footer {
  flex-shrink: 0;
  font-size: 12px;
  color: $text-muted;
}

.login-page.is-dark {
  background: #0b1020;
}

.login-page.is-dark .login-brand {
  // 深蓝渐变底（原 bg-dark.svg），暗色下不随主题色变化
  background: linear-gradient(135deg, #0f1a33 0%, #0c1428 55%, #0a1020 100%);

  // 网格纹理（原 bg-dark.svg pattern）：白色细线 + 上浓下淡遮罩
  &::before {
    position: absolute;
    inset: 0;
    content: "";
    background-image:
      linear-gradient(rgb(255 255 255 / 5%) 1px, transparent 1px),
      linear-gradient(90deg, rgb(255 255 255 / 5%) 1px, transparent 1px);
    background-size: 72px 72px;
    mask-image: linear-gradient(
      to bottom,
      rgb(0 0 0 / 90%) 0%,
      rgb(0 0 0 / 30%) 70%,
      transparent 100%
    );
  }

  &__logo {
    background: rgb(255 255 255 / 10%);
    border-color: rgb(255 255 255 / 20%);
  }

  &__name {
    color: rgb(255 255 255 / 92%);
  }

  &__version {
    color: rgb(255 255 255 / 85%);
    background: rgb(255 255 255 / 10%);
    border-color: rgb(255 255 255 / 16%);
  }

  &__tag {
    color: #fff;
    background: rgb(255 255 255 / 8%);
    border-color: rgb(255 255 255 / 16%);
  }

  &__tag-dot {
    background: #fff;
    box-shadow: 0 0 0 3px rgb(255 255 255 / 18%);
  }

  &__title {
    color: #fff;
  }

  &__desc {
    color: rgb(255 255 255 / 65%);
  }

  &__feature {
    color: rgb(255 255 255 / 85%);

    &:not(:last-child)::after {
      background: rgb(255 255 255 / 18%);
    }
  }

  &__feature-mark {
    color: #fff;
    background: rgb(255 255 255 / 10%);
    border-color: rgb(255 255 255 / 16%);
  }

  &__feature-icon {
    color: #fff;
  }
}

.login-page.is-dark .login-card {
  background: #0b1020;

  &__title {
    color: rgb(255 255 255 / 85%);
  }

  &__desc {
    color: rgb(255 255 255 / 30%);
  }








}

.login-page.is-dark .login-footer {
  color: rgb(255 255 255 / 15%);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@keyframes login-pane-in {
  from {
    opacity: 0;
    filter: blur(4px);
  }

  to {
    opacity: 1;
    filter: blur(0);
  }
}

@media (max-width: 1024px) {
  .login-layout {
    flex-direction: column;
  }

  .login-toolbar {
    position: absolute;
    top: 37px;
  }

  .login-brand {
    flex: none;
    height: auto;
    min-height: auto;
    padding: 28px 40px 32px;
    background: transparent;

    &__hero {
      display: none;
    }
  }

  /* 深色品牌区的渐变与网格纹理不适合窄屏，透出页面底色 */
  .login-page.is-dark .login-brand {
    background: none;

    &::before {
      display: none;
    }
  }

  /* 卡片占满剩余高度并透出页面渐变，inner 沿用全局的垂直居中，页脚沉底 */
  .login-card {
    flex: 1;
    padding: 40px 48px 32px;
    background: transparent;
  }

  .login-page.is-dark .login-card {
    background: transparent;
  }
}

@media (max-width: 640px) {
  .login-toolbar {
    top: 33px;
    right: 20px;
  }

  .login-brand {
    padding: 24px 24px 28px;
  }

  .login-card {
    padding: 32px 24px 24px;

    &__inner {
      width: 100%;
      padding: 0;
    }
  }
}

/* 适配本项目的无验证码账号密码表单。 */
.login-page, .login-brand, .login-card { box-sizing: border-box; }
.login-page { min-height: 100svh; }
.login-brand, .login-card { min-width: 0; }
.login-toolbar__theme { color: var(--el-text-color-regular); }
:deep(.el-input__wrapper) { box-sizing: border-box; }
:deep(.el-form-item) { margin-bottom: 22px; }
.login-card__submit:focus-visible { outline: 2px solid var(--el-color-primary); outline-offset: 4px; }
@media (prefers-reduced-motion: reduce) { .login-brand, .login-card { animation: none; } }
@media (max-width: 640px) { .login-brand__name { font-size: 20px; } .login-brand__header { padding-right: 32px; } }
</style>
