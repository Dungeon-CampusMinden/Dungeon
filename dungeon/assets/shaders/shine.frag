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
uniform float u_sliceCount;
uniform float u_gapSize;
uniform float u_rotationSpeed;
uniform vec4 u_shineColor;

const float maxSliceCount = 8.0;
const vec2 u_centerPos = vec2(0.5);

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
float shineDistanceDropoff(float d) {
  float t = d * 2.0;
  float clamped_t = min(1.0, t);
  float dropoff = 1.0 - pow(clamped_t, 8.0);
  return max(0.0, dropoff * 2.0);
}

float shineDropoff(float d) {
  return max(0.0, -1.0 * (pow(d, 3.0)) + 1.0);
}

// Main
void main() {
  vec4 texColor = unPma(texture2D(u_texture, uv));
  // Skip fully opaque pixels
  if (texColor.a > 0.99) {
    gl_FragColor = texColor;
    return;
  }

  // For other pixels, calculate shine effect and later blend with texColor based on alpha
  vec4 color = vec4(0.0);

  float shineWidthRads = PI / (u_sliceCount * (u_gapSize + 1.0));

  vec2 dir = uv - u_centerPos;
  float distance = length(dir);
  float angle = atan(dir.y, dir.x);
  float radius = length(dir * u_aspect);

  float timeAdjustedAngle = angle - (u_time * u_rotationSpeed);
  float modAngle = mod(timeAdjustedAngle + PI, TAU);

  float shineIntensity = 0.0;
  for (float i = 0.0; i < maxSliceCount; i += 1.0) {
      if (i >= u_sliceCount) {
        break;
      }
      float targetAngle = i * (TAU / u_sliceCount);
      float angleDiff = abs(modAngle - targetAngle);
      if (angleDiff > PI) {
          angleDiff = TAU - angleDiff; // Wrap around
      }
      if (angleDiff < shineWidthRads) {
          float normalizedDiff = angleDiff / shineWidthRads;
          shineIntensity += shineDropoff(normalizedDiff);
      }
  }

  shineIntensity = clamp(shineIntensity, 0.0, 1.0);
  //shineIntensity *= u_shineColor.a * (1.0 - radius); // Fade out with radius
  vec4 finalShineColor = u_shineColor * shineIntensity;
  color += finalShineColor;

  // Alpha drop off based on distance from center
  float distFromCenter = length(uv - u_centerPos);
  float distDropoff = shineDistanceDropoff(distFromCenter);
  color.a *= distDropoff * 0.5;

  color = mix(color, texColor, texColor.a);
  gl_FragColor = pma(color);
}
