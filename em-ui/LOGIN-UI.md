# 登录界面改造（2026-10-23）

## 范围

仅修改 src/views/login.vue 的模板、样式和展示用依赖。复用有来官方 JavaScript 模板的登录布局、渐变、品牌区、表单样式和响应式样式，沿用现有本地 Logo；保留项目 LICENSE。
来源：https://github.com/youlaitech/vue3-element-admin-js

## 修改前

```vue
<div class="business-login">
  <el-card class="login-card">
    <h2>招商管理平台</h2>
    <!-- 账号、密码、记住我、登录 -->
  </el-card>
</div>
```

```css
.business-login { min-height:100vh; display:grid; place-items:center; }
.login-card { width:min(420px, 92vw); }
.el-button { float:right; }
```

## 修改后

```vue
<div class="login-page" :class="{ 'is-dark': appStore.resolvedTheme === 'dark' }">
  <!-- 深浅色切换按钮 -->
  <div class="login-layout">
    <section class="login-brand"><!-- Logo、平台名、介绍、特点 --></section>
    <main class="login-card">
      <div class="login-card__inner">
        <h2 class="login-card__title">欢迎回来</h2>
        <!-- 保留 formRef / user / rules / login / submitting，输入框加图标和 placeholder -->
      </div>
    </main>
  </div>
</div>
```

```scss
.login-brand { flex:0 0 65%; /* 官方渐变背景和品牌排版 */ }
.login-card { flex:0 0 35%; /* 登录表单独立区域 */ }
.login-card__submit { width:100%; height:44px; }
@media (max-width:1024px) {
  .login-layout { flex-direction:column; }
  .login-brand__hero { display:none; }
}
```

新增 app 布局 store 的主题切换，深色样式局限于本组件 `.login-page.is-dark`，不修改全局页面定位。样式保持 scoped；尊重系统减少动画偏好。

## 不变项

- async function login() 函数逐字对比未变。
- 账号/密码仍只检查必填，不擅自套用官方六位密码规则。
- POST /login 的 FormData、name/password/rememberMe 字段、返回码处理、token 存储和首页跳转不变。
- ajax.js、后端和数据库本次未修改。
- 未添加验证码、扫码、注册、忘记密码、演示账号、mock 或国际化。

## 验证

- npm run build、npm run check:static 通过。
- 浏览器 1440×900：左右区域分别为 936px / 504px，无水平溢出。
- 390×844：品牌主标题区隐藏、表单自适应，无水平溢出。
- 深色切换后表单区背景为 rgb(11,16,32)，可切回浅色。
- 空提交显示“请输入账号”和“请输入密码”，不发出接口请求。
- 测试中无页面脚本错误；未使用真实账号密码验证登录。
- 截图：output/playwright/login-desktop.png、login-dark.png、login-mobile.png。

修改前文件备份：../em-ui-backups/login-before-youlai-20261023.vue。
