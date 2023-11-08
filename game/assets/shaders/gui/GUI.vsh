#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vTexCoord;
out vec3 vFragmentPos;
out vec3 vElementPos;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0f);
    vFragmentPos = (uModel * vec4(aPosition, 1.0f)).xyz;
    vElementPos = aPosition;
    vTexCoord = aTexCoord;
}
