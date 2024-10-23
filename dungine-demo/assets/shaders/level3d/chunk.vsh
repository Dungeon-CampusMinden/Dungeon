#version 330 core
#define MAX_ATLASENTRIES 65536

in ivec3 aPosition;
in int aFaces;
in ivec3 aAtlasEntries;

flat out int vsFaces;
flat out ivec3 vsAtlasEntries;

void main() {
    gl_Position = vec4(aPosition, 1.0);
    vsFaces = aFaces;
    vsAtlasEntries = aAtlasEntries;
}
