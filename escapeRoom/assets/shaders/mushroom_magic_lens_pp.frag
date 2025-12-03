#ifdef GL_ES
precision mediump float;
#endif

// The next line is a special string that indicates to the AbstractShader to include the util.glsl file here.
// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform sampler2D u_magnifyingGlassTex;
uniform vec2 u_magnifyingGlassTexSize;
const float u_magnifyingGlassScale = 0.3;
const vec2 u_magnifyingGlassAnchor = vec2(0.525, 0.45);

uniform float u_lensRadius;
const float u_border = 0.0025;
const float intensity = 0.001;
const float speed = 2.0;
const float phase = TAU * 2.0;

// ----- Custom functions -----


// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    color = color.a > 0.0 ? color : BLACK;

    vec2 aspectUv = uv / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);
    vec2 aspectMouse = u_mouse / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);

    float dist = distance(aspectUv, aspectMouse);

    // Distort world colors
    vec2 distortedUv = uv;
    distortedUv.x += sin(distortedUv.x * TAU * (phase + PI) + u_time * speed) * intensity;
    distortedUv.y += sin(distortedUv.y * TAU * phase + u_time * speed) * intensity;
    // Wavy distortion on the lens
    vec3 hsv = rgb2hsv(unPma(texture2D(u_texture, distortedUv)).rgb);
//    hsv.x = 1.0 - hsv.x;
    hsv.x = mod(hsv.x + 0.5, 1.0);
    vec3 distortedColor = hsv2rgb(hsv);
    distortedColor *= 0.7;

    // Display the magnifying glass texture around the cursor. the mouse sits centered on the texture. scale the magnifying glass texture down to u_magnifyingGlassScale
    vec2 magnifyingGlassUv = (aspectUv - aspectMouse) / u_magnifyingGlassScale + u_magnifyingGlassAnchor;
    magnifyingGlassUv.y = 1.0 - magnifyingGlassUv.y;
    vec4 magnifyingGlassColor = texture2D(u_magnifyingGlassTex, magnifyingGlassUv);
    vec3 magHsv = rgb2hsv(magnifyingGlassColor.rgb);
    magHsv.z = 1.0 - magHsv.z;
    magHsv.z *= 0.7;
    vec3 magColorCorrected = hsv2rgb(magHsv);

    color.rgb = magnifyingGlassColor.g == 1.0 ? distortedColor.rgb : mix(color.rgb, magColorCorrected, magnifyingGlassColor.a);

    gl_FragColor = pma(color);
}
