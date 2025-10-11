package com.altf4studios.corebringer;

import com.altf4studios.corebringer.screens.*;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import com.badlogic.gdx.utils.Timer;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public Music corebringerbgm;
    public Music corebringerstartmenubgm;
    public Music corebringermapstartbgm;
    public Music corebringergamescreenbgm;
    public boolean isMusicMuted;
    public Skin testskin;
    public Label.LabelStyle responsivelabelstyle;
    public MainMenuScreen mainMenuScreen;
    public OptionsScreen optionsScreen;
    public GameMapScreen gameMapScreen;
    public GameScreen gameScreen;
    public StartGameMapScreen startGameMapScreen;
    public CodeEditorScreen codeEditorScreen;
    public DebugScreen debugScreen;
    public CardTestScren cardTestScren;
    public PuzzleScreen puzzleScreen;
    public MerchantScreen merchantScreen;
    public RestScreen restScreen;
    public SampleCardHandler selecteddebugcard;
    private AssetManager assetManager;
    public JShell jshell;
    private final ByteArrayOutputStream jshellOutputStream = new ByteArrayOutputStream();
    private InputMultiplexer globalMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        //AssetManager is located here
        assetManager = new AssetManager();
        assetManager.load("startup_bg.png", Texture.class);

        ///This is where music plays when the game starts
        corebringerbgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/Mortal-Gaming-144000-(GameIntro1).ogg"));
        corebringerbgm.setLooping(true);
        corebringerbgm.setVolume(1.0f);
        corebringerstartmenubgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/Mortal-Gaming-144000-(GameIntro1).ogg"));
        corebringerstartmenubgm.setLooping(true);
        corebringerstartmenubgm.setVolume(1.0f);
        corebringermapstartbgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/To-The-Teath-159171-(NormalBattleMusic1).ogg"));
        corebringermapstartbgm.setLooping(true);
        corebringermapstartbgm.setVolume(1.0f);
        corebringergamescreenbgm = Gdx.audio.newMusic(Utils.getInternalPath("audio/0-Top-Battle-Game-BGM-264625-(NormalBattleMusic2).ogg"));
        corebringergamescreenbgm.setLooping(true);
        corebringergamescreenbgm.setVolume(1.0f);

        isMusicMuted = false;
        //corebringerbgm.play();
        corebringerstartmenubgm.play();

        ///This is for the Skin to be declared and initialized so Screens can just call it
        //test skin used Utils.getInternalPath
        testskin = new Skin(Utils.getInternalPath("assets/ui/uiskin.json")); ///Usage of sample skin, can be changed soon
        responsivelabelstyle = new Label.LabelStyle(testskin.getFont("default"), Color.WHITE);
        responsivelabelstyle.font.getData().setScale(2f);

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
        merchantScreen = new MerchantScreen(this);
        restScreen = new RestScreen(this);
        // Lazily create GameScreen when a map node is clicked
        gameScreen = null;
        gameMapScreen = new GameMapScreen(this);
        puzzleScreen = new PuzzleScreen(this);
        setScreen(mainMenuScreen);
        // Ensure the input multiplexer is always set as the input processor
        Gdx.input.setInputProcessor(globalMultiplexer);
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
        // Removed continuous fullscreen switching each frame. Use toggleFullscreen() on demand instead.

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

    // Fade out the given music over duration (seconds)
    public void fadeOutMusic(final Music music, final float duration, final Runnable afterFade) {
        if (music == null || !music.isPlaying()) {
            if (afterFade != null) afterFade.run();
            return;
        }
        final float initialVolume = music.getVolume();
        final int steps = 20;
        final float stepTime = duration / steps;
        Timer.schedule(new Timer.Task() {
            int currentStep = 0;
            @Override
            public void run() {
                currentStep++;
                float newVolume = initialVolume * (1f - (float)currentStep / steps);
                music.setVolume(Math.max(newVolume, 0f));
                if (currentStep >= steps) {
                    music.stop();
                    music.setVolume(initialVolume); // Reset for next play
                    if (afterFade != null) afterFade.run();
                    this.cancel();
                }
            }
        }, 0, stepTime, steps);
    }

    // Fade in the given music over duration (seconds)
    public void fadeInMusic(final Music music, final float duration) {
        if (music == null) return;
        final float targetVolume = 1.0f;
        music.setVolume(0f);
        music.play();
        final int steps = 20;
        final float stepTime = duration / steps;
        Timer.schedule(new Timer.Task() {
            int currentStep = 0;
            @Override
            public void run() {
                currentStep++;
                float newVolume = targetVolume * ((float)currentStep / steps);
                music.setVolume(Math.min(newVolume, targetVolume));
                if (currentStep >= steps) {
                    music.setVolume(targetVolume);
                    this.cancel();
                }
            }
        }, 0, stepTime, steps);
    }

    @Override public void pause() {
        super.pause();
        if (!isMusicMuted && corebringerstartmenubgm.isPlaying()) {
            fadeOutMusic(corebringerstartmenubgm, 1f, null);
        }
        if (!isMusicMuted && corebringermapstartbgm.isPlaying()) {
            fadeOutMusic(corebringermapstartbgm, 1f, null);
        }
    }
    @Override public void resume() {
        super.resume();
        if (!isMusicMuted) {
            if (getScreen().equals(mainMenuScreen) && !corebringerstartmenubgm.isPlaying()) {
                fadeInMusic(corebringerstartmenubgm, 1f);
                isMusicMuted = false;
            }
            if (getScreen().equals(startGameMapScreen) && !corebringermapstartbgm.isPlaying()) {
                fadeInMusic(corebringermapstartbgm, 1f);
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

    // Add methods to manage input processors globally
    public void addInputProcessor(InputProcessor processor) {
        if (!globalMultiplexer.getProcessors().contains(processor, true)) {
            globalMultiplexer.addProcessor(processor);
        }
        Gdx.input.setInputProcessor(globalMultiplexer);
    }
    public void removeInputProcessor(InputProcessor processor) {
        globalMultiplexer.removeProcessor(processor);
        Gdx.input.setInputProcessor(globalMultiplexer);
    }
    public void clearInputProcessors() {
        globalMultiplexer.clear();
        Gdx.input.setInputProcessor(globalMultiplexer);
    }
    public void addInputProcessorAt(int index, InputProcessor processor) {
        if (!globalMultiplexer.getProcessors().contains(processor, true)) {
            globalMultiplexer.addProcessor(index, processor);
        }
        Gdx.input.setInputProcessor(globalMultiplexer);
    }
    public InputMultiplexer getGlobalMultiplexer() {
        return globalMultiplexer;
    }
}
