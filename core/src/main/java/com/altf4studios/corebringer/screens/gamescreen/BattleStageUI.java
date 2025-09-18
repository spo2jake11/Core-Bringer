package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

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
    private Label userShieldLabel;
    private Label enemyShieldLabel;
    private Label turnIndicatorLabel;
	// Status badge references for visibility toggling
	private Stack userShieldBadge;
	private Stack userPoisonBadge;
	private Stack userBleedBadge;
	private Stack userStunBadge;
	private Stack enemyShieldBadge;
	private Stack enemyPoisonBadge;
	private Stack enemyBleedBadge;
	private Stack enemyStunBadge;
	// Numeric overlays for badges
	private Label userShieldNum;
	private Label userPoisonNum;
	private Label userBleedNum;
	private Label userStunNum;
	private Label enemyShieldNum;
	private Label enemyPoisonNum;
	private Label enemyBleedNum;
	private Label enemyStunNum;

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

        enemyImage.setScaling(Scaling.fit);
        enemyImage.setScale(0.7f);
        enemyImage.setAlign(Align.center);
        return enemyImage;
    }

    private void setupBattleUI() {
        Texture bg = new Texture(Gdx.files.internal("assets/backgrounds/stg1_bg.png"));
        Drawable bgDraw = new TextureRegionDrawable(new TextureRegion(bg));

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setBackground(bgDraw);
        actionTable.setFillParent(true);
        actionTable.padTop(75);

		// HP Labels
		userHpLabel = new Label("20", skin);
		enemyHpLabel = new Label("100", skin);
		// Shield Labels (blue)
		userShieldLabel = new Label("0", skin);
		userShieldLabel.setColor(Color.CYAN);
		enemyShieldLabel = new Label("0", skin);
		enemyShieldLabel.setColor(Color.CYAN);
		Label userTemplate = new Label("", skin);
		Label enemyTemplate = new Label("", skin);

        // Turn indicator
        turnIndicatorLabel = new Label("Player's Turn", skin);
        turnIndicatorLabel.setAlignment(Align.center);

		// HP Stacks (placed above templates)
		Stack userHpStack = new Stack();
		userHpLabel.setAlignment(Align.center);
		userShieldLabel.setAlignment(Align.center);
		Table userHpTable = new Table();
		userHpTable.add(userHpLabel).row();
		userHpTable.add(userShieldLabel);
		userHpStack.add(userHpTable);

		Stack enemyHpStack = new Stack();
		enemyHpLabel.setAlignment(Align.center);
		enemyShieldLabel.setAlignment(Align.center);
		Table enemyHpTable = new Table();
		enemyHpTable.add(enemyHpLabel).row();
		enemyHpTable.add(enemyShieldLabel);
		enemyHpStack.add(enemyHpTable);

        userTemplate.setAlignment(Align.center);
        enemyTemplate.setAlignment(Align.center);

        // Character images
        Texture playerTexture = new Texture(Gdx.files.internal("assets/basic-characters/hero.png"));
        Image userImageBG = new Image(playerTexture);
        userImageBG.setScaling(Scaling.contain);
        userImageBG.setAlign(Align.center);

        // Random enemy selection
        currentEnemyName = getRandomEnemyName();
        enemyImageBG = createEnemyImage(currentEnemyName);

		Stack userTemplateStack = new Stack();
		userTemplateStack.add(userImageBG);
		userTemplateStack.add(userTemplate);
		enemyTemplateStack = new Stack();
		enemyTemplateStack.add(enemyImageBG);
		enemyTemplateStack.add(enemyTemplate);

		// Status placeholders (square with status name) above HP stacks
		Table userStatusTable = new Table();
		userStatusTable.defaults().pad(5).size(40, 40);
		userShieldBadge = createStatusBadge("Shield", Color.CYAN);
		userPoisonBadge = createStatusBadge("Poison", Color.PURPLE);
		userBleedBadge = createStatusBadge("Bleed", Color.RED);
		userStunBadge = createStatusBadge("Stun", Color.YELLOW);
		userShieldBadge.setVisible(false);
		userPoisonBadge.setVisible(false);
		userBleedBadge.setVisible(false);
		userStunBadge.setVisible(false);
		// numeric overlays
		userShieldNum = createBadgeNumberOverlay(userShieldBadge);
		userPoisonNum = createBadgeNumberOverlay(userPoisonBadge);
		userBleedNum = createBadgeNumberOverlay(userBleedBadge);
		userStunNum = createBadgeNumberOverlay(userStunBadge);
		userStatusTable.add(userShieldBadge);
		userStatusTable.add(userPoisonBadge);
		userStatusTable.add(userBleedBadge);
		userStatusTable.add(userStunBadge);

		Table enemyStatusTable = new Table();
		enemyStatusTable.defaults().pad(5).size(40, 40);
		enemyShieldBadge = createStatusBadge("Shield", Color.CYAN);
		enemyPoisonBadge = createStatusBadge("Poison", Color.PURPLE);
		enemyBleedBadge = createStatusBadge("Bleed", Color.RED);
		enemyStunBadge = createStatusBadge("Stun", Color.YELLOW);
		enemyShieldBadge.setVisible(false);
		enemyPoisonBadge.setVisible(false);
		enemyBleedBadge.setVisible(false);
		enemyStunBadge.setVisible(false);
		// numeric overlays
		enemyShieldNum = createBadgeNumberOverlay(enemyShieldBadge);
		enemyPoisonNum = createBadgeNumberOverlay(enemyPoisonBadge);
		enemyBleedNum = createBadgeNumberOverlay(enemyBleedBadge);
		enemyStunNum = createBadgeNumberOverlay(enemyStunBadge);
		enemyStatusTable.add(enemyShieldBadge);
		enemyStatusTable.add(enemyPoisonBadge);
		enemyStatusTable.add(enemyBleedBadge);
		enemyStatusTable.add(enemyStunBadge);

		// Left and right columns
		Table leftColumn = new Table();
		leftColumn.add(userStatusTable).row();
		leftColumn.add(userHpStack).height(50).width(200).padTop(10).row();
		leftColumn.add(userTemplateStack).height(300).width(300).padTop(20);

		Table rightColumn = new Table();
		rightColumn.add(enemyStatusTable).row();
		rightColumn.add(enemyHpStack).height(50).width(200).padTop(10).row();
		rightColumn.add(enemyTemplateStack).height(300).width(300).padTop(20);

		// Layout on main action table
		actionTable.defaults().padTop(30);
		actionTable.add(turnIndicatorLabel).colspan(2).height(30).padTop(10).row();
		actionTable.add(leftColumn).expand().left().padLeft(300);
		actionTable.add(rightColumn).expand().right().padRight(300);

        battleStage.addActor(actionTable);
    }

	private Stack createStatusBadge(String name, Color color) {
		// Colored square with centered status name
		Drawable square = skin.newDrawable("white", color);
		Image img = new Image(square);
        img.setSize(15, 15);
		Stack stack = new Stack();
		stack.add(img);
		return stack;
	}

	private Label createBadgeNumberOverlay(Stack badgeStack) {
		Label num = new Label("", skin);
		num.setAlignment(Align.bottomRight);
		Container<Label> cont = new Container<Label>(num);
		cont.align(Align.bottomRight);
		cont.pad(1);
		badgeStack.add(cont);
		return num;
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

    public void updateShieldBars(int playerShield, int enemyShield) {
        if (userShieldLabel != null) {
            userShieldLabel.setText(String.valueOf(playerShield));
        }
        if (enemyShieldLabel != null) {
            enemyShieldLabel.setText(String.valueOf(enemyShield));
        }
    }

    public void updateTurnIndicator(String turnText) {
        if (turnIndicatorLabel != null) {
            turnIndicatorLabel.setText(turnText);
        }
    }

    public void updateHpColors(boolean isPlayerPoisoned, boolean isEnemyPoisoned) {
        if (userHpLabel != null) {
            userHpLabel.setColor(isPlayerPoisoned ? Color.PURPLE : Color.WHITE);
        }
        if (enemyHpLabel != null) {
            enemyHpLabel.setColor(isEnemyPoisoned ? Color.PURPLE : Color.WHITE);
        }
    }

    public void updateShieldColors(boolean isPlayerShielded, boolean isEnemyShielded) {
        if (userShieldLabel != null) {
            userShieldLabel.setColor(isPlayerShielded ? Color.CYAN : Color.GRAY);
        }
        if (enemyShieldLabel != null) {
            enemyShieldLabel.setColor(isEnemyShielded ? Color.CYAN : Color.GRAY);
        }
    }

	public void updatePlayerStatusBadges(boolean hasShield, boolean hasPoison, boolean hasBleed, boolean hasStun) {
		if (userShieldBadge != null) userShieldBadge.setVisible(hasShield);
		if (userPoisonBadge != null) userPoisonBadge.setVisible(hasPoison);
		if (userBleedBadge != null) userBleedBadge.setVisible(hasBleed);
		if (userStunBadge != null) userStunBadge.setVisible(hasStun);
	}

	public void updateEnemyStatusBadges(boolean hasShield, boolean hasPoison, boolean hasBleed, boolean hasStun) {
		if (enemyShieldBadge != null) enemyShieldBadge.setVisible(hasShield);
		if (enemyPoisonBadge != null) enemyPoisonBadge.setVisible(hasPoison);
		if (enemyBleedBadge != null) enemyBleedBadge.setVisible(hasBleed);
		if (enemyStunBadge != null) enemyStunBadge.setVisible(hasStun);
	}

	public void updatePlayerStatusValues(int shield, int poisonStacks, int bleedStacks, int stunDuration) {
		if (userShieldNum != null) userShieldNum.setText(shield > 0 ? String.valueOf(shield) : "");
		if (userPoisonNum != null) userPoisonNum.setText(poisonStacks > 0 ? String.valueOf(poisonStacks) : "");
		if (userBleedNum != null) userBleedNum.setText(bleedStacks > 0 ? String.valueOf(bleedStacks) : "");
		if (userStunNum != null) userStunNum.setText(stunDuration > 0 ? String.valueOf(stunDuration) : "");
	}

	public void updateEnemyStatusValues(int shield, int poisonStacks, int bleedStacks, int stunDuration) {
		if (enemyShieldNum != null) enemyShieldNum.setText(shield > 0 ? String.valueOf(shield) : "");
		if (enemyPoisonNum != null) enemyPoisonNum.setText(poisonStacks > 0 ? String.valueOf(poisonStacks) : "");
		if (enemyBleedNum != null) enemyBleedNum.setText(bleedStacks > 0 ? String.valueOf(bleedStacks) : "");
		if (enemyStunNum != null) enemyStunNum.setText(stunDuration > 0 ? String.valueOf(stunDuration) : "");
	}

	public void updatePlayerStatusValuesWithDuration(int shield, int poisonStacks, int poisonDuration, int bleedStacks, int bleedDuration, int stunDuration) {
		if (userShieldNum != null) userShieldNum.setText(shield > 0 ? String.valueOf(shield) : "");
		if (userPoisonNum != null) userPoisonNum.setText(poisonStacks > 0 ? (poisonDuration > 0 ? (poisonStacks + "|" + poisonDuration) : String.valueOf(poisonStacks)) : "");
		if (userBleedNum != null) userBleedNum.setText(bleedStacks > 0 ? (bleedDuration > 0 ? (bleedStacks + "|" + bleedDuration) : String.valueOf(bleedStacks)) : "");
		if (userStunNum != null) userStunNum.setText(stunDuration > 0 ? String.valueOf(stunDuration) : "");
	}

	public void updateEnemyStatusValuesWithDuration(int shield, int poisonStacks, int poisonDuration, int bleedStacks, int bleedDuration, int stunDuration) {
		if (enemyShieldNum != null) enemyShieldNum.setText(shield > 0 ? String.valueOf(shield) : "");
		if (enemyPoisonNum != null) enemyPoisonNum.setText(poisonStacks > 0 ? (poisonDuration > 0 ? (poisonStacks + "|" + poisonDuration) : String.valueOf(poisonStacks)) : "");
		if (enemyBleedNum != null) enemyBleedNum.setText(bleedStacks > 0 ? (bleedDuration > 0 ? (bleedStacks + "|" + bleedDuration) : String.valueOf(bleedStacks)) : "");
		if (enemyStunNum != null) enemyStunNum.setText(stunDuration > 0 ? String.valueOf(stunDuration) : "");
	}

    // New: Update enemy HP color by status (priority: Stun → Bleed → Poison → White)
    public void updateEnemyHpStatusColor(boolean isEnemyPoisoned, boolean isEnemyBleeding, boolean isEnemyStunned) {
        if (enemyHpLabel == null) return;
        if (isEnemyStunned) {
            enemyHpLabel.setColor(Color.YELLOW);
        } else if (isEnemyBleeding) {
            enemyHpLabel.setColor(Color.RED);
        } else if (isEnemyPoisoned) {
            enemyHpLabel.setColor(Color.PURPLE);
        } else {
            enemyHpLabel.setColor(Color.WHITE);
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

    public void setEnemyHp(int hp) {
        if (enemyHpLabel != null) {
            enemyHpLabel.setText(String.valueOf(hp));
        }
    }
}
