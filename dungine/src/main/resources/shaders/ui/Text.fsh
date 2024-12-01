#version 330 core

in vec2 gs_TexCoord;
in float gs_Depth;

out vec4 fragColor;

uniform vec4 uColor;
uniform sampler2D uPage;

void main() {
  fragColor = texture(uPage, gs_TexCoord) * uColor;
  gl_FragDepth = gs_Depth;
}
