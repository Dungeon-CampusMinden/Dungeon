#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

uniform int uBillboardMode;

out vec2 vsTexCoord;

#include "../util/Billboard.vsh"

void main() {
  mat4 modelView = billboard(uModel, uView, uBillboardMode);
  gl_Position = uProjection * modelView * vec4(aPosition, 1.0f);
  vsTexCoord = aTexCoord;
}
