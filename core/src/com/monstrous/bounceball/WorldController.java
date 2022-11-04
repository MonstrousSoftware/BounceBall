package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Cursor;

public class WorldController extends InputAdapter {

    private static float MAX_TILT = 10f;    // degrees

    // key state
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;

    // controlled variables
    public float rotX;
    public float rotZ;


    public WorldController() {
        rotX = 0;
        rotZ = 0;
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
    }

    public void update( float deltaTime ) {
        // use the key state variables and the delta time to update the controlled variables

        float tiltSpeed = 20f;
        if(leftPressed && rotX < MAX_TILT)
            rotX += tiltSpeed * deltaTime;
        if(rightPressed && rotX > -MAX_TILT)
            rotX -= tiltSpeed * deltaTime;
        if(upPressed && rotZ < MAX_TILT)
            rotZ += tiltSpeed * deltaTime;
        if(downPressed && rotZ > -MAX_TILT)
            rotZ -= tiltSpeed * deltaTime;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.LEFT)
            leftPressed = true;
        if(keycode == Input.Keys.RIGHT)
            rightPressed = true;
        if(keycode == Input.Keys.UP)
            upPressed = true;
        if(keycode == Input.Keys.DOWN)
            downPressed = true;
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.LEFT)
            leftPressed = false;
        if(keycode == Input.Keys.RIGHT)
            rightPressed = false;
        if(keycode == Input.Keys.UP)
            upPressed = false;
        if(keycode == Input.Keys.DOWN)
            downPressed = false;
        return super.keyUp(keycode);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

//        rotX = -MAX_TILT * (screenX - 0.5f * Gdx.graphics.getWidth())/ Gdx.graphics.getWidth();
//        rotZ = -MAX_TILT * (screenY - 0.5f * Gdx.graphics.getHeight())/ Gdx.graphics.getHeight();

        return super.mouseMoved(screenX, screenY);
    }
}
