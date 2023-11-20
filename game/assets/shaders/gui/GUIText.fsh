#version 330 core

out vec4 fragColor;

uniform sampler2D[8] atlases;

in vec2 vTexCoords;
in vec4 vColor;
flat in int vAtlasIndex;

void main() {
    float r = texture(atlases[vAtlasIndex], vTexCoords).r;
    fragColor = vec4(1.0f, 1.0f, 1.0f, r) * vColor;
}
