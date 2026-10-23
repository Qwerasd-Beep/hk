<template>
  <el-form :inline="true" :model="dataQuery">
    <el-form-item label="ownerId">
      <el-input v-model="dataQuery.ownerId" placeholder="ownerId" clearable />
    </el-form-item>

    <el-form-item label="项目代码">
      <el-input v-model="dataQuery.项目代码" placeholder="项目代码" clearable />
    </el-form-item>

    <el-form-item label="审批事项ID" prop="审批事项ID">
      <el-input v-model="dataQuery.所属行业" placeholder="所属行业" clearable />
    </el-form-item>

    <el-form-item label="办理处室" prop="办理处室">
      <el-input v-model="dataQuery.办理处室" placeholder="办理处室" clearable />
    </el-form-item>

    <el-form-item label="办理状态" prop="办理状态">
      <el-input v-model="dataQuery.办理状态" placeholder="办理状态" clearable />
    </el-form-item>

    <el-form-item label="办理意见" prop="办理意见">
      <el-input
        v-model="dataQuery.联系人名称"
        placeholder="联系人名称"
        clearable
      />
    </el-form-item>

    <el-form-item label="办理时间" prop="办理时间">
      <el-input v-model="dataQuery.办理时间" placeholder="办理时间" clearable />
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

    <el-form-item>
      <el-button type="primary" @click="onSearch">搜 索</el-button>
      <el-button type="primary" plain @click="onReset">重 置</el-button>
    </el-form-item>
  </el-form>

  <el-table
    :data="dataList"
    style="width: 100%"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column prop="id" label="id" />
    <el-table-column prop="ownerId" label="ownerId" />
    <el-table-column prop="项目代码" label="项目代码" />
    <el-table-column prop="审批事项ID" label="审批事项ID" />
    <el-table-column prop="办理处室" label="办理处室" />
    <el-table-column prop="办理状态" label="办理状态" />
    <el-table-column prop="办理意见" label="办理意见" />
    <el-table-column prop="办理时间" label="办理时间" />
    <el-table-column prop="有效标识.字典项" label="有效标识" />

    <el-table-column label="操作" width="280">
      <template #default="scope">
        <el-button
          type="primary"
          @click="showDetails(scope.row.id)"
          v-hasPerm="'banli_zhuangtai:view'"
          >详情</el-button
        >
        <el-button
          type="success"
          @click="edit(scope.row.id)"
          v-hasPerm="'banli_zhuangtai:update'"
          >编辑</el-button
        >
        <el-button
          type="danger"
          @click="del(scope.row.id)"
          v-hasPerm="'banli_zhuangtai:delete'"
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
onMounted(() => {
  getData(1);
});

let dataList = reactive([]);
let dataQuery = reactive({});
let pageSize = ref(0);
let total = ref(0);

//获取数据
function getData(pageIndex) {
  doGet("/approval-processInfo/all", {
    pageIndex: pageIndex,
    //几个搜索条件参数,后台没处理,先不传
    /*  ownerId: dataQuery.ownerId,
				               项目代码: dataQuery.项目代码,
				               审批事项ID: dataQuery.审批事项ID,
				               办理处室: dataQuery.办理处室,
				               办理状态: dataQuery.办理状态,
				               办理意见: dataQuery.办理意见,
				               办理时间: dataQuery.办理时间,
				               有效标识: dataQuery.有效标识, */
  })
    .then((resp) => {
      if (resp.data.code === 20000) {
        let pageInfo = resp.data.data;
        dataList.length = 0;
        pageInfo.list.forEach((item) => {
          dataList.push(item);
        });

        pageSize.value = pageInfo.pageSize;
        total.value = pageInfo.total;
      } else {
        errorBox(resp.data.msg + "  数据获取失败");
      }
    })
    .catch((error) => {
      errorBox("请求出错，请稍后重试");
      console.error(error);
    });

  load有效标识列表();
}

//分页函数
function toPage(pageIndex) {
  getData(pageIndex);
}

const 有效标识列表 = reactive([]);
//从数据字典中查询
function load有效标识列表() {
  //AGENCY_STATE是字典表中的类型
  doGet("/dictionary-items/get-dic-bycode/AGENCY_STATE", {}).then((resp) => {
    if (resp.data.code === 20000) {
      Object.assign(有效标识列表, resp.data.data);
    }
  });
}

function onSearch() {
  getData(1);
}
</script>
