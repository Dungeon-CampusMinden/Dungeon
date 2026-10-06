#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
//varying vec2 worldPos; //Comment out if not needed for performance

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform float u_width;
uniform vec4 u_color;
uniform vec4 u_textureColor;

const float textureColorInfluence = 0.65;

// ----- Custom functions -----
// ----- Main -----
void main(){
  vec4 color = unPma(texture2D(u_texture, vec2(uv.x, uv.y)));

  vec2 stepSize = vec2(1.0) / u_resolution;
  int width = int(u_width);

  if(color.a > 0.0){
    color.rgb = mix(color.rgb, u_textureColor.rgb, textureColorInfluence);
    color.a = u_textureColor.a;
  } else if(width > 0 && isInOutline(u_texture, uv, stepSize, width)){
    color = u_color;
  }

  gl_FragColor = pma(color);
}
