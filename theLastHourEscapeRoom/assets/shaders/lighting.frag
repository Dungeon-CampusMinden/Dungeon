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
// Add a uniform for an array of light sources here
const int maxLights = 100;
uniform float u_ambientLight;
uniform vec3 u_lightSources[maxLights];
uniform int u_lightSources_count;
uniform vec3 u_lightColors[maxLights];
const float beatSpeed = 0.5;
const float beatIntensity = 0.3;
const float falloffStart = 0.0;
const float falloffEnd = 5.0;

// ----- Custom functions -----

// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    vec3 totalLight = vec3(u_ambientLight);

    // Calculate light here
    for (int i = 0; i < maxLights; i++) {
        if (i >= u_lightSources_count) {
            break;
        }
        float falloffEnd = 5.0 + sin(u_time * TAU * beatSpeed) * beatIntensity;

        vec3 lightPos = u_lightSources[i];
        vec3 lightColor = u_lightColors[i]; // New

        float distance = length(lightPos.xy - worldPos);
        float intensity = (1.0 - smoothstep(falloffStart, falloffEnd * lightPos.z, distance)) * lightPos.z;

        totalLight += lightColor * intensity;
    }

//    color *= min(1.0, totalLight);
    color.rgb *= min(vec3(1.0), totalLight);

    gl_FragColor = pma(color);
//    test(u_lightSources_count);
}
