package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class World implements Disposable {
    private static float GRAVITY = -18f;
    private static float START_HEIGHT = 14f;
    private static int BALL_TRAIL_SIZE = 12;

    private Array<Model> models;
    private Array<Texture> textures;
    public Array<ModelInstance> instances;

    private ModelInstance ball;
    private ModelInstance tiles[];
    private ModelInstance targets[];
    private ModelInstance ballTrail[];

    private Vector3 tileNormal;
    private Vector3 ballPosition;
    private Vector3 ballVelocity;
    private Vector3 tmpV = new Vector3();
    private BoundingBox bbox;
    private float trailTimer;       // to time the period between trail ghost balls
    private Score score;              // for call back e.g. updateScore()
    private WorldController controller;

    public World(Score score, WorldController controller) {
        this.score = score;
        this.controller = controller;

        init();
    }

    private void init() {
        String TEX_PREFIX = "textures\\";

        // create and position model instances
        // all model instances are added to 'instances' array for rendering
        // some instances are also stored in specific arrays or variables for manipulation (e.g. 'ball')
        //
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model;
        Texture textureDiffuse, textureNormal;
        models = new Array<>(); // keep array of all models to dispose them at shutdown (rather than use lots of variables)
        textures = new Array<>(); // keep array of all textures to dispose them at shutdown
        instances = new Array<ModelInstance>();

        // tiles
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "204.jpg"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);
        textureNormal = new Texture(Gdx.files.internal(TEX_PREFIX + "204_norm.jpg"), true);
        textureNormal.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureNormal);

        model = modelBuilder.createBox(4f, 0.2f, 4f,
                new Material(TextureAttribute.createNormal(textureNormal), TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);  // for later disposal

        tiles = new ModelInstance[100]; // 10x10
        int i = 0;
        for (int x = -18; x <= 18; x += 4) {
            for (int z = -18; z <= 18; z += 4) {
                tiles[i] = new ModelInstance(model, x, -1, z);
                instances.add( tiles[i]);
                i++;
            }
        }

        // walls
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "243.jpg"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);
        textureNormal = new Texture(Gdx.files.internal(TEX_PREFIX + "243_norm.jpg"), true);
        textureNormal.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureNormal);

        model = modelBuilder.createBox(8f, 8f, 1f,
                new Material(TextureAttribute.createNormal(textureNormal), TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);

        ModelInstance wall;
        for (int x = -16; x <= 16; x += 8) {
            wall = new ModelInstance(model, x, 0, -20.5f);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
            wall = new ModelInstance(model, x, 0, 20.5f);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
        }

        for (int z = -16; z <= 16; z += 8) {
            wall = new ModelInstance(model, 20.5f, 0, z);

            wall.transform.rotate(Vector3.Y, 90);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
            wall = new ModelInstance(model, -20.5f, 0, z);

            wall.transform.rotate(Vector3.Y, 90);
            wall.transform.rotate(Vector3.Z, 90);
            instances.add(wall);
        }

        // floor below the tiles
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "solid_black.png"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);

        model = modelBuilder.createBox(40f, 1f, 40f,
                //new Material(ColorAttribute.createDiffuse(Color.BLACK)),
                new Material(TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);


        ModelInstance floor = new ModelInstance(model, 0, -3, 0);
        instances.add(floor);

        // targets
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "209.jpg"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);
        textureNormal = new Texture(Gdx.files.internal(TEX_PREFIX + "209_norm.jpg"), true);
        textureNormal.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureNormal);

        model = modelBuilder.createBox(2f, 2f, 2f,
                new Material(TextureAttribute.createNormal(textureNormal), TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);

        targets = new ModelInstance[2];
        targets[0] = new ModelInstance(model, -10, 8, 10);
        instances.add( targets[0]);
        targets[1] = new ModelInstance(model, 10, 8, -10);
        instances.add( targets[1]);
//        targets[2] = new ModelInstance(model, 0, 8, 0);
//        instances.add( targets[2]);


        // trail of ghost balls
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "solid_blue.png"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);

        model = modelBuilder.createSphere(1.5f, 1.5f, 1.5f, 4, 4,
                new Material(TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);
        ballTrail = new ModelInstance[BALL_TRAIL_SIZE];
        for (i = 0; i < BALL_TRAIL_SIZE; i++) {
                ballTrail[i] = new ModelInstance(model, 0, -10, 0); // start in hidden location
                float scale = 0.2f + 0.8f*(BALL_TRAIL_SIZE-i)/BALL_TRAIL_SIZE;      // diminishing scale from 1.0 to 0.2
                ballTrail[i].transform.setToScaling(scale, scale, scale);
                instances.add( ballTrail[i]);
        }

        // ball
        textureDiffuse = new Texture(Gdx.files.internal(TEX_PREFIX + "solid_ball.png"), true);
        textureDiffuse.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textures.add(textureDiffuse);

        model = modelBuilder.createSphere(1.5f, 1.5f, 1.5f, 16, 16,
                new Material(TextureAttribute.createDiffuse(textureDiffuse)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        models.add(model);
        ball = new ModelInstance(model,0, 5, 0);
        instances.add(ball);

        // general variables

//        rotX = 0;
//        rotZ = 0;
        tileNormal = new Vector3( 0, 1, 0);
        ballPosition = new Vector3(0,START_HEIGHT,0);
        ballVelocity = new Vector3(0,0,0);
        bbox = new BoundingBox();
        score.setScore(0);
        trailTimer = 0;

    }


    public void update( float deltaTime ) {
        controller.update(deltaTime);

        float rotX = controller.rotX;
        float rotZ = controller.rotZ;

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


        // update trail that follows ball trajectory
        trailTimer += deltaTime;        // show a ghost ball at evenly spaced time intervals
        if(trailTimer > 0.1f) {
            trailTimer = 0;

            for (int i = BALL_TRAIL_SIZE-2; i >= 0; i--) {
                ballTrail[i].transform.getTranslation(tmpV);
                ballTrail[i + 1].transform.setTranslation(tmpV);
            }
            ballTrail[0].transform.setTranslation(ballPosition);
        }

        // collision detection with targets
        for(int i = 0; i < targets.length; i++) {
            ModelInstance target = targets[i];

            target.calculateBoundingBox(bbox);      // to cache?
            bbox.mul(target.transform);
            if(bbox.contains(ballPosition)){
                // collision ball and target box
                target.transform.setTranslation(0,-10,0);       // hide below court
                score.setScore( score.getScore() + 1 );
            }
        }

        // update ball position
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
        for(Model model : models)
            model.dispose();
        for(Texture texture: textures )
            texture.dispose();
    }
}
