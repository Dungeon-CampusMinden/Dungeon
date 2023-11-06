#version 330 core
layout (location = 0) in vec3 aPosition;

uniform mat4x4 uProjection;
uniform mat4x4 uView;

void main() {
    gl_Position = (uView * uProjection) * vec4(aPosition, 1.0f);
}
