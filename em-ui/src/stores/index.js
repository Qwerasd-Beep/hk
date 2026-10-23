import { createPinia } from "pinia";
export const store = createPinia();
export { useAppStore } from "./app";
export { useTagsViewStore } from "./tags-view";
