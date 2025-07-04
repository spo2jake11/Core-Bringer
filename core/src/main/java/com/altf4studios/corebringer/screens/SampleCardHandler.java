package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.screens.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class SampleCardHandler {
    public String idcard;
    public String cardname;
    public String cardtype;
    public int power;

    @Override
    public String toString() {
        return ("Card{id= '" + idcard + "', name= '" + cardname + "', type= '" + cardtype + "', power=" + power + "}");
    }
}
