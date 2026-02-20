import { defineConfig } from "vite"
import { viteSingleFile } from "vite-plugin-singlefile"
import { viteStaticCopy } from 'vite-plugin-static-copy'

export default defineConfig({
	plugins: [
    viteSingleFile(),
    viteStaticCopy({
    targets: [
      {
        src: 'assets/**/*',
        dest: 'assets'
      }
    ]
  })],
})
