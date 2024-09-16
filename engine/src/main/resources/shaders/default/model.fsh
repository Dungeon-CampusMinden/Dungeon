#version 330 core

#include "../util/Material.fsh"

in vec2 vsTexCoord;
in vec3 vsNormal;

uniform Material uMaterial;

layout(location = 0) out vec4 fragColor;

void main() {
  vec4 diffuseColor = vec4(vsNormal, 1.0f);//uMaterial.diffuseColor;
  if ((uMaterial.flags & MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE) != 0) {
    diffuseColor = texture(uMaterial.diffuseTexture, vsTexCoord);
  }
  fragColor = diffuseColor;
}

