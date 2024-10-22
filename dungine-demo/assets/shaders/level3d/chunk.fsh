#version 330 core

#include "/shaders/util/TextureAtlas.fsh"

in vec2 gsTexCoord;
flat in int gsTextureIndex;

out vec4 fragColor;

void main() {
  if (gsTextureIndex == -1) {
    fragColor = vec4(1.0f, 0.08f, 0.58f, 1.0f);
    return;
  }
  fragColor = textureAtlas(gsTextureIndex, gsTexCoord);
  //fragColor = vec4(1.0f, 0, 0, 1.0f);
}
