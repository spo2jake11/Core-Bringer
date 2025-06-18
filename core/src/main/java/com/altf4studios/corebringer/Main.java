package com.altf4studios.corebringer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public Music corebringerbgm;
    public Pixmap corebringerpixmap;
    public Texture whitepixel;
    public Image brightnessoverlay;
    public Stage brightnessoverlaystage;
    public boolean isMusicMuted;

    @Override
    public void create() {
        ///This is where music plays when the game starts
        corebringerbgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Pepito Manaloto Background Music [RE-UPLOADED].mp3"));
        corebringerbgm.setLooping(true);
        corebringerbgm.setVolume(1.0f);
        isMusicMuted = false;
        corebringerbgm.play();

        ///Pixel Map is used for brightness changing as it fills the Brightness Overlay with 1x1 white pixel textures
        corebringerpixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        corebringerpixmap.setColor(1,1,1,1);
        corebringerpixmap.fill();
        whitepixel = new Texture(corebringerpixmap);
        corebringerpixmap.dispose();

        /* This overlay is used as a kind of asset to be made and used by the game as a brightness mask where
         * it is filled with black pixels to be transparent and the Brightness Slider filling it slowly with white
         * pixels to brighten up*/
        brightnessoverlaystage = new Stage(new ScreenViewport());

        brightnessoverlay = new Image(new TextureRegionDrawable(new TextureRegion(whitepixel)));
        brightnessoverlay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        brightnessoverlay.setColor(0f, 0f, 0f, 0f);
        brightnessoverlay.setTouchable(Touchable.disabled);

        brightnessoverlaystage.addActor(brightnessoverlay);
        setScreen(new MainMenuScreen(this));
    }
    @Override
    public void dispose() {
        if (corebringerbgm != null) {
            corebringerbgm.dispose();
        }
        if (whitepixel != null) {
            whitepixel.dispose();
        }
        if (brightnessoverlaystage != null) {
            brightnessoverlaystage.dispose();
        }
    }
    @Override public void resize(int width, int height) {
        brightnessoverlay.setSize(width, height);
    }
}
