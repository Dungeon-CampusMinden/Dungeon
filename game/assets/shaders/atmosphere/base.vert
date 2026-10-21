// Bases pass: passes on the batch colour, which carries the entity's base, see
// AtmosphereRenderer#baseColor.
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec2 v_uv;
varying vec3 v_base;

void main() {
    v_uv = a_texCoord0;
    v_base = a_color.rgb;
    gl_Position = u_projTrans * a_position;
}
