#version 150

uniform sampler2D DiffuseSampler;
uniform float fadeAmount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    fragColor = mix(color, vec4(0.0, 0.0, 0.0, 1.0), fadeAmount);
    // fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}