<template>
  <el-form :inline="true" :model="dataQuery">
    <el-form-item label="中介机构名称">
      <el-input
        v-model="dataQuery.中介机构名称"
        placeholder="请输入中介机构名称"
        clearable
      />
    </el-form-item>

    <el-form-item label="中介机构证照类型">
      <el-input
        v-model="dataQuery.企业类型ID"
        placeholder="输入中介机构证照类型"
        clearable
      />
    </el-form-item>

    <el-form-item label="所属行业" prop="所属行业">
      <el-input v-model="dataQuery.所属行业" placeholder="所属行业" clearable />
    </el-form-item>

    <el-form-item label="中介机构证照号码" prop="中介机构证照号码">
      <el-input
        v-model="dataQuery.中介机构证照号码"
        placeholder="中介机构证照号码"
        clearable
      />
    </el-form-item>

    <el-form-item label="法定代表人" prop="法定代表人">
      <el-input
        v-model="dataQuery.法定代表人"
        placeholder="法定代表人"
        clearable
      />
    </el-form-item>

    <el-form-item label="联系人名称" prop="联系人名称">
      <el-input
        v-model="dataQuery.联系人名称"
        placeholder="联系人名称"
        clearable
      />
    </el-form-item>

    <el-form-item label="联系电话" prop="联系电话">
      <el-input v-model="dataQuery.企业状态" placeholder="联系电话" clearable />
    </el-form-item>

    <el-form-item label="联系人邮箱" prop="联系人邮箱">
      <el-input
        v-model="dataQuery.联系人邮箱"
        placeholder="联系人邮箱"
        clearable
      />
    </el-form-item>

    <el-form-item label="通讯地址" prop="通讯地址">
      <el-input v-model="dataQuery.通讯地址" placeholder="通讯地址" clearable />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="onSearch">搜 索</el-button>
      <el-button type="primary" plain @click="onReset">重 置</el-button>
    </el-form-item>

    <el-form-item label="有效标识">
      <el-select
        v-model="dataQuery.有效标识ID"
        placeholder="请选择有效标识"
        clearable
        style="width: 200px"
      >
        <el-option
          v-for="item in 有效标识列表"
          :key="item.id"
          :label="item.字典项"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
  </el-form>

  <el-table
    :data="dataList"
    style="width: 100%"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column prop="中介机构名称" label="中介机构名称" />
    <el-table-column prop="中介机构证照类型" label="中介机构证照类型" />
    <el-table-column prop="法定代表人" label="法定代表人" />
    <el-table-column prop="联系人名称" label="联系人名称" />
    <el-table-column prop="联系电话" label="联系电话" />
    <el-table-column prop="通讯地址" label="通讯地址" />
    <el-table-column prop="有效标识.字典项" label="有效标识" /> //注意这里

    <el-table-column label="操作" width="280">
      <template #default="scope">
        <el-button
          v-hasPerm="'zhongjie_jigou:view'"
          type="primary"
          @click="showDetails(scope.row.id)"
          >详情</el-button
        >
        <el-button
          v-hasPerm="'zhongjie_jigou:update'"
          type="success"
          @click="edit(scope.row.id)"
          >编辑</el-button
        >
        <el-button
          v-hasPerm="'zhongjie_jigou:delete'"
          type="danger"
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
</template>
<script setup>
import { doGet, doPost, doPut } from "@/utils/ajax.js";
import { confirmBox, errorBox, infoBox, msgBox, warnBox } from "@/utils/common";
import { onMounted, ref, reactive, getCurrentInstance, inject } from "vue";
import { useRouter } from "vue-router";

let dataList = reactive([]);
let dataQuery = reactive({});

let pageSize = ref(0);
let total = ref(0);

const 有效标识列表 = reactive([]);

//分页函数
function toPage(pageIndex) {
  getData(pageIndex);
}

onMounted(() => {
  getData(1);
  load有效标识列表();
});

function load有效标识列表() {
  //AGENCY_STATE是字典表中的类型
  doGet("/dictionary-items/get-dic-bycode/AGENCY_STATE").then((resp) => {
    if (resp.data.code === 20000) {
      console.log(resp.data.data);
      Object.assign(有效标识列表, resp.data.data);
    }
  });
}

function getData(pageIndex) {
  doGet("/agencies/all", {
    pageIndex: pageIndex, //当前查询第几页
    //几个搜索条件参数
    中介机构名称: dataQuery.中介机构名称,
    中介机构证照类型: dataQuery.中介机构证照类型,
    中介机构证照号码: dataQuery.中介机构证照号码,
    法定代表人: dataQuery.法定代表人,
    联系人名称: dataQuery.联系人名称,
    联系电话: dataQuery.联系电话,
    联系人邮箱: dataQuery.联系人邮箱,
    通讯地址: dataQuery.通讯地址,
    有效标识ID: dataQuery.有效标识ID,
  }).then((resp) => {
    if (resp.data.code === 20000) {
      let pageInfo = resp.data.data;
      dataList.length = 0;
      pageInfo.list.forEach((item) => {
        dataList.push(item);
      });

      pageSize.value = pageInfo.pageSize;
      total.value = pageInfo.total;
    }
  });
}

function onSearch() {
  getData(1);
}
</script>
