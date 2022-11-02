// phong + specular + normal map + ambient


#ifdef GL_ES 
precision mediump float;
#endif


struct PointLight
{
    vec3 color;
    vec3 position;
    float intensity;
};

uniform vec3 u_cameraPosition;
uniform vec3 u_lightPosition;
uniform PointLight u_pointLights[1]; // not used
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_normalTexture;
uniform bool u_useNormalMap;
uniform float u_shininess;		//  set how?
uniform vec3 u_ambient;

varying vec2 v_texCoords;
varying vec3 v_vertPosWorld;
varying vec3 v_vertNVWorld;


varying vec3 v_lightDir;	// light vector in tangent space
varying vec3 v_viewDir;		// eye vector in tangent space

void main() {
	vec3 N = v_vertNVWorld;
	vec3  nColor = vec3(0.5);

	if(u_useNormalMap)
	{
		   nColor  		 = texture2D( u_normalTexture, v_texCoords.st ).rgb;
		   nColor.g = 1.0 - nColor.g;		// flip the green colour depending on source material
		   vec3 normal = normalize(nColor*2.0 - 1.0); // convert to range [-1.0, 1.0] and normalize
		   N = normal;
	}

    vec3  toLightVector  = u_lightPosition - v_vertPosWorld.xyz;	// vector from fragment to light source
    float D = length(toLightVector);	// distance to light source (for attenuation)
    //vec3 L = normalize(toLightVector);
    vec3 L = v_lightDir;	// normalized light vector (pointing towards light)

    float Idiffuse = max( 0.0, dot(N, L));

    //vec3 eyeV =  normalize( u_cameraPosition - v_vertPosWorld.xyz );	// vector towards the eye
    vec3 eyeV = v_viewDir;

    vec3 H = normalize( eyeV + L);	// halfway vector between light vector and eye vector
    float NdotH = max(0.0, dot(N, H));
    float kSpecular = pow( NdotH, u_shininess);

    float att =  1.0; //1.0/(.3 + 0.0*D + 0.05*D*D);		// hard coded c1, c2, c3 for attenuation
    att = clamp(att, 0.0, 1.0);

	vec4  texCol         = texture2D( u_diffuseTexture, v_texCoords.st );

	Idiffuse = 0.5;
	vec3  finalCol       = texCol.rgb * Idiffuse * att + u_ambient  + Idiffuse*kSpecular;
    //gl_FragColor         = vec4(finalCol.rgb, 1.0 );
    gl_FragColor         = vec4(vec3(texCol.rgb* Idiffuse*att+ Idiffuse*kSpecular), 1.0 );
}
