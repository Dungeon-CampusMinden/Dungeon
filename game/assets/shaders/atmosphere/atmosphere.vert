// Shared vertex shader of the atmosphere passes. v_world is only meaningful for passes drawn with
// the world projection (the light pass).
attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec2 v_uv;
varying vec2 v_world;

void main() {
    v_uv = a_texCoord0;
    v_world = a_position.xy;
    gl_Position = u_projTrans * a_position;
}
