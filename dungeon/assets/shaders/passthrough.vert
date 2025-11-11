attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform vec4 u_entityBounds;
uniform float u_rotation;

varying vec2 uv;
varying vec2 worldPos;

void main() {
    uv = a_texCoord0;

    vec2 offsetFromCenter = (a_texCoord0 * u_entityBounds.zw) - (u_entityBounds.zw / 2.0);
    float cos_r = cos(u_rotation);
    float sin_r = sin(u_rotation);
    vec2 rotatedOffset;
    rotatedOffset.x = offsetFromCenter.x * cos_r - offsetFromCenter.y * sin_r;
    rotatedOffset.y = offsetFromCenter.x * sin_r + offsetFromCenter.y * cos_r;
    worldPos = u_entityBounds.xy + (u_entityBounds.zw / 2.0) + rotatedOffset;

    gl_Position = u_projTrans * a_position;
}
