package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.SaveData;
import com.altf4studios.corebringer.utils.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Treasure chest arithmetic puzzle with multiple variations:
 * - 10 ? 3 ? 5 = 6
 * - 5 ? 6 ? 10 = 3  
 * - 6 ? 5 ? 10 = 3
 * - Two operator slots, both clickable.
 * - Operators cycle: QuestionMark -> Multiply (x) -> Divide (÷) -> Add (+) -> Subtract (-) -> QuestionMark
 * - Chest is closed by default; opens when expression equals target.
 */
public class TreasurePuzzleScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Table root;
    private Table content;

    private Image num1Img;
    private Image num2Img;
    private Image num3Img;
    private Image targetImg;
    private Image op1Img;
    private Image op2Img;
    private Image equalsImg;
    private Image chestImg;
    private Image congratulationsImage;
    private Image subtractionImg;
    private Label rewardLabel;
    private Label instructionLabel;


    private Texture subTexture;
    private Texture bgTexture;
    private Texture qTexture;
    private Texture mulTexture;
    private Texture divTexture;
    private Texture plusTexture;
    private Texture n1Texture;
    private Texture n2Texture;
    private Texture n3Texture;
    private Texture targetTexture;
    private Texture equalsTexture;
    private Texture chestClosedTexture;
    private Texture chestOpenTexture;
    private Texture congratulationsTexture;

    private int op1State = 0; // 0 = ?, 1 = x, 2 = ÷, 3 = +, 4 = -
    private int op2State = 0; // 0 = ?, 1 = x, 2 = ÷, 3 = +, 4 = -

    private TextButton backButton;
    private Label titleLabel;
    private boolean puzzleSolved = false;
    
    // Puzzle data structure
    private static class PuzzleData {
        int num1, num2, num3, target;
        String description;
        
        PuzzleData(int num1, int num2, int num3, int target, String description) {
            this.num1 = num1;
            this.num2 = num2;
            this.num3 = num3;
            this.target = target;
            this.description = description;
        }
    }
    
    private PuzzleData[] puzzles = {
        new PuzzleData(10, 3, 5, 6, "Choose the operators to make 6"),
        new PuzzleData(5, 6, 10, 3, "Choose the operators to make 3"),
        new PuzzleData(6, 5, 10, 3, "Choose the operators to make 3")
    };
    
    private PuzzleData currentPuzzle;

    public TreasurePuzzleScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Select a random puzzle
        currentPuzzle = puzzles[com.badlogic.gdx.math.MathUtils.random(puzzles.length - 1)];
        
        loadTextures();
        buildUI();
    }

    private void loadTextures() {
        bgTexture = new Texture(Utils.getInternalPath("assets/Puzzle/woodThemeBG.png"));
        qTexture = new Texture(Utils.getInternalPath("assets/Puzzle/QuestionMark.png"));
        mulTexture = new Texture(Utils.getInternalPath("assets/Puzzle/x.png"));
        divTexture = new Texture(Utils.getInternalPath("assets/Puzzle/divisionSign.png"));
        plusTexture = new Texture(Utils.getInternalPath("assets/Puzzle/plusSign.png"));
        subTexture = new Texture(Utils.getInternalPath("assets/Puzzle/subtractionSign.png"));
        equalsTexture = new Texture(Utils.getInternalPath("assets/Puzzle/equalSign.png"));
        chestClosedTexture = new Texture(Utils.getInternalPath("assets/Puzzle/closedTreasureChest.png"));
        chestOpenTexture = new Texture(Utils.getInternalPath("assets/Puzzle/openTreasureChest.png"));
        congratulationsTexture = new Texture(Utils.getInternalPath("assets/Puzzle/Congratulations.png"));
        
        // Load number textures based on current puzzle
        n1Texture = new Texture(Utils.getInternalPath("assets/Puzzle/" + currentPuzzle.num1 + ".png"));
        n2Texture = new Texture(Utils.getInternalPath("assets/Puzzle/" + currentPuzzle.num2 + ".png"));
        n3Texture = new Texture(Utils.getInternalPath("assets/Puzzle/" + currentPuzzle.num3 + ".png"));
        targetTexture = new Texture(Utils.getInternalPath("assets/Puzzle/" + currentPuzzle.target + ".png"));
    }

    private void buildUI() {
        // Background
        root.setBackground(new TextureRegionDrawable(bgTexture));

        // Title and back
        titleLabel = new Label(currentPuzzle.description, corebringer.testskin);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setFontScale(1.2f);
        backButton = new TextButton("Back to Map", corebringer.testskin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (corebringer.gameMapScreen != null) {
                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                }
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });

        Table topBar = new Table();
        topBar.add(titleLabel).expandX().left().pad(15f);
        topBar.add(backButton).right().pad(15f);

        // Create instruction label
        instructionLabel = new Label("INSTRUCTION: Click on the operator boxes (?) to change them!\nCycle through: ? → × → ÷ → + → -\nSelect the correct operators to make the expression equal the target number.", corebringer.testskin);
        instructionLabel.setColor(Color.WHITE);
        instructionLabel.setFontScale(1.8f);

        // Expression row: num1 op num2 op num3 = target   [chest]
        num1Img = new Image(n1Texture);
        num2Img = new Image(n2Texture);
        num3Img = new Image(n3Texture);
        targetImg = new Image(targetTexture);
        equalsImg = new Image(equalsTexture);

        op1Img = new Image(qTexture);
        op2Img = new Image(qTexture);
        op1Img.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                op1State = (op1State + 1) % 5; // ?, x, ÷, +, -
                refreshOperator(op1Img, op1State);
                updateChest();
            }
        });
        op2Img.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                op2State = (op2State + 1) % 5; // ?, x, ÷, +, -
                refreshOperator(op2Img, op2State);
                updateChest();
            }
        });

        chestImg = new Image(chestClosedTexture);
        congratulationsImage = new Image(congratulationsTexture);
        congratulationsImage.setVisible(false);
        
        // Create reward label
        rewardLabel = new Label("Reward: +50 Gold", corebringer.testskin);
        rewardLabel.setColor(Color.GOLD);
        rewardLabel.setFontScale(1.5f);
        rewardLabel.setVisible(false);

        Table expr = new Table();
        expr.add(num1Img).size(140, 98).pad(6f);
        expr.add(op1Img).size(120, 84).pad(6f);
        expr.add(num2Img).size(140, 98).pad(6f);
        expr.add(op2Img).size(120, 84).pad(6f);
        expr.add(num3Img).size(140, 98).pad(6f);
        expr.add(equalsImg).size(140, 98).pad(6f);
        expr.add(targetImg).size(140, 98).pad(6f);

        content = new Table();
        content.add(expr).padTop(30f).row();
        content.add(chestImg).size(256, 192).padTop(20f).row();
        // Center congratulations image and reward label over content
        root.addActor(congratulationsImage);
        congratulationsImage.setPosition(stage.getWidth() / 2f - congratulationsImage.getWidth() / 2f,
            stage.getHeight() / 2f - congratulationsImage.getHeight() / 2f);
        
        root.addActor(rewardLabel);
        rewardLabel.setPosition(stage.getWidth() / 2f - rewardLabel.getWidth() / 2f,
            stage.getHeight() / 2f - rewardLabel.getHeight() / 2f);

        root.top();
        root.add(topBar).growX().row();
        root.add(instructionLabel).padTop(15f).row();
        root.add(content).expand().center();

        updateChest();
    }

    private void refreshOperator(Image target, int state) {
        TextureRegionDrawable drawable;
        switch (state) {
            case 1: drawable = new TextureRegionDrawable(mulTexture); break;
            case 2: drawable = new TextureRegionDrawable(divTexture); break;
            case 3: drawable = new TextureRegionDrawable(plusTexture); break;
            case 4: drawable = new TextureRegionDrawable(subTexture); break;
            default: drawable = new TextureRegionDrawable(qTexture); break;
        }
        target.setDrawable(drawable);
    }

    private void updateChest() {
        boolean hasQuestion = (op1State == 0) || (op2State == 0);
        boolean isCorrect = (!hasQuestion) && evaluatesToTarget(op1State, op2State);
        chestImg.setDrawable(new TextureRegionDrawable(isCorrect ? chestOpenTexture : chestClosedTexture));

        if (isCorrect && !puzzleSolved) {
            puzzleSolved = true;
            triggerCongratulationsAndReturn();
        }
    }

    private boolean evaluatesToTarget(int op1, int op2) {
        // Map: 1 => multiply, 2 => divide, 3 => add, 4 => subtract
        float a = currentPuzzle.num1;
        float b = currentPuzzle.num2;
        float c = currentPuzzle.num3;
        float first;
        if (op1 == 1) {
            first = a * b;
        } else if (op1 == 2) {
            first = a / b;
        } else if (op1 == 3) {
            first = a + b;
        } else if (op1 == 4) {
            first = a - b;
        } else {
            return false; // Invalid operator
        }

        float result;
        if (op2 == 1) {
            result = first * c;
        } else if (op2 == 2) {
            result = first / c;
        } else if (op2 == 3) {
            result = first + c;
        } else if (op2 == 4) {
            result = first - c;
        } else {
            return false; // Invalid operator
        }

        return Math.abs(result - currentPuzzle.target) < 0.0001f;
    }

    @Override
    public void show() {
        // Reset to defaults
        op1State = 0;
        op2State = 0;
        puzzleSolved = false;
        congratulationsImage.setVisible(false);
        rewardLabel.setVisible(false);
        refreshOperator(op1Img, op1State);
        refreshOperator(op2Img, op2State);
        updateChest();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); Gdx.input.setInputProcessor(stage); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        bgTexture.dispose();
        qTexture.dispose();
        mulTexture.dispose();
        divTexture.dispose();
        plusTexture.dispose();
        n1Texture.dispose();
        n2Texture.dispose();
        n3Texture.dispose();
        targetTexture.dispose();
        subTexture.dispose();
        equalsTexture.dispose();
        chestClosedTexture.dispose();
        chestOpenTexture.dispose();
        congratulationsTexture.dispose();
    }

    private void triggerCongratulationsAndReturn() {
        // Give rewards immediately
        giveRewards();
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Gdx.app.postRunnable(() -> {
                    if (content != null) content.setVisible(false);
                    congratulationsImage.setVisible(false); // Hide congratulations
                    rewardLabel.setVisible(true); // Show reward instead
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            Gdx.app.postRunnable(() -> {
                                if (corebringer.gameMapScreen != null) {
                                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                                }
                                corebringer.setScreen(corebringer.gameMapScreen);
                                Gdx.app.postRunnable(TreasurePuzzleScreen.this::dispose);
                            });
                        } catch (InterruptedException ignored) { }
                    }).start();
                });
            } catch (InterruptedException ignored) { }
        }).start();
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
}


