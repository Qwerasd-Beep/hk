import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import { getToken } from "@/utils/ajax";
import { loadLoginUser } from "@/composables/useLoginUser";

const Layout = () => import("@/layouts/index.vue");
export const constantRoutes = [
  { path: "/login", component: () => import("@/views/login.vue"), meta: { hidden: true } },
  {
    path: "/main", component: Layout, redirect: "/dashboard",
    meta: { hidden: true },
    children: [
      { path: "users", name: "users", component: () => import("@/views/users.vue"), meta: { title: "用户维护", menuPath: "/main/users" } },
      { path: "user-details/:id", name: "user_details", component: () => import("@/views/user_details.vue"), meta: { title: "用户详情", menuPath: "/main/users", activeMenu: "/main/users" } },
      { path: "foreign-enterprise", name: "foreign_enterprise", component: () => import("@/views/foreign_enterprise.vue"), meta: { title: "企业信息", menuPath: "/main/foreign-enterprise" } },
      { path: "foreign-enterprise/add", name: "foreign_enterprise_add", component: () => import("@/views/foreign_enterprise_add.vue"), meta: { title: "添加企业", menuPath: "/main/foreign-enterprise", activeMenu: "/main/foreign-enterprise" } },
      { path: "foreign-enterprise/edit/:id", name: "foreign_enterprise_edit", component: () => import("@/views/foreign_enterprise_add.vue"), meta: { title: "编辑企业", menuPath: "/main/foreign-enterprise", activeMenu: "/main/foreign-enterprise" } },
      { path: "dictionary", name: "dictionary", component: () => import("@/views/dictionary.vue"), meta: { title: "字典维护", menuPath: "/main/dictionary" } },
      { path: "agencies", name: "agencies", component: () => import("@/views/agencies.vue"), meta: { title: "中介机构", menuPath: "/main/agencies" } },
      { path: "approval-processinfo", name: "approval_processinfo", component: () => import("@/views/approval_processinfo.vue"), meta: { title: "审批办理", menuPath: "/main/approval-processinfo" } },
      { path: "role-perm", name: "role_perm", component: () => import("@/views/role_perm.vue"), meta: { title: "系统角色管理", menuPath: "/main/role-perm" } },
      { path: "sse-chat", name: "sse_chat", component: () => import("@/views/sse_chat.vue"), meta: { title: "AI 对话", menuPath: "/main/sse-chat" } },
      { path: "ai-chat", name: "ai_chat", component: () => import("@/views/ai_chat.vue"), meta: { title: "智能对话", menuPath: "/main/ai-chat" } },
      { path: "ai-img-generate", name: "ai_img_generate", component: () => import("@/views/ai_img_generate.vue"), meta: { title: "图片生成", menuPath: "/main/ai-img-generate" } },
      { path: "ai-sound", name: "ai_sound", component: () => import("@/views/ai_sound.vue"), meta: { title: "语音生成", menuPath: "/main/ai-sound" } },
    ],
  },
  {
    path: "/",
    component: Layout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        meta: { title: "首页", icon: "homepage", affix: true, keepAlive: true },
      },
    ],
  },
  {
    path: "/table",
    component: Layout,
    redirect: "/table/crud-single",
    meta: { title: "表格示例", icon: "table" },
    children: [
      {
        path: "crud-single",
        name: "CrudSingle",
        component: () => import("@/views/demo/table/crud-single.vue"),
        meta: { title: "增删改查", icon: "el-icon-Grid", keepAlive: true },
      },
      {
        path: "auto-operation-column",
        name: "AutoOperationColumn",
        component: () => import("@/views/demo/table/auto-operation-column.vue"),
        meta: {
          title: "自适应操作栏",
          icon: "el-icon-Operation",
          keepAlive: true,
        },
      },
    ],
  },
  {
    path: "/form",
    component: Layout,
    redirect: "/form/basic",
    meta: { title: "表单示例", icon: "document" },
    children: [
      {
        path: "basic",
        name: "FormBasic",
        component: () => import("@/views/demo/route/navigate/edit.vue"),
        meta: { title: "基础编辑表单", icon: "el-icon-Edit", keepAlive: true },
      },
      {
        path: "navigate",
        name: "RouteNavigate",
        component: () => import("@/views/demo/route/navigate/index.vue"),
        meta: {
          title: "路由表单交互",
          icon: "el-icon-Switch",
          keepAlive: true,
        },
      },
      {
        path: "edit/:id",
        name: "RouteExampleEdit",
        component: () => import("@/views/demo/route/navigate/edit.vue"),
        meta: {
          title: "编辑成员",
          hidden: true,
          activeMenu: "/form/navigate",
          keepAlive: true,
        },
      },
    ],
  },
  {
    path: "/component",
    component: Layout,
    redirect: "/component/icon-select",
    meta: { title: "组件示例", icon: "menu" },
    children: [
      {
        path: "icon-select",
        name: "DemoIconSelect",
        component: () => import("@/views/demo/component/icon-select.vue"),
        meta: { title: "图标选择器", icon: "el-icon-Picture", keepAlive: true },
      },
      {
        path: "icons",
        name: "Icons",
        component: () => import("@/views/demo/component/icons.vue"),
        meta: { title: "图标库", icon: "el-icon-MagicStick", keepAlive: true },
      },
      {
        path: "dictionary",
        name: "DictDemo",
        component: () => import("@/views/demo/component/dictionary.vue"),
        meta: { title: "字典组件", icon: "dict", keepAlive: true },
      },
      {
        path: "drag",
        name: "DragDemo",
        component: () => import("@/views/demo/component/drag.vue"),
        meta: { title: "拖拽示例", icon: "el-icon-Rank", keepAlive: true },
      },
      {
        path: "text-scroll",
        name: "TextScrollDemo",
        component: () => import("@/views/demo/component/text-scroll.vue"),
        meta: {
          title: "滚动文本",
          icon: "el-icon-ChatLineSquare",
          keepAlive: true,
        },
      },
    ],
  },
  {
    path: "/redirect",
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: ":path(.*)",
        component: () => import("@/views/redirect.vue"),
        meta: { hidden: true },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: "",
        component: () => import("@/views/error/404.vue"),
        meta: { title: "页面不存在", hidden: true },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ left: 0, top: 0 }),
});

router.beforeEach(async to => {
  if (to.path === "/login") return true;
  if (!getToken()) return "/login";
  try {
    const user = await loadLoginUser();
    if (to.meta.menuPath) {
      const contains = menus => (menus || []).some(menu => menu.url === to.meta.menuPath || contains(menu.subMenuList));
      if (!contains(user.menuList)) {
        ElMessage.warning("当前账号没有该菜单的访问权限");
        return "/dashboard";
      }
    }
    return true;
  } catch(error) {
    ElMessage.error(error.message || "读取登录信息失败，请确认后端已启动");
    return "/login";
  }
});
export default router;
