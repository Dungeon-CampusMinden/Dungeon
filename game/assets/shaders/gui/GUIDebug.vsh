#version 330 core

in vec2 aPosition;
in vec2 aTexCoord;

out vec2 vTexCoord;

void main() {
    gl_Position = vec4(aPosition, 0.0, 1.0);
    vTexCoord = aTexCoord;
}
