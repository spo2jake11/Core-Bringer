package com.altf4studios.corebringer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

///Now using Screens instead of ApplicationAdapter to implement button functionality and navigation
public class MainMenuScreen implements Screen {
    //// Declaration of variables and elements here.
    private Main corebringer;
    private Stage mainmenustage;
    private Table mainmenutable;
    private Table gametitleandicontable;
    private Table gamestartandnavigationtable;
    private Label gametitle;
    private Skin testskin;
    private Image corebringericon;
    private TextButton startbutton;
    private TextButton optionsbutton;
    private TextButton exitbutton;

    public MainMenuScreen(Main corebringer) {
        ///Here's all the things that will initiate upon start-up
        this.corebringer = corebringer;
        mainmenustage = new Stage(new ScreenViewport()); ////Used Stage as a skeletal framework for the UI
        mainmenutable = new Table(); ////Used Table as a flesh framework for the UI
        testskin =  new Skin(Gdx.files.internal("ui/uiskin.json")); ////Usage of Sample Skin, can be changed soon

        ////Core Table parameters
        mainmenutable.setFillParent(true);
        mainmenutable.top();
        mainmenustage.addActor(mainmenutable);

        ////Game Title and Icon Table initialization
        gametitleandicontable = new Table();

        ////Game Title and Icon Table parameters and values
        corebringericon = new Image(new Texture("corebringercoffee.png"));
        gametitle = new Label("CORE BRINGER!", testskin);
        gametitle.setWrap(true);
        gametitle.setFontScale(2f);

        gametitleandicontable.add(corebringericon).pad(50f).expandX().right();
        gametitleandicontable.add(gametitle).width(300f).padTop(50f).expandX().left();

        ///Game Start and Options Table initialization
        gamestartandnavigationtable = new Table();

        ///Game Start and Options Table parameters and values
        startbutton = new TextButton("Start", testskin);
        optionsbutton = new TextButton("Options", testskin);
        exitbutton = new TextButton("Exit", testskin);

        gamestartandnavigationtable.add(startbutton).width(250f).height(50f).pad(10f).row();
        gamestartandnavigationtable.add(optionsbutton).width(250f).height(50f).pad(10f).row();
        gamestartandnavigationtable.add(exitbutton).width(250f).height(50f).pad(10f).row();

        optionsbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(new OptionsScreen(corebringer));
            }
        });

        exitbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        ////Table calling here since IDE reads code per line
        mainmenutable.add(gametitleandicontable).center().padTop(20f);
        mainmenutable.row();
        mainmenutable.add(gamestartandnavigationtable).center().padTop(50f).padBottom(20f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mainmenustage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        mainmenustage.act(delta); ////Used to call the Stage and render the elements that is inside it
        mainmenustage.draw();

        corebringer.brightnessoverlaystage.act(delta);
        corebringer.brightnessoverlaystage.draw();
    }

    @Override public void resize(int width, int height) {

    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
        corebringer.brightnessoverlay.clear();
    }

    @Override
    public void dispose() {
        mainmenustage.dispose();
    }
}
