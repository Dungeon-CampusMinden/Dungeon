#version 330 core
#define MAX_ATLASPAGES 32

flat in int o_TextureIndex;
in vec2 o_TextureCoord;
uniform sampler2D[MAX_ATLASPAGES] uTextureAtlasPages;

out vec4 o_fragColor;

void main() {
    o_fragColor = texture(uTextureAtlasPages[o_TextureIndex], o_TextureCoord);
    o_fragColor = vec4(1.0f, 0.0f, 0.0f, 1.0f);
}
