#version 330 core

in vec3 a_Position;
in vec3 a_Color;

out vec4 color;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
    color = vec4(a_Color, 1.01);
}
