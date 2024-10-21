#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in vec3 vsWorldPosition[];
in vec3 vsNormal[];
in vec3 vsTangent[];
in vec3 vsBitangent[];
in vec2 vsTexCoord[];

out vec3 gsWorldPos;
out vec3 gsNormal;
out vec3 gsTangent;
out vec3 gsBitangent;
out vec2 gsTexCoord;

void vertex(int index) {
  gl_Position = gl_in[index].gl_Position;
  gsWorldPos = vsWorldPosition[index];
  gsNormal = vsNormal[index];
  gsTexCoord = vsTexCoord[index];
  gsTangent = vsTangent[index];
  gsBitangent = vsBitangent[index];
  EmitVertex();
}

void main() {
  vertex(0);
  vertex(1);
  vertex(2);
  EndPrimitive();
}
