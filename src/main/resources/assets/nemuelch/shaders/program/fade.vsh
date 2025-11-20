#version 150

in vec3 Position;

uniform mat4 ProjMat;
uniform vec2 OutSize;

uniform vec3 CameraPos;

out vec2 texCoord;

void main() {
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    float dist = distance(Position, CameraPos);

    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;
}