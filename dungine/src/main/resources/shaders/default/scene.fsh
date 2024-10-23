#version 330 core

#include "../util/Lighting.fsh"
#include "../util/Material.fsh"

in vec3 gsNormal;
in vec3 gsTangent;
in vec3 gsBitangent;
in vec3 gsWorldPos;
in vec2 gsTexCoord;

uniform Material uMaterial;
uniform vec3 uCameraPosition;
uniform bool uForceIlluminate;

layout (location = 0) out vec4 fragColor;

void main() {
  vec3 normal = gsNormal;

  if((uMaterial.flags & MATERIAL_FLAG_HAS_NORMAL_TEXTURE) != 0) {
    mat3 TBN = mat3(gsTangent, gsBitangent, gsNormal);
    normal = normalize(normalColor(uMaterial, gsTexCoord).rgb * 2.0 - 1.0);
    normal = normalize(TBN * normal);
  }

  if(uForceIlluminate) {
    fragColor = diffuseColor(uMaterial, gsTexCoord);
  } else {
    fragColor = calcLighting(uMaterial, gsTexCoord, normal, gsWorldPos, uCameraPosition);
  }
}

