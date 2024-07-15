#version 330 core
#define MAX_ENTRIES 65536

in vec3 a_Position;
in vec2 a_TextureCoords;
in ivec3 i_TilePosition;
in int i_EntryIndex;

out vec2 o_TextureCoord;
flat out int o_TextureIndex;

/* BEGIN: Texture Atlas */

uniform ivec2 uTextureAtlasSize;
uniform samplerBuffer uTextureAtlasEntries;

/* END: Texture Atlas */

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

uniform ivec3 uChunkPosition;
uniform ivec3 uChunkSize;

void main() {
    vec3 pos = a_Position + (uChunkPosition * uChunkSize) + i_TilePosition;
    gl_Position = uProjection * uView * uModel * vec4(pos, 1.0);

    int x = (int(texelFetch(uTextureAtlasEntries, i_EntryIndex).x) >> 16) & 0xFFFF;
    int y = int(texelFetch(uTextureAtlasEntries, i_EntryIndex).x) & 0xFFFF;
    int width = (int(texelFetch(uTextureAtlasEntries, i_EntryIndex).y) >> 16) & 0xFFFF;
    int height = int(texelFetch(uTextureAtlasEntries, i_EntryIndex).y) & 0xFFFF;
    int textureIndex = (int(texelFetch(uTextureAtlasEntries, i_EntryIndex).z) >> 16) & 0xFFFF;

    float tx = (x + a_TextureCoords.x * width) / uTextureAtlasSize.x;
    float ty = (y + a_TextureCoords.y * height) / uTextureAtlasSize.y;

    o_TextureCoord = vec2(tx, ty);
    o_TextureIndex = textureIndex;
}
