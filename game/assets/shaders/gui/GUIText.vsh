#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in int  aAtlas;
layout (location = 2) in vec4 aColor;
layout (location = 3 /*4 5 6*/) in mat4 aTransformMatrix;
layout (location = 7 /*8 9 10 11 12 13 14*/) in vec2[4] aTexCoords;

out vec2 vPosition;
out vec2 vTexCoords;
flat out int  vAtlasIndex;
out vec4 vColor;

uniform mat4 uProjection;
uniform mat4 uView;

void main() {
    gl_Position = uProjection * uView * aTransformMatrix * vec4(aPosition, 0.0f, 1.0f);
    vTexCoords = aTexCoords[gl_VertexID];
    vColor = aColor;
    vAtlasIndex = aAtlas;
}
