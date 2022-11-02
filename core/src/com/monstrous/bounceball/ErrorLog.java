package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

// class to display error log on screen (debug option)

public class ErrorLog implements Disposable {
    private BitmapFont font;
    private SpriteBatch batch;
    private OrthographicCamera cam;
    private Array<String> errors;           // todo keep only the last N messages


    public ErrorLog() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        errors = new Array<>();
    }

    public void addMessage( String message ){
        errors.add(message);
    }

    public void resize(int screenWidth, int screenHeight) {
        cam = new OrthographicCamera(screenWidth, screenHeight);
        cam.translate(screenWidth / 2, screenHeight / 2);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
    }


    public void render() {
        batch.begin();
        int y = Gdx.graphics.getHeight() - 4*(int)font.getLineHeight();
        for(int i = errors.size-1; i > errors.size - 20; i--) {
            if(i < 0)
                break;
            font.draw(batch, errors.get(i), 3, y);
            y += font.getLineHeight();
            if(y < 0 )
                break;
        }
        batch.end();
    }

    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
