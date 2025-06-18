package com.altf4studios.corebringer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class OptionsScreen implements Screen {
    private Main corebringer;
    private Stage optionstage;
    private Table optiontable;
    private Table optionlabeltable;
    private Table optionbuttonstable;
    private Label optionlabel;
    private Skin testskin;
    private TextButton returnbutton;
    private Slider volumeslider;
    private Label volumelabel;
    private CheckBox volumemutecheckbox;
    private Slider screenbrightnessslider;
    private Label screenbrightnesslabel;

    public OptionsScreen(Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer;
        optionstage = new Stage(new ScreenViewport());
        testskin =  new Skin(Gdx.files.internal("ui/uiskin.json")); ////Usage of Sample Skin, can be changed soon
        optiontable = new Table();
        optiontable.setFillParent(true);
        optionstage.addActor(optiontable);

        ///Option Label will initialize in this table with its parameters
        optionlabeltable = new Table();
        optionlabel = new Label("Options Menu", testskin);
        optionlabel.setFontScale(3f);

        ///This is where the label will be called
        optionlabeltable.add(optionlabel).pad(20f).row();

        ///Options Clickables will initialize in this table with its parameters
        ///For the separate table
        optionbuttonstable = new Table();
        ///For the Return Button
        returnbutton = new TextButton("Back", testskin);
        ///For the Volume Slider
        volumeslider = new Slider(0f, 100f, 1f, false, testskin);
        float currentvolume = corebringer.corebringerbgm.getVolume() * 100f;
        volumeslider.setValue(currentvolume);
        volumelabel = new Label("Volume: " + (int) currentvolume + "%", testskin);
        volumemutecheckbox = new CheckBox(" Mute Music?", testskin);
        volumemutecheckbox.setChecked(corebringer.isMusicMuted);
        volumeslider.setDisabled(corebringer.isMusicMuted);
        if (corebringer.isMusicMuted) {
            volumelabel.setText("Volume is Muted.");
        } else {
            volumelabel.setText("Volume: " + (int) currentvolume + "%");
        }
        ///For the Brightness Slider
        screenbrightnesslabel = new Label("Brightness 100%", testskin);
        screenbrightnessslider = new Slider(0f, 100f, 1f, false, testskin);
        float currentbrightnessalpha = corebringer.brightnessoverlay.getColor().a;
        float currentbrightness = (1f - currentbrightnessalpha) * 100f;
        screenbrightnessslider.setValue(currentbrightness);
        screenbrightnesslabel = new Label("Brightness: " + (int) currentbrightness + "%", testskin);

        ///This code gives function to the Volume Slider
        volumeslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                if (corebringer.isMusicMuted) {
                    volumelabel.setText("Volume is Muted.");
                    corebringer.corebringerbgm.setVolume(0f);
                } else {
                    float volume = volumeslider.getValue() / 100f;
                    volumelabel.setText("Volume: " + (int) volumeslider.getValue() + "%");
                    corebringer.corebringerbgm.setVolume(volume);
                }
            }
        });

        ///This code gives function to the Mute Checkbox
        volumemutecheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                corebringer.isMusicMuted = volumemutecheckbox.isChecked();
                volumeslider.setDisabled(corebringer.isMusicMuted);

                if (corebringer.isMusicMuted) {
                    volumelabel.setText("Volume is Muted.");
                    corebringer.corebringerbgm.setVolume(0f);
                } else {
                    float volume = volumeslider.getValue() / 100f;
                    volumelabel.setText("Volume: " + (int) volumeslider.getValue() + "%");
                    corebringer.corebringerbgm.setVolume(volume);
                }
            }
        });

        ///This code gives function to the Brightness Slider
        screenbrightnessslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                int brightness = (int) screenbrightnessslider.getValue();
                screenbrightnesslabel.setText("Brightness: " + brightness + "%");

                /* This just means that the Brightness will take the value of the Brightness slider to
                command the below code to color the Brightness Overlay Pixmap, being 1f for full darkness and
                0f for full brightness*/
                float brightnessalpha = 1f - (brightness / 100f);
                corebringer.brightnessoverlay.setColor(0f, 0f, 0f, brightnessalpha);
            }
        });

        ///This code gives function to the Return Button
        returnbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(new MainMenuScreen(corebringer));
            }
        });

        ///This is where the buttons, sliders and whatnot will be called
        optionbuttonstable.add(volumelabel).pad(20f).row();
        optionbuttonstable.add(volumeslider).width(300f).pad(20f).row();
        optionbuttonstable.add(volumemutecheckbox).pad(20f).row();
        optionbuttonstable.add(screenbrightnesslabel).pad(20f).row();
        optionbuttonstable.add(screenbrightnessslider).width(300f).pad(20f).row();
        optionbuttonstable.add(returnbutton).width(250f).height(50f).pad(20f).row();

        ///Table calling here since IDE reads code per line
        optiontable.add(optionlabeltable).expandX().padTop(10f).row();
        optiontable.row();
        optiontable.add(optionbuttonstable).expandX().padTop(10f).center();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(optionstage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        optionstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        optionstage.draw();

        corebringer.brightnessoverlaystage.act(delta);
        corebringer.brightnessoverlaystage.draw();
    }

    @Override public void resize(int width, int height) {

    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
        corebringer.brightnessoverlay.clear();
    }

    @Override
    public void dispose() {
        optionstage.dispose();
    }
}
