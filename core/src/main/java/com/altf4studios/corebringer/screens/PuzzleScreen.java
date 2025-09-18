package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PuzzleScreen implements Screen {
    private final Main corebringer;
    private Stage stage;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private TextButton backButton;

    public PuzzleScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));

        backgroundTexture = new Texture(Utils.getInternalPath("assets/Puzzle/stonePuzzleBG.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setScaling(Scaling.stretch);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table uiTable = new Table();
        uiTable.setFillParent(true);

        backButton = new TextButton("Back", corebringer.testskin);
        backButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });

        uiTable.top().left().pad(15f);
        uiTable.add(backButton).width(120f).height(40f);
        stage.addActor(uiTable);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}


