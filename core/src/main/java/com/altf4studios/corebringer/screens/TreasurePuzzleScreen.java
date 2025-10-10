package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
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
 * Treasure chest arithmetic puzzle: 10 ? 3 ? 5 = 6
 * - Two operator slots, both clickable.
 * - Operators cycle: QuestionMark -> Multiply (x) -> Divide (÷) -> QuestionMark
 * - Chest is closed by default; opens when expression equals 6.
 */
public class TreasurePuzzleScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Table root;
    private Table content;

    private Image num10Img;
    private Image num3Img;
    private Image num5Img;
    private Image num6Img;
    private Image op1Img;
    private Image op2Img;
    private Image equalsImg;
    private Image chestImg;
    private Image congratulationsImage;

    private Texture bgTexture;
    private Texture qTexture;
    private Texture mulTexture;
    private Texture divTexture;
    private Texture plusTexture;
    private Texture n10Texture;
    private Texture n3Texture;
    private Texture n5Texture;
    private Texture n6Texture;
    private Texture equalsTexture;
    private Texture chestClosedTexture;
    private Texture chestOpenTexture;
    private Texture congratulationsTexture;

    private int op1State = 0; // 0 = ?, 1 = x, 2 = ÷, 3 = +
    private int op2State = 0; // 0 = ?, 1 = x, 2 = ÷, 3 = +

    private TextButton backButton;
    private Label titleLabel;
    private boolean puzzleSolved = false;

    public TreasurePuzzleScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        loadTextures();
        buildUI();
    }

    private void loadTextures() {
        bgTexture = new Texture(Utils.getInternalPath("Puzzle/woodThemeBG.png"));
        qTexture = new Texture(Utils.getInternalPath("Puzzle/QuestionMark.png"));
        mulTexture = new Texture(Utils.getInternalPath("Puzzle/x.png"));
        divTexture = new Texture(Utils.getInternalPath("Puzzle/divisionSign.png"));
        plusTexture = new Texture(Utils.getInternalPath("Puzzle/plusSign.png"));
        n10Texture = new Texture(Utils.getInternalPath("Puzzle/10.png"));
        n3Texture = new Texture(Utils.getInternalPath("Puzzle/3.png"));
        n5Texture = new Texture(Utils.getInternalPath("Puzzle/5.png"));
        n6Texture = new Texture(Utils.getInternalPath("Puzzle/6.png"));
        equalsTexture = new Texture(Utils.getInternalPath("Puzzle/equalSign.png"));
        chestClosedTexture = new Texture(Utils.getInternalPath("Puzzle/closedTreasureChest.png"));
        chestOpenTexture = new Texture(Utils.getInternalPath("Puzzle/openTreasureChest.png"));
        congratulationsTexture = new Texture(Utils.getInternalPath("Puzzle/Congratulations.png"));
    }

    private void buildUI() {
        // Background
        root.setBackground(new TextureRegionDrawable(bgTexture));

        // Title and back
        titleLabel = new Label("Choose the operators to make 6", corebringer.testskin);
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

        // Expression row: 10 op 3 op 5 = 6   [chest]
        num10Img = new Image(n10Texture);
        num3Img = new Image(n3Texture);
        num5Img = new Image(n5Texture);
        num6Img = new Image(n6Texture);
        equalsImg = new Image(equalsTexture);

        op1Img = new Image(qTexture);
        op2Img = new Image(qTexture);
        op1Img.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                op1State = (op1State + 1) % 4; // ?, x, ÷, +
                refreshOperator(op1Img, op1State);
                updateChest();
            }
        });
        op2Img.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                op2State = (op2State + 1) % 4; // ?, x, ÷, +
                refreshOperator(op2Img, op2State);
                updateChest();
            }
        });

        chestImg = new Image(chestClosedTexture);
        congratulationsImage = new Image(congratulationsTexture);
        congratulationsImage.setVisible(false);

        Table expr = new Table();
        expr.add(num10Img).size(140, 98).pad(6f);
        expr.add(op1Img).size(120, 84).pad(6f);
        expr.add(num3Img).size(140, 98).pad(6f);
        expr.add(op2Img).size(120, 84).pad(6f);
        expr.add(num5Img).size(140, 98).pad(6f);
        expr.add(equalsImg).size(140, 98).pad(6f);
        expr.add(num6Img).size(140, 98).pad(6f);

        content = new Table();
        content.add(expr).padTop(30f).row();
        content.add(chestImg).size(256, 192).padTop(20f).row();
        // Center congratulations image over content
        root.addActor(congratulationsImage);
        congratulationsImage.setPosition(stage.getWidth() / 2f - congratulationsImage.getWidth() / 2f,
            stage.getHeight() / 2f - congratulationsImage.getHeight() / 2f);

        root.top();
        root.add(topBar).growX().row();
        root.add(content).expand().center();

        updateChest();
    }

    private void refreshOperator(Image target, int state) {
        TextureRegionDrawable drawable;
        switch (state) {
            case 1: drawable = new TextureRegionDrawable(mulTexture); break;
            case 2: drawable = new TextureRegionDrawable(divTexture); break;
            case 3: drawable = new TextureRegionDrawable(plusTexture); break;
            default: drawable = new TextureRegionDrawable(qTexture); break;
        }
        target.setDrawable(drawable);
    }

    private void updateChest() {
        boolean hasQuestion = (op1State == 0) || (op2State == 0);
        boolean isCorrect = (!hasQuestion) && evaluatesToSix(op1State, op2State);
        chestImg.setDrawable(new TextureRegionDrawable(isCorrect ? chestOpenTexture : chestClosedTexture));

        if (isCorrect && !puzzleSolved) {
            puzzleSolved = true;
            triggerCongratulationsAndReturn();
        }
    }

    private boolean evaluatesToSix(int op1, int op2) {
        // Map: 1 => multiply, 2 => divide, 3 => add
        float a = 10f;
        float b = 3f;
        float c = 5f;
        float first;
        if (op1 == 1) {
            first = a * b;
        } else if (op1 == 2) {
            first = a / b;
        } else if (op1 == 3) {
            first = a + b;
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
        } else {
            return false; // Invalid operator
        }
        
        return Math.abs(result - 6f) < 0.0001f;
    }

    @Override
    public void show() {
        // Reset to defaults
        op1State = 0;
        op2State = 0;
        puzzleSolved = false;
        congratulationsImage.setVisible(false);
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
        n10Texture.dispose();
        n3Texture.dispose();
        n5Texture.dispose();
        n6Texture.dispose();
        equalsTexture.dispose();
        chestClosedTexture.dispose();
        chestOpenTexture.dispose();
        congratulationsTexture.dispose();
    }

    private void triggerCongratulationsAndReturn() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Gdx.app.postRunnable(() -> {
                    if (content != null) content.setVisible(false);
                    congratulationsImage.setVisible(true);
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            Gdx.app.postRunnable(() -> {
                                if (corebringer.gameMapScreen != null) {
                                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                                }
                                corebringer.setScreen(corebringer.gameMapScreen);
                            });
                        } catch (InterruptedException ignored) { }
                    }).start();
                });
            } catch (InterruptedException ignored) { }
        }).start();
    }
}


