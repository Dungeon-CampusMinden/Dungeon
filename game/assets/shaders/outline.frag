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

// ----- Custom functions -----
float smoothPulse(float t) {
  float tri = abs(fract(t) * 2.0 - 1.0);
  return smoothstep(0.0, 1.0, 1.0 - tri);
}

// ----- Main -----
void main(){
  vec4 color = unPma(texture2D(u_texture, vec2(uv.x, uv.y)));

  vec2 stepSize = vec2(1.0) / u_resolution;

  float beat_time = smoothPulse(u_time * u_beatSpeed) * 2.0 - 1.0;
  float beat = beat_time * u_beatIntensity;
  int width = int(u_width + beat);

  if(color.a == 0.0 && width > 0){
    bool foundOutline = isInOutline(u_texture, uv, stepSize, width);
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
