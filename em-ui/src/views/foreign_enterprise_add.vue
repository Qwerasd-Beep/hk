<template>
  <el-form
    :model="newEnterprise"
    ref="formRef"
    label-width="120px"
    :rules="rules"
  >
    <el-form-item label="企业名称" prop="企业名称">
      <el-input
        v-model="newEnterprise.企业名称"
        placeholder="请输入企业名称"
        clearable
      />
    </el-form-item>

    <el-form-item label="企业类型" prop="enterpriseType.id">
      <el-select
        v-model="newEnterprise.enterpriseType.id"
        placeholder="请选择企业类型"
        clearable
        style="width: 200px"
      >
        <el-option
          v-for="item in enterpriseTypes"
          :label="item.类型名称"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="录入时间" prop="录入时间">
      <el-date-picker
        v-model="newEnterprise.录入时间"
        type="datetime"
        placeholder="请选择录入时间"
        value-format="YYYY-MM-DD HH:mm:ss"
      />
    </el-form-item>

    <el-form-item label="所属行业" prop="所属行业">
      <el-input
        v-model="newEnterprise.所属行业"
        placeholder="所属行业"
        clearable
      />
    </el-form-item>

    <el-form-item label="企业状态" prop="企业状态">
      <el-input
        v-model="newEnterprise.企业状态"
        placeholder="企业状态"
        clearable
      />
    </el-form-item>

    <el-form-item label="数据来源" prop="数据来源">
      <el-input
        v-model="newEnterprise.数据来源"
        placeholder="数据来源"
        clearable
      />
    </el-form-item>

    <el-form-item label="企业代码" prop="企业代码">
      <el-input
        v-model="newEnterprise.企业代码"
        placeholder="企业代码"
        clearable
      />
    </el-form-item>

    <el-form-item label="行业门类" prop="行业门类">
      <el-input
        v-model="newEnterprise.行业门类"
        placeholder="行业门类"
        clearable
      />
    </el-form-item>

    <el-form-item label="资金来源地" prop="资金来源地">
      <el-input
        v-model="newEnterprise.资金来源地"
        placeholder="资金来源地"
        clearable
      />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="submitForm">提交</el-button>
      <el-button @click="cancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>
<script setup>
import { reactive, ref, onMounted } from "vue";
import { doGet, doPost } from "@/utils/ajax";
import { errorBox, msgBox } from "@/utils/common";
const newEnterprise = reactive({
  企业名称: "",
  enterpriseType: {
    id: "",
    类型名称: "",
  },
  录入时间: "",
  所属行业: "",
  企业状态: "",
  数据来源: "",
  企业代码: "",
  行业门类: "",
  资金来源地: "",
});
let enterpriseTypes = reactive([]);
function loadEnterpriseType() {
  doGet("/foreign-enterprise/enterprise-type").then((resp) => {
    if (resp.data.code == 20000) {
      enterpriseTypes.push(...resp.data.data);
    }
  });
}
onMounted(() => {
  loadEnterpriseType();
  loadEnterprise();
});
const rules = reactive({
  企业名称: [{ required: true, message: "请输入企业名称", trigger: "blur" }],
  //注意这里
  "enterpriseType.id": [
    { required: true, message: "请选择企业类型", trigger: "change" },
  ],
  录入时间: [{ required: true, message: "请选择录入时间", trigger: "change" }],
  所属行业: [{ required: true, message: "请输入所属行业", trigger: "blur" }],
  企业状态: [{ required: true, message: "请输入企业状态", trigger: "blur" }],
  数据来源: [{ required: true, message: "请输入数据来源", trigger: "blur" }],
  企业代码: [{ required: true, message: "请输入企业代码", trigger: "blur" }],
  行业门类: [{ required: true, message: "请输入行业门类", trigger: "blur" }],
  资金来源地: [
    { required: true, message: "请输入资金来源地", trigger: "blur" },
  ],
});
import { useRoute, useRouter } from "vue-router";
const formRef = ref(null);
const route = useRoute();
const router = useRouter();

function cancel() {
  router.push("/main/foreign-enterprise");
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      if (newEnterprise.id > 0) {
        doPost("/foreign-enterprise/update", newEnterprise).then((resp) => {
          if (resp.data.code == 20000) {
            msgBox("修改成功");
            router.push("/main/foreign-enterprise");
          }
        });
      } else {
        doPost("/foreign-enterprise/add", newEnterprise).then((resp) => {
          if (resp.data.code == 20000) {
            msgBox("添加成功");
            router.push("/main/foreign-enterprise");
          }
        });
      }
    } else {
      errorBox("校验失败");
    }
  });
}
function loadEnterprise() {
  const id = route.params.id;
  if (!id) return;

  doGet("/foreign-enterprise/" + id)
    .then((resp) => {
      if (resp.data.code == 20000 && resp.data.data) {
        const loadedEnterprise = resp.data.data;
        Object.assign(newEnterprise, loadedEnterprise);
        newEnterprise.enterpriseType = loadedEnterprise.enterpriseType ?? {
          id: "",
          类型名称: "",
        };
      } else {
        errorBox(resp.data.msg || "加载企业信息失败");
      }
    })
    .catch(() => errorBox("加载企业信息失败"));
}
</script>
