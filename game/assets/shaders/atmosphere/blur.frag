// Separable 9-tap gaussian; u_dir is one texel along the blur axis.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;
uniform sampler2D u_texture;
uniform vec2 u_dir;

void main() {
    vec3 c = texture2D(u_texture, v_uv).rgb * 0.227027;
    c += texture2D(u_texture, v_uv + u_dir * 1.384615).rgb * 0.316216;
    c += texture2D(u_texture, v_uv - u_dir * 1.384615).rgb * 0.316216;
    c += texture2D(u_texture, v_uv + u_dir * 3.230769).rgb * 0.070270;
    c += texture2D(u_texture, v_uv - u_dir * 3.230769).rgb * 0.070270;
    gl_FragColor = vec4(c, 1.0);
}
