#define ATLAS_MAX_PAGES 32
#define ATLAS_MAX_ENTRIES 65536

uniform ivec2 uTextureAtlasSize;
uniform sampler2D uTextureAtlasPages[ATLAS_MAX_PAGES];
uniform isamplerBuffer uTextureAtlasEntries;

vec4 textureAtlas(int entry, vec2 pTexCoord) {
  int texelX = texelFetch(uTextureAtlasEntries, (entry * 3) + 0).r;
  int texelY = texelFetch(uTextureAtlasEntries, (entry * 3) + 1).r;
  int texelZ = texelFetch(uTextureAtlasEntries, (entry * 3) + 2).r;

  int x = (texelX >> 16) & 0xFFFF;
  int y = texelX & 0xFFFF;
  int width = (texelY >> 16) & 0xFFFF;
  int height = texelY & 0xFFFF;
  int page = (texelZ >> 16) & 0xFFFF;

  vec2 texCoord = vec2(float(x) / float(uTextureAtlasSize.x), float(y) / float(uTextureAtlasSize.y));
  vec2 texSize = vec2(float(width) / float(uTextureAtlasSize.x), float(height) / float(uTextureAtlasSize.y));
  return texture(uTextureAtlasPages[page], texCoord + texSize * pTexCoord);
}
