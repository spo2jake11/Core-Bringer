package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;

public class PuzzleScreen implements Screen {
    private Main corebringer;
    private Stage puzzleStage;
    private Table mainTable;
    private Table puzzleTable;
    private Table buttonTable;

    // Puzzle elements for 4 layers
    private Image[][] inputImages; // [layer][input] - 4 layers, 2 inputs each
    private Image[] operatorImages; // 4 operators
    private Image[] resultCubeImages; // 4 result cubes
    private Label instructionLabel;
    private Label[] resultLabels; // 4 result labels
    private Image congratulationsImage;
    private TextButton backButton;

    // Puzzle state for 4 layers
    private boolean[][] inputs; // [layer][input] - 4 layers, 2 inputs each
    private boolean[][] isQuestionMark; // [layer][input] - track if input is question mark
    private String[] operators = {"&&", "||", "^", "!"}; // AND, OR, XOR, NOT
    private boolean[] layerSolved = new boolean[4];
    private boolean allPuzzlesSolved = false;

    // Textures
    private Texture input1Texture;
    private Texture input0Texture;
    private Texture questionMarkTexture;
    private Texture andTexture;
    private Texture orTexture;
    private Texture xorTexture;
    private Texture notTexture;
    private Texture cubePlainTexture;
    private Texture cubeGreenTexture;
    private Texture cubeRedTexture;
    private Texture backgroundTexture;
    private Texture congratulationsTexture;

    public PuzzleScreen(Main corebringer) {
        this.corebringer = corebringer;
        puzzleStage = new Stage(new FitViewport(1280, 720));
        mainTable = new Table();
        mainTable.setFillParent(true);
        puzzleStage.addActor(mainTable);

        // Load textures
        loadTextures();

        // Initialize UI elements
        initializeUI();

        // Setup layout
        setupLayout();
    }

    private void loadTextures() {
        input1Texture = new Texture(Utils.getInternalPath("Puzzle/1.png"));
        input0Texture = new Texture(Utils.getInternalPath("Puzzle/0.png"));
        questionMarkTexture = new Texture(Utils.getInternalPath("Puzzle/Questionmark.png"));
        andTexture = new Texture(Utils.getInternalPath("Puzzle/&&.png"));
        orTexture = new Texture(Utils.getInternalPath("Puzzle/OR.png"));
        xorTexture = new Texture(Utils.getInternalPath("Puzzle/^.png"));
        notTexture = new Texture(Utils.getInternalPath("Puzzle/!.png"));
        cubePlainTexture = new Texture(Utils.getInternalPath("Puzzle/cubePlain.png"));
        cubeGreenTexture = new Texture(Utils.getInternalPath("Puzzle/cubeGreen.png"));
        cubeRedTexture = new Texture(Utils.getInternalPath("Puzzle/cubeRed.png"));
        backgroundTexture = new Texture(Utils.getInternalPath("Puzzle/stonePuzzleBG.png"));
        congratulationsTexture = new Texture(Utils.getInternalPath("Puzzle/Congratulations.png"));
    }

