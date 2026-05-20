#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef DISCARD_ALPHA
    uniform float m_AlphaDiscardThreshold;
#endif
uniform float m_Alpha;
uniform vec4 m_Color;
uniform sampler2D m_ColorMap;
varying vec2 texCoord;

uniform float g_Time;
uniform vec2 g_Resolution;

const float lineHeight = 5.0;
const float lineSpeed = 5.0;
const float lineOverflow = 1.4;
const float noise = 0.70;
const float pixelDensity = 450.0;
const vec3 lum = vec3(0.299, 0.587, 0.114);

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float rand2(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453) * 2.0 - 1.0;
}

float offset(float blocks, vec2 uv) {
    return rand2(vec2(fract(g_Time), floor(uv.y * blocks)));
}

void main() {
    vec4 color = vec4(1.0);
    vec2 uv = texCoord;

    #ifdef HAS_COLORMAP
        color = texture2D(m_ColorMap, uv);
    #endif

    #ifdef HAS_COLOR
        color *= m_Color;
    #endif

    // VHS dirt
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_VHS)
        vec2 pos = vec2(0.5 + 0.5 * sin(g_Time), uv.y);
        color.rgb += texture2D(m_ColorMap, pos).rgb * 0.2;
    #endif

    // Moving strip effect
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_LINE)
        float theta = uv.y * lineHeight + g_Time * lineSpeed;
        float blurLine = clamp(sin(theta) + 1.22, 0.0, 1.0);
        float line = clamp(floor(sin(theta) + 1.90), 0.0, lineOverflow);
        color.rgb = mix(color.rgb - noise * vec3(0.08), color.rgb, line);
        color.rgb = mix(color.rgb - noise * vec3(0.25), color.rgb, blurLine);
    #endif

    // Grain
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_GRAIN)
        vec2 grainUV = floor(uv * pixelDensity) + fract(g_Time);
        color.rgb *= clamp(rand(grainUV) + 1.0 - noise, 0.0, 1.0);
    #endif

    // Scanlines
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_SCANLINE)
        float scanline = sin(uv.y * g_Resolution.y) * 0.04;
        color.rgb -= scanline;
    #endif

    // Vignette
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_VIGNETTE)
        color.rgb *= 1.0 - pow(distance(uv, vec2(0.5)), 3.0) * 3.0;
    #endif

    // LCD
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_LCD)
        float scanline2 = clamp(0.95 + 0.05 * cos(3.14 * (uv.y + 0.008 * g_Time) * 240.0), 0.0, 1.0);
        float lins = 0.85 + 0.15 * clamp(1.5 * cos(3.14 * uv.x * 640.0), 0.0, 1.0);
        color.rgb *= scanline2 * lins * 1.2;
    #endif

    // CRT
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_CRT)
        float modX = mod(gl_FragCoord.x, 3.0);
        vec3 crtMask = vec3(0.1);

        if (modX < 1.0) crtMask.r = 1.0;
        else if (modX < 2.0) crtMask.g = 1.0;
        else crtMask.b = 1.0;

        color.rgb *= crtMask * 2.0;

        if (mod(gl_FragCoord.y, 3.0) < 1.0) {
            color.rgb *= 0.8;
        }
    #endif

    // Glitch
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_GLITCH)
        color.r = texture2D(m_ColorMap, uv + vec2(offset(16.0, uv) * 0.1, 0.0)).r;
        color.g = texture2D(m_ColorMap, uv + vec2(offset(8.0, uv) * 0.0166666, 0.0)).g;
        color.b = texture2D(m_ColorMap, uv + vec2(offset(8.0, uv) * 0.1, 0.0)).b;
    #endif

    // Black And White
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_BAW)
        color.rgb = vec3(dot(color.rgb, lum));
    #endif

    #ifdef DISCARD_ALPHA
        if (color.a < m_AlphaDiscardThreshold) {
            discard;
        }
    #endif

    #ifdef HAS_ALPHA
        color.a = m_Alpha;
    #endif

    gl_FragColor = color;
}
