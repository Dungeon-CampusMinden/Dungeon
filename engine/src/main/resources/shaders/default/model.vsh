#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vsTexCoord;
out vec3 vsNormal;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
  vsTexCoord = aTexCoord;
  vsNormal = aNormal;
}
