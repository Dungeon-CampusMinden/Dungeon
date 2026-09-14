#ifdef GL_ES
precision mediump float;
#endif

varying vec2 uv;
uniform sampler2D u_texture;
uniform vec2 u_texelSize;
uniform float u_time;
uniform float u_age;
uniform float u_strength;

void main() {
    vec2 p = uv * 2.0 - 1.0;
    float radius = length(p);
    float edge = smoothstep(0.38, 1.28, radius);
    float breath = 0.5 + 0.5 * sin(u_time * 1.35);
    float flow = sin(p.y * 7.0 + u_time * 0.8)
               * sin(p.x * 5.0 - u_time * 0.55);

    // Subpixel refraction stays at the edge, leaving the maze and Nox readable.
    vec2 offset = vec2(flow, sin(p.x * 8.0 + u_time * 0.7));
    offset *= u_texelSize * edge * u_strength * 0.65;
    vec2 sampleUv = clamp(uv + offset, u_texelSize * 0.5, 1.0 - u_texelSize * 0.5);
    vec4 color = texture2D(u_texture, sampleUv);

    float veil = edge * u_strength * (0.22 + 0.075 * breath + 0.025 * flow);
    vec3 magic = mix(vec3(0.14, 0.18, 0.48), vec3(0.32, 0.14, 0.53),
                     0.5 + 0.5 * sin(p.y * 2.0 + u_time * 0.35));
    color.rgb *= 1.0 - edge * u_strength * 0.16;
    // Composite a translucent veil even over the transparent void around the corridors.
    color.rgb = color.rgb * (1.0 - veil) + magic * veil;
    color.a = color.a * (1.0 - veil) + veil;

    // Two soft startup disturbances; no continuous full-screen flashing.
    float startup = exp(-pow((u_age - 0.22) / 0.09, 2.0))
                  + 0.55 * exp(-pow((u_age - 0.48) / 0.12, 2.0));
    color.rgb *= 1.0 - startup * u_strength * 0.13;
    gl_FragColor = color;
}
