package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class TestShader implements Shader {
	public ShaderProgram program;
	Camera camera;
	RenderContext context;
	int u_projTrans;
	int u_worldTrans;
	int u_Time;
	int u_lightPosition;
	int u_cameraPosition;
	int u_shininess;
	int u_ambient;
	int u_useNormalMap;
	float time;
	ErrorLog log;
	PointLight pointLight;
	
	String SHADER_NAME = "lit_N";

	public TestShader(ErrorLog log, PointLight pointLight ) {

		this.log = log;
		this.pointLight = pointLight;
	}

	@Override
	public void init ( ) {
		String vert = Gdx.files.internal("shaders\\" + SHADER_NAME + ".vertex.glsl").readString();
		String frag = Gdx.files.internal("shaders\\" + SHADER_NAME + ".fragment.glsl").readString();
		program = new ShaderProgram(vert, frag);
		if (!program.isCompiled()) {
			log.addMessage("Shader program compile failed.");
			log.addMessage(program.getLog());
			return;
		}
		u_projTrans = program.getUniformLocation("u_projTrans");
		u_worldTrans = program.getUniformLocation("u_worldTrans");
		u_Time = program.getUniformLocation("u_Time");
		u_lightPosition = program.getUniformLocation("u_lightPosition");
		u_cameraPosition = program.getUniformLocation("u_cameraPosition");
		u_shininess = program.getUniformLocation("u_shininess");
		u_ambient = program.getUniformLocation("u_ambient");
		u_useNormalMap = program.getUniformLocation("u_useNormalMap");

	}
	
	
	@Override
	public void dispose () {
		program.dispose();
	}
	
	@Override
	public void begin (Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		
		
		// circular swinging light source
		float lpos[] = { 3.0f, 5.0f, 5.0f };

		lpos[0] = pointLight.position.x;
		lpos[1] = pointLight.position.y;
		lpos[2] = pointLight.position.z;

		
		float amb[] = { .1f, .1f, .1f} ;	// default ambient light, can be overruled by Environment ambient
		
		
		float cam[] = {0,0,0};
		
		cam[0] = camera.position.x;
		cam[1] = camera.position.y;
		cam[2] = camera.position.z;
		
		program.bind();
		program.setUniformMatrix(u_projTrans, camera.combined);
		program.setUniformf(u_Time, time);
		program.setUniform3fv(u_lightPosition, lpos, 0, 3);
		program.setUniform3fv(u_cameraPosition, cam, 0, 3);
		program.setUniformf(u_shininess, 25.0f);
		program.setUniform3fv(u_ambient, amb, 0,3);
		program.setUniformi("u_diffuseTexture", 0);
		program.setUniformi("u_normalTexture", 1);
		
		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
	}
	
	@Override
	public void render (Renderable renderable) {
		program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
		time += Gdx.graphics.getDeltaTime();
		program.setUniformf(u_Time, time);
		
		// pick up the specific texture per  model instance?
		TextureAttribute ta = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);

		// todo:  there should be a fall back for materials without a texture
		if(ta != null)
			ta.textureDescription.texture.bind(0);
		if(renderable.material.has(TextureAttribute.Normal))
		{
			ta = (TextureAttribute) renderable.material.get(TextureAttribute.Normal);
			ta.textureDescription.texture.bind(1);
			program.setUniformi(u_useNormalMap, 1);
		}
		else
			program.setUniformi(u_useNormalMap, 0);
		
		// follow Environment ambient light value
		if(renderable.environment.has(ColorAttribute.AmbientLight))
		{
			ColorAttribute ambient = (ColorAttribute) renderable.environment.get(ColorAttribute.AmbientLight);
			float amb[] = {.1f, .1f, .1f};
			amb[0] = ambient.color.r;
			amb[1] = ambient.color.g;
			amb[2] = ambient.color.b;
			program.setUniform3fv(u_ambient, amb, 0,3);
		}
		
		renderable.meshPart.render(program);
	}
	
	@Override
	public void end () {
	}
	
	@Override
	public int compareTo (Shader other) {
		return 0;
	}
	@Override
	public boolean canRender (Renderable instance) {
		return true;
	}
}  