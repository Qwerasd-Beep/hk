# 2026-10-23 恢复原业务与权限导航

## 原因

上一轮按“纯静态模板”要求，侧栏使用 constantRoutes，业务 Vue、登录及请求工具移到备份中。因此旧菜单和旧业务页不会显示。这次保留模板 Layout 和 dashboard，恢复真实业务数据与授权菜单。

## 修改前后

### src/layouts/composables/useLayout.js

之前：
```js
routes: computed(() => constantRoutes)
```

之后：
```js
routes: computed(() => [
  constantRoutes.find(route => route.path === "/"),
  ...toSidebarMenus(loginUser.value?.menuList),
])
```

其中 menuName → meta.title、url → path、icon → Element Plus 图标名、subMenuList → children。菜单仍由后端 `/login-info` 按用户角色筛选，不在前端写死用户角色。

### src/router/index.js

之前只有 `/dashboard`、`/table`、`/form`、`/component` 等 Demo 路由，无登录检查。

之后增加 `/login`、原来的 `/main/...` 业务路由，外层仍使用当前 Layout：
```js
{ path: "/main", component: Layout, children: [
  { path: "users", component: () => import("@/views/users.vue"), meta: {title:"用户维护", menuPath:"/main/users"} },
  // 企业列表、添加、编辑、详情、字典、中介、审批、角色授权、原 AI 页面
] }
```
beforeEach 检查 token，读取登录用户；业务页还检查对应 menuPath 是否在用户菜单树中。无权限时回首页，无登录时跳转登录。API 权限仍由后端校验，前端菜单隐藏不能代替后端鉴权。

### src/main.js

之前无权限指令；之后恢复 `v-hasPerm`：
```js
app.directive("hasPerm", {
  mounted(el, binding) {
    if (!hasPermission(binding.value)) el.remove();
  },
});
```
permissionList 取自本次 `/login-info`，而非手动解析浏览器 JWT。

### src/utils/ajax.js / common.js

之前不存在。恢复旧页面的消息框工具与 `doGet/doPost/doPut/doDel` 调用接口。
请求工具内部用原生 fetch，保留旧页面的 `resp.data.code`、`resp.data.data` 结构；传递 hk_token 和 rememberMe，数组查询参数重复编码，不添加任何 mock 或 axios 依赖。

### 登录和退出

之前无需登录、工具栏显示“本地演示”。之后 `/login` 向 `/login` 提交原 name/password/rememberMe 表单，成功保存原 zhaoshang_token；工具栏显示真实 name、支持退出。切换账户采用整页导航，清理前一个账户的内存菜单、标签和组件缓存。登录页样式 scoped，避免旧登录页全局 `.el-container` 等样式覆盖新 Layout。

### src/layouts/components/LayoutMain.vue

之前没有 `provide("reload")`。之后提供旧页面需要的 reload 方法，清理当前缓存并重新挂载当前组件。原业务组件常有多个根节点，渲染时加一层带间距的 business-page 容器以兼容新布局的切换动画，不改变其内部业务逻辑。

### 原业务页

从 `../em-ui-backups/original-source-20261022/src/views/` 恢复：users、user_details、foreign_enterprise、foreign_enterprise_add、dictionary、agencies、approval_processinfo、role_perm、sse_chat、ai_chat、ai_img_generate、ai_sound。
业务逻辑沿用备份；dictionary 的 `<style>` 改为 `<style scoped>`，避免影响整个新布局。
没有恢复旧 main.vue 布局；dashboard 未修改。Demo 文件和静态数据保留，但不出现在业务侧栏中。

## 验证与备份

- 修改前的静态版备份：`../em-ui-backups/em-ui-static-before-business-20261023.zip`。
- 构建通过；Demo 静态检查通过，无 mock、axios、国际化。
- 浏览器验证未登录访问根地址与业务路由均跳转 `/login`，登录表单正常显示。
- 本机 `localhost:8080` 当前拒绝连接，因此尚不能验证真实账号登录、实际菜单和业务接口数据。未伪造登录信息或接口响应，也没有修改后端或数据库。

本文件是本次状态说明；MIGRATION.md / VERIFICATION.md 中 2026-10-22 的记录是上一轮纯静态版的历史，不再代表当前业务网络行为。
