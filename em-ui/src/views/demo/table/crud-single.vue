<template>
  <div class="page-container h-full flex flex-1 flex-col">
    <div class="mb-10">
      <el-button
        tag="a"
        href="https://gitee.com/youlaiorg/vue3-element-admin/blob/master/src/views/demo/table/crud-single.vue"
        target="_blank"
        link
        type="info"
        size="small"
      >
        <el-icon class="mr-1"><Link /></el-icon>
        示例源码
      </el-button>
    </div>

    <!-- 搜索 -->
    <crud-search
      ref="searchRef"
      :search-config="searchConfig"
      @query-click="handleQueryClick"
      @reset-click="handleResetClick"
    />

    <!-- 列表 -->
    <crud-table
      ref="contentRef"
      :content-config="contentConfig"
      @add-click="handleAddClick"
      @export-click="handleExportClick"
      @search-click="handleSearchClick"
      @toolbar-click="handleToolbarClick"
      @operate-click="handleOperateClick"
      @filter-change="handleFilterChange"
    >
      <template #status="scope">
        <el-tag :type="scope.row[scope.prop] === 1 ? 'success' : 'info'">
          {{ scope.row[scope.prop] === 1 ? "启用" : "禁用" }}
        </el-tag>
      </template>
      <template #gender="scope">
        <DictTag
          v-model="scope.row[scope.prop]"
          code="gender"
          :options="genderOptions"
        />
      </template>
      <template #mobile="scope">
        <el-text>{{ scope.row[scope.prop] }}</el-text>
        <copy-button
          v-if="scope.row[scope.prop]"
          :text="scope.row[scope.prop]"
          :style="{ marginLeft: '2px' }"
        />
      </template>
    </crud-table>

    <!-- 新增 -->
    <crud-modal
      ref="addModalRef"
      :modal-config="addModalConfig"
      @submit-click="handleSubmitClick"
    >
      <template #gender="scope">
        <DictSelect
          v-model="scope.formData[scope.prop]"
          code="gender"
          :options="genderOptions"
          v-bind="scope.attrs"
        />
      </template>
    </crud-modal>

    <!-- 编辑 -->
    <crud-modal
      ref="editModalRef"
      :modal-config="editModalConfig"
      @submit-click="handleSubmitClick"
    >
      <template #gender="scope">
        <DictSelect
          v-model="scope.formData[scope.prop]"
          code="gender"
          :options="genderOptions"
          v-bind="scope.attrs"
          :disabled="editModalConfig.form?.disabled"
        />
      </template>
    </crud-modal>
  </div>
</template>

<script setup>
import localAvatar from "@/assets/images/logo.png";
import { DeviceEnum } from "@/enums/settings";
import { useAppStore } from "@/stores";
import useCrudPage from "@/components/Crud/useCrudPage";

