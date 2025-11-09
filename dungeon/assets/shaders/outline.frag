#ifdef GL_ES
precision mediump float;
#endif

// ----- Defines -----
#define PI 3.1415926
#define TAU 6.2831852

// ----- From vertex shader -----
varying vec2 uv;
//varying vec2 worldPos; //Comment out if not needed for performance

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform float u_width;
uniform vec4 u_color;
uniform bool u_isRainbow;
uniform float u_beatSpeed;
uniform float u_beatIntensity;

// ----- Helper functions for PMA conversion -----
// All shaders outputting transparency or calculating colors should unPma from texture, and pma before outputting
vec4 unPma(vec4 color) {
    if (color.a < 1e-5) {
        return vec4(0.0);
    }
    return vec4(color.rgb / color.a, color.a);
}

vec4 pma(vec4 color) {
    return vec4(color.rgb * color.a, color.a);
}

// Custom functions

vec3 rgb2hsv(vec3 c) {
    float maxC = max(max(c.r, c.g), c.b);
    float minC = min(min(c.r, c.g), c.b);
    float delta = maxC - minC;
    float h = 0.0;

    if (delta > 1e-5) {
        if (maxC == c.r)
            h = mod((c.g - c.b) / delta, 6.0);
        else if (maxC == c.g)
            h = (c.b - c.r) / delta + 2.0;
        else
            h = (c.r - c.g) / delta + 4.0;
        h /= 6.0;
    }

    float s = (maxC <= 1e-5) ? 0.0 : delta / maxC;
    float v = maxC;
    return vec3(h, s, v);
}

vec3 hsv2rgb(vec3 c) {
    float h = c.x * 6.0;
    float s = c.y;
    float v = c.z;

    float i = floor(h);
    float f = h - i;
    float p = v * (1.0 - s);
    float q = v * (1.0 - f * s);
    float t = v * (1.0 - (1.0 - f) * s);

    if (i == 0.0) return vec3(v, t, p);
    else if (i == 1.0) return vec3(q, v, p);
    else if (i == 2.0) return vec3(p, v, t);
    else if (i == 3.0) return vec3(p, q, v);
    else if (i == 4.0) return vec3(t, p, v);
    else return vec3(v, p, q);
}

float smoothPulse(float t) {
  float tri = abs(fract(t) * 2.0 - 1.0);
  return smoothstep(0.0, 1.0, 1.0 - tri);
}

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

// Main
void main(){
  vec4 color = unPma(texture2D(u_texture, vec2(uv.x, uv.y)));

  vec2 stepSize = vec2(1.0) / u_resolution;

  float beat_time = smoothPulse(u_time * u_beatSpeed) * 2.0 - 1.0;
  float beat = beat_time * u_beatIntensity;
  int width = int(u_width + beat);

  if(color.a == 0.0 && width > 0){
    bool foundOutline = isInOutline(uv, stepSize, width);
    if(foundOutline){
      if(u_isRainbow){
        vec2 center = vec2(0.5, 0.5);
        vec2 fromCenter = uv - center;
        float angle = atan(fromCenter.y, fromCenter.x);
        float hue = (angle / TAU) + 0.5;
        hue += u_time * 0.2 * u_beatSpeed;
        hue = mod(hue, 1.0);
        vec3 rgb = hsv2rgb(vec3(hue, 1.0, 1.0));
        color = vec4(rgb, 1.0);
      } else {
        color = u_color;
      }
    }
  }

  gl_FragColor = pma(color);
}
