package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.SaveData;
import com.altf4studios.corebringer.utils.SaveManager;
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

    // Puzzle elements for single expression: input op input op input op input = cube
    private Image[] inputImages; // 4 inputs
    private Image[] operatorImages; // 3 operators
    private Image resultCubeImage; // 1 result cube
    private Label instructionLabel;
    private Label resultLabel;
    private Label expressionLabel;
    private Label legendLabel;
    private Image congratulationsImage;
    private Label rewardLabel;
    private TextButton backButton;
    private TextButton makeOneButton;

    // Puzzle state for single expression
    private boolean[] inputs; // 4 inputs
    private boolean[] isQuestionMark; // track if input is question mark
    private String[] operators = {"&&", "||", "^"}; // AND, OR, XOR (no NOT)
    private int[] currentOperators = {-1, -1, -1}; // indices into operators array (-1 = question mark)
    private boolean puzzleSolved = false;

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

    private Texture getOperatorTextureForIndex(int idx) {
        switch (idx) {
            case 0: return andTexture;
            case 1: return orTexture;
            case 2: return xorTexture;
            default: return questionMarkTexture;
        }
    }

    private void solveForOne() {
        // Try all 16 combinations to find one that evaluates to true
        for (int mask = 0; mask < 16; mask++) {
            // Apply combination to inputs
            for (int i = 0; i < 4; i++) {
                boolean bit = ((mask >> i) & 1) == 1;
                inputs[i] = bit;
                isQuestionMark[i] = false;
                inputImages[i].setDrawable(new TextureRegionDrawable(bit ? input1Texture : input0Texture));
            }
            boolean result = evaluateExpression();
            if (result) {
                updateResult();
                return;
            }
        }
        // If no solution found (shouldn't happen for OR present), just set all 1s
        for (int i = 0; i < 4; i++) {
            inputs[i] = true;
            isQuestionMark[i] = false;
            inputImages[i].setDrawable(new TextureRegionDrawable(input1Texture));
        }
        updateResult();
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

        // Initialize arrays for single expression (4 inputs, 3 operators)
        inputImages = new Image[4];
        operatorImages = new Image[3];
        inputs = new boolean[4];
        isQuestionMark = new boolean[4];

        // Initialize inputs (all start as question marks)
        for (int i = 0; i < 4; i++) {
            inputs[i] = false;
            isQuestionMark[i] = true; // Start as question marks
        }

        // Create input images (start with question marks)
        for (int i = 0; i < 4; i++) {
            inputImages[i] = new Image(questionMarkTexture);
        }

        // Create operator images (randomized and non-clickable)
        for (int i = 0; i < 3; i++) {
            currentOperators[i] = MathUtils.random(0, 2);
            operatorImages[i] = new Image(getOperatorTextureForIndex(currentOperators[i]));
            operatorImages[i].setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }

        // Create result cube image
        resultCubeImage = new Image(cubePlainTexture);

        // Create labels
        instructionLabel = new Label("Operators randomized. Toggle inputs to reach: ? op ? op ? op ? = Cube (or press Make 1)", corebringer.testskin);
        instructionLabel.setColor(Color.WHITE);
        instructionLabel.setFontScale(1.2f);

        expressionLabel = new Label("", corebringer.testskin);
        expressionLabel.setColor(Color.YELLOW);
        expressionLabel.setFontScale(1.0f);

        resultLabel = new Label("Result: ?", corebringer.testskin);
        resultLabel.setColor(Color.WHITE);
        resultLabel.setFontScale(1.0f);

        // Create legend label
        legendLabel = new Label("Legend: ^ = XOR, || = OR, && = AND", corebringer.testskin);
        legendLabel.setColor(Color.CYAN);
        legendLabel.setFontScale(1.0f);

        // Create reward label
        rewardLabel = new Label("Reward: +40 Gold, +15 HP", corebringer.testskin);
        rewardLabel.setColor(Color.GOLD);
        rewardLabel.setFontScale(1.5f);
        rewardLabel.setVisible(false);

        // Create congratulations image
        congratulationsImage = new Image(congratulationsTexture);
        congratulationsImage.setVisible(false);

        // Create back button
        backButton = new TextButton("Back to Map", corebringer.testskin);
        // Create make-one button
        makeOneButton = new TextButton("Make 1", corebringer.testskin);

        // Setup input listeners
        setupInputToggleListeners();
        // Operators are randomized and non-clickable; do not attach listeners
        // Add button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (corebringer.gameMapScreen != null) {
                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                }
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });
        makeOneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                solveForOne();
            }
        });
    }

    private void setupInputToggleListeners() {
        // Setup listeners for all 4 inputs
        for (int i = 0; i < 4; i++) {
            final int currentInput = i;

            inputImages[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Cycle: Question Mark → 0 → 1 → Question Mark
                    if (isQuestionMark[currentInput]) {
                        // Question Mark → 0
                        isQuestionMark[currentInput] = false;
                        inputs[currentInput] = false;
                        inputImages[currentInput].setDrawable(
                            new TextureRegionDrawable(input0Texture)
                        );
                    } else if (!inputs[currentInput]) {
                        // 0 → 1
                        inputs[currentInput] = true;
                        inputImages[currentInput].setDrawable(
                            new TextureRegionDrawable(input1Texture)
                        );
                    } else {
                        // 1 → Question Mark
                        isQuestionMark[currentInput] = true;
                        inputs[currentInput] = false;
                        inputImages[currentInput].setDrawable(
                            new TextureRegionDrawable(questionMarkTexture)
                        );
                    }
                    updateResult();
                }
            });
        }
    }

    // Operators are randomized; no toggle listeners

    private void updateResult() {
        // Check if any input or operator is question mark
        boolean hasQuestionMark = false;
        for (int i = 0; i < 4; i++) {
            if (isQuestionMark[i]) {
                hasQuestionMark = true;
                break;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (currentOperators[i] == -1) {
                hasQuestionMark = true;
                break;
            }
        }

        // Build expression string for display
        StringBuilder expressionBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (isQuestionMark[i]) {
                expressionBuilder.append("?");
            } else {
                expressionBuilder.append(inputs[i] ? "1" : "0");
            }

            if (i < 3) { // Add operator after first 3 inputs
                if (currentOperators[i] == -1) {
                    expressionBuilder.append(" ? ");
                } else {
                    expressionBuilder.append(" ").append(operators[currentOperators[i]]).append(" ");
                }
            }
        }
        expressionBuilder.append(" = ");

        // If any input or operator is question mark, show plain cube
        if (hasQuestionMark) {
            resultCubeImage.setDrawable(new TextureRegionDrawable(cubePlainTexture));
            expressionLabel.setText(expressionBuilder.toString() + "?");
            resultLabel.setText("Result: ?");
            puzzleSolved = false;
        } else {
            // Evaluate expression with proper operator precedence
            // Expression: input[0] op[0] input[1] op[1] input[2] op[2] input[3]
            boolean result = evaluateExpression();

            expressionLabel.setText(expressionBuilder.toString() + (result ? "1" : "0"));
            resultLabel.setText("Result: " + (result ? "1" : "0"));

            // Update cube color based on result
            if (result) {
                resultCubeImage.setDrawable(new TextureRegionDrawable(cubeGreenTexture));
                puzzleSolved = true;
            } else {
                resultCubeImage.setDrawable(new TextureRegionDrawable(cubeRedTexture));
                puzzleSolved = false;
            }
        }

        // Check if puzzle is solved (result is true/1)
        checkPuzzleSolved();
    }

    private boolean evaluateExpression() {
        // Evaluate: input[0] op[0] input[1] op[1] input[2] op[2] input[3]
        // Following standard operator precedence: && has higher precedence than ||, ^ has same as &&
        // We'll evaluate left to right for same precedence operators

        boolean result = inputs[0];

        for (int i = 0; i < 3; i++) {
            String op = operators[currentOperators[i]];
            boolean nextInput = inputs[i + 1];

            if (op.equals("&&")) {
                result = result && nextInput;
            } else if (op.equals("||")) {
                result = result || nextInput;
            } else if (op.equals("^")) {
                result = result ^ nextInput;
            }
        }

        return result;
    }

    private void checkPuzzleSolved() {
        if (puzzleSolved) {
            // Give rewards immediately
            giveRewards();
            
            // Show reward after 2 seconds delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Gdx.app.postRunnable(() -> {
                        congratulationsImage.setVisible(false); // Hide congratulations
                        rewardLabel.setVisible(true); // Show reward instead
                        // Auto-return to map after another 3 seconds
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                Gdx.app.postRunnable(() -> {
                                    if (corebringer.gameMapScreen != null) {
                                        try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                                    }
                                    corebringer.setScreen(corebringer.gameMapScreen);
                                    Gdx.app.postRunnable(PuzzleScreen.this::dispose);
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

    private void giveRewards() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            return; // No save found, skip rewards
        }
        
        // Remove health reward, only give 50 gold
        int newGold = stats.gold + 50;
        
        SaveManager.saveStats(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            stats.cards,
            stats.battleWon,
            newGold
        );
    }

    private void setupLayout() {
        // Add background
        mainTable.setBackground(new TextureRegionDrawable(backgroundTexture));

        // Add instruction label
        puzzleTable.add(instructionLabel).colspan(9).padTop(20f).padBottom(20f).row();

        // Create expression table for horizontal layout: input op input op input op input = cube
        Table expressionTable = new Table();

        // Add first input
        expressionTable.add(inputImages[0]).size(120, 84).pad(5f);

        // Add operator 1
        expressionTable.add(operatorImages[0]).size(120, 84).pad(5f);

        // Add second input
        expressionTable.add(inputImages[1]).size(120, 84).pad(5f);

        // Add operator 2
        expressionTable.add(operatorImages[1]).size(120, 84).pad(5f);

        // Add third input
        expressionTable.add(inputImages[2]).size(120, 84).pad(5f);

        // Add operator 3
        expressionTable.add(operatorImages[2]).size(120, 84).pad(5f);

        // Add fourth input
        expressionTable.add(inputImages[3]).size(120, 84).pad(5f);

        // Add equals label
        Label equalsLabel = new Label("=", corebringer.testskin);
        equalsLabel.setColor(Color.WHITE);
        equalsLabel.setFontScale(2.0f);
        expressionTable.add(equalsLabel).pad(10f);

        // Add result cube
        expressionTable.add(resultCubeImage).size(120, 84).pad(5f);

        // Add expression table to puzzle table
        puzzleTable.add(expressionTable).padBottom(20f).row();

        // Add expression label (shows the current expression)
        puzzleTable.add(expressionLabel).colspan(9).padBottom(10f).row();

        // Add result label
        puzzleTable.add(resultLabel).colspan(9).padBottom(10f).row();

        // Add legend label
        puzzleTable.add(legendLabel).colspan(9).padBottom(20f).row();

        // Add puzzle table to main table
        mainTable.add(puzzleTable).center().row();
        // Add Make 1 button below puzzle
        mainTable.add(makeOneButton).padTop(15f).center();

        // Add back button to upper left corner
        backButton.setPosition(20f, puzzleStage.getHeight() - backButton.getHeight() - 20f);
        puzzleStage.addActor(backButton);

        // Add congratulations image to center
        congratulationsImage.setPosition(puzzleStage.getWidth() / 2 - congratulationsImage.getWidth() / 2,
                                       puzzleStage.getHeight() / 2 - congratulationsImage.getHeight() / 2);
        puzzleStage.addActor(congratulationsImage);

        // Add reward label to center (initially hidden)
        rewardLabel.setPosition(puzzleStage.getWidth() / 2 - rewardLabel.getWidth() / 2,
                               puzzleStage.getHeight() / 2 - rewardLabel.getHeight() / 2);
        puzzleStage.addActor(rewardLabel);

        // Initialize result
        updateResult();
    }

    @Override
    public void show() {
        // Reset all inputs to question marks
        for (int i = 0; i < 4; i++) {
            inputs[i] = false;
            isQuestionMark[i] = true;
            inputImages[i].setDrawable(new TextureRegionDrawable(questionMarkTexture));
        }

        // Randomize and show operators (non-clickable)
        for (int i = 0; i < 3; i++) {
            currentOperators[i] = MathUtils.random(0, 2);
            operatorImages[i].setDrawable(new TextureRegionDrawable(getOperatorTextureForIndex(currentOperators[i])));
            operatorImages[i].setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }

        // Hide congratulations image and reward label
        congratulationsImage.setVisible(false);
        rewardLabel.setVisible(false);
        puzzleSolved = false;

        // Update result
        updateResult();

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
        input1Texture.dispose();
        input0Texture.dispose();
        questionMarkTexture.dispose();
        andTexture.dispose();
        orTexture.dispose();
        xorTexture.dispose();
        notTexture.dispose(); // Still dispose it even though we don't use it
        cubePlainTexture.dispose();
        cubeGreenTexture.dispose();
        cubeRedTexture.dispose();
        backgroundTexture.dispose();
        congratulationsTexture.dispose();
    }
}
