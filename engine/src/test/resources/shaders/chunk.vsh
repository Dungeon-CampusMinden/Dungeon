#version 330 core
#define MAX_ATLASENTRIES 65536

in ivec3 a_Position;
in int a_Faces;
in ivec3 a_FaceAtlasEntries;

flat out int vs_Faces;
flat out ivec3 vs_FaceAtlasEntries;

void main() {
    gl_Position = vec4(a_Position, 1.0);
    vs_Faces = a_Faces;
    vs_FaceAtlasEntries = a_FaceAtlasEntries;
}
