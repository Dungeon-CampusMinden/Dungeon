#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

varying vec2 uv;
varying vec2 worldPos;
uniform sampler2D u_texture;
uniform float u_time;
uniform float u_ambientLight;
const int maxLights = 100;
uniform vec3 u_lightSources[maxLights];
uniform int u_lightSources_count;
uniform vec3 u_lightColors[maxLights];
uniform vec3 u_steamSources[4];
uniform int u_steamSources_count;

void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    vec3 illumination = vec3(u_ambientLight);

    for (int i = 0; i < maxLights; i++) {
        if (i >= u_lightSources_count) break;
        vec3 source = u_lightSources[i];
        vec3 tint = u_lightColors[i];
        // Spatial phases keep neighboring flames from pulsing in unison.
        // Only torches use the bright red coefficient; kettles and the core stay steady.
        float phase = dot(source.xy, vec2(1.73, 2.91));
        float flicker = 1.0;
        if (tint.r > 0.7) {
            flicker += 0.055 * sin(u_time * 4.1 + phase)
                     + 0.025 * sin(u_time * 7.7 + phase * 1.37);
        }
        float radius = 5.0 * source.z * (0.96 + 0.04 * flicker);
        float distanceToLight = length(worldPos - source.xy);
        float falloff = 1.0 - smoothstep(0.0, radius, distanceToLight);
        // Concentrate the warm light near the flame, with a soft edge on nearby tiles.
        illumination += tint * falloff * falloff * flicker;
    }

    // Allow local brightening instead of clipping every light to the ambient ceiling.
    // Multiplication preserves empty black space and the original pixel-art texture.
    color.rgb *= min(illumination, vec3(1.65));
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
