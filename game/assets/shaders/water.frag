#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform float u_time;
uniform vec2 u_mouse;

// ----- Custom uniforms -----
uniform sampler2D u_dudv;
uniform vec4 u_waterRegion; // x,y = bottom-left corner, z,w = size
uniform vec4 u_waterColor;
uniform float u_speed;
uniform float u_repeat;

// ----- Custom functions -----
float getWaterHeight(vec2 p, float timeOffset) {
  float value = 0.0;
  float amplitude = 0.5;
  float frequency = 1.0;

  vec2 shift = vec2(100.0);
  mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));

  for (int i = 0; i < 5; i++) {
    // 1. Create a unique movement vector for this specific octave
    // We use sine/cosine of the octave index (i) to make waves move in different directions
    vec2 waveDirection = vec2(cos(float(i) * 1.45), sin(float(i) * 3.12));

    // 2. Animate the noise coordinates using the custom direction vector
    // Higher frequency octaves (small ripples) move faster than low frequencies (big swells)
    vec2 timeMovement = waveDirection * timeOffset * (0.15 + float(i) * 0.05);

    // 3. Sample the noise with the chaotic offset applied
    value += amplitude * snoise(p * frequency + timeMovement);

    // Rotate and scale coordinate space for the next octave
    p = rot * p * 2.0 + shift;
    amplitude *= 0.5;
  }

  return value * 0.5 + 0.5;
}

// ----- Main -----
void main() {
  vec4 color = unPma(texture2D(u_texture, uv));
  if (worldPos.x < u_waterRegion.x || worldPos.y < u_waterRegion.y
    || worldPos.x >= u_waterRegion.x + u_waterRegion.z
    || worldPos.y >= u_waterRegion.y + u_waterRegion.w) {
    gl_FragColor = pma(color);
    return;
  }

  vec3 bright = min(u_waterColor.rgb + vec3(0.3), vec3(1.0));
  vec3 deepWater = max(u_waterColor.rgb - vec3(0.1), vec3(0.0));
  float height = getWaterHeight(worldPos * 0.05, 5.0);
  float step = 0.34;
  float d = 0.1;
  color.rgb = mix(deepWater, u_waterColor, smoothstep(step - d, step + d, height));

  float t = u_time * u_speed;
  vec2 uv = worldPos * u_repeat;

  // Animate the two gradients independently.
  vec2 waveR = vec2(
    sin(t * 1.00),
    cos(t * 0.73 + 1.0)
  ) * 0.15;

  vec2 waveG = vec2(
    cos(t * 0.81 + 2.0),
    sin(t * 1.17 - 0.6)
  ) * 0.15;

  // Convert [0,1] -> [-1,1] so they can interfere.
  float r = texture2D(u_dudv, fract(uv + waveR)).r * 2.0 - 1.0;
  float g = texture2D(u_dudv, fract(uv + waveG)).g * 2.0 - 1.0;

  // Constructive interference: same sign adds.
  // Destructive interference: opposite signs cancel.
  float interference = r + g;

  // Map the interference to the highlight.
  float highlight = smoothstep(0.0, 0.8, interference);

  color = vec4(
    mix(color.rgb, bright, highlight),
    color.a * u_waterColor.a
  );

  gl_FragColor = pma(color);
}
