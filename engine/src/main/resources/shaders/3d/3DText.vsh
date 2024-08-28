#version 330 core

in vec3 a_Position;
in ivec2 a_TexCoord; //Texture coordinates in pixels
in ivec2 a_Size; //Size of the character in pixels

flat out ivec2 vs_TexCoord;
flat out ivec2 vs_Size;

void main() {
  gl_Position = vec4(a_Position, 1.0f);
  vs_TexCoord = a_TexCoord;
  vs_Size = a_Size;
}
