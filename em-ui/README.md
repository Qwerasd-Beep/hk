# em-ui · 招商业务后台

> 2026-10-23：已恢复原业务页面、登录和按权限显示的业务导航；首页与模板布局保持不变。当前状态和前后代码对照见 [BUSINESS-RESTORE.md](./BUSINESS-RESTORE.md)。下面的 Demo 清单保留，旧纯静态说明已相应更新。

基于 [有来官方 JavaScript 模板](https://github.com/youlaitech/vue3-element-admin-js) 4.9.0，
复用原版左侧 Layout、SCSS、SVG、标签页及部分 Demo。保留上游 MIT 协议，见 LICENSE。

## 启动

```sh
npm install
npm run dev
```

开发地址：http://localhost:7070。未登录时进入登录页；真实业务需要先启动 em-server 及其 MySQL、Redis 等依赖。默认后端地址为 http://localhost:8080，可用 VITE_API_BASE_URL 修改。

```sh
npm run check:static
npm run build
npm run preview -- --host 127.0.0.1 --port 7071
```

预览地址：http://127.0.0.1:7071。生产部署使用 history 路由，需要把不存在的文件路径回退到 index.html。

## 保留页面

| 路由                         | 原版页面           | 本地交互                                               |
| ---------------------------- | ------------------ | ------------------------------------------------------ |
| /dashboard                   | 首页               | 静态统计、访问趋势、7/30 天切换                        |
| /table/crud-single           | CRUD 单文件示例    | 本地筛选、分页、新增、编辑、查看、删除、Excel 导入导出 |
| /table/auto-operation-column | 自适应操作栏       | 复选框控制操作按钮显示                                 |
| /form/basic                  | 原路由示例编辑表单 | 本地填写、保存、取消                                   |
| /form/navigate               | 原路由示例成员列表 | 筛选、详情弹窗、跳转编辑                               |
| /form/edit/:id               | 原路由示例编辑表单 | 根据本地记录 ID 回显、保存并返回                       |
| /component/icon-select       | 图标选择器         | 本地 SVG / Element Plus 图标选择                       |
| /component/icons             | 图标库             | 图标预览、复制使用代码                                 |
| /component/dictionary        | 字典组件           | 页面内静态选项，单选、多选和下拉                       |
| /component/drag              | 拖拽示例           | 列表与表格排序                                         |
| /component/text-scroll       | 滚动文本           | 滚动公告、打字机效果                                   |

Demo 的原始 Vue 文件路径保留在 src/views/demo 下；没有增加业务页面。
基础编辑表单复用官方 demo/route/navigate/edit.vue。
表格数据仅在当前组件实例内有效，刷新页面恢复初始数组。
路由表单为了演示跨页面回显，在当前浏览器标签页的 sessionStorage 中保存演示修改。

## 目录及改造前后

- src/layouts：复用官方左侧布局、导航栏、Logo、菜单、标签页和内容区。
- src/router/index.js：保留静态 Demo 路由，恢复 `/main/...` 业务路由、登录检查和业务菜单访问检查；菜单读取 `/login-info`，不动态注册后端组件。
- src/stores：仅 app（视觉布局状态）和 tags-view（标签/缓存状态），没有用户或权限 store。
- src/views/demo/table/crud-single.vue：原 UserAPI/DeptAPI/RoleAPI 改为本组件内数组和本地函数；分页使用 filter 和 slice。
- src/components/DictSelect 与 DictTag：原字典 store 加载改为接收当前 Demo 的 options。
- src/main.js：注册原业务按钮使用的 v-hasPerm，依据 `/login-info` 的 permissionList 显示。
- vite.config.js：仅 Vue、UnoCSS、Vue/Element Plus 自动导入；没有代理、mock 插件或请求拦截。
- src/utils/ajax.js 使用原生 fetch 兼容原业务请求；没有 axios 实例、mock 目录或国际化。

两种环境中 VITE_MOCK_DEV_SERVER=false、VITE_MOCK_ENABLED=false，
且依赖中没有任何 mock 插件；这两个开关只明确标记关闭状态，不会加载任何模拟服务。

Demo 数据及首页仍是组件内静态数据；登录与原业务页面使用真实后端请求，原 AI 页保留其流式请求。
首页及部分示例保留官方源码/文档外链，仅在用户主动点击时打开外部页面。

## 后续接入业务

在 src/views 增加你自己的业务 Vue 组件，在 src/router/index.js 手工添加静态路由。
原业务页可继续使用 utils/ajax.js 的 doGet/doPost/doPut/doDel。侧栏由当前账号的授权菜单决定，增加路由后还需要在现有后端权限系统配置相应菜单。
所有组件使用 JavaScript，Element Plus 组件与 Vue API 自动导入。

## 原工程备份

改造前 Git 工作区干净，已完整保存原提交和实际源码：

- ../em-ui-backups/em-ui-before-youlai-20261022.zip
- ../em-ui-backups/original-source-20261022/src
- ../em-ui-backups/original-source-20261022/public

原用户维护、企业、字典、审批、AI 等业务页面保存在上述备份中，不再参与当前静态模板运行。
恢复旧工程时应先备份当前改造结果，再解压原归档，避免混合两套 src 目录。
