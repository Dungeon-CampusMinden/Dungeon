#version 330 core

in layout(location = 0) vec3 a_Position;
in layout(location = 4) mat4 i_Model;

uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    gl_Position = uProjection * uView * i_Model * vec4(a_Position, 1.0);
}
