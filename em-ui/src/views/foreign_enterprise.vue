<template>
  <el-form :inline="true" :model="enterprise">
    <el-form-item label="企业名称">
      <el-input
        v-model="enterprise.企业名称"
        placeholder="请输入企业名称"
        clearable
      />
    </el-form-item>

    <el-form-item label="录入时间">
      <el-date-picker
        v-model="enterprise.录入时间"
        type="datetimerange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
      />
    </el-form-item>

    <el-form-item label="所属行业" prop="所属行业">
      <el-input
        v-model="enterprise.所属行业"
        placeholder="所属行业"
        clearable
      />
    </el-form-item>

    <el-form-item label="企业状态" prop="企业状态">
      <el-input
        v-model="enterprise.企业状态"
        placeholder="企业状态"
        clearable
      />
    </el-form-item>

    <el-form-item label="企业类型">
      <el-select
        v-model="enterprise.企业类型ID"
        placeholder="请选择企业类型"
        clearable
        style="width: 200px"
      >
        <el-option
          v-for="item in enterpriseTypes"
          :key="item.id"
          :label="item.类型名称"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="onSearch">搜 索</el-button>
      <el-button type="primary" plain @click="onReset">重 置</el-button>
    </el-form-item>
  </el-form>
  <el-button type="primary" @click="add">添加企业</el-button>
  <el-button type="danger">批量删除</el-button>
  <el-table :data="dataList" style="width: 100%">
    <el-table-column type="selection" width="55" />
    <el-table-column prop="录入时间" label="录入时间" />
    <el-table-column prop="数据来源" label="数据来源" />
    <el-table-column prop="企业状态" label="企业状态" />
    <el-table-column prop="企业代码" label="企业代码" />
    <el-table-column prop="企业名称" label="企业名称" />
    <el-table-column prop="enterpriseType.id" label="企业类型ID" />
    <el-table-column prop="enterpriseType.类型名称" label="企业类型名称" />
    <el-table-column prop="行业门类" label="行业门类" />
    <el-table-column prop="所属行业" label="所属行业" />
    <el-table-column prop="资金来源地" label="资金来源地" />

    <el-table-column label="操作" width="280">
      <template #default="scope">
        <el-button type="primary" @click="showDetails(scope.row.id)"
          >详情</el-button
        >
        <el-button type="success" @click="edit(scope.row.id)">编辑</el-button>
        <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
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
</template>
<script setup>
import { doGet, doPost, doDel } from "@/utils/ajax";
import { errorBox, msgBox, confirmBox, warnBox, infoBox } from "@/utils/common";
import { onMounted, reactive, ref, getCurrentInstance, inject } from "vue";
let enterprise = reactive({});
let dataList = reactive([]);
let pageSize = ref(0);
let total = ref(0);

function toPage(pageIndex) {
  getData(pageIndex);
}
onMounted(() => {
  getData(1);
  loadEnterpriseTypes();
});

let enterpriseTypes = reactive([]);
function loadEnterpriseTypes() {
  doGet("/foreign-enterprise/enterprise-type").then((resp) => {
    if (resp.data.code == 20000) {
      Object.assign(enterpriseTypes, resp.data.data);
    }
  });
}
function getData(pageIndex) {
  let startTime = "";
  let endTime = "";
  if (Array.isArray(enterprise.录入时间) && enterprise.录入时间.length == 2) {
    startTime = enterprise.录入时间[0];
    endTime = enterprise.录入时间[1];
  }
  doGet("/foreign-enterprise/all", {
    pageIndex: pageIndex,
    企业名称: enterprise.企业名称,
    所属行业: enterprise.所属行业,
    企业状态: enterprise.企业状态,
    startTime: startTime,
    endTime: endTime,
    企业类型ID: enterprise.企业类型ID,
  }).then((resp) => {
    if (resp.data.code == 20000) {
      let pageInfo = resp.data.data;
      dataList.length = 0;
      pageInfo.list.forEach((e) => {
        dataList.push(e);
      });
      pageSize.value = pageInfo.pageSize;
      total.value = pageInfo.total;
    }
  });
}
function onSearch() {
  getData(1);
}
function onReset() {
  enterprise.企业名称 = "";
  enterprise.所属行业 = "";
  enterprise.企业状态 = "";
  enterprise.录入时间 = null;
}
import { useRouter } from "vue-router";
const router = useRouter();
function add() {
  router.push("/main/foreign-enterprise/add");
}
function edit(id) {
  router.push("/main/foreign-enterprise/edit/" + id);
}
function del(id) {
  confirmBox("确定要删除吗").then(() => {
    doDel("/foreign-enterprise/delete/" + id).then((resp) => {
      if (resp.data.code == 20000) {
        msgBox("删除成功");
        getData(1);
      }
    });
  });
}
</script>
