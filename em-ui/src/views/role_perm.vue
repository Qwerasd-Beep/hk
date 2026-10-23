<template>
  <el-table :data="roleList" style="width: 100%">
    <el-table-column type="index" label="序号" />
    <el-table-column prop="id" label="ID" />
    <el-table-column prop="roleCode" label="角色CODE" />
    <el-table-column prop="des" label="说明" />
    <el-table-column label="操作" width="280">
      <template #default="scope">
        <el-button type="success" @click="edit(scope.row.id)">编辑</el-button>
        <el-button type="success" @click="showRoleMenuDlg(scope.row)"
          >授权</el-button
        >
      </template>
    </el-table-column>
  </el-table>
  <el-dialog v-model="dlgVisible" title="角色授权" width="55%" center draggable>
    <el-form ref="form1" :model="user" label-width="110px">
      <el-form-item label="角色名" prop="roleName">
        <el-input v-model="role.roleName" />
      </el-form-item>

      <el-form-item label="角色码" prop="roleCode">
        <el-input v-model="role.roleCode" />
      </el-form-item>

      <el-form-item label="备注" prop="des">
        <el-input v-model="role.des" type="textarea" />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="reload()">关 闭</el-button>
        <el-button type="primary" @click="subForm">提 交</el-button>
      </span>
    </template>

    <el-tree
      :data="menuData"
      show-checkbox
      ref="treeRef"
      node-key="id"
      :default-checked-keys="roleMenuIdList"
      :props="customProps"
      :check-strictly="isCheckStrictly"
    />
  </el-dialog>
</template>

<script setup>
import { doGet, doPost, doPut } from "@/utils/ajax.js";
import { confirmBox, errorBox, infoBox, msgBox, warnBox } from "@/utils/common";
import {
  inject,
  onMounted,
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
} from "vue";
import { useRoute, useRouter } from "vue-router";
let dlgVisible = ref(false);
let role = reactive({});
const treeRef = ref(null);
function subForm() {
  // 获取选中节点的 ID 列表(它只能取出选中的子级)
  const checkedKeys_sub = treeRef.value.getCheckedKeys();

  //取出所有选中节点的父级
  const checkedKeys_parent = treeRef.value.getHalfCheckedKeys();

  //把两伙合在一起
  const checkedMenuIds = [...checkedKeys_sub, ...checkedKeys_parent];

  const data = {
    ...role,
    checkedMenuIds: checkedMenuIds,
  };

  // 发送 POST 请求到后端
  doPost("/perm/update-rolemenu", data).then((resp) => {
    if (resp.data.code === 20000) {
      infoBox("保存成功");
      reload();
    }
  });
}

const reload = inject("reload");
const customProps = {
  children: "subMenuList",
  label: "menuName",
};

const menuData = reactive([]);
const roleMenuIdList = ref([]);
const isCheckStrictly = ref(true);
function showRoleMenuDlg(row) {
  dlgVisible.value = true;
  role.id = row.id;
  role.roleName = row.roleName;
  role.des = row.des;
  role.roleCode = row.roleCode;

  doGet("/perm/all-menu_role-menus", { roleId: row.id }).then((resp) => {
    if (resp.data.code === 20000) {
      const map = resp.data.data;

      //响应的数据包含两部分,所有的菜单信息 和当前角色拥用的菜单id列表
      Object.assign(menuData, map.menuList);
      roleMenuIdList.value = map.roleMenuIdList;
      nextTick(() => {
        // 数据绑定完成后，解除 check-strictly 属性
        isCheckStrictly.value = false;
      });
    }
  });
}

//点击了修改用户后，打开窗口
function edit(id) {
  alert(id);
}

let roleList = reactive([]);
const router = useRouter();

function getRoleListData() {
  doGet("/perm/role-list")
    .then((resp) => {
      if (resp.data.code === 20000) {
        Object.assign(roleList, resp.data.data);
      } else {
        errorBox(resp.data.message + " 角色列表数据获取失败");
      }
    })
    .catch((error) => {
      errorBox("请求出错，请稍后重试");
      console.error(error);
    });
}

onMounted(() => {
  getRoleListData();
});
</script>
