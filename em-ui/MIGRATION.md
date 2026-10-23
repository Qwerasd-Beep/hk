# 本次改造的代码对照

## 1. 项目入口与路由

原 em-ui 的 src/main.js 注册了权限指令并读取 JWT，src/router/index.js 的根路径显示登录页：

```js
app.directive("hasPerm", (el, binding) => {
  // 从浏览器存储的 token 读取 permissionList，决定按钮是否显示
});
// 原路由
{ path: "/", component: login }
```

现在入口仅挂载路由、Pinia 和本地图标；根路径直接显示后台布局：

```js
app.use(store);
app.use(router);
// 静态路由，不注册登录或权限守卫
{ path: "/", component: Layout, redirect: "/dashboard", children: [/* 固定页面 */] }
```

## 2. 官方 Demo 的数据来源

上游 src/views/demo/table/crud-single.vue 原来导入三个业务 API：

```js
import UserAPI from "@/api/system/user";
import DeptAPI from "@/api/system/dept";
import RoleAPI from "@/api/system/role";
indexAction(params) { return UserAPI.getPage(params); }
```

现在同一 Vue 文件内部定义 12 条静态用户记录、部门、角色和性别选项，保留原模板及配置式 CRUD 组件：

```js
const localUsers = ref([
  { id: 1, username: "admin", nickname: "管理员", /* 固定字段 */ },
  // 其余固定记录均在该组件中
]);
function getLocalPage(params = {}) {
  const filtered = filterLocalUsers(params);
  const start = (Number(params.pageNum || 1) - 1) * Number(params.pageSize || 10);
  return Promise.resolve({
    total: filtered.length,
    list: filtered.slice(start, start + Number(params.pageSize || 10)),
  });
}
```

Promise 仅为了兼容原 CRUD 组件的回调约定，不发 HTTP，也没有模拟服务、定时网络或请求拦截。
新增使用 unshift，编辑修改组件内数组项，删除使用 filter；Excel 导入读取用户本机文件，导出通过本地 Workbook 和 Blob 完成。

## 3. 布局与数据字典

上游布局通过权限 store 读取动态菜单：

```js
const permissionStore = usePermissionStore();
const routes = computed(() => permissionStore.routes);
```

现在菜单直接读取本地静态路由：

```js
import { constantRoutes } from "@/router";
const routes = computed(() => constantRoutes);
```

上游 DictSelect/DictTag 通过字典 store 加载选项，现在由 Demo 组件内的固定数组通过 options 属性传入；没有字典 store 或字典接口。

## 4. mock、请求与国际化

- 没有复制官方 mock、src/api、src/lang、user/permission/tenant/dict store。
- 删除原 em-ui 的 axios 依赖和 src/utils/ajax.js（旧文件完整保存在工程外备份中）。
- Vite 只启用 vue、UnoCSS、自动导入插件，未引入 mockDevServerPlugin，也没有 server.proxy。
- 两个环境文件显式设置 VITE_MOCK_DEV_SERVER=false 和 VITE_MOCK_ENABLED=false。
- 面包屑、侧边栏、标签页直接显示中文 meta.title，不再调用翻译函数。
- Header 使用固定本地演示标识；语言、登录退出、通知请求、租户切换均已移除。
- Pinia 仅保留 app 和 tags-view 两个布局相关 store。

## 5. 保留与备份

官方 Demo 的 9 个 Vue 文件保留在 src/views/demo 中，表单页面复用官方路由 Demo，不新增业务页面。
完整上游 Layout 样式与图标放在 src/layouts、src/styles、src/assets/icons；原版 MIT 协议见 LICENSE。
原工程旧业务代码位于 ../em-ui-backups/original-source-20261022，原完整提交归档位于 ../em-ui-backups/em-ui-before-youlai-20261022.zip。

生产预览的页面仅加载项目自身静态资源。开发模式下 Vite 自身的热更新 WebSocket 属于开发工具，不是业务接口或 mock 服务。

更多路由与运行方式见 README.md。
