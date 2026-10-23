<template>
  <el-form :model="user" label-width="120px">
    <el-form-item label="ID">
      <div class="detail">&nbsp;{{ user.id }}</div>
    </el-form-item>
    <el-form-item label="账号">
      <div class="detail">&nbsp;{{ user.name }}</div>
    </el-form-item>

    <el-form-item label="备注">
      <div class="detail">&nbsp;{{ user.note }}</div>
    </el-form-item>

    <el-form-item label="修改时间">
      <div class="detail">&nbsp;{{ user.editTime }}</div>
    </el-form-item>
    <el-form-item>
      <el-button type="success" @click="goBack()">返回</el-button>
    </el-form-item>
  </el-form>
</template>
<script setup>
import { doGet } from "@/utils/ajax";
import { onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
let route = useRoute();
let router = useRouter();
let user = reactive({});
onMounted(() => {
  let id = route.params.id;
  doGet("/user/" + id).then((resp) => {
    if (resp.data.code == 20000) {
      Object.assign(user, resp.data.data);
    }
  });
});
function goBack() {
  router.back();
}
</script>
