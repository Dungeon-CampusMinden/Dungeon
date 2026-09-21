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
const float textureAlpha = 0.75;

// ----- Custom functions -----
bool isInOutline(vec2 uv, vec2 stepSize, int width) {
  for(int ix = -10; ix <= 10; ix++){
    if(abs(float(ix)) > float(width)) continue;
    for(int iy = -10; iy <= 10; iy++){
      if(abs(float(iy)) > float(width)) continue;
      float sampleX = uv.x + float(ix) * stepSize.x;
      float sampleY = uv.y + float(iy) * stepSize.y;
      vec4 neighbor = texture2D(u_texture, vec2(sampleX, sampleY));
      if(neighbor.a >= 0.01){
        return true;
      }
    }
  }
  return false;
}

// ----- Main -----
void main(){
  vec4 color = unPma(texture2D(u_texture, vec2(uv.x, uv.y)));

  vec2 stepSize = vec2(1.0) / u_resolution;
  int width = int(u_width);

  if(color.a > 0.0){
    color.rgb = mix(color.rgb, u_textureColor.rgb, textureColorInfluence);
    color.a = u_textureColor.a;
  } else if(width > 0 && isInOutline(uv, stepSize, width)){
    color = u_color;
  }

  gl_FragColor = pma(color);
}
