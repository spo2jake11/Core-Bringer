package com.altf4studios.corebringer;

import com.altf4studios.corebringer.screens.*;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.altf4studios.corebringer.utils.SettingsData;
import com.altf4studios.corebringer.utils.SettingsManager;
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
    public TreasurePuzzleScreen treasurePuzzleScreen;
    public RestScreen restScreen;
    public AcidFloorEventScreen acidFloorEventScreen;
    public SampleCardHandler selecteddebugcard;
    private AssetManager assetManager;
    public JShell jshell;
    private final ByteArrayOutputStream jshellOutputStream = new ByteArrayOutputStream();
    private InputMultiplexer globalMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        //AssetManager is located here
        assetManager = new AssetManager();
        // Removed loading of 'startup_bg.png' (file not present). Load assets on demand per screen.

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

        // Load global audio settings
        SettingsData settings = SettingsManager.loadSettings();
        if (settings != null) {
            float vol = Math.max(0f, Math.min(1f, settings.volume));
            isMusicMuted = settings.muted;
            corebringerbgm.setVolume(vol);
            corebringerstartmenubgm.setVolume(vol);
            corebringermapstartbgm.setVolume(vol);
            corebringergamescreenbgm.setVolume(vol);
        } else {
            isMusicMuted = false;
        }

        // Only auto-play if not muted
        if (!isMusicMuted) {
            corebringerstartmenubgm.play();
        }

        ///This is for the Skin to be declared and initialized so Screens can just call it
        //test skin used Utils.getInternalPath
        testskin = new Skin(Utils.getInternalPath("assets/ui/uiskin.json")); ///Usage of sample skin, can be changed soon
        responsivelabelstyle = new Label.LabelStyle(testskin.getFont("default"), Color.WHITE);
        responsivelabelstyle.font.getData().setScale(2f);

        ///This is just temporary reference for the Card Handler to be used in the debug screen
        selecteddebugcard = null;

        ///This is for initalizing JShell
        initJShell();

        mainMenuScreen = null;
        optionsScreen = null;
        startGameMapScreen = null;
        codeEditorScreen = null;
        debugScreen = null;
        cardTestScren = null;
        merchantScreen = null;
        restScreen = null;
        // Lazily create GameScreen when a map node is clicked
        gameScreen = null;
        gameMapScreen = null;
        puzzleScreen = null;
        treasurePuzzleScreen = null;
        showMainMenu();
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
                    // Reset to settings volume (not the pre-fade initial), so future play uses global volume
                    SettingsData s = SettingsManager.loadSettings();
                    float target = (s != null) ? Math.max(0f, Math.min(1f, s.volume)) : 1.0f;
                    music.setVolume(target);
                    if (afterFade != null) afterFade.run();
                    this.cancel();
                }
            }
        }, 0, stepTime, steps);
    }

    // Fade in the given music over duration (seconds)
    public void fadeInMusic(final Music music, final float duration) {
        if (music == null) return;
        if (isMusicMuted) return; // honor global mute
        SettingsData s = SettingsManager.loadSettings();
        final float targetVolume = (s != null) ? Math.max(0f, Math.min(1f, s.volume)) : music.getVolume();
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
        try { if (corebringerbgm != null) corebringerbgm.dispose(); } catch (Exception ignored) {}
        try { if (corebringerstartmenubgm != null) corebringerstartmenubgm.dispose(); } catch (Exception ignored) {}
        try { if (corebringermapstartbgm != null) corebringermapstartbgm.dispose(); } catch (Exception ignored) {}
        try { if (corebringergamescreenbgm != null) corebringergamescreenbgm.dispose(); } catch (Exception ignored) {}
        try { if (assetManager != null) assetManager.dispose(); } catch (Exception ignored) {}

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

    public AssetManager getAssets() {
        return assetManager;
    }

    // --- Lazy screen helpers ---
    public void showMainMenu() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }
        setScreen(mainMenuScreen);
    }

    public void showOptions() {
        if (optionsScreen == null) {
            optionsScreen = new OptionsScreen(this);
        }
        setScreen(optionsScreen);
    }

    public void showStartGameMap() {
        if (startGameMapScreen == null) {
            startGameMapScreen = new StartGameMapScreen(this);
        }
        setScreen(startGameMapScreen);
    }

    public void showCodeEditor() {
        if (codeEditorScreen == null) {
            codeEditorScreen = new CodeEditorScreen(this);
        }
        setScreen(codeEditorScreen);
    }

    public void showDebug() {
        if (debugScreen == null) {
            debugScreen = new DebugScreen(this);
        }
        setScreen(debugScreen);
    }

    public void showCardTest() {
        if (cardTestScren == null) {
            cardTestScren = new CardTestScren(this);
        }
        setScreen(cardTestScren);
    }

    public void showMerchant() {
        if (merchantScreen == null) {
            merchantScreen = new MerchantScreen(this);
        }
        setScreen(merchantScreen);
    }

    public void showRest() {
        if (restScreen == null) {
            restScreen = new RestScreen(this);
        }
        setScreen(restScreen);
    }

    public void showGameMap() {
        if (gameMapScreen == null) {
            gameMapScreen = new GameMapScreen(this);
        }
        setScreen(gameMapScreen);
    }

    public void showPuzzle() {
        if (puzzleScreen == null) {
            puzzleScreen = new PuzzleScreen(this);
        }
        setScreen(puzzleScreen);
    }

    public void showTreasurePuzzle() {
        if (treasurePuzzleScreen == null) {
            treasurePuzzleScreen = new TreasurePuzzleScreen(this);
        }
        setScreen(treasurePuzzleScreen);
    }

    // Dispose all game-related screens except MainMenuScreen
    // Called when player dies or wants to return to main menu with a fresh state
    public void disposeAllScreensExceptMainMenu() {
        Gdx.app.log("Main", "Disposing all screens except MainMenuScreen...");
        
        // Dispose GameScreen
        if (gameScreen != null) {
            try {
                gameScreen.dispose();
                Gdx.app.log("Main", "Disposed GameScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing GameScreen: " + e.getMessage());
            }
            gameScreen = null;
        }
        
        // Dispose GameMapScreen
        if (gameMapScreen != null) {
            try {
                gameMapScreen.dispose();
                Gdx.app.log("Main", "Disposed GameMapScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing GameMapScreen: " + e.getMessage());
            }
            gameMapScreen = null;
        }
        
        // Dispose CodeEditorScreen
        if (codeEditorScreen != null) {
            try {
                codeEditorScreen.dispose();
                Gdx.app.log("Main", "Disposed CodeEditorScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing CodeEditorScreen: " + e.getMessage());
            }
            codeEditorScreen = null;
        }
        
        // Dispose MerchantScreen
        if (merchantScreen != null) {
            try {
                merchantScreen.dispose();
                Gdx.app.log("Main", "Disposed MerchantScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing MerchantScreen: " + e.getMessage());
            }
            merchantScreen = null;
        }
        
        // Dispose RestScreen
        if (restScreen != null) {
            try {
                restScreen.dispose();
                Gdx.app.log("Main", "Disposed RestScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing RestScreen: " + e.getMessage());
            }
            restScreen = null;
        }
        
        // Dispose PuzzleScreen
        if (puzzleScreen != null) {
            try {
                puzzleScreen.dispose();
                Gdx.app.log("Main", "Disposed PuzzleScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing PuzzleScreen: " + e.getMessage());
            }
            puzzleScreen = null;
        }
        
        // Dispose TreasurePuzzleScreen
        if (treasurePuzzleScreen != null) {
            try {
                treasurePuzzleScreen.dispose();
                Gdx.app.log("Main", "Disposed TreasurePuzzleScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing TreasurePuzzleScreen: " + e.getMessage());
            }
            treasurePuzzleScreen = null;
        }
        
        // Dispose AcidFloorEventScreen
        if (acidFloorEventScreen != null) {
            try {
                acidFloorEventScreen.dispose();
                Gdx.app.log("Main", "Disposed AcidFloorEventScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing AcidFloorEventScreen: " + e.getMessage());
            }
            acidFloorEventScreen = null;
        }
        
        // Dispose StartGameMapScreen
        if (startGameMapScreen != null) {
            try {
                startGameMapScreen.dispose();
                Gdx.app.log("Main", "Disposed StartGameMapScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing StartGameMapScreen: " + e.getMessage());
            }
            startGameMapScreen = null;
        }
        
        // Dispose DebugScreen
        if (debugScreen != null) {
            try {
                debugScreen.dispose();
                Gdx.app.log("Main", "Disposed DebugScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing DebugScreen: " + e.getMessage());
            }
            debugScreen = null;
        }
        
        // Dispose CardTestScreen
        if (cardTestScren != null) {
            try {
                cardTestScren.dispose();
                Gdx.app.log("Main", "Disposed CardTestScreen");
            } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing CardTestScreen: " + e.getMessage());
            }
            cardTestScren = null;
        }
        
        // Keep MainMenuScreen and OptionsScreen alive
        // They will be reused
        
        Gdx.app.log("Main", "All game screens disposed successfully");
    }
}
