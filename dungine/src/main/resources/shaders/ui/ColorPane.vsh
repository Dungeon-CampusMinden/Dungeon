#version 330 core

layout (location = 0) in vec3 aPosition;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

out vec3 vs_FragPos;
out float vs_Depth;

void main() {
  vec4 pos = uView * uModel * vec4(aPosition, 1.0f);
  gl_Position = uProjection * pos;
  vs_FragPos = aPosition;
  vs_Depth = pos.z;
}
