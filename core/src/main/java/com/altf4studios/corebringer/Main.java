package com.altf4studios.corebringer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    //// Declaration of variables and elements here.
    private Stage corestage;
    private Table coretable;
    private Table gametitleandicontable;
    private Table gamestartandnavigationtable;
    private SpriteBatch batch;
    private Texture image;
    private Label gametitle;
    private Skin testskin;
    private TextButton startbutton;
    private TextButton optionsbutton;
    private TextButton exitbutton;

    @Override
    public void create() {
        ///Here's all the things that will initiate upon start-up
        batch = new SpriteBatch();
        image = new Texture("corebringercoffee.png");
        corestage = new Stage(); ////Used Stage as a skeletal framework for the UI
        coretable = new Table(); ////Used Table as a flesh framework for the UI
        testskin =  new Skin(Gdx.files.internal("ui/uiskin.json")); ////Usage of Sample Skin, can be changed soon

        ////Core Table parameters
        coretable.setFillParent(true);
        coretable.top();
        corestage.addActor(coretable);

        ////Game Title and Icon Table initialization
        gametitleandicontable = new Table();

        ////Game Title and Icon Table parameters and values
        Image img = new Image(new Texture("corebringercoffee.png"));
        gametitle = new Label("CORE BRINGER: A JAVA LEARNING ROGUELIKE GAME!", testskin);
        gametitle.setWrap(true);
        gametitle.setFontScale(2f);

        gametitleandicontable.add(img).pad(50f).expandX().right();
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

        ////Table calling here since IDE reads code per line
        coretable.add(gametitleandicontable).center().padTop(20f);
        coretable.row();
        coretable.add(gamestartandnavigationtable).center().padTop(50f).padBottom(20f);

        Gdx.input.setInputProcessor(corestage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        corestage.act(Gdx.graphics.getDeltaTime()); ////Used to call the Stage and render the elements that is inside it
        corestage.draw();
    }

    @Override
    public void dispose() {
        corestage.dispose();
        image.dispose();
    }
}
