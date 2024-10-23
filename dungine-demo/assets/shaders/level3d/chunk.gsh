#version 330 core

#define FACE_UP 0x20
#define FACE_DOWN 0x10
#define FACE_NORTH 0x08
#define FACE_SOUTH 0x04
#define FACE_WEST 0x02
#define FACE_EAST 0x01

layout (points) in;
layout (triangle_strip, max_vertices = 24) out;

flat in int vsFaces[];
flat in ivec3 vsAtlasEntries[];

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 gsTexCoord;
flat out int gsTextureIndex;

void vertex(int x, int y, int z, float u, float v, int entry);

void main() {
  int faces = vsFaces[0];
  if (faces == 0) {
    //No faces to render
    return;
  }

  if ((faces & FACE_UP) == FACE_UP) {
    int atlasEntry = (vsAtlasEntries[0].x >> 16) & 0xFFFF;
    vertex(0, 1, 0, 0, 0, atlasEntry);
    vertex(0, 1, 1, 1, 0, atlasEntry);
    vertex(1, 1, 0, 0, 1, atlasEntry);
    vertex(1, 1, 1, 1, 1, atlasEntry);
    EndPrimitive();
  }
  if ((faces & FACE_DOWN) == FACE_DOWN) {
    int atlasEntry = (vsAtlasEntries[0].x) & 0xFFFF;
    vertex(0, 0, 0, 0, 0, atlasEntry);
    vertex(1, 0, 0, 1, 0, atlasEntry);
    vertex(0, 0, 1, 0, 1, atlasEntry);
    vertex(1, 0, 1, 1, 1, atlasEntry);
    EndPrimitive();
  }
  if ((faces & FACE_NORTH) == FACE_NORTH) {
    int atlasEntry = (vsAtlasEntries[0].y >> 16) & 0xFFFF;
    vertex(0, 0, 1, 0, 0, atlasEntry);
    vertex(1, 0, 1, 1, 0, atlasEntry);
    vertex(0, 1, 1, 0, 1, atlasEntry);
    vertex(1, 1, 1, 1, 1, atlasEntry);
    EndPrimitive();
  }
  if ((faces & FACE_SOUTH) == FACE_SOUTH) {
    int atlasEntry = (vsAtlasEntries[0].y) & 0xFFFF;
    vertex(1, 0, 0, 0, 0, atlasEntry);
    vertex(0, 0, 0, 1, 0, atlasEntry);
    vertex(1, 1, 0, 0, 1, atlasEntry);
    vertex(0, 1, 0, 1, 1, atlasEntry);
    EndPrimitive();
  }
  if ((faces & FACE_WEST) == FACE_WEST) {
    int atlasEntry = (vsAtlasEntries[0].z >> 16) & 0xFFFF;
    vertex(0, 0, 0, 0, 0, atlasEntry);
    vertex(0, 0, 1, 1, 0, atlasEntry);
    vertex(0, 1, 0, 0, 1, atlasEntry);
    vertex(0, 1, 1, 1, 1, atlasEntry);
    EndPrimitive();
  }
  if ((faces & FACE_EAST) == FACE_EAST) {
    int atlasEntry = (vsAtlasEntries[0].z) & 0xFFFF;
    vertex(1, 0, 1, 0, 0, atlasEntry);
    vertex(1, 0, 0, 1, 0, atlasEntry);
    vertex(1, 1, 1, 0, 1, atlasEntry);
    vertex(1, 1, 0, 1, 1, atlasEntry);
    EndPrimitive();
  }
}

void vertex(int x, int y, int z, float u, float v, int entry) {
  vec3 pos = gl_in[0].gl_Position.xyz + vec3(x, y, z);
  gl_Position = uProjection * uView * uModel * vec4(pos, 1.0f);
  gsTexCoord = vec2(u, v);
  gsTextureIndex = entry;
  EmitVertex();
}


