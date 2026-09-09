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
    gl_FragColor = pma(color);
}
