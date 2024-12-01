#version 330 core
#include "../util/Animation.fsh"

in vec2 vsTexCoord;
out vec4 fragColor;

uniform Animation[1] uAnimation;

void main() {
  fragColor = animationColor(uAnimation[0], vsTexCoord);
}
