export const isExternal = (path = "") => /^(https?:|mailto:|tel:)/.test(path);
export const formatGrowthRate = (value) =>
  `${(Math.abs(value) * 100).toFixed(1)}%`;
