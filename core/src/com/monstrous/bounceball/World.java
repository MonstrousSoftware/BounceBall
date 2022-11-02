package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class World implements Disposable {
    private static float GRAVITY = -18f;
    private static float START_HEIGHT = 20f;
    private static float MAX_TILT = 10f;    // degrees

    private Model modelBall, modelTile;
    private ModelInstance ball;
    private ModelInstance tiles[];
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    private Texture texture, textureNormal;
    private float rotX, rotZ;		// tilt of tiles in X and Z direction
    private Vector3 tileNormal;
    private Vector3 ballPosition;
    private Vector3 ballVelocity;
    private Vector3 tmpV = new Vector3();
    private float time;

    public void init() {
        String TEX_PREFIX = "textures\\";

        Texture textureBall = new Texture(Gdx.files.internal(TEX_PREFIX + "solid_ball.png"), false);
        Texture textureBlack = new Texture(Gdx.files.internal(TEX_PREFIX + "solid_black.png"), false);

        Texture textureTile = new Texture(Gdx.files.internal(TEX_PREFIX + "204.jpg"), true);
        textureTile.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        Texture textureTileN = new Texture(Gdx.files.internal(TEX_PREFIX + "204_norm.jpg"), true);
        textureTileN.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        Texture textureWall = new Texture(Gdx.files.internal(TEX_PREFIX + "243.jpg"), true);
        textureTile.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        Texture textureWallN = new Texture(Gdx.files.internal(TEX_PREFIX + "243_norm.jpg"), true);
        textureWallN.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        // todo these should all be disposed

        ModelBuilder modelBuilder = new ModelBuilder();

        // create models


        modelBall = modelBuilder.createSphere(1.5f, 1.5f, 1.5f, 16, 16,
                //new Material(ColorAttribute.createDiffuse(Color.ORANGE)),
                new Material(TextureAttribute.createDiffuse(textureBall)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        modelTile = modelBuilder.createBox(4f, 0.2f, 4f,
                new Material(TextureAttribute.createNormal(textureTileN), TextureAttribute.createDiffuse(textureTile)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Model modelFloor = modelBuilder.createBox(40f, 1f, 40f,
                //new Material(ColorAttribute.createDiffuse(Color.BLACK)),
                new Material(TextureAttribute.createDiffuse(textureBlack)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Model modelWall = modelBuilder.createBox(8f, 8f, 1f,
                new Material(TextureAttribute.createNormal(textureWallN), TextureAttribute.createDiffuse(textureWall)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);


        // create and position model instances

        // tiles
        tiles = new ModelInstance[100];
        int i = 0;
        for (int x = -18; x <= 18; x += 4) {
            for (int z = -18; z <= 18; z += 4) {
                tiles[i] = new ModelInstance(modelTile, x, -1, z);
                instances.add( tiles[i]);
                i++;
            }
        }

        // walls
        ModelInstance wall;
        for (int x = -16; x <= 16; x += 8) {
            wall = new ModelInstance(modelWall, x, 0, -20.5f);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
            wall = new ModelInstance(modelWall, x, 0, 20.5f);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
        }

        for (int z = -16; z <= 16; z += 8) {
            wall = new ModelInstance(modelWall, 20.5f, 0, z);

            wall.transform.rotate(Vector3.Y, 90);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
            wall = new ModelInstance(modelWall, -20.5f, 0, z);

            wall.transform.rotate(Vector3.Y, 90);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
        }


        ball = new ModelInstance(modelBall,0, 5, 0);
        instances.add(ball);

        ModelInstance floor = new ModelInstance(modelFloor, 0, -3, 0);
        instances.add(floor);

        rotX = 0;
        rotZ = 0;
        tileNormal = new Vector3( 0, 1, 0);
        ballPosition = new Vector3(0,START_HEIGHT,0);
        ballVelocity = new Vector3(0,0,0);
        time = 0;
    }

    public void update( float deltaTime ) {
        time += deltaTime;

        float tiltSpeed = 80f;

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && rotX < MAX_TILT)
            rotX += tiltSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rotX > -MAX_TILT)
            rotX -= tiltSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && rotZ < MAX_TILT)
            rotZ += tiltSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && rotZ > -MAX_TILT)
            rotZ -= tiltSpeed * deltaTime;

        // tilt all the tiles in X and Z direction
        for (int i = 0; i < tiles.length; i++) {
            ModelInstance instance = tiles[i];

            instance.transform.setFromEulerAngles(0,rotX, rotZ);
            float z = (i / 10) * 4 -18;
            float x = (i % 10) * 4 -18;
            instance.transform.setTranslation(x, 0, z);
        }

        // calculate tile normal vector
        tileNormal.set(0,1,0);
        tileNormal.rot(tiles[0].transform);



        if(ballPosition.y <= 0) {// bounce on ground


            if(ballPosition.x < -20 || ballPosition.x > 20 || ballPosition.z < -20 || ballPosition.z > 20 ) // ball lands on ground outside court?
            {
                ballPosition.set(0,START_HEIGHT,0); // new ball
                ballVelocity.set(0,0,0);
            }
            else {

                ballVelocity.scl(-1f);

                // reflect vector from plane: r = 2(d.n)n - d
                float dot = ballVelocity.dot(tileNormal);
                tmpV.set(tileNormal);
                tmpV.scl(2 * dot);
                tmpV.sub(ballVelocity);
                ballVelocity.set(tmpV);
            }
        }

        ballVelocity.y += GRAVITY * deltaTime;
        tmpV.set(ballVelocity);
        tmpV.scl(deltaTime);
        ballPosition.add(tmpV);
        ball.transform.setTranslation(ballPosition);
    }

    @Override
    public void dispose () {

		modelBall.dispose();
		modelTile.dispose();
		texture.dispose();
    }
}
