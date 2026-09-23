#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

varying vec2 uv;
uniform sampler2D u_texture;
uniform bool u_completed;

void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    // The lettering is green/cyan; the housing and background have neutral RGB values.
    // Keep the original brightness, antialiasing and transparency of each letter pixel.
    if (!u_completed && color.g > color.r + 0.08 && color.g >= color.b * 0.8) {
        color.rgb = vec3(color.g, color.r, color.r);
    }
    gl_FragColor = pma(color);
}
