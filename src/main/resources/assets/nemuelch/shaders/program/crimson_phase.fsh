#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Intensity;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

float linearizeDepth(float depth, float near, float far) {
    if (depth >= 0.9999) {
        return far;
    }
    float z = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - z * (far - near));
}

float getDepth() {
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    float near = 0.01;
    float far = 1.0;
    float linearDepth = 1. - linearizeDepth(depth, near, far);
    return clamp(linearDepth / far, 0., 1.);
}

vec3 desaturateAndTint(vec3 color, float amount) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luma);
    vec3 redTinting = mix(gray, gray * vec3(1.4, 0.5, 0.45), 0.6);
    return mix(color, redTinting, amount);
}

vec3 applyDepthFog(vec3 color, float depth, float strength) {
    vec3 fogColor = vec3(0.18, 0.01, 0.01);
    float fogFactor = pow(depth, 2.2) * strength;
    return mix(color, fogColor, clamp(fogFactor, 0.0, 0.85));
}

float vignette(vec2 uv) {
    vec2 center = vec2(0.5, 0.55);
    float dist = length(uv - center) * 1.4;
    return 1.0 - clamp(pow(dist, 2.5), 0.0, 1.0);
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    if (color.a < 0.1) {
        fragColor = color;
        return;
    }

    float depth = getDepth();

    float pulse = 0.5 + 0.5 * sin(Time * 1.05);
    float pulsed = Intensity * (1.0 + pulse * 0.08);

    vec3 result = color.rgb;
    result = desaturateAndTint(result, pulsed * 0.65);
    result = applyDepthFog(result, 1.0 - depth, pulsed * 1.1);

    vec3 screenTint = vec3(0.72, 0.04, 0.04);
    result = mix(result, result * screenTint * 2.0, pulsed * 0.35);

    float vig = vignette(texCoord);
    result *= mix(0.25, 1.0, vig * (1.0 - pulsed * 0.3));

    fragColor = vec4(result, color.a);
}