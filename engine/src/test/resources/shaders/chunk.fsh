#version 330 core
#define MAX_ATLASPAGES 32

flat in ivec3 gs_Debug;
flat in int gs_TextureIndex;
in vec2 gs_TexCoord;

uniform sampler2D uTextureAtlasPages[MAX_ATLASPAGES];

out vec4 fragColor;

void main() {
  if (gs_TextureIndex == -1) {
    fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
  }
  fragColor = texture(uTextureAtlasPages[gs_TextureIndex], gs_TexCoord);
  //fragColor.r = gs_Debug.x;
  //fragColor.g = gs_Debug.y;
  //fragColor.b = gs_Debug.z / 255.0f;
}
