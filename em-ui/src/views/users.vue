<template>
  <el-button type="primary" @click="addUser()">添加用户</el-button>
  <el-button type="danger" v-hasPerm="'user:delete'" @click="batchDelete">批量删除</el-button>

  <el-table
    :data="userList"
    style="width: 100%"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column type="index" label="序号" />

    <el-table-column prop="id" label="ID" />
    <el-table-column property="name" label="账号" />
    <el-table-column property="editTime" label="更新时间" />
    <el-table-column property="note" label="备注" show-overflow-tooltip />
    <el-table-column label="操作" width="280">
      <template #default="scope">
        <el-button
          type="primary"
          v-hasPerm="'user:view'"
          @click="showDetails(scope.row.id)"
          >详情</el-button
        >
        <el-button
          type="success"
          v-hasPerm="'user:update'"
          @click="edit(scope.row.id)"
          >编辑</el-button
        >
        <el-button
          type="danger"
          v-hasPerm="'user:delete'"
          @click="del(scope.row.id)"
          >删除</el-button
        >
      </template>
    </el-table-column>
  </el-table>
  <el-pagination
    background
    layout="prev, pager, next"
    :page-size="pageSize"
    :total="total"
    @prev-click="toPage"
    @next-click="toPage"
    @current-change="toPage"
  >
  </el-pagination>
  <el-dialog
    v-model="dlgVisible"
    :title="user.id > 0 ? '修改用户' : '添加用户'"
    width="55%"
    center
    draggable
  >
    <el-form ref="form1" :model="user" label-width="110px">
      <el-form-item label="账号" prop="name">
        <el-input v-model="user.name" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="user.password" />
      </el-form-item>

      <el-form-item label="备注" prop="note">
        <el-input v-model="user.note" type="textarea" />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dlgVisible = false">关 闭</el-button>
        <el-button type="primary" @click="subForm">提 交</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script setup>
import { doGet, doPost, doDel } from "@/utils/ajax";
import { errorBox, msgBox, confirmBox, warnBox } from "@/utils/common";
import { onMounted, reactive, ref, inject } from "vue";
const reload = inject("reload");
import { useRouter } from "vue-router";
let userList = reactive([]);
let pageSize = ref(0);
let total = ref(0);
let router = useRouter();
let dlgVisible = ref(false);
const createEmptyUser = () => ({
  id: 0,
  name: "",
  password: "",
  note: "",
});
let userIdList = ref([]);
function handleSelectionChange(checkedUserList) {
  userIdList.value = checkedUserList.map((item) => item.id);
}
let user = reactive(createEmptyUser());
function del(id) {
  confirmBox("确定要删除吗")
    .then(() => {
      doDel("/user-delete/" + id).then((resp) => {
        if (resp.data.code == 20000) {
          msgBox("删除成功");
          window.location.reload();
        } else {
          errorBox("删除失败");
        }
      });
    })
    .catch(() => {
      warnBox("取消了删除");
    });
}
function resetUser() {
  Object.keys(user).forEach((key) => delete user[key]);
  Object.assign(user, createEmptyUser());
}
function edit(id) {
  resetUser();
  dlgVisible.value = true;
  doGet("/user/" + id).then((resp) => {
    if (resp.data.code == 20000) {
      Object.assign(user, resp.data.data);
    }
  });
}
function addUser() {
  resetUser();
  dlgVisible.value = true;
}
function subForm() {
  if (user.id > 0) {
    doPost("/user-update", user).then((resp) => {
      if (resp.data.code == 20000) {
        msgBox("修改成功");
        dlgVisible.value = false;
        getData();
      } else {
        errorBox("修改失败");
      }
    });
  } else {
    doPost("/user-add", user).then((resp) => {
      if (resp.data.code == 20000) {
        msgBox("添加成功");
        dlgVisible.value = false;
        getData();
      } else {
        errorBox("添加失败");
      }
    });
  }
}
function showDetails(id) {
  router.push({ path: "/main/user-details/" + id });
}
function toPage(pageIndex) {
  getData(pageIndex);
}
onMounted(() => {
  getData();
});
function getData(pageIndex) {
  doGet("/users", { pageIndex: pageIndex }).then((res) => {
    if (res.data.code == 20000) {
      let pageInfo = res.data.data;
      pageSize.value = pageInfo.pageSize;
      total.value = pageInfo.total;
      userList.length = 0;
      pageInfo.list.forEach((user) => {
        userList.push(user);
      });
    }
  });
}
function batchDelete() {
  if (userIdList.value.length == 0) {
    warnBox("请先选择要删除的用户");
  } else {
    confirmBox("确定要删除选中的" + userIdList.value.length + "个用户吗").then(
      () => {
        doDel("/user-batch-delete", { ids: userIdList.value }).then((resp) => {
          if (resp.data.code == 20000) {
            msgBox("删除成功");
            userIdList.value = [];
            getData();
          } else {
            errorBox("删除失败");
          }
        });
      },
    );
  }
}
</script>
