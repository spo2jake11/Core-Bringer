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

public class PuzzleScreen implements Screen {
    private Main corebringer;
    private Stage puzzleStage;
    private Table mainTable;
    private Table puzzleTable;
    private Table buttonTable;

    // Puzzle elements
    private ImageButton input1Image;
    private ImageButton input0Image;
    private Image operatorImage;
    private Image resultCubeImage;
    private Label instructionLabel;
    private Label resultLabel;
//    private TextButton andButton;
//    private TextButton orButton;
    private TextButton backButton;

    // Puzzle state
    private boolean input1 = true;  // 1
    private boolean input0 = false; // 0
    private String currentOperator = "&&";
    private boolean puzzleSolved = false;

    // Textures
    private Texture input1Texture;
    private Texture input0Texture;
    private Texture andTexture;
    private Texture orTexture;
    private Texture cubePlainTexture;
    private Texture cubeGreenTexture;
    private Texture cubeRedTexture;
    private Texture backgroundTexture;

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
        andTexture = new Texture(Utils.getInternalPath("Puzzle/&&.png"));
        orTexture = new Texture(Utils.getInternalPath("Puzzle/OR.png"));
        cubePlainTexture = new Texture(Utils.getInternalPath("Puzzle/cubePlain.png"));
        cubeGreenTexture = new Texture(Utils.getInternalPath("Puzzle/cubeGreen.png"));
        cubeRedTexture = new Texture(Utils.getInternalPath("Puzzle/cubeRed.png"));
        backgroundTexture = new Texture(Utils.getInternalPath("Puzzle/stonePuzzleBG.png"));
    }

    private void initializeUI() {
        // Create puzzle table
        puzzleTable = new Table();

        // Create input images
        input1Image = new ImageButton(new TextureRegionDrawable(input1Texture));
        input0Image = new ImageButton(new TextureRegionDrawable(input0Texture));

        // Create operator image (default to &&)
        operatorImage = new Image(andTexture);

        // Create result cube image (default to plain)
        resultCubeImage = new Image(cubePlainTexture);

        // Create labels
        instructionLabel = new Label("Choose the correct operator to make the result TRUE:", corebringer.testskin);
        instructionLabel.setColor(Color.WHITE);
        instructionLabel.setFontScale(1.5f);

        resultLabel = new Label("Result: " + (input1 && input0), corebringer.testskin);
        resultLabel.setColor(Color.WHITE);
        resultLabel.setFontScale(1.2f);

        // Create operator buttons
//        andButton = new TextButton("&&", corebringer.testskin);
//        orButton = new TextButton("||", corebringer.testskin);

        // Create back button
        backButton = new TextButton("Back to Map", corebringer.testskin);

        // Add button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
//        andButton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                currentOperator = "&&";
//                operatorImage.setDrawable(new TextureRegionDrawable(andTexture));
//                updateResult();
//            }
//        });
//
//        orButton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                currentOperator = "||";
//                operatorImage.setDrawable(new TextureRegionDrawable(orTexture));
//                updateResult();
//            }
//        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });
    }

    private void updateResult() {
        boolean result;
        if (currentOperator.equals("&&")) {
            result = input1 && input0;
        } else {
            result = input1 || input0;
        }

        resultLabel.setText("Result: " + result);

        // Update cube color based on result
        if (result) {
            resultCubeImage.setDrawable(new TextureRegionDrawable(cubeGreenTexture));
            puzzleSolved = true;
        } else {
            resultCubeImage.setDrawable(new TextureRegionDrawable(cubeRedTexture));
            puzzleSolved = false;
        }
    }

    private void setupLayout() {
        // Add background
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setScaling(com.badlogic.gdx.utils.Scaling.fill);
        mainTable.setBackground(new TextureRegionDrawable(backgroundTexture));

        // Create button table
        buttonTable = new Table();
//        buttonTable.add(andButton).pad(10f);
//        buttonTable.add(orButton).pad(10f);

        // Add elements to puzzle table
        puzzleTable.add(instructionLabel).colspan(3).padBottom(30f).row();
        puzzleTable.add(input1Image).pad(20f);
        puzzleTable.add(operatorImage).pad(20f);
        puzzleTable.add(input0Image).pad(20f).row();
        puzzleTable.add(resultCubeImage).colspan(3).padTop(20f).row();
        puzzleTable.add(resultLabel).colspan(3).padTop(20f).row();
        puzzleTable.add(buttonTable).colspan(3).padTop(30f).row();
        puzzleTable.add(backButton).colspan(3).padTop(20f);

        // Add puzzle table to main table
        mainTable.add(puzzleTable).center();

        // Initialize result
        updateResult();
    }

    @Override
    public void show() {
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
        andTexture.dispose();
        orTexture.dispose();
        cubePlainTexture.dispose();
        cubeGreenTexture.dispose();
        cubeRedTexture.dispose();
        backgroundTexture.dispose();
    }
}