    private void initializeUI() {
        // Create puzzle table
        puzzleTable = new Table();

        // Initialize arrays for 4 layers
        inputImages = new Image[4][2];
        operatorImages = new Image[4];
        resultCubeImages = new Image[4];
        resultLabels = new Label[4];
        inputs = new boolean[4][2];
        isQuestionMark = new boolean[4][2];

        // Initialize inputs (all start as question marks)
        for (int layer = 0; layer < 4; layer++) {
            for (int input = 0; input < 2; input++) {
                inputs[layer][input] = false;
                isQuestionMark[layer][input] = true; // Start as question marks
            }
        }

        // Create input images for all layers (start with question marks)
        for (int layer = 0; layer < 4; layer++) {
            for (int input = 0; input < 2; input++) {
                inputImages[layer][input] = new Image(questionMarkTexture);
            }
        }

        // Create operator images for all layers
        operatorImages[0] = new Image(andTexture);
        operatorImages[1] = new Image(orTexture);
        operatorImages[2] = new Image(xorTexture);
        operatorImages[3] = new Image(notTexture);

        // Create result cube images for all layers
        for (int layer = 0; layer < 4; layer++) {
            resultCubeImages[layer] = new Image(cubePlainTexture);
        }

        // Create labels
        instructionLabel = new Label("Solve all 4 layers to complete the puzzle:", corebringer.testskin);
        instructionLabel.setColor(Color.WHITE);
        instructionLabel.setFontScale(1.5f);

        // Create congratulations image
        congratulationsImage = new Image(congratulationsTexture);
        congratulationsImage.setVisible(false);

        // Create result labels for each layer
        for (int layer = 0; layer < 4; layer++) {
            resultLabels[layer] = new Label("Layer " + (layer + 1) + ": ", corebringer.testskin);
            resultLabels[layer].setColor(Color.WHITE);
            resultLabels[layer].setFontScale(1.0f);
        }

        // Create back button
        backButton = new TextButton("Back to Map", corebringer.testskin);

        // Setup input listeners for all layers
        setupInputToggleListeners();
        // Add button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });
    }

