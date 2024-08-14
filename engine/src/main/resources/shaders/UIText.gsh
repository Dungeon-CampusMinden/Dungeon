#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

flat in ivec2 vs_TexCoord[]; //Texture coordinates in pixels
flat in ivec2 vs_Size[]; //Size of the character in pixels

uniform mat4 uProjection;
uniform mat4 uView;
uniform ivec2 uPageSize;

out vec2 gs_TexCoord;

void main() {

  vec4 pos = gl_in[0].gl_Position;

  //Top Right
  gl_Position = uProjection * uView * vec4(pos.x + vs_Size[0].x, pos.y, pos.z, 1.0f);
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  EmitVertex();

  //Top Left
  gl_Position = uProjection * uView * vec4(pos.x, pos.y, pos.z, 1.0f);
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  EmitVertex();

  //Bottom Right
  gl_Position = uProjection * uView * vec4(pos.x + vs_Size[0].x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  EmitVertex();

  //Bottom Left
  gl_Position = uProjection * uView * vec4(pos.x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  EmitVertex();

  EndPrimitive();
}
