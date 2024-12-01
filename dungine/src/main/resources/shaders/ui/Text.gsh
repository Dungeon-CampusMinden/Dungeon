#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

flat in ivec2 vs_TexCoord[]; //Texture coordinates in pixels
flat in ivec2 vs_Size[]; //Size of the character in pixels

uniform mat4 uProjection;
uniform mat4 uView;
uniform ivec2 uPageSize;
uniform vec3 uBasePosition;

out vec2 gs_TexCoord;
out float gs_Depth;

void main() {

  vec4 pos = gl_in[0].gl_Position + vec4(uBasePosition, 0.0f);

  //Top Left
  vec4 position = uView * vec4(pos.x, pos.y, pos.z, 1.0f);
  gl_Position = uProjection * position;
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  gs_Depth = position.z;
  EmitVertex();

  //Top Right
  position = uView * vec4(pos.x + vs_Size[0].x, pos.y, pos.z, 1.0f);
  gl_Position = uProjection * position;
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  gs_Depth = position.z;
  EmitVertex();

  //Bottom Left
  position = uView * vec4(pos.x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gl_Position = uProjection * position;
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  gs_Depth = position.z;
  EmitVertex();

  //Bottom Right
  position = uView * vec4(pos.x + vs_Size[0].x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gl_Position = uProjection * position;
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  gs_Depth = position.z;
  EmitVertex();

  EndPrimitive();
}
