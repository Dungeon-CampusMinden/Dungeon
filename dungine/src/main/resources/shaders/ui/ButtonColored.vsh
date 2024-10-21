#version 330 core

in vec3 aPosition;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec3 vs_FragCoord;
out float vs_Depth;

void main() {
  vec4 pos = uView * uModel * vec4(aPosition, 1.0f);
  gl_Position = uProjection * pos;
  vs_FragCoord = aPosition;
  vs_Depth = pos.z;
}