const genderOptions = [
  { label: "男", value: 1, tagType: "primary" },
  { label: "女", value: 2, tagType: "success" },
];
const localDepartments = [
  {
    label: "有来科技",
    value: 1,
    children: [
      { label: "研发部", value: 2 },
      { label: "产品部", value: 3 },
      { label: "运营部", value: 4 },
    ],
  },
];
const localRoles = [
  { label: "管理员", value: 1 },
  { label: "普通用户", value: 2 },
  { label: "访客", value: 3 },
];
const localUsers = ref(
  [
    {
      id: 1,
      username: "admin",
      nickname: "管理员",
      gender: 1,
      deptId: 2,
      roleIds: [1],
      mobile: "13800000001",
      status: 1,
    },
    {
      id: 2,
      username: "zhangsan",
      nickname: "张三",
      gender: 1,
      deptId: 2,
      roleIds: [2],
      mobile: "13800000002",
      status: 1,
    },
    {
      id: 3,
      username: "lisi",
      nickname: "李四",
      gender: 2,
      deptId: 3,
      roleIds: [2],
      mobile: "13800000003",
      status: 1,
    },
    {
      id: 4,
      username: "wangwu",
      nickname: "王五",
      gender: 1,
      deptId: 4,
      roleIds: [3],
      mobile: "13800000004",
      status: 0,
    },
    {
      id: 5,
      username: "zhaoliu",
      nickname: "赵六",
      gender: 2,
      deptId: 2,
      roleIds: [2],
      mobile: "13800000005",
      status: 1,
    },
    {
      id: 6,
      username: "qianqi",
      nickname: "钱七",
      gender: 1,
      deptId: 3,
      roleIds: [2],
      mobile: "13800000006",
      status: 1,
    },
    {
      id: 7,
      username: "sunba",
      nickname: "孙八",
      gender: 2,
      deptId: 4,
      roleIds: [3],
      mobile: "13800000007",
      status: 0,
    },
    {
      id: 8,
      username: "zhoujiu",
      nickname: "周九",
      gender: 1,
      deptId: 2,
      roleIds: [2],
      mobile: "13800000008",
      status: 1,
    },
    {
      id: 9,
      username: "wushi",
      nickname: "吴十",
      gender: 2,
      deptId: 3,
      roleIds: [2],
      mobile: "13800000009",
      status: 1,
    },
    {
      id: 10,
      username: "linyi",
      nickname: "林一",
      gender: 1,
      deptId: 4,
      roleIds: [2],
      mobile: "13800000010",
      status: 1,
    },
    {
      id: 11,
      username: "chenxi",
      nickname: "陈曦",
      gender: 2,
      deptId: 2,
      roleIds: [1],
      mobile: "13800000011",
      status: 1,
    },
    {
      id: 12,
      username: "xiaoyu",
      nickname: "小雨",
      gender: 2,
      deptId: 3,
      roleIds: [3],
      mobile: "13800000012",
      status: 0,
    },
  ].map((row) => ({
    ...row,
    avatar: localAvatar,
    email: row.username + "@example.com",
    createTime: "2026-10-22 09:00:00",
  })),
);
const copyLocal = (value) => JSON.parse(JSON.stringify(value));
function filterLocalUsers(params = {}) {
  const keyword = String(params.keywords || "")
    .trim()
    .toLowerCase();
  return localUsers.value
    .filter(
      (row) =>
        (!keyword ||
          [row.username, row.nickname, row.mobile].some((value) =>
            value.toLowerCase().includes(keyword),
          )) &&
        (params.status === "" ||
          params.status == null ||
          String(row.status) === String(params.status)) &&
        (!params.deptId ||
          Number(params.deptId) === 1 ||
          row.deptId === Number(params.deptId)) &&
        (!Array.isArray(params.createTime) ||
          params.createTime.length < 2 ||
          (row.createTime.slice(0, 10) >= params.createTime[0] &&
            row.createTime.slice(0, 10) <= params.createTime[1])) &&
        (!params.roleIds ||
          String(params.roleIds)
            .split(",")
            .some((id) => row.roleIds.includes(Number(id)))),
    )
    .map((row) => ({
      ...copyLocal(row),
      deptName:
        localDepartments[0].children.find(
          (item) => item.value === Number(row.deptId),
        )?.label || "有来科技",
      roleNames: localRoles
        .filter((item) => row.roleIds.includes(item.value))
        .map((item) => item.label)
        .join("、"),
    }));
}
function getLocalPage(params = {}) {
  const filtered = filterLocalUsers(params);
  const start =
    (Number(params.pageNum || 1) - 1) * Number(params.pageSize || 10);
  return Promise.resolve({
    total: filtered.length,
    list: filtered.slice(start, start + Number(params.pageSize || 10)),
  });
}
function getLocalForm(id) {
  return Promise.resolve(
    copyLocal(localUsers.value.find((row) => row.id === Number(id))),
  );
}
function addLocalUser(data) {
  const nextId = Math.max(0, ...localUsers.value.map((row) => row.id)) + 1;
  localUsers.value.unshift({
    ...copyLocal(data),
    id: nextId,
    avatar: localAvatar,
    createTime: "2026-10-22 09:00:00",
  });
  return Promise.resolve();
}
function updateLocalUser(id, data) {
  const row = localUsers.value.find((item) => item.id === Number(id));
  if (row) Object.assign(row, copyLocal(data));
  return Promise.resolve();
}
function removeLocalUsers(ids) {
  const selected = String(ids).split(",").map(Number);
  localUsers.value = localUsers.value.filter(
    (row) => !selected.includes(row.id),
  );
  return Promise.resolve();
}
async function exportLocalWorkbook(rows, filename) {
  const excel = await import("exceljs");
  const workbook = new (excel.Workbook || excel.default.Workbook)();
  const worksheet = workbook.addWorksheet("演示数据");
  worksheet.columns = [
    { header: "用户名", key: "username" },
    { header: "用户昵称", key: "nickname" },
    { header: "手机号码", key: "mobile" },
    { header: "邮箱", key: "email" },
    { header: "性别", key: "gender" },
    { header: "部门编号", key: "deptId" },
    { header: "状态", key: "status" },
  ];
  worksheet.addRows(rows);
  return { buffer: await workbook.xlsx.writeBuffer(), fileName: filename };
}
const exportLocalUsers = (params) =>
  exportLocalWorkbook(filterLocalUsers(params), "演示用户.xlsx");
