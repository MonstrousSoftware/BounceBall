// TBN matrix for normal map rendering
// https://learnopengl.com/Advanced-Lighting/Normal-Mapping
// https://gist.github.com/xoppa/9766698


#ifdef GL_ES
precision mediump float;
#endif

attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

// i don't think these are filled
//attribute vec3 a_tangent;
//attribute vec3 a_bitangent;

uniform mat4 u_worldTrans;
uniform mat4 u_world;
uniform mat4 u_projTrans;
uniform mat3 u_normalMatrix;

uniform vec3 u_cameraPosition;
uniform vec3 u_lightPosition;


varying vec2 v_texCoords;
varying vec3 v_vertPosWorld;
varying vec3 v_vertNVWorld;
//varying vec3 v_tangent;
//varying vec3 v_bitangent;

varying vec3 v_pos;
varying vec3 v_lightDir;
varying vec3 v_viewDir;
//varying mat3 TBN;

vec3 g_normal = vec3(0,1.0,0);
vec3 g_binormal= vec3(0,0,1.0);
vec3 g_tangent= vec3(1.0,0,0);

vec3 biggestAngle(const in vec3 base, const in vec3 v1, const in vec3 v2) {
	vec3 c1 = cross(base, v1);
	vec3 c2 = cross(base, v2);
	return (dot(c2, c2) > dot(c1, c1)) ? c2 : c1;
}

void calculateTangentVectors() {
	g_binormal = normalize(cross(g_normal, biggestAngle(g_normal, vec3(1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0))));
	g_tangent = normalize(cross(g_normal, g_binormal));
}


void main() {
	vec4 vertPos   = u_worldTrans * a_position;
    v_vertPosWorld = vertPos.xyz;

	v_texCoords    = a_texCoord0;

	g_normal = a_normal;
	calculateTangentVectors();

	mat3 worldToTangent;

	// calculate transform matrix from world to tangent space (i.e. in the plane of the texture).
	worldToTangent[0] = g_tangent;
	worldToTangent[1] = g_binormal;
	worldToTangent[2] = g_normal;

	// calculate light vector and eye vector in wo4rld space
	// transform light and eye vector from world space to tangent space
	// fragment shader will calculate lighting in tangent space

	v_lightDir = normalize(u_lightPosition - v_vertPosWorld.xyz) * worldToTangent; // normalized light vector (pointing towards light) in tangent space
    v_viewDir = normalize( u_cameraPosition - v_vertPosWorld.xyz ) * worldToTangent; // unit vector towards the eye in tangent space

	gl_Position = u_projTrans * vertPos;
}

