#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

// 0.25f / (spread * scale)
const float smoothing = 0.25/(4.0 * 1.0);

void main() {
    float distance = texture2D(u_texture, v_texCoord).a;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    gl_FragColor = vec4(v_color.rgb, v_color.a * alpha);
}