package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class OptionsWindow extends Window {
    public interface JournalCallback {
        void onShowJournal();
    }
    public interface TitleCallback {
        void onGoToTitle();
    }

    private final Stage stage;
    private final Skin skin;
    private final JournalCallback journalCallback;
    private final TitleCallback titleCallback;

    public OptionsWindow(Stage stage, Skin skin, JournalCallback journalCallback, TitleCallback titleCallback) {
        super("Options", skin);
        this.stage = stage;
        this.skin = skin;
        this.journalCallback = journalCallback;
        this.titleCallback = titleCallback;
        initialize();
    }

    private void initialize() {
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        this.setModal(true);
        this.setMovable(true);
        this.pad(20);
        this.setSize(640, 480);
        this.setPosition(
            Gdx.graphics.getWidth() / 2f - 320f,
            Gdx.graphics.getHeight() / 2f - 240f
        );
        this.background(optionBGDrawable);
        this.setColor(1, 1, 1, 1);

        TextButton btnJournalLocal = new TextButton("Journal", skin);
        btnJournalLocal.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (journalCallback != null) journalCallback.onShowJournal();
            }
        });

        TextButton btnToMain = new TextButton("Title", skin);
        btnToMain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (titleCallback != null) titleCallback.onGoToTitle();
                OptionsWindow.this.setVisible(false);
                OptionsWindow.this.remove();
            }
        });

        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                OptionsWindow.this.setVisible(false);
                OptionsWindow.this.remove();
            }
        });

        TextButton btnDeck = new TextButton("Deck", skin);
        TextButton btnLogs = new TextButton("Logs", skin);
        TextButton btnCharacter = new TextButton("Character", skin);

        Table content = new Table();
        content.defaults().pad(10).growX();
        content.add(new Label("Options", skin)).colspan(2).center().row();
        content.add(btnJournalLocal).row();
        content.add(btnDeck).row();
        content.add(btnLogs).row();
        content.add(btnCharacter).row();
        content.add(btnToMain).row();

        Table bottom = new Table();
        bottom.add(btnClose).right();

        this.clear();
        this.add(content).grow().row();
        this.add(bottom).right();

        this.setVisible(false);
        stage.addActor(this);
    }

    public void toggle() {
        boolean show = !this.isVisible();
        this.setVisible(show);
        if (show) this.toFront();
    }
}