const downloadLocalTemplate = () => exportLocalWorkbook([], "导入模板.xlsx");
function importLocalRows(rows) {
  for (const row of rows)
    addLocalUser({
      username: row.username || row["用户名"] || "localuser",
      nickname: row.nickname || row["用户昵称"] || "导入用户",
      mobile: String(row.mobile || row["手机号码"] || ""),
      email: row.email || row["邮箱"] || "",
      gender: Number(row.gender || row["性别"] || 1),
      deptId: Number(row.deptId || row["部门编号"] || 2),
      status: Number(row.status ?? row["状态"] ?? 1),
      roleIds: [2],
    });
  return Promise.resolve();
}
async function importLocalFile(file) {
  const excel = await import("exceljs");
  const workbook = new (excel.Workbook || excel.default.Workbook)();
  await workbook.xlsx.load(await file.arrayBuffer());
  const worksheet = workbook.worksheets[0];
  if (!worksheet) return;
  const headers = worksheet.getRow(1).values.slice(1);
  const rows = [];
  worksheet.eachRow((row, index) => {
    if (index > 1)
      rows.push(
        Object.fromEntries(
          headers.map((key, cell) => [key, row.getCell(cell + 1).value]),
        ),
      );
  });
  return importLocalRows(rows);
}
function resetLocalPassword(id, value) {
  return Promise.resolve();
}

defineOptions({
  name: "CrudSingle",
  inheritAttrs: false,
});
// 选项数据
const deptArr = ref([]);
const roleArr = ref([]);
const stateArr = ref([
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 },
]);
/**
 * 加载表单用到的下拉选项
 */
