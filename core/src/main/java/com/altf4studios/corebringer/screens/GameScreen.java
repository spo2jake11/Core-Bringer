package com.altf4studios.corebringer.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {

    private Main corebringer;
    private Stage gameStage;
    private Table mainTable;
    private Table battleTable;
    private Table editorTable;
    private Table cardSlotTable;
    private Label battleLabel;
    private Label editorLabel;
    private Label cardLabel;


    public GameScreen(Main game){
        this.corebringer = game;
        gameStage = new Stage(new FitViewport(1280, 720));
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();
        gameStage.addActor(mainTable);

        battleTable = new Table();
        battleLabel = new Label("This is battle area", corebringer.testskin);
        battleTable.add(battleLabel);
        battleTable.setColor(Color.BLUE);
        battleTable.debugTable();


        editorTable = new Table();
        editorTable.setColor(Color.CORAL);
        editorLabel = new Label("This is the editor area", corebringer.testskin);
        editorTable.add(editorLabel);
        editorTable.debugTable();


        cardSlotTable = new Table();
        cardSlotTable.setColor(Color.GOLD);
        cardLabel = new Label("This is the card area", corebringer.testskin);
        cardSlotTable.add(cardLabel);
        cardSlotTable.debugTable();

        mainTable.add(battleTable).width(mainTable.getWidth()).height(300).center().row();
        mainTable.add(cardSlotTable).width(mainTable.getWidth()).height(50).center().row();
        mainTable.add(editorTable).width(mainTable.getWidth()).height(300).center().row();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        gameStage.act(delta);
        gameStage.getBatch().begin();
        gameStage.getBatch().end();
        gameStage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
