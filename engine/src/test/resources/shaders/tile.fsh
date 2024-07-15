#version 330 core
#define MAX_ATLASPAGES 32

in vec2 texCoord;
flat in int textureIndex;
in vec3 test;

uniform sampler2D uTextureAtlasPages[MAX_ATLASPAGES];

out vec4 fragColor;

void main() {
    fragColor = texture(uTextureAtlasPages[textureIndex], texCoord);
}
