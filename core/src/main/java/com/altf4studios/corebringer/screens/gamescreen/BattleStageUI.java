package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class BattleStageUI {
    private Stage battleStage;
    private Skin skin;
    private TextureAtlas enemyAtlas;
    private Array<String> enemyNames;
    private String currentEnemyName;
    private Image enemyImageBG;
    private Stack enemyTemplateStack;
    private Stack userTemplateStack;
    private Label userHpLabel;
    private Label enemyHpLabel;
    private Label userShieldLabel;
    private Label enemyShieldLabel;
    private Label turnIndicatorLabel;
	// Status badge references for visibility toggling
	private Stack userShieldBadge;
	private Stack userBuffBadge;
	private Stack userPoisonBadge;
	private Stack userBleedBadge;
	private Stack userStunBadge;
	private Stack enemyShieldBadge;
	private Stack enemyPoisonBadge;
	private Stack enemyBleedBadge;
	private Stack enemyStunBadge;
	// Numeric overlays for badges
	private Label userShieldNum;
    private Label userBuffNum;
    private Label userPoisonNum;
	private Label userBleedNum;
	private Label userStunNum;
	private Label enemyShieldNum;
	private Label enemyPoisonNum;
	private Label enemyBleedNum;
	private Label enemyStunNum;

    // Cached textures to avoid repeated allocations
    private Texture bgTexture;
    private TextureAtlas bgAtlas;
    private Table actionTableRef;
    private Texture playerTexture;
    private ObjectMap<String, Texture> statusTextures = new ObjectMap<>();
    // Cache fallback merchant texture to avoid repeated allocations
    private Texture merchantTexture;
    // Asset manager reference (optional)
    private final AssetManager assets;
    private boolean enemyAtlasOwned = true;
    private boolean bgAtlasOwned = true;

    public BattleStageUI(Stage battleStage, Skin skin) {
        this(battleStage, skin, null);
    }

    public BattleStageUI(Stage battleStage, Skin skin, AssetManager assets) {
        this.battleStage = battleStage;
        this.skin = skin;
        this.assets = assets;
        loadEnemyAtlas();
        setupBattleUI();
        Gdx.app.log("BattleStageUI", "Battle stage UI initialized successfully");
    }

    private void loadEnemyAtlas() {
        try {
            // Read authoritative roster and atlas path from enemies.json
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("assets/enemies.json"));
            String atlasPath = root.getString("atlasPath", "assets/basic-characters/enemies_atlas.atlas");

            // Load the enemy atlas via AssetManager if available
            if (assets != null) {
                if (!assets.isLoaded(atlasPath, TextureAtlas.class)) {
                    assets.load(atlasPath, TextureAtlas.class);
                    assets.finishLoadingAsset(atlasPath);
                }
                enemyAtlas = assets.get(atlasPath, TextureAtlas.class);
                enemyAtlasOwned = false;
            } else {
                enemyAtlas = new TextureAtlas(Gdx.files.internal(atlasPath));
                enemyAtlasOwned = true;
            }

            // Build enemy name list from JSON levels (common + boss), keeping only regions present in atlas
            enemyNames = new Array<>();
            JsonValue levels = root.get("levels");
            if (levels != null) {
                for (JsonValue lvl : levels) {
                    JsonValue common = lvl.get("common");
                    if (common != null) {
                        for (JsonValue en : common) {
                            String name = en.getString("name", null);
                            if (name != null && enemyAtlas.findRegion(name) != null && !enemyNames.contains(name, false)) {
                                enemyNames.add(name);
                            }
                        }
                    }
                    JsonValue boss = lvl.get("boss");
                    if (boss != null) {
                        for (JsonValue en : boss) {
                            String name = en.getString("name", null);
                            if (name != null && enemyAtlas.findRegion(name) != null && !enemyNames.contains(name, false)) {
                                enemyNames.add(name);
                            }
                        }
                    }
                }
            }

            // Fallback: if JSON had none or atlas missing regions, try to use a safe fallback
            if (enemyNames.size == 0) {
                Gdx.app.log("BattleStageUI", "No enemy names found from JSON/atlas; using fallback 'merchant'.");
                enemyNames.add("merchant");
            } else {
                Gdx.app.log("BattleStageUI", "Loaded " + enemyNames.size + " enemies from JSON roster and atlas");
            }
        } catch (Exception e) {
            Gdx.app.error("BattleStageUI", "Error loading enemy atlas or roster: " + e.getMessage());
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
            // Fallback to cached merchant texture
            if (merchantTexture == null) {
                merchantTexture = new Texture(Gdx.files.internal("basic-characters/merchant.png"));
            }
            enemyImage = new Image(merchantTexture);
            Gdx.app.log("BattleStageUI", "Using fallback enemy texture for: " + enemyName);
        }

        enemyImage.setScaling(Scaling.fit);
        // Make character models responsive to screen size
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float characterScale = Math.min(screenWidth / 1920f, screenHeight / 1080f); // Scale based on 1920x1080 reference
        characterScale = Math.max(0.4f, Math.min(characterScale * 0.8f, 1.2f)); // Clamp between 0.4x and 1.2x
        enemyImage.setScale(characterScale);
        enemyImage.setAlign(Align.center);
        return enemyImage;
    }

    private void setupBattleUI() {
        // Load background atlas (stg1_bg..stg5_bg)
        if (bgAtlas == null) {
            try {
                if (assets != null) {
                    String bgPath = "assets/backgrounds/backgrounds_atlas.atlas";
                    if (!assets.isLoaded(bgPath, TextureAtlas.class)) {
                        assets.load(bgPath, TextureAtlas.class);
                        assets.finishLoadingAsset(bgPath);
                    }
                    bgAtlas = assets.get(bgPath, TextureAtlas.class);
                    bgAtlasOwned = false;
                } else {
                    bgAtlas = new TextureAtlas(Gdx.files.internal("assets/backgrounds/backgrounds_atlas.atlas"));
                    bgAtlasOwned = true;
                }
            } catch (Exception e) {
                Gdx.app.error("BattleStageUI", "Failed to load backgrounds atlas: " + e.getMessage());
            }
        }
        Drawable bgDraw;
        if (bgAtlas != null && bgAtlas.findRegion("stg1_bg") != null) {
            bgDraw = new TextureRegionDrawable(bgAtlas.findRegion("stg1_bg"));
        } else {
            if (bgTexture == null) bgTexture = new Texture(Gdx.files.internal("assets/backgrounds/stg1_bg.png"));
            bgDraw = new TextureRegionDrawable(new TextureRegion(bgTexture));
        }

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setBackground(bgDraw);
        actionTable.setFillParent(true);
        actionTable.padTop(200);
        this.actionTableRef = actionTable;

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
        if (playerTexture == null) {
            playerTexture = new Texture(Gdx.files.internal("assets/basic-characters/hero.png"));
        }
        Image userImageBG = new Image(playerTexture);
        userImageBG.setScaling(Scaling.contain);
        userImageBG.setAlign(Align.center);


        // Random enemy selection
        currentEnemyName = getRandomEnemyName();
        enemyImageBG = createEnemyImage(currentEnemyName);

		userTemplateStack = new Stack();
		userTemplateStack.add(userImageBG);
		userTemplateStack.add(userTemplate);
		enemyTemplateStack = new Stack();
		enemyTemplateStack.add(enemyImageBG);
		enemyTemplateStack.add(enemyTemplate);

		// Status placeholders (square with status name) above HP stacks
		Table userStatusTable = new Table();
		userStatusTable.defaults().pad(5).size(40, 40);
		userShieldBadge = createStatusBadge("Shield");
		userBuffBadge = createStatusBadge("Buff");
//		userPoisonBadge = createStatusBadge("Poison");
//		userBleedBadge = createStatusBadge("Bleed");
//		userStunBadge = createStatusBadge("Stun");
		userShieldBadge.setVisible(false);
		userBuffBadge.setVisible(false);
//		userPoisonBadge.setVisible(false);
//		userBleedBadge.setVisible(false);
//		userStunBadge.setVisible(false);
		// numeric overlays
		userShieldNum = createBadgeNumberOverlay(userShieldBadge);
        userBuffNum = createBadgeNumberOverlay(userBuffBadge);
//		userPoisonNum = createBadgeNumberOverlay(userPoisonBadge);
//		userBleedNum = createBadgeNumberOverlay(userBleedBadge);
//		userStunNum = createBadgeNumberOverlay(userStunBadge);
		userStatusTable.add(userShieldBadge);
		userStatusTable.add(userBuffBadge);
//		userStatusTable.add(userPoisonBadge);
//		userStatusTable.add(userBleedBadge);
//		userStatusTable.add(userStunBadge);

		Table enemyStatusTable = new Table();
		enemyStatusTable.defaults().pad(5).size(40, 40);
		enemyShieldBadge = createStatusBadge("Shield");
//		enemyPoisonBadge = createStatusBadge("Poison");
//		enemyBleedBadge = createStatusBadge("Bleed");
//		enemyStunBadge = createStatusBadge("Stun");
		enemyShieldBadge.setVisible(false);
//		enemyPoisonBadge.setVisible(false);
//		enemyBleedBadge.setVisible(false);
//		enemyStunBadge.setVisible(false);
		// numeric overlays
		enemyShieldNum = createBadgeNumberOverlay(enemyShieldBadge);
//		enemyPoisonNum = createBadgeNumberOverlay(enemyPoisonBadge);
//		enemyBleedNum = createBadgeNumberOverlay(enemyBleedBadge);
//		enemyStunNum = createBadgeNumberOverlay(enemyStunBadge);
		enemyStatusTable.add(enemyShieldBadge);
//		enemyStatusTable.add(enemyPoisonBadge);
//		enemyStatusTable.add(enemyBleedBadge);
//		enemyStatusTable.add(enemyStunBadge);

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
		actionTable.defaults().padTop(10);
		actionTable.add(turnIndicatorLabel).colspan(2).padBottom(50).row();
        actionTable.add(leftColumn).expandX().fillX().left();
        actionTable.add(rightColumn).expandX().fillX().right();

        battleStage.addActor(actionTable);
    }

    // Change background by stage using backgrounds_atlas regions (stg{n}_bg)
    public void setBackgroundStage(int stageLevel) {
        if (stageLevel < 1) stageLevel = 1;
        if (stageLevel > 5) stageLevel = 5;
        if (bgAtlas == null) return;
        String regionName = "stg" + stageLevel + "_bg";
        if (bgAtlas.findRegion(regionName) == null) return;
        Drawable bgDraw = new TextureRegionDrawable(bgAtlas.findRegion(regionName));
        if (actionTableRef != null) actionTableRef.setBackground(bgDraw);
    }

	private Stack createStatusBadge(String name) {
        String imgPath = null;
        switch (name){
            case "Shield":
                imgPath = "assets/Status/shield.png";
                break;            case "Buff":
                imgPath = "assets/Status/buff.png";
                break;
            case "Poison":
                imgPath = "assets/Status/poison.png";
                break;
            case "Bleed":
                imgPath = "assets/Status/droplets.png";
                break;
            case "Stun":
                imgPath = "assets/Status/stun.png";
                break;
            default:
                imgPath = null;
        }
        // Cached status textures
        Texture status = statusTextures.get(imgPath);
        if (status == null) {
            status = new Texture(Gdx.files.internal(imgPath));
            statusTextures.put(imgPath, status);
        }
        Image img = new Image(status);
        img.setSize(20, 20);
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

//    public void updateHpColors(boolean isPlayerPoisoned, boolean isEnemyPoisoned) {
//        if (userHpLabel != null) {
//            userHpLabel.setColor(isPlayerPoisoned ? Color.PURPLE : Color.WHITE);
//        }
//        if (enemyHpLabel != null) {
//            enemyHpLabel.setColor(isEnemyPoisoned ? Color.PURPLE : Color.WHITE);
//        }
//    }

//    public void updateShieldColors(boolean isPlayerShielded, boolean isEnemyShielded) {
//        if (userShieldLabel != null) {
//            userShieldLabel.setColor(isPlayerShielded ? Color.CYAN : Color.GRAY);
//        }
//        if (enemyShieldLabel != null) {
//            enemyShieldLabel.setColor(isEnemyShielded ? Color.CYAN : Color.GRAY);
//        }
//    }

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
//    public void updateEnemyHpStatusColor(boolean isEnemyPoisoned, boolean isEnemyBleeding, boolean isEnemyStunned) {
//        if (enemyHpLabel == null) return;
//        if (isEnemyStunned) {
//            enemyHpLabel.setColor(Color.YELLOW);
//        } else if (isEnemyBleeding) {
//            enemyHpLabel.setColor(Color.RED);
//        } else if (isEnemyPoisoned) {
//            enemyHpLabel.setColor(Color.PURPLE);
//        } else {
//            enemyHpLabel.setColor(Color.WHITE);
//        }
//    }

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

            // Create new enemy image and add with a new empty template label
            Image newEnemy = createEnemyImage(enemyName);
            enemyTemplateStack.add(newEnemy);
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

    // --- Floating Combat Text ---
    private void showFloatingText(Stack targetStack, String text, Color color) {
        if (targetStack == null || text == null || text.isEmpty()) return;
        try {
            Label lbl = new Label(text, skin);
            if (color != null) lbl.setColor(color);
            lbl.setAlignment(Align.center);
            // Start slightly above center
            Table container = new Table();
            container.add(lbl).padTop(0);
            // Place overlaying the stack
            targetStack.add(container);
            // Animate: move up and fade out, then remove
            float duration = 0.9f;
            container.getColor().a = 1f;
            container.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(0, 30f, duration),
                    Actions.fadeOut(duration)
                ),
                Actions.removeActor()
            ));
        } catch (Exception ignored) {}
    }

    public void showDamageOnEnemy(int amount) {
        if (amount > 0) showFloatingText(enemyTemplateStack, "-" + amount, Color.RED);
    }

    public void showDamageOnPlayer(int amount) {
        if (amount > 0) showFloatingText(userTemplateStack, "-" + amount, Color.RED);
    }

    public void showShieldOnEnemy(int amount) {
        if (amount > 0) showFloatingText(enemyTemplateStack, "+" + amount, Color.CYAN);
    }

    public void showShieldOnPlayer(int amount) {
        if (amount > 0) showFloatingText(userTemplateStack, "+" + amount, Color.CYAN);
    }

    public void showHealOnEnemy(int amount) {
        if (amount > 0) showFloatingText(enemyTemplateStack, "+" + amount, Color.GREEN);
    }

    public void showHealOnPlayer(int amount) {
        if (amount > 0) showFloatingText(userTemplateStack, "+" + amount, Color.GREEN);
    }    // Toggle the player's Buff badge visibility
    public void setPlayerBuffBadgeVisible(boolean visible) {
        if (userBuffBadge != null) {
            userBuffBadge.setVisible(visible);
        }
    }    // Set the player's Buff badge text (e.g., x2.0)
    public void setPlayerBuffBadgeText(String text) {
        if (text == null) text = "";
        if (userBuffNum != null) userBuffNum.setText(text);
    }



    public void dispose() {
        if (enemyAtlasOwned && enemyAtlas != null) {
            enemyAtlas.dispose();
        }
        if (bgTexture != null) { bgTexture.dispose(); bgTexture = null; }
        if (playerTexture != null) { playerTexture.dispose(); playerTexture = null; }
        if (bgAtlasOwned && bgAtlas != null) { bgAtlas.dispose(); bgAtlas = null; }
        if (statusTextures != null) {
            for (Texture t : statusTextures.values()) {
                if (t != null) t.dispose();
            }
            statusTextures.clear();
        }
    }
}


