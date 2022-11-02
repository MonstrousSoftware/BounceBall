package com.monstrous.bounceball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
	private static float GRAVITY = -5f;

	private PerspectiveCamera cam;
	private Environment environment;
	private ModelBatch modelBatch;
	private CameraInputController camController;
	private FrameBuffer fbo = null;
	private SpriteBatch batch;
	private ShaderProgram program;
	private int viewHeight, viewWidth;
	private PointLight pointLight;
	private ErrorLog log;
	private FrameRate frameRateCounter;
	private World world;
	private TestShader shader;

	@Override
	public void create () {

		log = new ErrorLog();

		viewWidth = Gdx.graphics.getWidth();
		viewHeight = Gdx.graphics.getHeight();

		cam = new PerspectiveCamera(67, viewWidth, viewHeight);
		cam.position.set(10f, 5f, 5f);
		cam.lookAt(0, 0, 0);
		cam.near = .1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
		environment.add(pointLight = new PointLight().set(1, 1, 1, 3, 10, 5, 100));    // r,g,b, x,y,z, intensity

		world = new World();
		world.init();

		modelBatch = new ModelBatch();

		batch = new SpriteBatch();

		shader = new TestShader( log, pointLight );
		shader.init();
		if(!shader.program.isCompiled())
			shader = null;	// revert to default so that at least the error log will be visible


		// full screen post processing shader
		// vignette effect, i.e. darkened screen corners
		program = new ShaderProgram(
				Gdx.files.internal("shaders\\vignette.vertex.glsl"),
				Gdx.files.internal("shaders\\vignette.fragment.glsl"));

		if (!program.isCompiled()) {
			log.addMessage("Shader program compile failed.");
			log.addMessage(program.getLog());
		}
		ShaderProgram.pedantic = false;		// suppress warning about missing u_texture uniform in shader

		frameRateCounter = new FrameRate();
	}


	@Override
	public void resize (int width, int height) {
		// adjust aspect ratio after a windows resize
		viewWidth = width;
		viewHeight = height;
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);  // to ensure the fbo is rendered to the full window after a resize

		// cannot resize fbo, so create a new one
		if(fbo != null)
		{
			fbo.dispose();
			fbo = null; // let garbage collector delete it, next render() call will create a new one
		}
		frameRateCounter.resize(width, height);
		log.resize(width, height);
	}




	@Override
	public void render () {

		world.update( Gdx.graphics.getDeltaTime() );

		camController.update();

		if(fbo == null)
			fbo = new FrameBuffer(Pixmap.Format.RGBA8888, viewWidth, viewHeight, true);	// need to be powers of 2?

		// render the scene to a frame buffer object so that we can apply some post processing
		//
		fbo.begin();	// render to a frame buffer

		Gdx.gl.glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
		Gdx.gl.glClearColor(0.9f,  0.9f,  0.9f,  1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		for (ModelInstance instance : world.instances) {
			modelBatch.render(instance, environment, shader);
		}
		modelBatch.end();

		fbo.end(0,0,viewWidth, viewHeight);

		// now put the frame buffer on screen
		// applying full screen shader as desired

		Sprite s = new Sprite(fbo.getColorBufferTexture());
		s.flip(false,  true); // coordinate system in buffer differs from screen

		batch.begin();
		batch.setShader(program);						// post-processing shader
		batch.draw(s,  0,  0, viewWidth, viewHeight);	// draw frame buffer as screen filling texture
		batch.end();
		batch.setShader(null);

		// show fps value
		frameRateCounter.update();
		frameRateCounter.render();

		log.render();

	}

	@Override
	public void dispose () {
		modelBatch.dispose();

		batch.dispose();
		fbo.dispose();
		frameRateCounter.dispose();
		log.dispose();
		world.dispose();
		shader.dispose();
	}

}
