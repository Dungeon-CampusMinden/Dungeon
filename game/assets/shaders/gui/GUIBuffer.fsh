#version 330 core

in vec2 vTexCoord;

uniform sampler2D uTexture;

layout (location = 0) out vec4 fragColor;

void main() {
    vec4 textureColor = texture(uTexture, vTexCoord);
    fragColor = textureColor;
}

