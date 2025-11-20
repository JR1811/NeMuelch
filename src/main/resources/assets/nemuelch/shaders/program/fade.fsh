#version 150

uniform sampler2D DiffuseSampler;
uniform float FadeAmount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(DiffuseSampler, texCoord) * vec4(FadeAmount, FadeAmount, FadeAmount, 1.0);
}