#version 330 core
layout (points) in;
layout (triangle_strip, max_vertices = 24) out;

flat in int vs_Faces[];
flat in ivec3 vs_FaceAtlasEntries[];

flat out ivec3 gs_Debug;
flat out int gs_TextureIndex;
out vec2 gs_TexCoord;

uniform ivec2 uTextureAtlasSize;
uniform isamplerBuffer uTextureAtlasEntries;

uniform mat4 uProjection;
uniform mat4 uView;

uniform ivec3 uChunkPosition;
uniform ivec3 uChunkSize;

void vertex(int x, int y, int z, int u, int v, int index);

void main() {
  int faces = vs_Faces[0];
  if (faces == 0) { //No faces to render
                    return;
  }

  if ((faces & 0x20) == 0x20) { //Top
                                int atlasEntry = (vs_FaceAtlasEntries[0].x >> 16) & 0xFFFF;
                                vertex(0, 1, 0, 0, 0, atlasEntry);
                                vertex(0, 1, 1, 1, 0, atlasEntry);
                                vertex(1, 1, 0, 0, 1, atlasEntry);
                                vertex(1, 1, 1, 1, 1, atlasEntry);
                                EndPrimitive();
  }
  if ((faces & 0x10) == 0x10) { //Bottom
                                int atlasEntry = (vs_FaceAtlasEntries[0].x) & 0xFFFF;
                                vertex(1, 0, 0, 0, 0, atlasEntry);
                                vertex(1, 0, 1, 1, 0, atlasEntry);
                                vertex(0, 0, 0, 0, 1, atlasEntry);
                                vertex(0, 0, 1, 1, 1, atlasEntry);
                                EndPrimitive();
  }
  if ((faces & 0x08) == 0x08) { //Front
                                int atlasEntry = (vs_FaceAtlasEntries[0].y >> 16) & 0xFFFF;
                                vertex(0, 0, 0, 0, 0, atlasEntry);
                                vertex(0, 0, 1, 1, 0, atlasEntry);
                                vertex(0, 1, 0, 0, 1, atlasEntry);
                                vertex(0, 1, 1, 1, 1, atlasEntry);
                                EndPrimitive();
  }
  if ((faces & 0x04) == 0x04) { //Back
                                int atlasEntry = (vs_FaceAtlasEntries[0].y) & 0xFFFF;
                                vertex(1, 0, 1, 0, 0, atlasEntry);
                                vertex(1, 0, 0, 1, 0, atlasEntry);
                                vertex(1, 1, 1, 0, 1, atlasEntry);
                                vertex(1, 1, 0, 1, 1, atlasEntry);
                                EndPrimitive();
  }
  if ((faces & 0x02) == 0x02) { //Left
                                int atlasEntry = (vs_FaceAtlasEntries[0].z >> 16) & 0xFFFF;
                                vertex(1, 0, 0, 0, 0, atlasEntry);
                                vertex(0, 0, 0, 1, 0, atlasEntry);
                                vertex(1, 1, 0, 0, 1, atlasEntry);
                                vertex(0, 1, 0, 1, 1, atlasEntry);
                                EndPrimitive();
  }
  if ((faces & 0x01) == 0x01) { //Right
                                int atlasEntry = (vs_FaceAtlasEntries[0].z) & 0xFFFF;
                                vertex(0, 0, 1, 0, 0, atlasEntry);
                                vertex(1, 0, 1, 1, 0, atlasEntry);
                                vertex(0, 1, 1, 0, 1, atlasEntry);
                                vertex(1, 1, 1, 1, 1, atlasEntry);
                                EndPrimitive();
  }
}

void vertex(int pX, int pY, int pZ, int u, int v, int index) {
  vec3 pos = vec3(pX, pY, pZ) + (uChunkPosition * uChunkSize) + gl_in[0].gl_Position.xyz;
  gl_Position = uProjection * uView * vec4(pos, 1.0f);

  int offset = index * 3;
  int texelX = texelFetch(uTextureAtlasEntries, offset + 0).r;
  int texelY = texelFetch(uTextureAtlasEntries, offset + 1).r;
  int texelZ = texelFetch(uTextureAtlasEntries, offset + 2).r;
  int x = (texelX >> 16) & 0xFFFF;
  int y = texelX & 0xFFFF;
  int width = (texelY >> 16) & 0xFFFF;
  int height = texelY & 0xFFFF;
  int eTextureIndex = (texelZ >> 16) & 0xFFFF;
  float tx = (x + float(u) * width) / uTextureAtlasSize.x;
  float ty = (y + float(v) * height) / uTextureAtlasSize.y;

  gs_TexCoord = vec2(tx, ty);
  gs_TextureIndex = eTextureIndex;

  gs_Debug.x = int(tx * 255.0f);
  gs_Debug.y = int(ty * 255.0f);
  gs_Debug.z = eTextureIndex;

  EmitVertex();
}
