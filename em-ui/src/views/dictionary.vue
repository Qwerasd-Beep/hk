<template>
  <h1>数据字典管理</h1>
  <el-row>
    <el-col :span="6">
      <div class="col1">
        <el-tree
          style="max-width: 600px"
          :data="treeData"
          :props="treeProps"
          @node-click="handleNodeClick"
        ></el-tree>
      </div>
    </el-col>
    <el-col :span="18">
      <el-dialog v-model="addTypeDialogVisible" title="新增字典类型">
        <el-form :model="newType" label-width="120px">
          <el-form-item label="类型编码">
            <el-input v-model="newType.类型编码"></el-input>
          </el-form-item>
          <el-form-item label="类型名称">
            <el-input v-model="newType.类型名称"></el-input>
          </el-form-item>
          <el-form-item label="类型描述">
            <el-input v-model="newType.类型描述"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addType">确定</el-button>
        </template>
      </el-dialog>

      <div v-if="selectedNode.type === 'type'">
        <h2>编辑字典类型</h2>
        <el-form :model="selectedType" label-width="120px">
          <el-form-item label="类型编码">
            <el-input v-model="selectedType.类型编码" disabled></el-input>
          </el-form-item>
          <el-form-item label="类型名称">
            <el-input v-model="selectedType.类型名称"></el-input>
          </el-form-item>
          <el-form-item label="类型描述">
            <el-input v-model="selectedType.类型描述"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="updateType">保存</el-button>
            <el-button @click="deleteType">删除</el-button>
          </el-form-item>
        </el-form>
        <el-button @click="openAddItemDialog">新增字典项</el-button>
      </div>

      <div v-else-if="selectedNode.type === 'item'">
        <h2>编辑字典项</h2>
        <el-form :model="selectedItem" label-width="120px">
          <el-form-item label="类型编码">
            <el-input v-model="selectedItem.类型编码" disabled></el-input>
          </el-form-item>
          <el-form-item label="字典编码">
            <el-input v-model="selectedItem.字典编码" disabled></el-input>
          </el-form-item>
          <el-form-item label="字典项">
            <el-input v-model="selectedItem.字典项"></el-input>
          </el-form-item>
          <el-form-item label="字典描述">
            <el-input v-model="selectedItem.字典描述"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="updateItem">保存</el-button>
            <el-button @click="deleteItem">删除</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-dialog v-model="addItemDialogVisible" title="新增字典项">
        <el-form :model="newItem" label-width="120px">
          <el-form-item label="类型编码">
            <el-input v-model="newItem.类型编码" disabled></el-input>
          </el-form-item>
          <el-form-item label="字典编码">
            <el-input v-model="newItem.字典编码"></el-input>
          </el-form-item>
          <el-form-item label="字典项">
            <el-input v-model="newItem.字典项"></el-input>
          </el-form-item>
          <el-form-item label="字典描述">
            <el-input v-model="newItem.字典描述"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addItemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addItem">确定</el-button>
        </template>
      </el-dialog>
    </el-col>
  </el-row>
  <el-button @click="openAddTypeDialog">新增字典类型</el-button>
</template>

<script setup>
// 是可以自已指定label 和 chidren对应的名称的,比如定义成下面的样子
import { onMounted, ref, inject } from "vue";
import { doGet, doPost, doPut, doDel } from "@/utils/ajax";
import { msgBox, errorBox, confirmBox } from "@/utils/common";

