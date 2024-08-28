#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

flat in ivec2 vs_TexCoord[]; //Texture coordinates in pixels
flat in ivec2 vs_Size[]; //Size of the character in pixels

uniform int uBillboardMode;
uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform vec3 uBasePosition;
uniform vec3 uScale;
uniform ivec2 uPageSize;

out vec2 gs_TexCoord;

mat4 billboard(mat4 modelView);

void main() {

  vec4 pos = gl_in[0].gl_Position + vec4(uBasePosition, 0.0f);
  mat4 modelView = billboard(uView * uModel);

  //Top Left
  gl_Position = uProjection * modelView * vec4(pos.x, pos.y, pos.z, 1.0f);
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  EmitVertex();

  //Top Right
  gl_Position = uProjection * modelView * vec4(pos.x + vs_Size[0].x, pos.y, pos.z, 1.0f);
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), (vs_TexCoord[0].y + vs_Size[0].y) / float(uPageSize.y));
  EmitVertex();

  //Bottom Left
  gl_Position = uProjection * modelView * vec4(pos.x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gs_TexCoord = vec2(vs_TexCoord[0].x / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  EmitVertex();

  //Bottom Right
  gl_Position = uProjection * modelView * vec4(pos.x + vs_Size[0].x, pos.y + vs_Size[0].y, pos.z, 1.0f);
  gs_TexCoord = vec2((vs_TexCoord[0].x + vs_Size[0].x) / float(uPageSize.x), vs_TexCoord[0].y / float(uPageSize.y));
  EmitVertex();

  EndPrimitive();
}

mat4 billboard(mat4 modelView) {
  if(uBillboardMode == 1) {
    modelView[0][0] = uScale.x;
    modelView[0][1] = 0.0;
    modelView[0][2] = 0.0;
    modelView[1][0] = 0.0;
    modelView[1][1] = uScale.y;
    modelView[1][2] = 0.0;
    modelView[2][0] = 0.0;
    modelView[2][1] = 0.0;
    modelView[2][2] = uScale.z;
    return modelView;
  } else if(uBillboardMode == 2) {
    modelView[0][0] = uScale.x;
    modelView[0][1] = 0.0;
    modelView[0][2] = 0.0;
    modelView[2][0] = 0.0;
    modelView[2][1] = 0.0;
    modelView[2][2] = uScale.z;
    return modelView;
  } else {
    return modelView;
  }
}
