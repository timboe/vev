#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

// 0.25f / (spread * scale)
const float smoothing = 0.25/(4.0 * 25.0);

const float outlineDistance = 0.365; // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline
const vec4 outlineColor = vec4(39./255., 32./255., 49./255., 1.);

// Main with outline
void main() {
    float distance = texture2D(u_texture, v_texCoord).a;
    float outlineFactor = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    vec4 color = mix(outlineColor, v_color, outlineFactor);
    float alpha = smoothstep(outlineDistance - smoothing, outlineDistance + smoothing, distance);
    gl_FragColor = vec4(color.rgb, color.a * alpha);
}