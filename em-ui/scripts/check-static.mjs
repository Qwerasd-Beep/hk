import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const violations = [];
for (const directory of ["mock", "mocks"]) {
  if (fs.existsSync(path.join(root, directory))) {
    violations.push(`${directory}: forbidden root directory`);
  }
}
const forbidden = [
  [/\baxios\b/i, "axios"],
  [/(?:from\s*["']|import\s*\(["'])@\/api(?:\/|["'])/, "API import"],
  [/vue-i18n|useI18n|@\/lang(?:\/|["'])/, "internationalization"],
  [/<script[^>]*lang=["']ts["']/, "TypeScript component"],
];
let vueCount = 0;
function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const file = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      if (["mock", "mocks", "api", "lang"].includes(entry.name))
        violations.push(`${file}: forbidden directory`);
      walk(file);
    } else if (/\.(vue|js|ts|scss|json)$/.test(entry.name)) {
      if (/\.ts$/.test(entry.name)) violations.push(`${file}: TypeScript file`);
      if (/\.vue$/.test(entry.name)) vueCount++;
      const content = fs.readFileSync(file, "utf8");
      const relative = path.relative(root, file).replaceAll("\\", "/");
      if (relative.startsWith("src/views/demo/") || relative.startsWith("src/views/dashboard/")) {
        if (/\bfetch\s*\(|\bXMLHttpRequest\b|\bEventSource\b|\bWebSocket\b|\bdo(?:Get|Post|Put|Del)\s*\(|@\/utils\/ajax|v-has(?:Perm|Role)?\b/.test(content))
          violations.push(`${relative}: Demo/dashboard must remain static`);
      }
      for (const [pattern, label] of forbidden)
        if (pattern.test(content))
          violations.push(`${path.relative(root, file)}: ${label}`);
    }
  }
}
walk(path.join(root, "src"));
const pkg = JSON.parse(
  fs.readFileSync(path.join(root, "package.json"), "utf8"),
);
for (const name of Object.keys({
  ...pkg.dependencies,
  ...pkg.devDependencies,
})) {
  if (/axios|mock|i18n/.test(name)) violations.push(`package.json: ${name}`);
}
const vite = fs.readFileSync(path.join(root, "vite.config.js"), "utf8");
if (/mockDevServerPlugin|mock-dev-server|proxy\s*:/.test(vite))
  violations.push("vite.config.js: request interception or proxy");
for (const mode of ["development", "production"]) {
  const env = fs.readFileSync(path.join(root, `.env.${mode}`), "utf8");
  if (!/^VITE_MOCK_DEV_SERVER=false$/m.test(env))
    violations.push(`${mode}: mock flag must be false`);
}
if (violations.length) {
  console.error(violations.join("\n"));
  process.exitCode = 1;
} else
  console.log(
    `UI checks passed (${vueCount} Vue files; Demo/dashboard static; no axios, i18n, or mock runtime).`,
  );
