package com.altf4studios.corebringer;

import com.altf4studios.corebringer.screens.*;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.altf4studios.corebringer.utils.SettingsData;
import com.altf4studios.corebringer.utils.SettingsManager;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import com.badlogic.gdx.utils.Timer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    // OPTIMIZED: Changed to on-demand music loading (was loading all 4 at startup = 40-60MB)
    private Music currentlyPlayingMusic;
    private String currentMusicType; // Track which music is loaded

    // Backward compatibility: Public accessors for old music field names
    public Music corebringerbgm;  // Deprecated, kept for compatibility
    public Music corebringerstartmenubgm;  // Deprecated, kept for compatibility
    public Music corebringermapstartbgm;  // Deprecated, kept for compatibility
    public Music corebringergamescreenbgm;  // Deprecated, kept for compatibility

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
    // OPTIMIZED: JShell lazy-loaded (was 20-50MB at startup)
    private JShell jshell;
    private ByteArrayOutputStream jshellOutputStream;
    private InputMultiplexer globalMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        //AssetManager is located here
        assetManager = new AssetManager();
        // Removed loading of 'startup_bg.png' (file not present). Load assets on demand per screen.

        // OPTIMIZED: Music now loaded on-demand via playMusic() instead of all at startup
        currentlyPlayingMusic = null;
        currentMusicType = null;

        // Initialize backward compatibility fields as null (loaded on-demand)
        corebringerbgm = null;
        corebringerstartmenubgm = null;
        corebringermapstartbgm = null;
        corebringergamescreenbgm = null;

        // Load global audio settings
        SettingsData settings = SettingsManager.loadSettings();
        if (settings != null) {
            isMusicMuted = settings.muted;
        } else {
            isMusicMuted = false;
        }

        ///This is for the Skin to be declared and initialized so Screens can just call it
        //test skin used Utils.getInternalPath
        testskin = new Skin(Utils.getInternalPath("assets/ui/uiskin.json")); ///Usage of sample skin, can be changed soon
        responsivelabelstyle = new Label.LabelStyle(testskin.getFont("default"), Color.WHITE);
        responsivelabelstyle.font.getData().setScale(2f);

        ///This is just temporary reference for the Card Handler to be used in the debug screen
        selecteddebugcard = null;

        // OPTIMIZED: JShell now lazy-loaded via getJShell() instead of at startup

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

        // Auto-play main menu music if not muted
        if (!isMusicMuted) {
            playMusic("menu");
        }
    }

    // ENHANCED: On-demand music loading system with fade in/out transitions
    public void playMusic(String musicType) {
        playMusicWithFade(musicType, 1.2f); // Default 1.2s fade
    }

    // Enhanced music system with fade transitions
    public void playMusicWithFade(String musicType, float fadeDuration) {
        if (isMusicMuted) return;

        // If same music is already playing, don't reload
        if (currentMusicType != null && currentMusicType.equals(musicType)) {
            if (currentlyPlayingMusic != null && currentlyPlayingMusic.isPlaying()) {
                // Update backward compatibility references
                updateBackwardCompatibilityRefs();
                return;
            }
        }

        // Get the new music path
        String musicPath = getMusicPath(musicType);
        if (musicPath == null) return;

        // If there's currently playing music, fade it out then switch
        if (currentlyPlayingMusic != null && currentlyPlayingMusic.isPlaying()) {
            final Music oldMusic = currentlyPlayingMusic;
            final String newMusicPath = musicPath;
            final String newMusicType = musicType;

            Gdx.app.log("Main", "Fading out current music (" + currentMusicType + ") over " + fadeDuration + "s");

            // Fade out current music, then load new music
            fadeOutMusic(oldMusic, fadeDuration, () -> {
                // Dispose old music
                try {
                    oldMusic.dispose();
                    Gdx.app.log("Main", "Disposed previous music after fade out");
                } catch (Exception e) {
                    Gdx.app.error("Main", "Error disposing old music: " + e.getMessage());
                }

                // Load and fade in new music
                loadAndFadeInMusic(newMusicPath, newMusicType, fadeDuration);
            });
        } else {
            // No current music, just load and fade in new music
            loadAndFadeInMusic(musicPath, musicType, fadeDuration);
        }
    }

    // Helper method to load and fade in new music
    private void loadAndFadeInMusic(String musicPath, String musicType, float fadeDuration) {
        try {
            currentlyPlayingMusic = Gdx.audio.newMusic(Utils.getInternalPath(musicPath));
            currentlyPlayingMusic.setLooping(true);
            currentMusicType = musicType;

            // Update backward compatibility references
            updateBackwardCompatibilityRefs();

            Gdx.app.log("Main", "Loading and fading in music: " + musicType + " over " + fadeDuration + "s");

            // Fade in the new music
            fadeInMusic(currentlyPlayingMusic, fadeDuration);

        } catch (Exception e) {
            Gdx.app.error("Main", "Error loading music " + musicPath + ": " + e.getMessage());
        }
    }


    // Update backward compatibility field references
    private void updateBackwardCompatibilityRefs() {
        corebringerbgm = currentlyPlayingMusic;
        corebringerstartmenubgm = currentlyPlayingMusic;
        corebringermapstartbgm = currentlyPlayingMusic;
        corebringergamescreenbgm = currentlyPlayingMusic;
    }

    // Helper method to get music file path by type
    private String getMusicPath(String musicType) {
        switch (musicType) {
            case "menu":
            case "intro":
                return "audio/Mortal-Gaming-144000-(GameIntro1).ogg";
            case "map":
                return "audio/To-The-Teath-159171-(NormalBattleMusic1).ogg";
            case "battle":
                return "audio/0-Top-Battle-Game-BGM-264625-(NormalBattleMusic2).ogg";
            default:
                return "audio/Mortal-Gaming-144000-(GameIntro1).ogg";
        }
    }

    // Backward compatibility methods for existing code
    public Music corebringerstartmenubgm() {
        if (currentMusicType == null || !currentMusicType.equals("menu")) {
            playMusic("menu");
        }
        return currentlyPlayingMusic;
    }

    public Music corebringermapstartbgm() {
        if (currentMusicType == null || !currentMusicType.equals("map")) {
            playMusic("map");
        }
        return currentlyPlayingMusic;
    }

    public Music corebringergamescreenbgm() {
        if (currentMusicType == null || !currentMusicType.equals("battle")) {
            playMusic("battle");
        }
        return currentlyPlayingMusic;
    }

    // OPTIMIZED: JShell lazy initialization
    public JShell getJShell() {
        if (jshell == null) {
            Gdx.app.log("Main", "Lazy-loading JShell...");
            jshellOutputStream = new ByteArrayOutputStream();
            jshell = JShell.builder()
                .out(new PrintStream(jshellOutputStream))
                .err(new PrintStream(jshellOutputStream))
                .build();
            jshell.eval("import com.badlogic.gdx.*;");
            jshell.eval("import com.altf4studios.corebringer.*;");
        }
        return jshell;
    }

    ///The method that initializes JShell as well as things it will import
    public void initJShell() {
        getJShell();
    }

    ///This is for JShell input evaluation
    public String evaluateJShellInput(String input) {
        // Ensure JShell is initialized
        JShell shell = getJShell();
        jshellOutputStream.reset(); ///This clears JShell's previous output snippers

        StringBuilder result = new StringBuilder();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream pS = new PrintStream(outputStream);

        List<SnippetEvent> events = shell.eval(input);
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

        // Memory monitoring with F12 key (OPTIMIZATION FEATURE)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            Gdx.app.log("MEMORY", "========================================");
            Gdx.app.log("MEMORY", "Used: " + usedMemory + " MB");
            Gdx.app.log("MEMORY", "Total: " + totalMemory + " MB");
            Gdx.app.log("MEMORY", "Max: " + maxMemory + " MB");
            Gdx.app.log("MEMORY", "Current Music: " + (currentMusicType != null ? currentMusicType : "none"));
            Gdx.app.log("MEMORY", "JShell loaded: " + (jshell != null));
            Gdx.app.log("MEMORY", "========================================");
        }

        ///This is to give function to the F11 key
        /*if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }*/

    }

    // Fade out the given music over duration (seconds)
    public void fadeOutMusic(final Music music, final float duration, final Runnable afterFade) {
        // UPDATED: Handle both old music references and new system
        Music targetMusic = music != null ? music : currentlyPlayingMusic;

        if (targetMusic == null || !targetMusic.isPlaying()) {
            if (afterFade != null) afterFade.run();
            return;
        }
        final float initialVolume = targetMusic.getVolume();
        final int steps = 20;
        final float stepTime = duration / steps;
        Timer.schedule(new Timer.Task() {
            int currentStep = 0;
            @Override
            public void run() {
                currentStep++;
                float newVolume = initialVolume * (1f - (float)currentStep / steps);
                if (targetMusic != null) {
                    targetMusic.setVolume(Math.max(newVolume, 0f));
                }
                if (currentStep >= steps) {
                    if (targetMusic != null) {
                        targetMusic.stop();
                    }
                    // Reset to settings volume (not the pre-fade initial), so future play uses global volume
                    SettingsData s = SettingsManager.loadSettings();
                    float target = (s != null) ? Math.max(0f, Math.min(1f, s.volume)) : 1.0f;
                    if (targetMusic != null) {
                        targetMusic.setVolume(target);
                    }
                    if (afterFade != null) afterFade.run();
                    this.cancel();
                }
            }
        }, 0, stepTime, steps);
    }

    // Fade in the given music over duration (seconds)
    public void fadeInMusic(final Music music, final float duration) {
        // UPDATED: Handle both old music references and new system
        Music targetMusic = music != null ? music : currentlyPlayingMusic;

        if (targetMusic == null) return;
        if (isMusicMuted) return; // honor global mute
        SettingsData s = SettingsManager.loadSettings();
        final float targetVolume = (s != null) ? Math.max(0f, Math.min(1f, s.volume)) : targetMusic.getVolume();
        targetMusic.setVolume(0f);
        targetMusic.play();
        final int steps = 20;
        final float stepTime = duration / steps;
        Timer.schedule(new Timer.Task() {
            int currentStep = 0;
            @Override
            public void run() {
                currentStep++;
                float newVolume = targetVolume * ((float)currentStep / steps);
                if (targetMusic != null) {
                    targetMusic.setVolume(Math.min(newVolume, targetVolume));
                }
                if (currentStep >= steps) {
                    if (targetMusic != null) {
                        targetMusic.setVolume(targetVolume);
                    }
                    this.cancel();
                }
            }
        }, 0, stepTime, steps);
    }

    @Override public void pause() {
        super.pause();
        // UPDATED: Handle new music system
        if (!isMusicMuted && currentlyPlayingMusic != null && currentlyPlayingMusic.isPlaying()) {
            fadeOutMusic(currentlyPlayingMusic, 1f, null);
        }
    }

    @Override public void resume() {
        super.resume();
        // UPDATED: Handle new music system
        SettingsData settings = SettingsManager.loadSettings();
        if (settings != null) {
            isMusicMuted = settings.muted;
            if (!isMusicMuted) {
                // Resume music based on current type
                if (currentMusicType != null) {
                    playMusic(currentMusicType);
                }
            }
        }
    }

    @Override
    public void dispose() {
        // UPDATED: Dispose new music system
        try { if (currentlyPlayingMusic != null) currentlyPlayingMusic.dispose(); } catch (Exception ignored) {}
        try { if (assetManager != null) assetManager.dispose(); } catch (Exception ignored) {}
        // UPDATED: Dispose JShell if loaded
        try { if (jshell != null) jshell.close(); } catch (Exception ignored) {}
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
        // Dispose other screens to free memory
        disposeNonEssentialScreens(mainMenuScreen);

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
        // Dispose other screens to free memory
        disposeNonEssentialScreens(startGameMapScreen);

        if (startGameMapScreen == null) {
            startGameMapScreen = new StartGameMapScreen(this);
        }
        setScreen(startGameMapScreen);
    }

    public void showCodeEditor() {
        // CodeEditor is tied to GameScreen, don't dispose game screens
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
        // Keep merchant screen for this session
        if (merchantScreen == null) {
            merchantScreen = new MerchantScreen(this);
        }
        setScreen(merchantScreen);
    }

    public void showRest() {
        // Keep rest screen for this session
        if (restScreen == null) {
            restScreen = new RestScreen(this);
        }
        setScreen(restScreen);
    }

    public void showGameMap() {
        // Keep game map for this session
        if (gameMapScreen == null) {
            gameMapScreen = new GameMapScreen(this);
        }
        setScreen(gameMapScreen);
    }

    public void showPuzzle() {
        // Dispose puzzle screens when done
        if (puzzleScreen == null) {
            puzzleScreen = new PuzzleScreen(this);
        }
        setScreen(puzzleScreen);
    }

    public void showTreasurePuzzle() {
        // Dispose puzzle screens when done
        if (treasurePuzzleScreen == null) {
            treasurePuzzleScreen = new TreasurePuzzleScreen(this);
        }
        setScreen(treasurePuzzleScreen);
    }

    // Helper method to dispose screens not currently in use
    private void disposeNonEssentialScreens(Screen keepScreen) {
        // Dispose debug/test screens
        if (debugScreen != null && debugScreen != keepScreen) {
            try { debugScreen.dispose(); } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing debugScreen: " + e.getMessage());
            }
            debugScreen = null;
        }
        if (cardTestScren != null && cardTestScren != keepScreen) {
            try { cardTestScren.dispose(); } catch (Exception e) {
                Gdx.app.error("Main", "Error disposing cardTestScren: " + e.getMessage());
            }
            cardTestScren = null;
        }
        // Don't dispose active game screens (gameScreen, gameMapScreen, etc.) during gameplay
        // Only dispose them via disposeAllScreensExceptMainMenu when returning to main menu
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
