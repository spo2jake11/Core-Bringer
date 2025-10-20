package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.utils.SettingsData;
import com.altf4studios.corebringer.utils.SettingsManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class OptionsScreen implements Screen {
    /// Declaration of variables and elements here.
    private Main corebringer;
    private Stage optionstage;
    private Table optiontable;
    private Table optionlabeltable;
    private Table optionbuttonstable;
    private Label optionlabel;
    private TextButton returnbutton;
    private Slider volumeslider;
    private Label volumelabel;
    private CheckBox volumemutecheckbox;

    public OptionsScreen(Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        optionstage = new Stage(new FitViewport(1280, 720));
        optiontable = new Table();
        optiontable.setFillParent(true);
        optionstage.addActor(optiontable);

        ///Option Label will initialize in this table with its parameters
        optionlabeltable = new Table();
        optionlabel = new Label("Options Menu", corebringer.testskin);

        ///This is where the label will be called
        optionlabeltable.add(optionlabel).pad(20f).row();

        ///Options Clickables will initialize in this table with its parameters
        ///For the separate table
        optionbuttonstable = new Table();
        ///For the Return Button
        returnbutton = new TextButton("Back", corebringer.testskin);
        ///For the Volume Slider
        volumeslider = new Slider(0f, 100f, 1f, false, corebringer.testskin);
        // Load settings to initialize UI and app state
        SettingsData settings = SettingsManager.loadSettings();
        if (settings != null) {
            float vol = Math.max(0f, Math.min(1f, settings.volume));
            corebringer.isMusicMuted = settings.muted;
            corebringer.corebringerstartmenubgm.setVolume(vol);
            corebringer.corebringermapstartbgm.setVolume(vol);
            if (corebringer.corebringerbgm != null) corebringer.corebringerbgm.setVolume(vol);
            if (corebringer.corebringergamescreenbgm != null) corebringer.corebringergamescreenbgm.setVolume(vol);
        }
        float currentvolume = corebringer.corebringerstartmenubgm.getVolume() * 100f;
        volumeslider.setValue(currentvolume);
        volumelabel = new Label("Volume: " + (int) currentvolume + "%", corebringer.responsivelabelstyle);
        volumemutecheckbox = new CheckBox(" Mute Music?", corebringer.testskin);
        volumemutecheckbox.setChecked(corebringer.isMusicMuted);
        volumeslider.setDisabled(corebringer.isMusicMuted);
        if (corebringer.isMusicMuted) {
            volumelabel.setText("Volume is Muted.");
        } else {
            volumelabel.setText("Volume: " + (int) currentvolume + "%");
        }

        ///This code gives function to the Volume Slider
        volumeslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                updateVolumeUI();
                // Persist settings on every change
                SettingsManager.saveSettings(volumeslider.getValue() / 100f, volumemutecheckbox.isChecked());
            }
        });

        ///This code gives function to the Mute Checkbox
        volumemutecheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                corebringer.isMusicMuted = volumemutecheckbox.isChecked();
                volumeslider.setDisabled(corebringer.isMusicMuted);
                updateVolumeUI();
                // Persist settings whenever mute toggles
                SettingsManager.saveSettings(volumeslider.getValue() / 100f, volumemutecheckbox.isChecked());
            }
        });

        ///This code gives function to the Return Button
        returnbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
            }
        });

        ///This is where the buttons, sliders and whatnot will be called
        optionbuttonstable.add(volumelabel).pad(20f).row();
        optionbuttonstable.add(volumeslider).width(300f).pad(20f).row();
        optionbuttonstable.add(volumemutecheckbox).pad(20f).row();
        optionbuttonstable.add(returnbutton).width(250f).height(50f).pad(20f).row();

        ///Table calling here since IDE reads code per line
        optiontable.add(optionlabeltable).expandX().padTop(10f).row();
        optiontable.row();
        optiontable.add(optionbuttonstable).expandX().padTop(10f).center();
    }

    ///This is for updating Music UI for the logic to be reusable in Options Screen
    private void updateVolumeUI() {
        if (corebringer.isMusicMuted) {
            volumelabel.setText("Volume is Muted.");
            // Pause all known music instances
            corebringer.corebringerstartmenubgm.pause();
            corebringer.corebringermapstartbgm.pause();
            if (corebringer.corebringerbgm != null) corebringer.corebringerbgm.pause();
            if (corebringer.corebringergamescreenbgm != null) corebringer.corebringergamescreenbgm.pause();
        } else {
            float volume = volumeslider.getValue() / 100f;
            volumelabel.setText("Volume: " + (int) volumeslider.getValue() + "%");
            // Apply volume to all music instances
            corebringer.corebringerstartmenubgm.setVolume(volume);
            corebringer.corebringermapstartbgm.setVolume(volume);
            if (corebringer.corebringerbgm != null) corebringer.corebringerbgm.setVolume(volume);
            if (corebringer.corebringergamescreenbgm != null) corebringer.corebringergamescreenbgm.setVolume(volume);

            if (corebringer.getScreen() == corebringer.mainMenuScreen && !corebringer.corebringerstartmenubgm.isPlaying()) {
                corebringer.corebringerstartmenubgm.play();
            } else if (corebringer.getScreen() == corebringer.startGameMapScreen && !corebringer.corebringermapstartbgm.isPlaying()) {
                corebringer.corebringermapstartbgm.play();
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(optionstage);
        corebringer.playMusic("options");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        optionstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        optionstage.draw();
    }

    @Override public void resize(int width, int height) {
        optionstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(optionstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        optionstage.dispose();
    }
}
