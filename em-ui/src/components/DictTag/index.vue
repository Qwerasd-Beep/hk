<template>
  <el-tag v-if="item?.tagType" :type="item.tagType" :size="size">{{
    item.label
  }}</el-tag>
  <span v-else>{{ item?.label ?? modelValue }}</span>
</template>
<script setup>
const props = defineProps({
  code: String,
  modelValue: [String, Number],
  options: { type: Array, default: () => [] },
  size: { type: String, default: "default" },
});
const item = computed(() =>
  props.options.find(
    (option) => String(option.value) === String(props.modelValue),
  ),
);
</script>
