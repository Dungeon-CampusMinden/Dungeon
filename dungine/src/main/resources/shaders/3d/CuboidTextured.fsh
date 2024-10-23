#version 330 core
#include "../util/Animation.fsh"

in vec2 vsTexCoord;
flat in int vsAnimationIndex;

uniform Animation uAnimation[6];

out vec4 fragColor;

void main() {
  fragColor = animationColor(uAnimation[vsAnimationIndex], vsTexCoord);
}
