import { ref } from "vue";
import { doGet } from "@/utils/ajax";

// 登录身份不持久化；每次页面重新加载都向后端获取授权菜单。
export const loginUser = ref(null);
let loading;
export function loadLoginUser() {
  if (loginUser.value) return Promise.resolve(loginUser.value);
  if (!loading) loading = doGet("/login-info").then(({data}) => {
    if (data.code !== 20000 || !data.data) throw new Error(data.msg || "读取用户权限失败");
    loginUser.value = data.data;
    return data.data;
  }).finally(() => { loading = undefined; });
  return loading;
}
export function hasPermission(permission) {
  return (loginUser.value?.permissionList || []).includes(permission);
}
export function toSidebarMenus(menus) {
  return (menus || []).filter(menu => menu.type !== "button").map(menu => ({
    path: menu.url && menu.url !== "#" ? menu.url : `/menu-group/${menu.id}`,
    meta: { title: menu.menuName, icon: menu.icon ? (menu.icon.startsWith("el-icon-") ? menu.icon : `el-icon-${menu.icon}`) : "menu" },
    children: toSidebarMenus(menu.subMenuList),
  }));
}
