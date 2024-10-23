#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec3 aTangent;
layout (location = 4) in vec3 aBitangent;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vsTexCoord;
out vec3 vsNormal;
out vec3 vsTangent;
out vec3 vsBitangent;
out vec3 vsWorldPosition;

void main() {
  vec4 worldPos = uModel * vec4(aPosition, 1.0);
  gl_Position = uProjection * uView * worldPos;
  vsTexCoord = aTexCoord;
  vsNormal = normalize(uModel * vec4(aNormal, 0.0f)).xyz;
  vsTangent = normalize(uModel * vec4(aPosition, 0.0f)).xyz;
  vsBitangent = normalize(uModel * vec4(aBitangent, 0.0f)).xyz;
  vsWorldPosition = worldPos.xyz;
}
