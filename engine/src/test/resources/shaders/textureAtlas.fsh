#version 330 core
#define MAX_TEXTURES 48
#define MAX_ENTRIES 65536 // 2^16 -> indices are stored in uint16_t

in uint16_t a_textureIndex;

layout (std40) uniform ubTextureAtlas {
    ivec2 size;                         //  0 - 7
    sampler2D[MAX_TEXTURES] textures;   //  8 - 199
    ivec3[MAX_ENTRIES] entries;         //200 - 1.048.775
};

out vec4 o_fragColor;

void main() {
    vec2 texCoord = ubTextureAtlas.entries[a_textureIndex].xy / ubTextureAtlas.size;
    o_fragColor = texture(ubTextureAtlas.textures[a_textureIndex], texCoord);
}
