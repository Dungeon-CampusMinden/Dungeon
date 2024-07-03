#version 330 core

in layout(location=0) vec3 a_Position;

uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uModel;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
}
