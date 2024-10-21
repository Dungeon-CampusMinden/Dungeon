#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

out vec2 vsTexCoord;
out float vsDepth;

void main() {
  vec4 pos = uView * uModel * vec4(aPosition, 1.0f);
  gl_Position = uProjection * pos;
  vsTexCoord = aTexCoord;
  vsDepth = pos.z;
}
