#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 InSize;    // used for kernel based fsh effects
uniform vec2 OutSize;   // used for kernel based fsh effects
uniform float Intensity;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// This seems to somewhat work with near uniform 0.005 and far uniform 1.1

float linearizeDepth(float depth, float near, float far) {
    if (depth >= 0.9999) {
        return far;
    }
    float z = depth * 2.0 - 1.0;  // Convert from [0,1] to [-1,1]
    return (2.0 * near * far) / (far + near - z * (far - near));
}

float getDepth() {
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    float near = 0.01;
    float far = 1.0;
    float linearDepth = 1. - linearizeDepth(depth, near, far);
    return clamp(linearDepth / far, 0., 1.);
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    vec3 result = mix(color.rgb, vec3(getDepth()), Intensity);

    fragColor = vec4(result.rgb, color.a);
    // fragColor = vec4(result.r, 0., 0., color.a);        // good solo red - black transition with far = 1.1 and near = 0.01
}