#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

varying vec2 uv;
uniform sampler2D u_texture;
uniform float u_time;

void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    float pulse = 0.5 + 0.5 * sin(u_time * 8.0);
    float strength = 0.10 + pulse * 0.08;
    color.rgb = mix(color.rgb, vec3(0.95, 0.03, 0.02), strength);
    gl_FragColor = pma(color);
}