const initOptions = async () => {
  const [dept, roles] = await Promise.all([
    Promise.resolve(localDepartments),
    Promise.resolve(localRoles),
  ]);
  deptArr.value = dept;
  roleArr.value = roles;
};
// 搜索配置
const searchConfig = reactive({
  formItems: [
    {
      tips: "支持模糊搜索",
      type: "input",
      label: "关键字",
      prop: "keywords",
      attrs: {
        placeholder: "用户名/昵称/手机",
        clearable: true,
        style: { width: "200px" },
      },
    },
    {
      type: "tree-select",
      label: "部门",
      prop: "deptId",
      attrs: {
        placeholder: "请选择",
        data: deptArr,
        filterable: true,
        "check-strictly": true,
        "render-after-expand": false,
        clearable: true,
        style: { width: "200px" },
      },
    },
    {
      type: "select",
      label: "状态",
      prop: "status",
      attrs: {
        placeholder: "全部",
        clearable: true,
        style: { width: "200px" },
      },
      options: stateArr,
    },
    {
      type: "date-picker",
      label: "创建时间",
      prop: "createTime",
      attrs: {
        type: "daterange",
        "range-separator": "~",
        "start-placeholder": "开始时间",
        "end-placeholder": "截止时间",
        "value-format": "YYYY-MM-DD",
        style: { width: "200px" },
      },
    },
  ],
});
// 内容配置
const contentConfig = reactive({
  table: {
    border: true,
    highlightCurrentRow: true,
  },
  pagination: {
    background: true,
    layout: "prev,pager,next,jumper,total,sizes",
    pageSize: 10,
    pageSizes: [10, 20, 30, 50],
  },
  /**
   * 列表数据查询
   */
  indexAction(params) {
    return getLocalPage(params);
  },
  deleteAction: removeLocalUsers,
  /**
   * 导入文件处理
   */
  importAction(file) {
    return importLocalFile(file);
  },
  exportAction: exportLocalUsers,
  importTemplate: downloadLocalTemplate,
  /**
   * 导入完成后的处理（示例为空实现）
   */
  importsAction(rows) {
    return importLocalRows(rows);
  },
  /**
   * 导出数据（示例取全量列表）
   */
  exportsAction(params) {
    return Promise.resolve(filterLocalUsers(params));
  },
  pk: "id",
  toolbar: [
    "add",
    "delete",
    "import",
    "export",
    {
      name: "custom1",
      text: "自定义",
      attrs: { icon: "plus", color: "#626AEF" },
    },
  ],
  defaultToolbar: ["refresh", "filter", "imports", "exports", "search"],
  cols: [
    { type: "selection", width: 50, align: "center" },
    { label: "编号", align: "center", prop: "id", width: 100, show: false },
    { label: "用户名", align: "center", prop: "username" },
    { label: "头像", align: "center", prop: "avatar", templet: "image" },
    { label: "用户昵称", align: "center", prop: "nickname", width: 120 },
    {
      label: "性别",
      align: "center",
      prop: "gender",
      width: 100,
      templet: "custom",
      slotName: "gender",
    },
    { label: "部门", align: "center", prop: "deptName", width: 120 },
    {
      label: "角色",
      align: "center",
      prop: "roleNames",
      width: 120,
      columnKey: "roleIds",
      filters: [],
      filterMultiple: true,
      filterJoin: ",",
      async initFn(colItem) {
        const roleOptions = await Promise.resolve(localRoles);
        colItem.filters = roleOptions.map((item) => {
          return { text: item.label, value: item.value };
        });
      },
    },
    {
      label: "手机号码",
      align: "center",
      prop: "mobile",
      templet: "custom",
      slotName: "mobile",
      width: 150,
    },
    {
      label: "状态",
      align: "center",
      prop: "status",
      templet: "custom",
      slotName: "status",
    },
    { label: "创建时间", align: "center", prop: "createTime", width: 180 },
    {
      label: "操作",
      align: "center",
      fixed: "right",
      width: 280,
      templet: "tool",
      operat: [
        {
          name: "detail",
          text: "详情",
          attrs: { icon: "Document", type: "primary" },
        },
        {
          name: "reset_pwd",
          text: "重置密码",
          attrs: {
            icon: "refresh-left",
            style: {
              "--el-button-text-color": "#626AEF",
              "--el-button-hover-link-text-color": "#9197f4",
            },
          },
        },
        "edit",
        "delete",
      ],
    },
  ],
});
// 新增配置
const addModalConfig = reactive({
  dialog: {
    title: "新增用户",
    width: 800,
    draggable: true,
  },
  form: {
    labelWidth: 100,
  },
  formAction: addLocalUser,
  formItems: [
    {
      label: "用户名",
      prop: "username",
      rules: [{ required: true, message: "用户名不能为空", trigger: "blur" }],
      type: "input",
      attrs: {
        placeholder: "请输入用户名",
      },
      col: {
        xs: 24,
        sm: 12,
      },
    },
    {
      label: "用户昵称",
      prop: "nickname",
      rules: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
      type: "input",
      attrs: {
        placeholder: "请输入用户昵称",
      },
      col: {
        xs: 24,
        sm: 12,
      },
    },
    {
      label: "所属部门",
      prop: "deptId",
      rules: [
        { required: true, message: "所属部门不能为空", trigger: "change" },
      ],
      type: "tree-select",
      attrs: {
        placeholder: "请选择所属部门",
        data: deptArr,
        filterable: true,
        "check-strictly": true,
        "render-after-expand": false,
      },
    },
    {
      type: "custom",
      label: "性别",
      prop: "gender",
      initialValue: 1,
      attrs: { style: { width: "100%" } },
    },
    {
      label: "角色",
      prop: "roleIds",
      rules: [
        { required: true, message: "用户角色不能为空", trigger: "change" },
      ],
      type: "select",
      attrs: {
        placeholder: "请选择",
        multiple: true,
      },
      options: roleArr,
      initialValue: [],
    },
    {
      type: "input",
      label: "手机号码",
      prop: "mobile",
      rules: [
        {
          pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
          message: "请输入正确的手机号码",
          trigger: "blur",
        },
      ],
      attrs: {
        placeholder: "请输入手机号码",
        maxlength: 11,
      },
    },
    {
      label: "邮箱",
      prop: "email",
      rules: [
        {
          pattern: /\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/,
          message: "请输入正确的邮箱地址",
          trigger: "blur",
        },
      ],
      type: "input",
      attrs: {
        placeholder: "请输入邮箱",
        maxlength: 50,
      },
    },
    {
      label: "状态",
      prop: "status",
      type: "radio",
      options: [
        { label: "正常", value: 1 },
        { label: "禁用", value: 0 },
      ],
      initialValue: 1,
    },
  ],
});
// 编辑配置
const editModalConfig = reactive({
  component: "drawer",
  drawer: {
    title: "修改用户",
    size: useAppStore().device === DeviceEnum.MOBILE ? "80%" : 500,
  },
  pk: "id",
  /**
   * 表单提交（新增/编辑保存）
   */
  formAction(data) {
    return updateLocalUser(data.id, data);
  },
  formItems: [
    {
      label: "用户名",
      prop: "username",
      rules: [{ required: true, message: "用户名不能为空", trigger: "blur" }],
      type: "input",
      attrs: {
        placeholder: "请输入用户名",
        readonly: true,
      },
    },
    {
      label: "用户昵称",
      prop: "nickname",
      rules: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
      type: "input",
      attrs: {
        placeholder: "请输入用户昵称",
      },
    },
    {
      label: "所属部门",
      prop: "deptId",
      rules: [{ required: true, message: "所属部门不能为空", trigger: "blur" }],
      type: "tree-select",
      attrs: {
        placeholder: "请选择所属部门",
        data: deptArr,
        filterable: true,
        "check-strictly": true,
        "render-after-expand": false,
      },
    },
    {
      type: "custom",
      label: "性别",
      prop: "gender",
      initialValue: 1,
      attrs: { style: { width: "100%" } },
    },
    {
      label: "角色",
      prop: "roleIds",
      rules: [{ required: true, message: "用户角色不能为空", trigger: "blur" }],
      type: "select",
      attrs: {
        placeholder: "请选择",
        multiple: true,
      },
      options: roleArr,
      initialValue: [],
    },
    {
      type: "input",
      label: "手机号码",
      prop: "mobile",
      rules: [
        {
          pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
          message: "请输入正确的手机号码",
          trigger: "blur",
        },
      ],
      attrs: {
        placeholder: "请输入手机号码",
        maxlength: 11,
      },
    },
    {
      label: "邮箱",
      prop: "email",
      rules: [
        {
          pattern: /\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/,
          message: "请输入正确的邮箱地址",
          trigger: "blur",
        },
      ],
      type: "input",
      attrs: {
        placeholder: "请输入邮箱",
        maxlength: 50,
      },
    },
    {
      label: "状态",
      prop: "status",
      type: "switch",
      initialValue: 1,
      attrs: {
        inlinePrompt: true,
        activeText: "正常",
        inactiveText: "禁用",
        activeValue: 1,
        inactiveValue: 0,
      },
    },
  ],
});
// 页面逻辑
const {
  searchRef,
  contentRef,
  addModalRef,
  editModalRef,
  handleQueryClick,
  handleResetClick,
  handleAddClick,
  handleEditClick,
  handleViewClick,
  handleSubmitClick,
  handleExportClick,
  handleSearchClick,
  handleFilterChange,
} = useCrudPage();
/**
 * 处理工具栏按钮点击
 */
function handleToolbarClick(name) {
  if (name === "custom1") {
    ElMessage.success("点击了自定义1按钮");
  }
}
/**
 * 处理操作列点击：详情、编辑等
 */
const handleOperateClick = (data) => {
  if (data.name === "detail") {
    editModalConfig.drawer = { ...editModalConfig.drawer, title: "查看" };
    handleViewClick(data.row, async () => {
      return await getLocalForm(data.row.id);
    });
  } else if (data.name === "edit") {
    editModalConfig.drawer = { ...editModalConfig.drawer, title: "修改" };
    handleEditClick(data.row, async () => {
      return await getLocalForm(data.row.id);
    });
  } else if (data.name === "reset_pwd") {
    ElMessageBox.prompt(
      "请输入用户名" + data.row.username + "」的新密码",
      "重置密码",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      },
    ).then(
      ({ value }) => {
        if (!value || value.length < 6) {
          ElMessage.warning("密码至少需6位字符，请重新输入");
          return false;
        }
        resetLocalPassword(data.row.id, value).then(() => {
          ElMessage.success("密码重置成功，新密码是：" + value);
        });
      },
      () => {
        // 用户取消
      },
    );
  }
};
// 初始化
onMounted(() => {
  initOptions();
});
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
