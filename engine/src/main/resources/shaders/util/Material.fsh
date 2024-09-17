#define MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE 1
#define MATERIAL_FLAG_HAS_NORMAL_TEXTURE 2
#define MATERIAL_FLAG_HAS_SPECULAR_TEXTURE 4
#define MATERIAL_FLAG_HAS_AMBIENT_TEXTURE 8

struct Material {
  vec4 diffuseColor;
  vec4 specularColor;
  vec4 ambientColor;
  sampler2D diffuseTexture;
  sampler2D ambientTexture;
  sampler2D specularTexture;
  sampler2D normalTexture;
  float reflectivity;
  int flags;
};
