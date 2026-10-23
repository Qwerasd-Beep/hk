<template><LeftLayout /></template>
<script setup>
import LeftLayout from "./modes/LeftLayout.vue";
import { useLayoutDevice } from "./composables/useLayoutDevice";
useLayoutDevice();
</script>
