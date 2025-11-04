#version 100

precision mediump float;

#define PI 3.1415926
#define TWO_PI 6.2831852

varying vec2 uv;
uniform sampler2D u_texture;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

uniform float u_width;
uniform vec4 u_color;
bool u_useTexels = false;
uniform float u_beatSpeed;
uniform float u_beatIntensity;

float easeSineSharp(float t) {
  float s = 0.5 - 0.5 * cos(TWO_PI * t);
  return pow(s, 1.5);
}

float smoothPulse(float t) {
  float tri = abs(fract(t) * 2.0 - 1.0);
  return smoothstep(0.0, 1.0, 1.0 - tri);
}

void main(){
  vec4 color = texture2D(u_texture, vec2(uv.x, uv.y));

  // This is how far we move in a given sample step
  vec2 sampleDistanceNormalized = u_useTexels ? u_texelSize : vec2(1.0);
  vec2 stepSize = sampleDistanceNormalized / u_resolution;

  int width = int(u_width + (smoothPulse(u_time * u_beatSpeed) * 2.0 - 1.0) * u_beatIntensity);

  // Outline sampling:
  //only apply to fully transparent pixels
  //sample in 8 directions around the pixel, scanning for a fragment with alpha >= 0.01
  //if any pixel is found, set this pixel to the outline color
  if(color.a == 0.0 && width > 0){
    bool foundOutline = false;
    for(int ix = -10; ix <= 10; ix++){
      if(abs(float(ix)) > float(width)) continue;
      for(int iy = -10; iy <= 10; iy++){
        if(abs(float(iy)) > float(width)) continue;
        float sampleX = uv.x + float(ix) * stepSize.x;
        float sampleY = uv.y + float(iy) * stepSize.y;
        vec4 neighbor = texture2D(u_texture, vec2(sampleX, sampleY));
        if(neighbor.a >= 0.01){
          foundOutline = true;
          break;
        }
      }
      if(foundOutline) break;
    }
    if(foundOutline){
      color = u_color;
    }
  }

  gl_FragColor = color;
}
