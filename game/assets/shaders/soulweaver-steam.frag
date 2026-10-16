#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

varying vec2 uv;
varying vec2 worldPos;
uniform sampler2D u_texture;
uniform float u_time;
uniform vec3 u_steamSources[4];
uniform int u_steamSources_count;

void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    // Small rising, pixel-aligned plumes reveal leaking pipes without obscuring the floor.
    vec2 pixelWorld = floor(worldPos * 16.0) / 16.0;
    for (int i = 0; i < 4; i++) {
        if (i >= u_steamSources_count) break;
        vec2 p = (pixelWorld - u_steamSources[i].xy) / u_steamSources[i].z;
        float rise = u_time * 1.3 + float(i) * 2.1;
        float sway = 0.18 * sin(p.y * 2.7 - rise);
        float plume = (1.0 - smoothstep(0.0, 0.45 + max(p.y, 0.0) * 0.3, abs(p.x - sway)))
                    * smoothstep(-0.2, 0.2, p.y) * (1.0 - smoothstep(0.5, 2.8, p.y));
        plume *= 0.18 + 0.08 * sin(p.y * 7.0 - rise * 3.0);
        float floorVisible = step(0.025, max(color.r, max(color.g, color.b)));
        color.rgb = mix(color.rgb, vec3(0.7, 0.77, 0.78), plume * floorVisible);
    }
    gl_FragColor = pma(color);
}
