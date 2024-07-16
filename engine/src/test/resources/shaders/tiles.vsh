#version 330 core
#define MAX_ENTRIES 65536

in vec3 a_Position;
in vec2 a_TextureCoords;
in ivec3 i_TilePosition;
in int i_AtlasEntry;

uniform ivec2 uTextureAtlasSize;
uniform isamplerBuffer uTextureAtlasEntries;

uniform mat4 uProjection;
uniform mat4 uView;

uniform ivec3 uChunkPosition;
uniform ivec3 uChunkSize;

out vec2 texCoord;
flat out int textureIndex;

void main() {
    vec3 pos = a_Position + (uChunkPosition * uChunkSize) + i_TilePosition;
    gl_Position = uProjection * uView /** uModel*/ * vec4(pos, 1.0);

    int index = i_AtlasEntry;
    int offset = index * 3;
    int texelX = texelFetch(uTextureAtlasEntries, offset + 0).r;
    int texelY = texelFetch(uTextureAtlasEntries, offset + 1).r;
    int texelZ = texelFetch(uTextureAtlasEntries, offset + 2).r;

    int x = (texelX >> 16) & 0xFFFF;
    int y = texelX & 0xFFFF;
    int width = (texelY >> 16) & 0xFFFF;
    int height = texelY & 0xFFFF;
    int eTextureIndex = (texelZ >> 16) & 0xFFFF;
    float tx = (x + a_TextureCoords.x * width) / uTextureAtlasSize.x;
    float ty = (y + a_TextureCoords.y * height) / uTextureAtlasSize.y;

    texCoord = vec2(tx, ty);
    textureIndex = textureIndex;
}
