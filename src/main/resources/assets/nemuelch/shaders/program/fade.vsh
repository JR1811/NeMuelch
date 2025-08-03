#version 150

uniform mat4 ProjMat;

in vec4 Position;

out vec2 texCoord;

void main() {
    gl_Position = ProjMat * vec4(Position.xy, 0.0, 1.0);
    texCoord = (Position.xy + 1.0) * 0.5;
}