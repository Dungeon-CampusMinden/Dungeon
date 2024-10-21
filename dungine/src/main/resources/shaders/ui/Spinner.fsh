#version 330 core

#define PI 3.14159265359

in vec3 vs_FragPos;
in float vs_Depth;

uniform mat4 aModel;
uniform float uThickness;
uniform int uColor;
uniform int uTime;

out vec4 fragColor;

void main() {
  vec2 center = vec2(0.5f, 0.5f);
  float headAngle = ((uTime % 1000) / 1000.0f) * (2 * PI);
  float dist = distance(vs_FragPos.xy, center);
  if (dist < 0.5f && dist > 0.5f - uThickness) {
    float outer = smoothstep(0.5f, 0.49f, dist);
    float inner = smoothstep(0.5f - uThickness, 0.51f - uThickness, dist);
    float a = outer * inner;

    float time = float(uTime % 1000) / 1000.0f;
    float angle = atan(vs_FragPos.y - center.y, vs_FragPos.x - center.x);
    if(angle < 0) angle += 2 * PI;
    angle += time * 2 * PI;
    angle = mod(angle, 2 * PI);
    a *= 1.0f - angle / (2 * PI);
    fragColor = vec4(((uColor >> 24) & 0xFF) / 255.0f, ((uColor >> 16) & 0xFF) / 255.0f, ((uColor >> 8) & 0xFF) / 255.0f, a);
  }
  gl_FragDepth = vs_Depth;
}
