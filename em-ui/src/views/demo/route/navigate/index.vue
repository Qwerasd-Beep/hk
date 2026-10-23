<template>
  <div class="page-container">
    <el-card class="page-content" shadow="never">
      <template #header>成员列表</template>

      <el-input v-model="keywords" placeholder="按姓名筛选" clearable />

      <el-table class="mt-4" :data="members" border>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="dept" label="部门" />
        <el-table-column prop="post" label="岗位" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEditClick(scope.row)"
              >编辑</el-button
            >
            <el-button type="warning" link @click="handleDialog(scope.row)"
              >弹窗查看</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <el-alert
        class="mt-4"
        type="info"
        show-icon
        :closable="false"
        title="两种方式对比着看"
        description="「弹窗查看」地址栏不变、刷新即丢、链接没法分享；「编辑」会跳到带 ID 的独立地址，刷新和分享都能复现，浏览器返回键即可回到本页。"
      />
    </el-card>

    <el-dialog
      v-model="memberDialogVisible"
      :title="memberDialogTitle"
      width="420px"
    >
      <p>
        这里是弹窗内容，地址栏没有变化，刷新页面后弹窗消失，也没法把这条记录分享给别人。
      </p>
    </el-dialog>
  </div>
</template>

<script setup>
import router from "@/router";
const memberSeed = [
  { id: 1, name: "张三", dept: "研发部", post: "前端开发" },
  { id: 2, name: "李四", dept: "研发部", post: "后端开发" },
  { id: 3, name: "王五", dept: "产品部", post: "产品经理" },
  { id: 4, name: "赵六", dept: "测试部", post: "测试工程师" },
  { id: 5, name: "钱七", dept: "运维部", post: "运维工程师" },
];
function readLocalMembers() {
  try {
    return (
      JSON.parse(sessionStorage.getItem("em-ui:form-demo") || "null") ||
      memberSeed
    );
  } catch {
    return memberSeed;
  }
}
function listMembers(keywords = "") {
  return readLocalMembers().filter((row) => row.name.includes(keywords.trim()));
}
defineOptions({
  name: "RouteNavigate",
});
// 列表筛选关键字
const keywords = ref("");
const members = ref([]);
const memberDialogVisible = ref(false);
const memberDialogTitle = ref("");
/**
 * 加载成员列表
 */
function loadMembers() {
  members.value = listMembers(keywords.value);
}
// 从编辑页返回时重新读取数据源，让修改结果回显到列表
onActivated(loadMembers);
// 关键字变化后立即过滤
watch(keywords, loadMembers);
onMounted(loadMembers);
/**
 * 跳转独立编辑页，用路由参数携带要编辑的记录 ID
 */
function handleEditClick(row) {
  router.push({ name: "RouteExampleEdit", params: { id: row.id } });
}
/**
 * 打开成员详情弹窗
 */
function handleDialog(row) {
  memberDialogTitle.value = `${row.name} · ${row.dept}`;
  memberDialogVisible.value = true;
}
</script>
