#version 330 core

in vec2 vsTexCoord;
in float vsDepth;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
  fragColor = texture(uTexture, vsTexCoord);
  gl_FragDepth = vsDepth;
}
