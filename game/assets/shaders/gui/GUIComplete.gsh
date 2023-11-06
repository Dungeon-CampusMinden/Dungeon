#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out vec2 vTexCoord;

void main() {
    //TR, TL, BR, BL
    gl_Position = vec4(1.0f, 1.0f, 0.0f, 1.0f);
    vTexCoord = vec2(1.0f, 1.0f);
    EmitVertex();

    gl_Position = vec4(-1.0f, 1.0f, 0.0f, 1.0f);
    vTexCoord = vec2(-1.0f, 1.0f);
    EmitVertex();

    gl_Position = vec4(1.0f, -1.0f, 0.0f, 1.0f);
    vTexCoord = vec2(1.0f, -1.0f);
    EmitVertex();

    gl_Position = vec4(-1.0f, -1.0f, 0.0f, 1.0f);
    vTexCoord = vec2(-1.0f, -1.0f);
    EmitVertex();

    EndPrimitive();
}

