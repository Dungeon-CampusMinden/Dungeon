#define MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE 1
#define MATERIAL_FLAG_HAS_NORMAL_TEXTURE 2
#define MATERIAL_FLAG_HAS_SPECULAR_TEXTURE 4

struct Material {
  vec4 diffuseColor;
  sampler2D diffuseTexture;
  sampler2D normalTexture;
  sampler2D specularTexture;
  int flags;
};
