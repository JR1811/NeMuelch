#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 InSize;    // used for kernel based fsh effects
uniform vec2 OutSize;   // used for kernel based fsh effects
uniform float Intensity;
uniform float Time;
uniform float Near;
uniform float Far;

in vec2 texCoord;

out vec4 fragColor;

// This seems to somewhat work with near uniform 0.005 and far uniform 1.1

float linearizeDepth(float depth, float Near, float Far) {
    if (depth >= 0.9999) {
        return Far;
    }
    float z = depth * 2.0 - 1.0;  // Convert from [0,1] to [-1,1]
    return (2.0 * Near * Far) / (Far + Near - z * (Far - Near));
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float depth = texture(DiffuseDepthSampler, texCoord).r; // 0.7

    float linearDepth = 1. - linearizeDepth(depth, Near, Far);
    float normalizedLiNearDepth = clamp(linearDepth / Far, 0., 1.);

    vec3 result = mix(color.rgb, vec3(normalizedLiNearDepth), Intensity);

    fragColor = vec4(result, color.a);
}