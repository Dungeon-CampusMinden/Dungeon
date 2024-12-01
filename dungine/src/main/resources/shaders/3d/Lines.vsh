#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in int aColor;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

out vec4 vsColor;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
  vsColor = vec4(((aColor >> 24) & 0xFF) / 255.0f, ((aColor >> 16) & 0xFF) / 255.0f, ((aColor >> 8) & 0xFF) / 255.0f, (aColor & 0xFF) / 255.0f);
}
