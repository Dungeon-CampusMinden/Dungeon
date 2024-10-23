#version 330 core

in vec3 aPosition;
in ivec2 aTexCoord; //Texture coordinates in pixels
in ivec2 aSize; //Size of the character in pixels

flat out ivec2 vs_TexCoord;
flat out ivec2 vs_Size;

void main() {
  gl_Position = vec4(aPosition, 1.0f);
  vs_TexCoord = aTexCoord;
  vs_Size = aSize;
}
