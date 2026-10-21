// Writes the entity's base under each opaque sprite pixel; later sprites cover earlier ones as in
// the scene.
#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_uv;
varying vec3 v_base;

uniform sampler2D u_texture;

void main() {
    if (texture2D(u_texture, v_uv).a < 0.5) discard;
    gl_FragColor = vec4(v_base, 1.0);
}
