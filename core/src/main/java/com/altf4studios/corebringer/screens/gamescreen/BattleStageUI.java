package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class BattleStageUI {
    private Stage battleStage;
    private Skin skin;
    private TextureAtlas enemyAtlas;
    private Array<String> enemyNames;
    private String currentEnemyName;
    private Image enemyImageBG;
    private Stack enemyTemplateStack;
    private Label userHpLabel;
    private Label enemyHpLabel;
    private Label turnIndicatorLabel;

    public BattleStageUI(Stage battleStage, Skin skin) {
        this.battleStage = battleStage;
        this.skin = skin;
        loadEnemyAtlas();
        setupBattleUI();
        Gdx.app.log("BattleStageUI", "Battle stage UI initialized successfully");
    }

    private void loadEnemyAtlas() {
        try {
            enemyAtlas = new TextureAtlas(Gdx.files.internal("basic-characters/normal_mob/normal_mobs.atlas"));
            enemyNames = new Array<>();

            // Add all enemy names from the atlas
            enemyNames.add("Experiment");
            enemyNames.add("Probe");
            enemyNames.add("Shock_slime");
            enemyNames.add("duckling_gun");
            enemyNames.add("liquid_metal");
            enemyNames.add("nanite");
            enemyNames.add("standard_slime");

            Gdx.app.log("BattleStageUI", "Loaded " + enemyNames.size + " enemies from atlas");
        } catch (Exception e) {
            Gdx.app.error("BattleStageUI", "Error loading enemy atlas: " + e.getMessage());
            enemyNames = new Array<>();
            enemyNames.add("merchant"); // fallback
        }
    }

    private String getRandomEnemyName() {
        if (enemyNames.size == 0) {
            return "merchant"; // fallback
        }
        int randomIndex = (int) (Math.random() * enemyNames.size);
        return enemyNames.get(randomIndex);
    }

    private Image createEnemyImage(String enemyName) {
        Image enemyImage;

        if (enemyAtlas != null && enemyAtlas.findRegion(enemyName) != null) {
            // Use enemy from atlas
            TextureRegion enemyRegion = enemyAtlas.findRegion(enemyName);
            enemyImage = new Image(enemyRegion);
            Gdx.app.log("BattleStageUI", "Using enemy from atlas: " + enemyName);
        } else {
            // Fallback to merchant texture
            Texture enemyTexture = new Texture(Gdx.files.internal("basic-characters/merchant.png"));
            enemyImage = new Image(enemyTexture);
            Gdx.app.log("BattleStageUI", "Using fallback enemy texture for: " + enemyName);
        }

        enemyImage.setSize(200, 200);
        return enemyImage;
    }

    private void setupBattleUI() {
        Texture bg = new Texture(Gdx.files.internal("backgrounds/Stage1_bg.png"));
        Drawable bgDraw = new TextureRegionDrawable(new TextureRegion(bg));

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setBackground(bgDraw);
        actionTable.setFillParent(true);

        // HP Labels
        userHpLabel = new Label("100", skin);
        enemyHpLabel = new Label("100", skin);
        Label userTemplate = new Label("", skin);
        Label enemyTemplate = new Label("", skin);
        
        // Turn indicator
        turnIndicatorLabel = new Label("Player's Turn", skin);
        turnIndicatorLabel.setAlignment(Align.center);

        // HP Stacks
        Stack userHpStack = new Stack();
        userHpLabel.setAlignment(Align.center);
        userHpStack.add(userHpLabel);

        Stack enemyHpStack = new Stack();
        enemyHpLabel.setAlignment(Align.center);
        enemyHpStack.add(enemyHpLabel);

        userTemplate.setAlignment(Align.center);
        enemyTemplate.setAlignment(Align.center);

        // Character images
        Texture playerTexture = new Texture(Gdx.files.internal("basic-characters/hero.png"));
        Image userImageBG = new Image(playerTexture);
        userImageBG.setSize(200, 200);

        // Random enemy selection
        currentEnemyName = getRandomEnemyName();
        enemyImageBG = createEnemyImage(currentEnemyName);

        Stack userTemplateStack = new Stack();
        userTemplateStack.add(userImageBG);
        userTemplateStack.add(userTemplate);
        enemyTemplateStack = new Stack();
        enemyTemplateStack.add(enemyImageBG);
        enemyTemplateStack.add(enemyTemplate);

        actionTable.defaults().padTop(50);
        actionTable.add(userHpStack).height(25).width(200).padLeft(50);
        actionTable.add(enemyHpStack).height(25).width(200).padRight(50).row();
        
        // Add turn indicator
        actionTable.add(turnIndicatorLabel).colspan(2).height(30).padTop(20).row();
        
        actionTable.defaults().reset();
        actionTable.defaults().padTop(35);
        actionTable.add(userTemplateStack).height(200).width(200).pad(100).center();
        actionTable.add(enemyTemplateStack).height(200).width(200).pad(100).center();

        battleStage.addActor(actionTable);
    }
    /// HP Bar Updating mechanism
    public void updateHpBars(int playerHp, int enemyHp) {
        if (userHpLabel != null) {
            userHpLabel.setText(String.valueOf(playerHp));
        }
        if (enemyHpLabel != null) {
            enemyHpLabel.setText(String.valueOf(enemyHp));
        }
    }

    public void updateTurnIndicator(String turnText) {
        if (turnIndicatorLabel != null) {
            turnIndicatorLabel.setText(turnText);
        }
    }

    public String getCurrentEnemyName() {
        return currentEnemyName;
    }

    public void changeEnemy() {
        String randomEnemy = getRandomEnemyName();
        changeEnemy(randomEnemy);
        Gdx.app.log("BattleStageUI", "Randomly selected enemy: " + randomEnemy);
    }

    public void changeEnemy(String enemyName) {
        if (enemyName == null || enemyName.isEmpty()) {
            enemyName = getRandomEnemyName();
        }

        currentEnemyName = enemyName;

        // Update the enemy image
        if (enemyTemplateStack != null) {
            // Remove old enemy image
            enemyTemplateStack.clear();

            // Create new enemy image
            enemyImageBG = createEnemyImage(enemyName);

            // Add new enemy image and template label
            enemyTemplateStack.add(enemyImageBG);
            Label enemyTemplate = new Label("", skin);
            enemyTemplate.setAlignment(Align.center);
            enemyTemplateStack.add(enemyTemplate);
        }

        Gdx.app.log("BattleStageUI", "Changed enemy to: " + currentEnemyName);
    }

    public Array<String> getAvailableEnemies() {
        return new Array<>(enemyNames);
    }

    public boolean isEnemyAtlasLoaded() {
        return enemyAtlas != null;
    }

    public void dispose() {
        if (enemyAtlas != null) {
            enemyAtlas.dispose();
        }
    }
}
