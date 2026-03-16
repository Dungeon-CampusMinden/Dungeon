import fs from "fs";
import path from "path";

const cwd = process.cwd();
const pkgPath = path.join(cwd, "package.json");

if (!fs.existsSync(pkgPath)) {
  console.error("package.json not found; run from the extension root.");
  process.exit(1);
}

const pkg = JSON.parse(fs.readFileSync(pkgPath, "utf8"));
if (!pkg.contributes || !pkg.contributes.debuggers) {
  console.error("No debuggers contribution found in package.json.");
  process.exit(1);
}

console.log("DGIR debugger extension manifest looks OK.");

