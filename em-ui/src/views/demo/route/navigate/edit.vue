<template>
  <div class="page-container">
    <el-card class="page-content" shadow="never">
      <template #header>编辑成员信息</template>

      <el-form :model="formData" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="formData.dept" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="formData.post" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存并返回</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
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
function getMember(id) {
  return readLocalMembers().find((row) => row.id === id);
}
function updateMember(member) {
  const members = readLocalMembers().map((row) =>
    row.id === member.id ? { ...member } : row,
  );
  sessionStorage.setItem("em-ui:form-demo", JSON.stringify(members));
}

defineOptions({
  name: "RouteExampleEdit",
});
const route = useRoute();
const formData = reactive({ id: 0, name: "", dept: "", post: "" });
onMounted(() => {
  const member = getMember(Number(route.params.id || 1));
  if (!member) {
    ElMessage.warning("记录不存在");
    router.push({ name: "RouteNavigate" });
    return;
  }
  Object.assign(formData, member);
});
/**
 * 保存编辑并返回列表
 */
function handleSubmit() {
  updateMember({ ...formData });
  ElMessage.success("保存成功");
  router.push({ name: "RouteNavigate" });
}
/**
 * 取消编辑并返回列表
 */
function handleCancel() {
  router.push({ name: "RouteNavigate" });
}
</script>
