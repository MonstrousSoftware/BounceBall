package com.monstrous.bounceball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class GUI implements Disposable {

    public Stage stage;
    private Skin skin;
    private Label labelScore;
    private Score score;
    private int scoreValue;

    public GUI( Score score ) {
        this.score = score;

        // GUI elements via Stage class
        skin = new Skin(Gdx.files.internal("sgx.skin/sgx-ui.json"));
        stage = new Stage(new ScreenViewport());

        rebuild();
    }

    public void rebuild() {

        stage.clear();

        // root table that fills the whole screen
        Table screenTable = new Table();
        stage.addActor(screenTable);
        screenTable.setFillParent(true);        // size to match stage size

        labelScore = new Label("...", skin);

        screenTable.add(labelScore).top().expandY();

        screenTable.pack();
        scoreValue = -1;    // to trigger update
    }

    public void render( float delta ) {
        if(score.getScore() != scoreValue ) {   // has score changed? (check to avoid calling String.format at every frame)
            scoreValue = score.getScore();
            labelScore.setText(String.format("SCORE: %d", scoreValue));
        }

        stage.act(delta);
        stage.draw();
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose () {
        stage.dispose();
    }
}
