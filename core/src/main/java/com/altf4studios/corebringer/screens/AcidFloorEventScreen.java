package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.SaveData;
import com.altf4studios.corebringer.utils.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Random event: Acid Floor
 * Choices:
 * 1) Reach for the Gold: +50 gold, -15 HP
 * 2) Discard 1 card and gain +20 gold
 * 3) Leave
 */
public class AcidFloorEventScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Table root;
    private final Image bgImage;
    private final Label descriptionLabel;
    private final TextButton btnChoice1;
    private final TextButton btnChoice2;
    private final TextButton btnLeave;

    public AcidFloorEventScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Texture bgTex = new Texture(Utils.getInternalPath("assets/Puzzle/acidFloor.png"));
        bgImage = new Image(bgTex);
        bgImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        Label title = new Label("Acid Floor", corebringer.testskin);
        title.setAlignment(Align.center);

        descriptionLabel = new Label(
            "You spot coins shimmering over a sizzling acid floor.",
            corebringer.testskin
        );
        descriptionLabel.setAlignment(Align.center);

        btnChoice1 = new TextButton("Reach for the Gold (+50 Gold, -15 HP)", corebringer.testskin);
        btnChoice2 = new TextButton("Pick up spilled coins (+20 Gold, discard 1 card)", corebringer.testskin);
        btnLeave = new TextButton("Leave", corebringer.testskin);

        btnChoice1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveReachForGold();
            }
        });

        btnChoice2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveDiscardOneGainTwenty();
            }
        });

        btnLeave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                returnToMap();
            }
        });

        Table choices = new Table();
        choices.add(btnChoice1).pad(6f).row();
        choices.add(btnChoice2).pad(6f).row();
        choices.add(btnLeave).pad(6f).row();

        root.top();
        root.add(title).padTop(10f).row();
        root.add(bgImage).expand().fill().pad(10f).row();
        root.add(descriptionLabel).pad(8f).row();
        root.add(choices).padBottom(15f);
    }

    private void resolveReachForGold() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }
        int newHp = Math.max(0, (stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20)) - 15);
        int newGold = stats.gold + 50;
        SaveManager.saveStats(
            newHp,
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            stats.cards,
            stats.battleWon,
            newGold
        );
        showContinue("You burn your hands but grab a handful of coins. -15 HP, +50 Gold.");
    }

    private void resolveDiscardOneGainTwenty() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }

        String[] cards = (stats.cards != null) ? stats.cards : new String[]{};
        if (cards.length == 0) {
            // No cards to discard; just grant gold
            SaveManager.saveStats(
                stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
                stats.maxHp > 0 ? stats.maxHp : 20,
                stats.energy,
                stats.maxEnergy > 0 ? stats.maxEnergy : 3,
                cards,
                stats.battleWon,
                stats.gold + 20
            );
            showContinue("No cards to discard. You carefully collect some coins. +20 Gold.");
            return;
        }

        // Let the player choose a card id to discard
        Dialog picker = new Dialog("Discard a card", corebringer.testskin);
        Table list = new Table();
        for (int i = 0; i < cards.length; i++) {
            final int idx = i;
            TextButton btn = new TextButton(cards[i], corebringer.testskin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    picker.hide();
                    String[] updated = new String[cards.length - 1];
                    int p = 0;
                    for (int j = 0; j < cards.length; j++) if (j != idx) updated[p++] = cards[j];
                    SaveManager.saveStats(
                        stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20),
                        stats.maxHp > 0 ? stats.maxHp : 20,
                        stats.energy,
                        stats.maxEnergy > 0 ? stats.maxEnergy : 3,
                        updated,
                        stats.battleWon,
                        stats.gold + 20
                    );
                    showContinue("You discard a card and collect the coins. +20 Gold.");
                }
            });
            list.add(btn).pad(4f).row();
        }
        picker.getContentTable().add(new ScrollPane(list)).width(500f).height(300f).row();
        picker.button("Cancel");
        picker.show(stage);
    }

    private void showInfo(String msg) {
        Dialog dlg = new Dialog("", corebringer.testskin);
        dlg.text(msg);
        dlg.button("OK");
        dlg.show(stage);
    }

    private void showContinue(String msg) {
        Dialog dlg = new Dialog("", corebringer.testskin);
        dlg.text(msg);
        dlg.button("Continue");
        dlg.show(stage);
        dlg.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                returnToMap();
            }
        });
    }

    private void returnToMap() {
        if (corebringer.gameMapScreen != null) {
            try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
        }
        corebringer.setScreen(corebringer.gameMapScreen);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.08f, 0.12f, 1f);
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
        // The background texture is owned by bgImage's drawable; dispose explicitly
        try {
            TextureRegionDrawable d = (TextureRegionDrawable) bgImage.getDrawable();
            if (d != null && d.getRegion() != null && d.getRegion().getTexture() != null) {
                d.getRegion().getTexture().dispose();
            }
        } catch (Exception ignored) {}
    }
}


