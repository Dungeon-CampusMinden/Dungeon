#version 330 core

#include "/shaders/util/TextureAtlas.fsh"

in vec2 gsTexCoord;
flat in int gsAtlasEntry;


out vec4 color;

void main() {
  color = textureAtlas(gsAtlasEntry, gsTexCoord);
}
