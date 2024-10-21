#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in int aAtlasEntry;

flat out int vsAtlasEntry;

void main() {
    gl_Position = vec4(aPosition, 1.0);
    vsAtlasEntry = aAtlasEntry;
}
