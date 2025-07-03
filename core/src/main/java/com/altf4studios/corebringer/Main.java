package com.altf4studios.corebringer;

import com.altf4studios.corebringer.screens.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public Music corebringerbgm;
    public Music corebringerstartmenubgm;
    public Music corebringermapstartbgm;
    public boolean isMusicMuted;
    public Skin testskin;
    public Label.LabelStyle responsivelabelstyle;
    public MainMenuScreen mainMenuScreen;
    public OptionsScreen optionsScreen;
    public GameScreen gameScreen;
    public StartGameMapScreen startGameMapScreen;
    public DebugScreen debugScreen;
    private AssetManager assetManager;
    @Override
    public void create() {
        //AssetManager is located here
        assetManager =new AssetManager();
//<<<<<<< HEAD
        // TextArea, Texture, TextureAtlas class shows up, do test all three types to see what's useabl
//=======
        // TextArea, Texture, TextureAtlas class shows up, do test all three types to see what's useable
        assetManager.load("atlas/enemies/runicBoss_atlas.atlas", TextureAtlas.class);
//>>>>>>> b66f6915176fb407bda3b55c016f26f41ac3da7a
        assetManager.finishLoading();

        ///This is where music plays when the game starts
        corebringerbgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerbgm.setLooping(true);
        corebringerbgm.setVolume(1.0f);
        corebringerstartmenubgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerstartmenubgm.setLooping(true);
        corebringerstartmenubgm.setVolume(1.0f);
        corebringermapstartbgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Drake - Hotline Bling (Lyrics).mp3"));
        corebringermapstartbgm.setLooping(true);
        corebringermapstartbgm.setVolume(1.0f);

        isMusicMuted = false;
//        corebringerbgm.play();
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
        super.pause();
//        if (!isMusicMuted && corebringerbgm.isPlaying()) {
//            corebringerbgm.stop();
//        }
        if (!isMusicMuted && corebringerstartmenubgm.isPlaying()) {
            corebringerstartmenubgm.pause();
        }
        if (!isMusicMuted && corebringermapstartbgm.isPlaying()) {
            corebringermapstartbgm.pause();
        }
    }
    @Override public void resume() {
        super.resume();
//        if (!isMusicMuted && !corebringerbgm.isPlaying()) {
//            corebringerbgm.play();
//        }
        if (!isMusicMuted) {
            if (getScreen().equals(mainMenuScreen) && !corebringerstartmenubgm.isPlaying()) {
                corebringerstartmenubgm.play();
                isMusicMuted = false;
            }
            if (getScreen().equals(startGameMapScreen) && !corebringermapstartbgm.isPlaying()) {
                corebringermapstartbgm.play();
                isMusicMuted = false;
            }
        }
    }

    @Override
    public void dispose() {
//        if (corebringerbgm != null) {
//            corebringerbgm.dispose();
//        }
//        if (corebringerstartmenubgm != null) {
//            corebringerstartmenubgm.dispose();
//        }
//        if (corebringermapstartbgm != null) {
//            corebringermapstartbgm.dispose();
//        }
    }
    @Override public void resize(int width, int height) {
        super.resize(width, height);
    }
}
