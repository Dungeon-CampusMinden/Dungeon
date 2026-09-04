#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform float u_fillPercentage;
uniform vec4 u_color;

// ----- Main -----
void main() {
  vec4 textureColor = unPma(texture2D(u_texture, uv));
  if(textureColor.a < 0.1) {
    discard;
  }

  float y = uv.y;
  y += sin(uv.x * 5.0 + u_time * TAU * 0.28) * 0.028;
  float influence = 1.0 - smoothstep(u_fillPercentage-0.05, u_fillPercentage+0.05, y);

  // Only apply shader to non-black pixels to avoid darkening outlines and such
  if (textureColor.r + textureColor.g + textureColor.b > 0.5) {
    textureColor.rgb *= 1.0 + influence;
    textureColor.rgb = mix(textureColor.rgb, u_color.rgb, 0.7 * influence);
  }

  gl_FragColor = pma(textureColor);
}
