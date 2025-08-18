package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.compiler.JavaExternalRunner;
import com.altf4studios.corebringer.compiler.CodePolicyValidator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Disposable;

/**
 * UI component for code compilation similar to OnlineGDB
 * Integrates with the existing game screens
 */
public class CodeCompilerUI implements Disposable {

    private Stage stage;
    private TextArea codeInput;
    private TextButton compileButton;
    private TextButton clearButton;
    private Label statusLabel;
    private Label outputLabel;
    private ScrollPane codeScrollPane;

    private JavaExternalRunner javaRunner;
    private boolean isVisible = false;
    private ShapeRenderer shapeRenderer;
    private Texture backgroundTexture;
    private boolean useTextureBackground = false;
    
    // UI dimensions and positioning
    private float x, y, width, height;

        public CodeCompilerUI(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.javaRunner = new JavaExternalRunner();
        this.shapeRenderer = new ShapeRenderer();
        initializeUI();
    }

    private void initializeUI() {
        stage = new Stage();

        // Create skin for UI elements
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Code input area
        codeInput = new TextArea("// Enter your Java code here...\npublic class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}", skin);
        codeInput.setAlignment(Align.left);
        codeInput.setPrefRows(15);

        // Output label for showing compilation results
        outputLabel = new Label("Ready to compile", skin);
        outputLabel.setColor(Color.WHITE);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.left);
        outputLabel.setFontScale(0.9f);

        // Buttons
        compileButton = new TextButton("Compile & Run", skin);
        clearButton = new TextButton("Clear", skin);

        // Status label
        statusLabel = new Label("Ready to compile", skin);
        statusLabel.setColor(Color.GREEN);

        // Scroll pane for code input
        codeScrollPane = new ScrollPane(codeInput, skin);
        codeScrollPane.setFadeScrollBars(false);

        // Layout
        Table mainTable = new Table();
        mainTable.setPosition(x, y);
        mainTable.setSize(width, height);

        // Title
        Label titleLabel = new Label("Java Code Compiler", skin);
        titleLabel.setFontScale(1.2f);
        titleLabel.setColor(Color.WHITE);

        // Code input section
        Label codeLabel = new Label("Code Input:", skin);
        codeLabel.setColor(Color.WHITE);

        // Button row with output display
        Table buttonTable = new Table();
        buttonTable.add(compileButton).pad(5);
        buttonTable.add(clearButton).pad(5);
        buttonTable.add(statusLabel).expandX().left().pad(5);
        buttonTable.row();
        buttonTable.add(outputLabel).colspan(3).fillX().pad(10).height(80);

        // Main layout
        mainTable.add(titleLabel).center().pad(10);
        mainTable.row();
        mainTable.add(codeLabel).left().pad(5);
        mainTable.row();
        mainTable.add(codeScrollPane).width(width * 0.8f).height(height * 0.5f).pad(5);
        mainTable.row();
        mainTable.add(buttonTable).fillX().pad(5);

        stage.addActor(mainTable);

        // Event listeners
        setupEventListeners();

        // IDE-like behavior: Tab inserts 4 spaces; Enter auto-indents +4 spaces
        codeInput.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.TAB) {
                    CodeCompilerUIHelpers.insertAtCursor(codeInput, "    ");
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    String text = codeInput.getText();
                    int cursor = codeInput.getCursorPosition();
                    int lineStart = text.lastIndexOf('\n', Math.max(0, cursor - 1));
                    lineStart = (lineStart == -1) ? 0 : lineStart + 1;
                    int leading = CodeCompilerUIHelpers.countLeadingSpaces(text, lineStart);
                    String indent = CodeCompilerUIHelpers.buildSpaces(leading + 4);
                    CodeCompilerUIHelpers.insertAtCursor(codeInput, "\n" + indent);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupEventListeners() {
        compileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                compileAndRun();
            }
        });

        clearButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearAll();
            }
        });
    }

    private void compileAndRun() {
        String code = codeInput.getText();
        if (code.trim().isEmpty()) {
            outputLabel.setText("❌ No code to compile");
            statusLabel.setText("No code provided");
            statusLabel.setColor(Color.RED);
            return;
        }

        // Update status
        statusLabel.setText("Compiling...");
        statusLabel.setColor(Color.YELLOW);

        // Run compilation in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Validate first
                CodePolicyValidator.ValidationResult vr = CodePolicyValidator.validate(code);
                if (!vr.valid) {
                    String result = "❌ VALIDATION FAILED:\n" + vr.message + "\n\n" + CodePolicyValidator.policyTemplate();
                    Gdx.app.postRunnable(() -> {
                        outputLabel.setText(result);
                        statusLabel.setText("Validation failed");
                        statusLabel.setColor(Color.RED);
                    });
                    return;
                }

                String result = javaRunner.compileAndRun(code);

                // Update UI on main thread
                Gdx.app.postRunnable(() -> {
                    outputLabel.setText(result);
                    if (result.contains("✅")) {
                        statusLabel.setText("Compilation successful");
                        statusLabel.setColor(Color.GREEN);
                    } else {
                        statusLabel.setText("Compilation failed");
                        statusLabel.setColor(Color.RED);
                    }
                });
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> {
                    outputLabel.setText("❌ Unexpected error: " + e.getMessage());
                    statusLabel.setText("Error occurred");
                    statusLabel.setColor(Color.RED);
                });
            }
        }).start();
    }

    private void clearAll() {
        codeInput.setText("");
        outputLabel.setText("Ready to compile");
        statusLabel.setText("Ready to compile");
        statusLabel.setColor(Color.GREEN);
    }

        public void render(SpriteBatch batch) {
        if (!isVisible) return;
        
        if (useTextureBackground && backgroundTexture != null) {
            // Use texture background if available
            batch.setColor(1f, 1f, 1f, 0.9f);
            batch.draw(backgroundTexture, x, y, width, height);
            batch.setColor(Color.WHITE);
        } else {
            // Use colored rectangle background
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
            batch.begin();
        }
        
        // Render UI stage
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
        if (visible) {
            Gdx.input.setInputProcessor(stage);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // Getters for external access
    public String getCode() {
        return codeInput.getText();
    }

    public void setCode(String code) {
        codeInput.setText(code);
    }

    public void setOutput(String output) {
        outputLabel.setText(output);
    }
    
    /**
     * Set a background texture for the compiler UI
     * @param texturePath Path to the texture file
     */
    public void setBackgroundTexture(String texturePath) {
        try {
            if (backgroundTexture != null) {
                backgroundTexture.dispose();
            }
            backgroundTexture = new Texture(Gdx.files.internal(texturePath));
            useTextureBackground = true;
        } catch (Exception e) {
            System.err.println("Failed to load background texture: " + e.getMessage());
            useTextureBackground = false;
        }
    }
    
    /**
     * Use colored rectangle background instead of texture
     */
    public void useColoredBackground() {
        useTextureBackground = false;
    }
}

// --- Helpers for editor behavior ---
class CodeCompilerUIHelpers {
    static void insertAtCursor(TextArea area, String toInsert) {
        String text = area.getText();
        int cursor = area.getCursorPosition();
        String before = text.substring(0, cursor);
        String after = text.substring(cursor);
        area.setText(before + toInsert + after);
        area.setCursorPosition(cursor + toInsert.length());
    }
    static int countLeadingSpaces(String text, int fromIndex) {
        int i = 0;
        while (fromIndex + i < text.length() && text.charAt(fromIndex + i) == ' ') i++;
        return i;
    }
    static String buildSpaces(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }
}
