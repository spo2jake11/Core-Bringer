package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.altf4studios.corebringer.utils.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import java.util.ArrayList;
import java.util.Random;


public class MerchantScreen implements Screen{
    private Main corebringer;
    private Stage coremerchantscreenstage;
    private Table coremerchantscreentable;
    private Table underconstructiontable;
    private Label underconstructionlabel;
    private TextButton backtomapbutton;

    public MerchantScreen (Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        coremerchantscreenstage = new Stage(new FitViewport(1280, 720));
        coremerchantscreentable = new Table();
        coremerchantscreentable.setFillParent(true);
        coremerchantscreenstage.addActor(coremerchantscreentable);

        ///This is where items in the table are declared and initialized
        underconstructiontable = new Table();
        underconstructionlabel = new Label("Under Construction!", corebringer.testskin);

        backtomapbutton = new TextButton("Back to Map?", corebringer.testskin);

        ///Functionality for the Back to Map button
        backtomapbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (corebringer.gameMapScreen != null) {
                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                }
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });

        ///This is where the items in the First Table is called
        underconstructiontable.add(underconstructionlabel).padBottom(50f).row();
        underconstructiontable.add(backtomapbutton).padBottom(50f).row();

        ///This is where the first table is called to the core table
        coremerchantscreentable.add(underconstructiontable);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(coremerchantscreenstage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        coremerchantscreenstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        coremerchantscreenstage.draw();
    }

    @Override public void resize(int width, int height) {
        coremerchantscreenstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(coremerchantscreenstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        coremerchantscreenstage.dispose();
    }
}