    private void setupInputToggleListeners() {
        // Setup listeners for all layers and inputs
        for (int layer = 0; layer < 4; layer++) {
            for (int input = 0; input < 2; input++) {
                final int currentLayer = layer;
                final int currentInput = input;

                inputImages[layer][input].addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // Cycle: Question Mark → 0 → 1 → Question Mark
                        if (isQuestionMark[currentLayer][currentInput]) {
                            // Question Mark → 0
                            isQuestionMark[currentLayer][currentInput] = false;
                            inputs[currentLayer][currentInput] = false;
                            inputImages[currentLayer][currentInput].setDrawable(
                                new TextureRegionDrawable(input0Texture)
                            );
                        } else if (!inputs[currentLayer][currentInput]) {
                            // 0 → 1
                            inputs[currentLayer][currentInput] = true;
                            inputImages[currentLayer][currentInput].setDrawable(
                                new TextureRegionDrawable(input1Texture)
                            );
                        } else {
                            // 1 → Question Mark
                            isQuestionMark[currentLayer][currentInput] = true;
                            inputs[currentLayer][currentInput] = false;
                            inputImages[currentLayer][currentInput].setDrawable(
                                new TextureRegionDrawable(questionMarkTexture)
                            );
                        }
                        updateResult(currentLayer);
                    }
                });
            }
        }
    }

    private void updateResult(int layer) {
        boolean result = false;
        String operator = operators[layer];
        boolean hasQuestionMark = isQuestionMark[layer][0] || isQuestionMark[layer][1];

        // If any input is question mark, show plain cube
        if (hasQuestionMark) {
            resultCubeImages[layer].setDrawable(new TextureRegionDrawable(cubePlainTexture));
            resultLabels[layer].setText("Layer " + (layer + 1) + " (" + operator + "): ?");
            layerSolved[layer] = false;
        } else {
            // Calculate result based on operator
            if (operator.equals("&&")) {
                result = inputs[layer][0] && inputs[layer][1];
            } else if (operator.equals("||")) {
                result = inputs[layer][0] || inputs[layer][1];
            } else if (operator.equals("^")) {
                result = inputs[layer][0] ^ inputs[layer][1];
            } else if (operator.equals("!")) {
                result = !inputs[layer][0]; // NOT only uses first input
            }

            resultLabels[layer].setText("Layer " + (layer + 1) + " (" + operator + "): " + result);

            // Update cube color based on result
            if (result) {
                resultCubeImages[layer].setDrawable(new TextureRegionDrawable(cubeGreenTexture));
                layerSolved[layer] = true;
            } else {
                resultCubeImages[layer].setDrawable(new TextureRegionDrawable(cubeRedTexture));
                layerSolved[layer] = false;
            }
        }

        // Check if all layers are solved
        checkAllPuzzlesSolved();
    }

    private void checkAllPuzzlesSolved() {
        allPuzzlesSolved = true;
        for (int i = 0; i < 4; i++) {
            if (!layerSolved[i]) {
                allPuzzlesSolved = false;
                break;
            }
        }

        if (allPuzzlesSolved) {
            // Show congratulations after 3 seconds delay
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Gdx.app.postRunnable(() -> {
                        congratulationsImage.setVisible(true);
                        // Auto-return to map after another 3 seconds
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                Gdx.app.postRunnable(() -> {
                                    corebringer.setScreen(corebringer.gameMapScreen);
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void setupLayout() {
        // Add background
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setScaling(com.badlogic.gdx.utils.Scaling.fill);
        mainTable.setBackground(new TextureRegionDrawable(backgroundTexture));

        // Add instruction label
        puzzleTable.add(instructionLabel).colspan(3).padTop(20f).row();

        // Add 4 layers with proper spacing (2 inches = ~144 pixels at 72 DPI)
        for (int layer = 0; layer < 4; layer++) {
            // Create layer table
            Table layerTable = new Table();

            // Add input images (30% reduced from 216x151 = ~151x106 pixels)
            layerTable.add(inputImages[layer][0]).size(151, 106).pad(10f);
            layerTable.add(operatorImages[layer]).size(151, 106).pad(10f);
            layerTable.add(inputImages[layer][1]).size(151, 106).pad(10f);
            // Add result cube horizontally aligned with numbers (30% reduced size)
            layerTable.add(resultCubeImages[layer]).size(151, 106).pad(10f).row();

            // Add result label
            layerTable.add(resultLabels[layer]).colspan(4).padTop(10f).row();

            // Add layer to main puzzle table with reduced spacing between layers
            puzzleTable.add(layerTable).padBottom(10f).row();
        }

        // Add puzzle table to main table
        mainTable.add(puzzleTable).center();

        // Add back button to upper left corner
        backButton.setPosition(20f, puzzleStage.getHeight() - backButton.getHeight() - 20f);
        puzzleStage.addActor(backButton);

        // Add congratulations image to middle right
        congratulationsImage.setPosition(puzzleStage.getWidth() - congratulationsImage.getWidth() - 20f, 
                                       puzzleStage.getHeight() / 2 - congratulationsImage.getHeight() / 2);
        puzzleStage.addActor(congratulationsImage);

        // Initialize results for all layers
        for (int layer = 0; layer < 4; layer++) {
            updateResult(layer);
        }
    }

    @Override
    public void show() {
        // Reset all inputs to question marks for all layers
        for (int layer = 0; layer < 4; layer++) {
            for (int input = 0; input < 2; input++) {
                inputs[layer][input] = false;
                isQuestionMark[layer][input] = true;
                inputImages[layer][input].setDrawable(new TextureRegionDrawable(questionMarkTexture));
            }
            layerSolved[layer] = false;
        }

        // Hide congratulations image
        congratulationsImage.setVisible(false);
        allPuzzlesSolved = false;

        // Update results for all layers
        for (int layer = 0; layer < 4; layer++) {
            updateResult(layer);
        }

        Gdx.input.setInputProcessor(puzzleStage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        puzzleStage.act(delta);
        puzzleStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        puzzleStage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(puzzleStage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        puzzleStage.dispose();
        input1Texture.dispose();
        input0Texture.dispose();
        questionMarkTexture.dispose();
        andTexture.dispose();
        orTexture.dispose();
        xorTexture.dispose();
        notTexture.dispose();
        cubePlainTexture.dispose();
        cubeGreenTexture.dispose();
        cubeRedTexture.dispose();
        backgroundTexture.dispose();
        congratulationsTexture.dispose();
    }
}
