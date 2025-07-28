package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class BattleStageUI {
    private Stage battleStage;
    private Skin skin;
    
    public BattleStageUI(Stage battleStage, Skin skin) {
        this.battleStage = battleStage;
        this.skin = skin;
        setupBattleUI();
        Gdx.app.log("BattleStageUI", "Battle stage UI initialized successfully");
    }
    
    private void setupBattleUI() {
        Texture bg = new Texture(Gdx.files.internal("backgrounds/Stage1_bg.png"));
        Drawable bgDraw = new TextureRegionDrawable(new TextureRegion(bg));

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setBackground(bgDraw);
        actionTable.setFillParent(true);

        // HP Labels
        Label userHpLabel = new Label("100", skin);
        Label enemyHpLabel = new Label("100", skin);
        Label userTemplate = new Label("Player", skin);
        Label enemyTemplate = new Label("Enemy", skin);

        // HP Stacks
        Stack userHpStack = new Stack();
        userHpLabel.setAlignment(Align.center);
        userHpStack.add(userHpLabel);

        Stack enemyHpStack = new Stack();
        enemyHpLabel.setAlignment(Align.center);
        enemyHpStack.add(enemyHpLabel);

        userTemplate.setAlignment(Align.center);
        enemyTemplate.setAlignment(Align.center);

        // Character images
        Texture playerTexture = new Texture(Gdx.files.internal("basic-characters/hero.png"));
        Texture enemyTexture = new Texture(Gdx.files.internal("basic-characters/merchant.png"));
        Image userImageBG = new Image(playerTexture);
        userImageBG.setSize(200, 200);
        Image enemyImageBG = new Image(enemyTexture);
        enemyImageBG.setSize(200, 200);
        
        Stack userTemplateStack = new Stack();
        userTemplateStack.add(userImageBG);
        userTemplateStack.add(userTemplate);
        Stack enemyTemplateStack = new Stack();
        enemyTemplateStack.add(enemyImageBG);
        enemyTemplateStack.add(enemyTemplate);

        actionTable.defaults().padTop(50);
        actionTable.add(userHpStack).height(25).width(200).padLeft(50);
        actionTable.add(enemyHpStack).height(25).width(200).padRight(50).row();
        actionTable.defaults().reset();
        actionTable.defaults().padTop(65);
        actionTable.add(userTemplateStack).height(200).width(200).pad(100).center();
        actionTable.add(enemyTemplateStack).height(200).width(200).pad(100).center();
        
        battleStage.addActor(actionTable);
    }
} 