// 保留原业务页调用方式，使用原生请求，不拦截或模拟接口。
const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export const getToken = () => sessionStorage.getItem("zhaoshang_token") || localStorage.getItem("zhaoshang_token");
let loginRedirecting = false;
export async function request(method, path, params) {
  const url = new URL(path, baseURL);
  const headers = {};
  const token = getToken();
  if (token) headers.hk_token = token;
  if (!sessionStorage.getItem("zhaoshang_token") && localStorage.getItem("zhaoshang_token")) headers.rememberMe = "true";
  const options = { method, headers };
  if (method === "GET" || method === "DELETE") {
    Object.entries(params || {}).forEach(([key, value]) => {
      if (value == null) return;
      (Array.isArray(value) ? value : [value]).forEach(item => url.searchParams.append(key, item));
    });
  } else if (params instanceof FormData) {
    options.body = params;
  } else if (params !== undefined) {
    headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(params);
  }
  const response = await fetch(url, options);
  const data = await response.json().catch(() => {
    throw new Error(`接口响应不是有效 JSON（${response.status}）：${path}`);
  });
  if (response.status === 401 || data.code > 90000) {
    sessionStorage.removeItem("zhaoshang_token");
    localStorage.removeItem("zhaoshang_token");
    if (!loginRedirecting && location.pathname !== "/login") {
      loginRedirecting = true;
      location.replace("/login");
    }
    throw new Error(data.msg || "登录已失效，请重新登录");
  }
  if (!response.ok) throw new Error(data.msg || `请求失败：${response.status}`);
  return { data, status: response.status, headers: response.headers };
}
export const doGet = (url, params) => request("GET", url, params);
export const doPost = (url, data) => request("POST", url, data);
export const doPut = (url, data) => request("PUT", url, data);
export const doDel = (url, params) => request("DELETE", url, params);
