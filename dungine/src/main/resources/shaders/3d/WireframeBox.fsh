#version 330 core

uniform int uColor;

out vec4 oFragColor;

void main() {
  float r = ((uColor >> 24) & 0xFF) / 255.0f;
  float g = ((uColor >> 16) & 0xFF) / 255.0f;
  float b = ((uColor >> 8) & 0xFF) / 255.0f;
  float a = (uColor & 0xFF) / 255.0f;

  oFragColor = vec4(r, g, b, a);
}
