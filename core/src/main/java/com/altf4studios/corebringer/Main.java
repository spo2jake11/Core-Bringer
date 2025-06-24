package com.altf4studios.corebringer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public Music corebringerstartmenubgm;
    public Music corebringermapstartbgm;
    public boolean isMusicMuted;
    public Skin testskin;
    public Label.LabelStyle responsivelabelstyle;
    public MainMenuScreen mainMenuScreen;
    public OptionsScreen optionsScreen;
    public StartGameMapScreen startGameMapScreen;
    public DebugScreen debugScreen;

    @Override
    public void create() {
        ///This is where music plays when the game starts
        corebringerstartmenubgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerstartmenubgm.setLooping(true);
        corebringerstartmenubgm.setVolume(1.0f);
        corebringermapstartbgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Drake - Hotline Bling (Lyrics).mp3"));
        corebringermapstartbgm.setLooping(true);
        corebringermapstartbgm.setVolume(1.0f);
        isMusicMuted = false;
        corebringerstartmenubgm.play();

        ///This is for the Skin to be declared and initialized so Screens can just call it
        testskin = new Skin(Gdx.files.internal("ui/uiskin.json")); ///Usage of sample skin, can be changed soon
        responsivelabelstyle = new Label.LabelStyle(testskin.getFont("default"), Color.WHITE);
        responsivelabelstyle.font.getData().setScale(3f);

        mainMenuScreen = new MainMenuScreen(this);
        optionsScreen = new OptionsScreen(this);
        startGameMapScreen = new StartGameMapScreen(this);
        debugScreen = new DebugScreen(this);
        setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        super.render();

        ///This is to give function to the F11 key
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }
    }

    ///This method makes the F11 key to work properly for the game to achieve true fullscreen
    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            ///This is for when F11 is pressed the second time so it returns back to windowed mode
            Gdx.graphics.setWindowedMode(1280, 720);
        } else {
            ///This is for when F11 is pressed for the first time, with the game adapting the monitor resolution
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    @Override public void pause() {
        if (!isMusicMuted && corebringerstartmenubgm.isPlaying()) {
            corebringerstartmenubgm.pause();
        } else if (!isMusicMuted && corebringermapstartbgm.isPlaying()) {
            corebringermapstartbgm.pause();
        }
    }
    @Override public void resume() {
        super.resume();
        if (!isMusicMuted) {
            if (getScreen() == mainMenuScreen && !corebringerstartmenubgm.isPlaying()) {
                corebringerstartmenubgm.play();
            } else if (getScreen() == startGameMapScreen && !corebringermapstartbgm.isPlaying()) {
                corebringermapstartbgm.play();
            }
        }
    }

    @Override
    public void dispose() {
        if (corebringerstartmenubgm != null) {
            corebringerstartmenubgm.dispose();
        }
        if (corebringermapstartbgm != null) {
            corebringermapstartbgm.dispose();
        }
    }
    @Override public void resize(int width, int height) {
        super.resize(width, height);
    }
}
