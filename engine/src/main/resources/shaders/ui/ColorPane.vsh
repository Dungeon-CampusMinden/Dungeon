#version 330 core

layout (location = 0) in vec3 aPosition;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

out vec3 vs_FragPos;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0f);
  vs_FragPos = aPosition;
}
