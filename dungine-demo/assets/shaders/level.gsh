#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

flat in int vsAtlasEntry[];

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 gsTexCoord;
flat out int gsAtlasEntry;

void main() {
  gl_Position = uProjection * uView * uModel * gl_in[0].gl_Position;
  gsTexCoord = vec2(0.0, 0.0);
  gsAtlasEntry = vsAtlasEntry[0];
  EmitVertex();

  gl_Position = uProjection * uView * uModel * (gl_in[0].gl_Position + vec4(0.0, 0.0, 1.0, 0.0));
  gsTexCoord = vec2(0.0, 1.0);
  gsAtlasEntry = vsAtlasEntry[0];
  EmitVertex();

  gl_Position = uProjection * uView * uModel * (gl_in[0].gl_Position + vec4(1.0, 0.0, 0.0, 0.0));
  gsTexCoord = vec2(1.0, 0.0);
  gsAtlasEntry = vsAtlasEntry[0];
  EmitVertex();

  gl_Position = uProjection * uView * uModel * (gl_in[0].gl_Position + vec4(1.0, 0.0, 1.0, 0.0));
  gsTexCoord = vec2(1.0, 1.0);
  gsAtlasEntry = vsAtlasEntry[0];
  EmitVertex();

  EndPrimitive();
}
