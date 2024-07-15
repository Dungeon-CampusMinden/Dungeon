#version 330 core

in vec3 a_Position;
in vec2 a_TextureCoords;
in int i_AtlasEntry;

uniform ivec2 uTextureAtlasSize;
uniform isamplerBuffer uTextureAtlasEntries;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 texCoord;
flat out int textureIndex;
out vec3 test;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
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
    textureIndex = eTextureIndex;
}
