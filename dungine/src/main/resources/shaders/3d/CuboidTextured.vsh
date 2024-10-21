#version 330 core

in vec3 aPosition;
in vec2 aTexCoord;
in float aAnimationIndex;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vsTexCoord;
flat out int vsAnimationIndex;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0f);
  vsTexCoord = aTexCoord;
  vsAnimationIndex = int(aAnimationIndex);
}
