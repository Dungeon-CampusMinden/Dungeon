#version 330 core

in vec3 aPosition;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec3 vs_FragCoord;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition.xy, 0.0f, 1.0f);
  vs_FragCoord = aPosition;
}
