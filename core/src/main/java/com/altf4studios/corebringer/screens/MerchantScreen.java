package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.altf4studios.corebringer.utils.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import java.util.ArrayList;
import java.util.Random;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.altf4studios.corebringer.utils.SaveData;


public class MerchantScreen implements Screen{
    private Main corebringer;
    private Stage coremerchantscreenstage;
    private Table coremerchantscreentable;
    private Label topHpNumLabel;
    private Label goldLabel;
    private TextureAtlas cardAtlas;
    private Window shopWindow;
    private Table shopGrid;
    private final Random rng = new Random();

    public MerchantScreen (Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        coremerchantscreenstage = new Stage(new FitViewport(1280, 720));
        coremerchantscreentable = new Table();
        coremerchantscreentable.setFillParent(true);
        coremerchantscreenstage.addActor(coremerchantscreentable);
        // Load card atlas once
        cardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");

        // Build top UI (left side of GameScreen top UI)
        buildTopUI();

        // Create centered shop window 60% width, 70% height
        createShopWindow();
    }

    private void tryPurchase(String id, int price, Image img, Label priceLbl, ClickListener[] listenerHolder) {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            // No save exists; treat as insufficient context
            showInsufficientGold();
            return;
        }
        int currentGold = stats.gold;
        if (currentGold < price) {
            showInsufficientGold();
            return;
        }
        // Sufficient gold: deduct and add card id, then persist
        String[] existing = stats.cards != null ? stats.cards : new String[]{};
        String[] updated = new String[existing.length + 1];
        System.arraycopy(existing, 0, updated, 0, existing.length);
        updated[existing.length] = id;

        int newGold = currentGold - price;
        SaveManager.saveStats(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            updated,
            stats.battleWon,
            newGold
        );

        // Update HUD and visuals
        updateTopGoldAndHp(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            newGold
        );

