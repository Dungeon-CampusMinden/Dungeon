#ifdef GL_ES
precision mediump float;
#endif

varying vec2 uv;
uniform sampler2D u_texture;

void main() {
    vec4 tex = texture2D(u_texture, uv);
    gl_FragColor = tex;
}
