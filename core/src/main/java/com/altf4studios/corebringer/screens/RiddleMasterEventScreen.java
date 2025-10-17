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
 * Random event: Riddle Master
 * Riddle: "I can store numbers or words, I change my value when reassigned. Without me, your code cannot remember. What am I?"
 * Options:
 * 1) A Variable (Correct - Heal 12 HP)
 * 2) A Method (Wrong - -12 HP)
 * 3) Leave
 */
public class RiddleMasterEventScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Table root;
    private final Image bgImage;
    private final Label descriptionLabel;
    private final TextButton btnVariable;
    private final TextButton btnMethod;
    private final TextButton btnLeave;

    public RiddleMasterEventScreen(Main corebringer) {
        this.corebringer = corebringer;
        stage = new Stage(new FitViewport(1280, 720));
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Texture bgTex = new Texture(Utils.getInternalPath("assets/Puzzle/riddleMaster.png"));
        bgImage = new Image(bgTex);
        bgImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        Label title = new Label("Riddle Master", corebringer.testskin);
        title.setAlignment(Align.center);

        descriptionLabel = new Label(
            "A mysterious figure appears and poses a riddle:\n\n\"I can store numbers or words,\nI change my value when reassigned.\nWithout me, your code cannot remember.\nWhat am I?\"",
            corebringer.testskin
        );
        descriptionLabel.setAlignment(Align.center);

        btnVariable = new TextButton("A Variable", corebringer.testskin);
        btnMethod = new TextButton("A Method", corebringer.testskin);
        btnLeave = new TextButton("Leave", corebringer.testskin);

        btnVariable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveCorrectAnswer();
            }
        });

        btnMethod.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resolveWrongAnswer();
            }
        });

        btnLeave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                returnToMap();
            }
        });

        Table choices = new Table();
        choices.add(btnVariable).pad(6f).row();
        choices.add(btnMethod).pad(6f).row();
        choices.add(btnLeave).pad(6f).row();

        root.top();
        root.add(title).padTop(10f).row();
        root.add(bgImage).expand().fill().pad(10f).row();
        root.add(descriptionLabel).pad(8f).row();
        root.add(choices).padBottom(15f);
    }

    private void resolveCorrectAnswer() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }
        
        int currentHp = stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20);
        int maxHp = stats.maxHp > 0 ? stats.maxHp : 20;
        int newHp = Math.min(maxHp, currentHp + 12);
        
        SaveManager.saveStats(
            newHp,
            maxHp,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            stats.cards,
            stats.battleWon,
            stats.gold
        );
        showContinue("Correct! The Riddle Master smiles and grants you healing energy. +12 HP.");
    }

    private void resolveWrongAnswer() {
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            showInfo("No save found. Effect skipped.");
            return;
        }
        
        int currentHp = stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20);
        int newHp = Math.max(1, currentHp - 12); // Ensure HP doesn't go below 1
        
        SaveManager.saveStats(
            newHp,
            stats.maxHp > 0 ? stats.maxHp : 20,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            stats.cards,
            stats.battleWon,
            stats.gold
        );
        showContinue("Incorrect! The Riddle Master's disappointment manifests as damage. -12 HP.");
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
