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
import com.badlogic.gdx.utils.ObjectMap;


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
    // Remove-card flow resources
    private Window removeWindow;
    private ObjectMap<String, String> idToAtlasName;

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
        float windowWidth = Gdx.graphics.getWidth() * 0.4f;
        float windowHeight = Gdx.graphics.getHeight() * 0.57f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) * 0.2f;
        float windowY = 10f;

        shopWindow = new Window("", corebringer.testskin);
        shopWindow.setModal(false);
        shopWindow.setMovable(false);
        shopWindow.setResizable(false);
        shopWindow.setSize(windowWidth, windowHeight);
        shopWindow.setPosition(windowX, windowY);

        Table content = new Table();
        content.defaults().pad(10);

        // 2x2 grid of random cards (left)
        shopGrid = new Table();
        shopGrid.defaults().center();
        populateRandomCardsGrid(shopGrid);

        // Right-side stat items column
        Table rightCol = new Table();
        rightCol.top();
        addStatItems(rightCol);

        // Bottom controls: Remove card button and price label 150
        Table controls = new Table();
        controls.defaults().pad(5);
        TextButton removeBtn = new TextButton("Remove card", corebringer.testskin);
        removeBtn.setSize(150, 50);
        Label removePrice = new Label("150", corebringer.testskin);
        controls.add(removeBtn).row();
        controls.add(removePrice).padLeft(10);

        // Remove card behavior
        removeBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Load current gold
                SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
                int currentGold = (stats != null) ? stats.gold : 0;
                if (currentGold < 150) {
                    showInsufficientGold();
                    return;
                }
                // Ensure player has cards
                String[] ids = (stats != null && stats.cards != null) ? stats.cards : new String[]{};
                if (ids.length == 0) {
                    Dialog dlg = new Dialog("", corebringer.testskin);
                    dlg.text("You have no cards to remove.");
                    dlg.button("OK");
                    dlg.show(coremerchantscreenstage);
                    return;
                }
                // Open selection window
                showRemoveCardWindow();
            }
        });

        // Layout: cards on left, stat items on right, then controls row under them
        content.add(shopGrid).expand().fill();
        content.add(rightCol).top().padLeft(10);
        content.row();
        content.add(controls).colspan(2).right();

        shopWindow.add(content).grow();
        coremerchantscreenstage.addActor(shopWindow);

        // Proceed button outside the window at the right side
        final TextButton proceedBtn = new TextButton("Proceed", corebringer.testskin);
        // Give it a reasonable fixed size so we can position it reliably
        proceedBtn.setSize(140f, 45f);
        // Position strictly outside the shop window on the right, with padding
        float desiredX = windowX + windowWidth + 20f; // 20px to the right of window
        float maxX = Gdx.graphics.getWidth() - proceedBtn.getWidth() - 20f; // keep inside screen
        float btnX = windowX + windowWidth + 20f;
        float btnY = windowY + (windowHeight - proceedBtn.getHeight()) / 2f;
        proceedBtn.setPosition(btnX, btnY);
        coremerchantscreenstage.addActor(proceedBtn);

        proceedBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Navigate back to Game Map Screen
                if (corebringer.gameMapScreen != null) {
                    try {
                        corebringer.gameMapScreen.advanceToNextRank();
                    } catch (Exception ignored) {}
                }
                corebringer.setScreen(corebringer.gameMapScreen);
                Gdx.app.postRunnable(MerchantScreen.this::dispose);
            }
        });
    }

    private void addStatItems(Table rightCol) {
        // Common style
        rightCol.defaults().pad(6);

        // +10 Max HP for 150 gold
        Table hpItem = new Table();
        Label hpLabel = new Label("+10 Max HP", corebringer.testskin);
        Label hpPrice = new Label("150", corebringer.testskin);
        TextButton hpBuyArea = new TextButton("Buy", corebringer.testskin);
        hpItem.add(hpLabel).row();
        hpItem.add(hpPrice).padTop(4).row();
        hpItem.add(hpBuyArea).size(120, 36).padTop(4);
        final ClickListener[] hpListenerHolder = new ClickListener[1];
        ClickListener hpListener = new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryPurchaseStat("maxhp", 150, hpLabel, hpPrice, hpBuyArea, hpListenerHolder);
            }
        };
        hpListenerHolder[0] = hpListener;
        hpBuyArea.addListener(hpListener);

        // +1 Max Energy for 200 gold
        Table energyItem = new Table();
        Label energyLabel = new Label("+1 Max Energy", corebringer.testskin);
        Label energyPrice = new Label("200", corebringer.testskin);
        TextButton energyBuyArea = new TextButton("Buy", corebringer.testskin);
        energyItem.add(energyLabel).row();
        energyItem.add(energyPrice).padTop(4).row();
        energyItem.add(energyBuyArea).size(120, 36).padTop(4);
        final ClickListener[] energyListenerHolder = new ClickListener[1];
        ClickListener energyListener = new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryPurchaseStat("maxenergy", 200, energyLabel, energyPrice, energyBuyArea, energyListenerHolder);
            }
        };
        energyListenerHolder[0] = energyListener;
        energyBuyArea.addListener(energyListener);

        rightCol.add(hpItem).size(180, 120).row();
        rightCol.add(energyItem).size(180, 120).row();
    }

    private void tryPurchaseStat(String type, int price, Label titleLbl, Label priceLbl, Button buyBtn, ClickListener[] listenerHolder) {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null || stats.gold < price) {
            showInsufficientGold();
            return;
        }

        int hp = stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20);
        int maxHp = stats.maxHp > 0 ? stats.maxHp : 20;
        int energy = stats.energy;
        int maxEnergy = stats.maxEnergy > 0 ? stats.maxEnergy : 3;

        if ("maxhp".equals(type)) {
            maxHp += 10;
            // heal up to new max by +10 as QoL
            hp = Math.min(maxHp, hp + 10);
        } else if ("maxenergy".equals(type)) {
            maxEnergy += 1;
            // do not change current energy here; game flow can refill elsewhere
        }

        int newGold = stats.gold - price;

        SaveManager.saveStats(
            hp,
            maxHp,
            energy,
            maxEnergy,
            stats.cards,
            stats.battleWon,
            newGold
        );

        updateTopGoldAndHp(hp, maxHp, newGold);

        if (titleLbl != null) titleLbl.setText("Sold out");
        if (buyBtn != null) {
            buyBtn.setDisabled(true);
            buyBtn.setTouchable(Touchable.disabled);
        }
        if (listenerHolder != null && listenerHolder[0] != null) buyBtn.removeListener(listenerHolder[0]);
    }

    private void ensureDeckMappingLoaded() {
        if (idToAtlasName != null) return;
        idToAtlasName = new ObjectMap<>();
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("assets/cards.json"));
            for (JsonValue card : root.get("cards")) {
                String id = card.getString("id", null);
                String atlas = card.getString("atlasName", null);
                if (id != null && atlas != null) {
                    idToAtlasName.put(id, atlas);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MerchantScreen", "Failed to load cards.json for mapping: " + e.getMessage());
        }
    }

    private void showRemoveCardWindow() {
        ensureDeckMappingLoaded();

        if (removeWindow != null) {
            removeWindow.toFront();
            removeWindow.setVisible(true);
            return;
        }

        float windowWidth = Gdx.graphics.getWidth() * 0.8f;
        float windowHeight = Gdx.graphics.getHeight() * 0.75f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        removeWindow = new Window("", corebringer.testskin);
        removeWindow.setModal(true);
        removeWindow.setMovable(true);
        removeWindow.setResizable(false);
        float rw = Math.min(Gdx.graphics.getWidth() * 0.55f, 900f);
        float rh = Math.min(Gdx.graphics.getHeight() * 0.73f, 600f);
        removeWindow.setSize(rw, rh);
        // Center the remove window on screen
        float posX = (Gdx.graphics.getWidth() - rw) * 0.2f;
        float posY = 20f;
        removeWindow.setPosition(posX, posY);

        Table grid = new Table();
        grid.defaults().pad(10);
        grid.top().left();

        // Build from saved deck ids
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        String[] ids = (stats != null && stats.cards != null) ? stats.cards : new String[]{};

        int col = 0;
        for (String id : ids) {
            String atlasName = idToAtlasName != null ? idToAtlasName.get(id) : null;
            String region = atlasName != null ? atlasName.replace(" ", "_") : "bck_card";
            TextureRegionDrawable drawable = new TextureRegionDrawable(cardAtlas.findRegion(region) != null ? cardAtlas.findRegion(region) : cardAtlas.findRegion("bck_card"));
            Image img = new Image(drawable);
            // Make merchant card size responsive
            float screenWidth = Gdx.graphics.getWidth();
            float cardWidth = screenWidth * 0.1f;   // 10% of screen width (smaller than deck view)
            float cardHeight = cardWidth * 1.27f;   // Maintain aspect ratio
            img.setSize(cardWidth, cardHeight);
            // Store the id on the actor's name for retrieval
            img.setName(id);
            img.addListener(new ClickListener(){
                private boolean handled = false;
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (handled) return; // ensure only once
                    handled = true;
                    performCardRemoval(img.getName());
                }
            });
            grid.add(img).size(150, 190);
            col++;
            if (col == 4) { grid.row(); col = 0; }
        }

        ScrollPane scroll = new ScrollPane(grid, corebringer.testskin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollbarsOnTop(true);
        scroll.setForceScroll(false, true); // always allow vertical scrolling
        scroll.setScrollingDisabled(false, false);

        Table content = new Table();
        content.defaults().pad(10);
        content.add(scroll).grow().row();
        // Buttons row
        Table buttons = new Table();
        TextButton btnCancel = new TextButton("Cancel", corebringer.testskin);
        btnCancel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (removeWindow != null) {
                    removeWindow.remove();
                    removeWindow = null;
                }
            }
        });
        buttons.add(btnCancel).right();
        content.add(buttons).right();
        removeWindow.add(content).grow();
        coremerchantscreenstage.addActor(removeWindow);
    }

    private void performCardRemoval(String idToRemove) {
        // Re-validate gold and deck
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInsufficientGold();
            return;
        }
        if (stats.gold < 150) {
            showInsufficientGold();
            return;
        }
        String[] curr = stats.cards != null ? stats.cards : new String[]{};
        if (curr.length == 0) {
            Dialog dlg = new Dialog("", corebringer.testskin);
            dlg.text("You have no cards to remove.");
            dlg.button("OK");
            dlg.show(coremerchantscreenstage);
            return;
        }

        // Remove first occurrence
        int idxRemove = -1;
        for (int i = 0; i < curr.length; i++) {
            if (curr[i] != null && curr[i].equals(idToRemove)) { idxRemove = i; break; }
        }
        if (idxRemove == -1) {
            Dialog dlg = new Dialog("", corebringer.testskin);
            dlg.text("Selected card not found.");
            dlg.button("OK");
            dlg.show(coremerchantscreenstage);
            return;
        }

        String[] updated = new String[curr.length - 1];
        for (int i = 0, j = 0; i < curr.length; i++) {
            if (i == idxRemove) continue;
            updated[j++] = curr[i];
        }

        int newGold = stats.gold - 150;
        // Persist
        SaveManager.saveStats(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            updated,
            stats.battleWon,
            newGold
        );

        // Update UI
        updateTopGoldAndHp(
            stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
            stats.maxHp > 0 ? stats.maxHp : 20,
            newGold
        );

        // Close selection window to enforce single removal per transaction
        if (removeWindow != null) {
            removeWindow.remove();
            removeWindow = null;
        }

        // Feedback
        Dialog dlg = new Dialog("", corebringer.testskin);
        dlg.text("Card removed. -150 Gold");
        dlg.button("OK");
        dlg.show(coremerchantscreenstage);
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
            itemTable.add(img).size(126, 154).row();
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
