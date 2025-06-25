package com.altf4studios.corebringer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

///Now using Screens instead of ApplicationAdapter to implement button functionality and navigation
public class MainMenuScreen implements Screen {
    //// Declaration of variables and elements here.
    private Main corebringer; /// The Master Key that holds all screens together
    private Stage mainmenustage;
    private Table mainmenutable;
    private Table gametitleandicontable;
    private Table gamestartandnavigationtable;
    private Image corebringericon;
    private TextButton startbutton;
    private TextButton optionsbutton;
    private TextButton exitbutton;
    private int debugclickingcount;
    private Label magicword;
    private boolean isDebugMessageInvisible = false;

    public MainMenuScreen(Main corebringer) {
        ///Here's all the things that will initiate upon start-up
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        mainmenustage = new Stage(new FitViewport(1280, 720)); ////Used Stage as a skeletal framework for the UI
        mainmenutable = new Table(); ////Used Table as a flesh framework for the UI

        ////Core Table parameters
        mainmenutable.setFillParent(true);
        mainmenutable.top();
        mainmenustage.addActor(mainmenutable);

        ///Magic Word Parameters
        magicword = new Label("", corebringer.testskin);
        magicword.setColor(1,1,1,0); ///Makes the Debug Message transparent
        magicword.setAlignment(Align.center);
        magicword.setWrap(true);
        magicword.setWidth(600f);
        magicword.setPosition(340f, 340f); ///Makes it so it is placed at the center
        mainmenustage.addActor(magicword);

        ////Game Title and Icon Table initialization
        gametitleandicontable = new Table();

        ////Game Title and Icon Table parameters and values
        corebringericon = new Image(new Texture("titlecard/TitleCard2.png"));

        ///Game Title and Icon Table calling
        gametitleandicontable.add(corebringericon).width(700f).height(350f).pad(10f);

        ///Game Start and Options Table initialization
        gamestartandnavigationtable = new Table();

        ///Game Start and Options Table parameters and values
        startbutton = new TextButton("Start", corebringer.testskin);
        optionsbutton = new TextButton("Options", corebringer.testskin);
        exitbutton = new TextButton("Exit", corebringer.testskin);

        ///Game Start and Options Table calling
        gamestartandnavigationtable.add(startbutton).width(250f).height(50f).pad(10f).row();
        gamestartandnavigationtable.add(optionsbutton).width(250f).height(50f).pad(10f).row();
        gamestartandnavigationtable.add(exitbutton).width(250f).height(50f).pad(10f).row();

        ///This gives function to the Options Button
        optionsbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.optionsScreen);
            }
        });

        ///This gives function to the Exit Button
        exitbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        ///This gives function to the Start Button
        startbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.corebringerstartmenubgm.stop();
                corebringer.corebringermapstartbgm.setVolume(1f);
                corebringer.corebringermapstartbgm.play();
                corebringer.setScreen(corebringer.startGameMapScreen);
            }
        });

        ///Functionality to the Game Logo for the Debug Screen!
        corebringericon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                debugclickingcount++;
                if (debugclickingcount == 19) {
                    /// Message and Duration insie the parameter, this is only a test message
                    showthemagicword("You suck COCK, gobble on BALLS and swallow CUM!!! " +
                        " - Ronald ", 2.5f);
                    debugclickingcount = 0; ///This will reset the count so not everytime you can go to Debug
                    corebringer.setScreen(corebringer.debugScreen);
                }
            }
        });

        ////Table calling here since IDE reads code per line
        mainmenutable.add(gametitleandicontable).center().padTop(20f);
        mainmenutable.row();
        mainmenutable.add(gamestartandnavigationtable).center().padTop(50f).padBottom(20f);
    }
    private void showthemagicword(String message, float duration) {
        if (isDebugMessageInvisible) return; ///This makes it so that the message won't be overlapping

        isDebugMessageInvisible = true;
        magicword.setText(message);
        magicword.clearActions();
        magicword.getColor().a = 0f; ///This makes it color white

        ///This is for the basic fade-in fade-out animation :D
        magicword.addAction(Actions.sequence(
            Actions.fadeIn(0.5f),
            Actions.delay(duration),
            Actions.fadeOut(0.5f),
            Actions.run(() -> isDebugMessageInvisible = false) ///This updates the state of the magic word
        ));
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
    }

    @Override public void resize(int width, int height) {
        mainmenustage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(mainmenustage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        mainmenustage.dispose();
    }
}