function deleteType() {
  confirmBox("确定要删除吗").then(() => {
    doDel(`/dictionary-types/delete/${selectedType.value.id}`).then((resp) => {
      console.log(resp.data);
      if (resp.data.code == 20000) {
        msgBox("字典类型删除成功");
        getData();
      } else {
        errorBox("字典类型删除失败");
      }
    });
  });
}
const newItem = ref({
  类型编码: "",
  字典编码: "",
  字典项: "",
  字典描述: "",
});
const addItemDialogVisible = ref(false);
function openAddItemDialog() {
  newItem.value = {
    类型编码: selectedType.value.类型编码,
    字典编码: "",
    字典项: "",
    字典描述: "",
  };
  addItemDialogVisible.value = true;
}
function addItem() {
  doPost("/dictionary-items/add", newItem.value).then((resp) => {
    if (resp.data.code == 20000) {
      msgBox("字典项添加成功");
      addItemDialogVisible.value = false;
      reload();
    } else {
      errorBox("字典项添加失败");
    }
  });
}
const reload = inject("reload");
function deleteItem() {
  confirmBox("确定要删除吗").then(() => {
    doDel(`/dictionary-items/delete/${selectedItem.value.id}`).then((resp) => {
      console.log(resp.data);
      if (resp.data.code == 20000) {
        msgBox("字典项删除成功");
        getData();
      } else {
        errorBox("字典项删除失败");
      }
    });
  });
}

const treeProps = {
  label: "name",
  children: "children",
};
const treeData = ref([]);
onMounted(() => {
  getData();
});
async function getData() {
  const typesResp = await doGet("/dictionary-types/getall");
  const types = typesResp.data.data;
  const promises = types.map(async (t) => {
    const itemsResp = await doGet("/dictionary-items/getbycode/" + t.类型编码);
    const items = itemsResp.data.data;
    const children = items.map((item) => ({
      ...item,
      name: item.字典项,
      type: "item",
    }));
    return {
      ...t,
      name: t.类型名称,
      children: children,
      type: "type",
    };
  });
  const tree = await Promise.all(promises);
  treeData.value = tree;
}

// 则树控件的数据:
const mydata = [
  {
    menuName: "用户管理",
    subMenuList: [
      {
        menuName: "用户添加",
        subMenuList: [
          {
            menuName: "添普通用户",
          },
          {
            menuName: "添超级用户",
          },
        ],
      },
      {
        menuName: "用户删除",
        subMenuList: [
          {
            menuName: "普通删除",
          },
          {
            menuName: "强制删除",
          },
        ],
      },
    ],
  },
  {
    menuName: "商品管理",
    subMenuList: [
      {
        menuName: "商品添加",
        subMenuList: [
          {
            menuName: "添普通商品",
          },
          {
            menuName: "添超级商品",
          },
        ],
      },
      {
        menuName: "商品删除",
        subMenuList: [
          {
            menuName: "普通删除商品",
          },
          {
            menuName: "强制删除商品",
          },
        ],
      },
    ],
  },
];
const addTypeDialogVisible = ref(false);
const newType = ref({
  类型编码: "",
  类型名称: "",
  类型描述: "",
});
function openAddTypeDialog() {
  newType.value = {
    类型编码: "",
    类型名称: "",
    类型描述: "",
  };
  addTypeDialogVisible.value = true;
}
function addType() {
  doPost("/dictionary-types/add", newType.value).then((resp) => {
    if (resp.data.code == 20000) {
      msgBox("字典类型添加成功");
      addTypeDialogVisible.value = false;
      getData();
    } else {
      errorBox("字典类型添加失败");
    }
  });
}
const selectedNode = ref({});
const selectedType = ref({});
const selectedItem = ref({});
function handleNodeClick(node) {
  selectedNode.value = node;
  if (node.type === "type") {
    selectedType.value = { ...node };
    console.log("选中的是字典类型", selectedType.value);
  } else if (node.type === "item") {
    selectedItem.value = { ...node };
  }
}
function updateType() {
  doPut("/dictionary-types/update", selectedType.value).then((resp) => {
    if (resp.data.code == 20000) {
      msgBox("字典类型更新成功");
      getData();
    } else {
      errorBox("字典类型更新失败");
    }
  });
}
function updateItem() {
  doPut("/dictionary-items/update", selectedItem.value).then((resp) => {
    if (resp.data.code == 20000) {
      msgBox("字典项更新成功");
      getData();
    } else {
      errorBox("字典项更新失败");
    }
  });
}
</script>
<style scoped>
.col1 {
  background-color: pink;
}
.col2 {
  background-color: lightblue;
}
</style>
