#define MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE 1
#define MATERIAL_FLAG_HAS_NORMAL_TEXTURE 2
#define MATERIAL_FLAG_HAS_SPECULAR_TEXTURE 4
#define MATERIAL_FLAG_HAS_AMBIENT_TEXTURE 8

#include "./Animation.fsh"

uniform Animation[4] uAnimation;

struct Material {
  vec4 diffuseColor;
  vec4 specularColor;
  vec4 ambientColor;
  int diffuseTexture;
  int ambientTexture;
  int specularTexture;
  int normalTexture;
  float reflectivity;
  int flags;
};

vec4 diffuseColor(Material material, vec2 texCoords) {
  vec4 color = material.diffuseColor;
  if((material.flags & MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE) != 0) {
    color = animationColor(uAnimation[material.diffuseTexture], texCoords);
  }
  return color;
}

vec4 ambientColor(Material material, vec2 texCoords) {
  vec4 color = material.ambientColor;
  if((material.flags & MATERIAL_FLAG_HAS_AMBIENT_TEXTURE) != 0) {
    color = animationColor(uAnimation[material.ambientTexture], texCoords);
  }
  return color;
}

vec4 specularColor(Material material, vec2 texCoords) {
  vec4 color = material.specularColor;
  if((material.flags & MATERIAL_FLAG_HAS_SPECULAR_TEXTURE) != 0) {
    color = animationColor(uAnimation[material.specularTexture], texCoords);
  }
  return color;
}

vec4 normalColor(Material material, vec2 texCoords) {
  vec4 color = vec4(0.5f, 0.5f, 1.0f, 1.0f);
  if((material.flags & MATERIAL_FLAG_HAS_NORMAL_TEXTURE) != 0) {
    color = animationColor(uAnimation[material.normalTexture], texCoords);
  }
  return color;
}