        TextureRegionDrawable back = new TextureRegionDrawable(cardAtlas.findRegion("bck_card"));
        img.setDrawable(back);
        priceLbl.remove();
        img.setTouchable(Touchable.disabled);
        if (listenerHolder != null && listenerHolder[0] != null) img.removeListener(listenerHolder[0]);
    }

    private void showInsufficientGold() {
        Dialog dlg = new Dialog("", corebringer.testskin);
        dlg.text("Insufficient Gold");
        dlg.button("OK");
        dlg.show(coremerchantscreenstage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(coremerchantscreenstage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        coremerchantscreenstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        coremerchantscreenstage.draw();
    }

    @Override public void resize(int width, int height) {
        coremerchantscreenstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(coremerchantscreenstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        coremerchantscreenstage.dispose();
        if (cardAtlas != null) cardAtlas.dispose();
    }

    private void buildTopUI() {
        // Load from save
        int hp = 20;
        int maxHp = 20;
        int gold = 0;
        if (SaveManager.saveExists()) {
            SaveData stats = SaveManager.loadStats();
            if (stats != null) {
                hp = stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : hp);
                maxHp = stats.maxHp > 0 ? stats.maxHp : Math.max(hp, maxHp);
                gold = stats.gold;
            }
        }

        Table tbltopUI = new Table();
        tbltopUI.setFillParent(true);
        tbltopUI.top();

        Table innerTable = new Table();
        innerTable.setBackground(corebringer.testskin.newDrawable("white", new Color(0.5f, 0.5f, 0.5f, 1)));
        innerTable.defaults().padTop(10).padBottom(10);

        Table tblLeftPane = new Table();
        Label lblCharName = new Label("Player", corebringer.testskin);
        topHpNumLabel = new Label("HP: " + hp + "/" + maxHp, corebringer.testskin);
        goldLabel = new Label("Gold: " + gold, corebringer.testskin);

        Table tblFiller = new Table();

        tblLeftPane.defaults().padLeft(10).padRight(10).uniform();
        tblLeftPane.add(lblCharName);
        tblLeftPane.add(topHpNumLabel);
        tblLeftPane.add(goldLabel);

        innerTable.add(tblLeftPane).left();
        innerTable.add(tblFiller).growX(); // Exclude right pane entirely

        tbltopUI.add(innerTable).growX().height(75);
        coremerchantscreenstage.addActor(tbltopUI);
    }

    private void createShopWindow() {
        float windowWidth = Gdx.graphics.getWidth() * 0.6f;
        float windowHeight = Gdx.graphics.getHeight() * 0.6f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight());

        shopWindow = new Window("Merchant", corebringer.testskin);
        shopWindow.setModal(false);
        shopWindow.setMovable(false);
        shopWindow.setResizable(false);
        shopWindow.setSize(windowWidth, windowHeight);
        shopWindow.setPosition(windowX, windowY);

        Table content = new Table();
        content.defaults().pad(10);

        // 2x2 grid of random cards
        shopGrid = new Table();
        shopGrid.defaults().pad(10).center();
        populateRandomCardsGrid(shopGrid);

        // Bottom-right controls: Remove card button and price label 150
        Table controls = new Table();
        controls.defaults().pad(5);
        TextButton removeBtn = new TextButton("Remove card", corebringer.testskin);
        Label removePrice = new Label("150", corebringer.testskin);
        controls.add(removeBtn);
        controls.add(removePrice).padLeft(10);

        content.add(shopGrid).grow().row();
        content.add(controls).right();

        shopWindow.add(content).grow();
        coremerchantscreenstage.addActor(shopWindow);
    }

    private void populateRandomCardsGrid(Table grid) {
        ArrayList<JsonValue> pool = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("assets/cards.json"));
            for (JsonValue card : root.get("cards")) {
                pool.add(card);
            }
        } catch (Exception e) {
            Gdx.app.error("MerchantScreen", "Failed to read cards.json: " + e.getMessage());
        }
        // Pick 4 unique random cards
        ArrayList<JsonValue> picks = new ArrayList<>();
        for (int i = 0; i < 4 && pool.size() > 0; i++) {
            int idx = rng.nextInt(pool.size());
            picks.add(pool.remove(idx));
        }

        int col = 0;
        for (JsonValue card : picks) {
            String id = card.getString("id", null);
            String atlasName = card.getString("atlasName", "bck_card");
            int price = card.getInt("price", 0);

            // Normalize atlas region (replace spaces with underscore if any)
            String region = atlasName != null ? atlasName.replace(" ", "_") : "bck_card";
            TextureRegionDrawable drawable = new TextureRegionDrawable(cardAtlas.findRegion(region) != null ? cardAtlas.findRegion(region) : cardAtlas.findRegion("bck_card"));

            // Container for image + price
            Table itemTable = new Table();
            Image img = new Image(drawable);
            img.setScaling(Scaling.fit);
            itemTable.add(img).size(180, 220).row();
            Label priceLbl = new Label(String.valueOf(price), corebringer.testskin);
            itemTable.add(priceLbl).padTop(5);

            // Click behavior
            final ClickListener[] listenerHolder = new ClickListener[1];
            ClickListener listener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tryPurchase(id, price, img, priceLbl, listenerHolder);
                }
            };
            listenerHolder[0] = listener;
            img.addListener(listener);

            grid.add(itemTable).size(200, 260);
            col++;
            if (col == 2) { grid.row(); col = 0; }
        }
    }

    private void addCardIdToSave(String id) {
        if (id == null || id.isEmpty()) return;
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            // Minimal create if no save
            String[] newCards = new String[]{ id };
            SaveManager.saveStats(20, 20, 0, 3, newCards, 0, 0);
            updateTopGoldAndHp(20, 20, 0);
            return;
        }
        // Append to existing cards
        String[] existing = stats.cards != null ? stats.cards : new String[]{};
        String[] updated = new String[existing.length + 1];
        System.arraycopy(existing, 0, updated, 0, existing.length);
        updated[existing.length] = id;
        // Persist preserving other fields
        SaveManager.saveStats(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            updated,
            stats.battleWon,
            stats.gold
        );
        updateTopGoldAndHp(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.gold
        );
    }

    private void updateTopGoldAndHp(int hp, int maxHp, int gold) {
        if (topHpNumLabel != null) topHpNumLabel.setText("HP: " + hp + "/" + maxHp);
        if (goldLabel != null) goldLabel.setText("Gold: " + gold);
    }
}
