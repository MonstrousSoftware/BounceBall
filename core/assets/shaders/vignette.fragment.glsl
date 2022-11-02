#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform sampler2D u_sampler2D;

varying vec4 v_color;
varying vec2 v_texCoord0;


void main()
{
	vec4 color = texture2D(u_sampler2D, v_texCoord0);

	vec2 uv = v_texCoord0;

    uv *=  1.0 - uv.yx;   //vec2(1.0)- uv.yx; -> 1.-u.yx; Thanks FabriceNeyret !

    float vig = uv.x*uv.y * 25.0; // multiply with sth for intensity

    vig = pow(vig, 0.25); // change pow for modifying the extend of the  vignette

    color.rgb = mix(color.rgb, color.rgb*vig, 0.9);

    gl_FragColor = color;
}
