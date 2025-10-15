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
 * Random event: Phoenix
 * Choices:
 * 1) Heal: 35 Gold: Heal 17 HP
 * 2) Purify: 50 Gold: Remove a card from your deck
 * 3) Leave
 */
public class PhoenixEventScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Table root;
    private final Image bgImage;
    private final Label descriptionLabel;
    private final TextButton btnHeal;
    private final TextButton btnPurify;
    private final TextButton btnLeave;

    public PhoenixEventScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Texture bgTex = new Texture(Utils.getInternalPath("assets/Puzzle/phoenix.png"));
        bgImage = new Image(bgTex);
        bgImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        Label title = new Label("Phoenix", corebringer.testskin);
        title.setAlignment(Align.center);

        descriptionLabel = new Label(
            "A magnificent phoenix appears before you, offering its healing powers.",
            corebringer.testskin
        );
        descriptionLabel.setAlignment(Align.center);

        btnHeal = new TextButton("Heal (35 Gold: Heal 17 HP)", corebringer.testskin);
        btnPurify = new TextButton("Purify (50 Gold: Remove a card from your deck)", corebringer.testskin);
        btnLeave = new TextButton("Leave", corebringer.testskin);

        btnHeal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveHeal();
            }
        });

        btnPurify.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolvePurify();
            }
        });

        btnLeave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                returnToMap();
            }
        });

        Table choices = new Table();
        choices.add(btnHeal).pad(6f).row();
        choices.add(btnPurify).pad(6f).row();
        choices.add(btnLeave).pad(6f).row();

        root.top();
        root.add(title).padTop(10f).row();
        root.add(bgImage).expand().fill().pad(10f).row();
        root.add(descriptionLabel).pad(8f).row();
        root.add(choices).padBottom(15f);
    }

    private void resolveHeal() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }
        
        if (stats.gold < 35) {
            showInfo("Insufficient gold. You need 35 gold to heal.");
            return;
        }
        
        int currentHp = stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20);
        int maxHp = stats.maxHp > 0 ? stats.maxHp : 20;
        int newHp = Math.min(maxHp, currentHp + 17);
        int newGold = stats.gold - 35;
        
        SaveManager.saveStats(
            newHp,
            maxHp,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            stats.cards,
            stats.battleWon,
            newGold
        );
        showContinue("The phoenix's healing flames restore your vitality. -35 Gold, +17 HP.");
    }

    private void resolvePurify() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }
        
        if (stats.gold < 50) {
            showInfo("Insufficient gold. You need 50 gold to purify.");
            return;
        }

        String[] cards = (stats.cards != null) ? stats.cards : new String[]{};
        if (cards.length == 0) {
            showInfo("No cards to remove from your deck.");
            return;
        }

        // Let the player choose a card id to remove
        Dialog picker = new Dialog("Remove a card from your deck", corebringer.testskin);
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
                        stats.gold - 50
                    );
                    showContinue("The phoenix purifies your deck, removing an unwanted card. -50 Gold.");
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
