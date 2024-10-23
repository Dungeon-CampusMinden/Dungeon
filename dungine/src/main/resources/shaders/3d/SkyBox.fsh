#version 330 core

in vec2 vsTexCoord;

uniform sampler2D uCubeMap;

out vec4 fragColor;

void main() {
  fragColor = texture(uCubeMap, vsTexCoord);
}
