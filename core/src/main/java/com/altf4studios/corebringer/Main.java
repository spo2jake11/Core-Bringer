package com.altf4studios.corebringer;

import com.altf4studios.corebringer.screens.*;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


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
    public CodeEditorScreen codeEditorScreen;
    public DebugScreen debugScreen;
    public CardTestScren cardTestScren;
    public SampleCardHandler selecteddebugcard;
    private AssetManager assetManager;
    public JShell jshell;
    private final ByteArrayOutputStream jshellOutputStream = new ByteArrayOutputStream();

    @Override
    public void create() {
        //AssetManager is located here
        assetManager =new AssetManager();
        assetManager.finishLoading();

        ///This is where music plays when the game starts
        corebringerbgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerbgm.setLooping(true);
        corebringerbgm.setVolume(1.0f);
        corebringerstartmenubgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerstartmenubgm.setLooping(true);
        corebringerstartmenubgm.setVolume(1.0f);
        corebringermapstartbgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/Drake - Hotline Bling (Lyrics).mp3"));
        corebringermapstartbgm.setLooping(true);
        corebringermapstartbgm.setVolume(1.0f);

        isMusicMuted = false;
//        corebringerbgm.play();
        //corebringerstartmenubgm.play();

        ///This is for the Skin to be declared and initialized so Screens can just call it
        //test skin used Utils.getInternalPath
        testskin = new Skin(Utils.getInternalPath("ui/uiskin.json")); ///Usage of sample skin, can be changed soon
        responsivelabelstyle = new Label.LabelStyle(testskin.getFont("default"), Color.WHITE);
        responsivelabelstyle.font.getData().setScale(3f);

        ///This is just temporary reference for the Card Handler to be used in the debug screen
        selecteddebugcard = null;

        ///This is for initalizing JShell
        initJShell();

        mainMenuScreen = new MainMenuScreen(this);
        optionsScreen = new OptionsScreen(this);
        startGameMapScreen = new StartGameMapScreen(this);
        codeEditorScreen = new CodeEditorScreen(this);
        debugScreen = new DebugScreen(this);
        cardTestScren = new CardTestScren(this);
        gameScreen = new GameScreen(this);
        setScreen(mainMenuScreen);
    }

    ///The method that initializes JShell as well as things it will import
    public void initJShell() {
        jshell = JShell.builder()
            .out(new PrintStream(jshellOutputStream))
            .err(new PrintStream(jshellOutputStream))
            .build();

        jshell.eval("import com.badlogic.gdx.*;");
        jshell.eval("import com.altf4studios.corebringer.*;");
    }

    ///This is for JShell input evaluation
    public String evaluateJShellInput(String input) {
        jshellOutputStream.reset(); ///This clears JShell's previous output snippers

        StringBuilder result = new StringBuilder();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream pS = new PrintStream(outputStream);

        List<SnippetEvent> events = jshell.eval(input);
        for (SnippetEvent e : events) {
            if (e.exception() != null) {
                result.append("Exception: ").append(e.exception().getMessage()).append("\n");
            } else if (e.status() == Snippet.Status.REJECTED) {
                result.append("Rejected: ").append(e.snippet().source()).append("\n");
            } else if (e.value() != null) {
                result.append(e.value()).append("\n");
            }
        }
        ///This will get everything JShell printed
        String jshellOutput = jshellOutputStream.toString();
        if (!jshellOutput.isEmpty()) {
            result.append(jshellOutput);
        }

        return result.toString().isEmpty() ? "No output." : result.toString();
    }

    @Override
    public void render() {
        super.render();

        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        ///This is to give function to the F11 key
        /*if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }*/
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
