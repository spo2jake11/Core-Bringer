package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
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

public class GameMapScreen implements Screen{
    private Main corebringer;
    private Stage coregamemapstage;
    private Table coregamemaptable;
    private Table gamemapnodetable;
    private Table gamemapmessagetable;
    private Table gamemapbuttonstable;
    private Table rank1table;
    private Table rank2table;
    private Table rank3table;
    private Table rank4table;
    private Table rank5table;
    private TextButton nodeA;
    private TextButton nodeB;
    private TextButton nodeC;
    private TextButton nodeD;
    private TextButton nodeE;
    private TextButton nodeF;
    private TextButton nodeG;
    private TextButton nodeH;
    private TextButton nodeI;
    private TextButton nodeJ;
    private TextButton nodeK;
    private TextButton nodeL;
    private TextButton nodeM;
    private TextButton nodeN;
    private TextButton nodeO;
    private TextButton nodeP;
    private TextButton nodeQ;
    private TextButton nodeR;
    private TextButton nodeS;
    private TextButton nodeT;
    private TextButton returnbutton;
    private Label gamemapmessages;

    public GameMapScreen(Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        coregamemapstage = new Stage(new FitViewport(1280, 720));
        coregamemaptable = new Table();
        coregamemaptable.setFillParent(true);
        coregamemapstage.addActor(coregamemaptable);

        ///Code for the initialization of the Game Map Node Table
        gamemapnodetable = new Table();

        ///Code for the Nodes (Rank represents Columns, meaning Rank 1 is First Node Column of 4)
        ///First row (RANK 1) of nodes (A-D is always battle nodes)
        rank1table = new Table();
        nodeA = new TextButton("Battle", corebringer.testskin);
        nodeB = new TextButton("Battle", corebringer.testskin);
        nodeC = new TextButton("Battle", corebringer.testskin);
        nodeD = new TextButton("Battle", corebringer.testskin);
        ///Second row (RANK 2) of nodes (E-H is a static random of nodes)
        rank2table = new Table();
        nodeE = new TextButton("Merchant", corebringer.testskin);
        nodeF = new TextButton("Rest", corebringer.testskin);
        nodeG = new TextButton("Search", corebringer.testskin);
        nodeH = new TextButton("Battle", corebringer.testskin);
        ///Third row (RANK 3) of nodes (I-L is also a static random of nodes)
        rank3table = new Table();
        nodeI = new TextButton("Battle", corebringer.testskin);
        nodeJ = new TextButton("Battle", corebringer.testskin);
        nodeK = new TextButton("Battle", corebringer.testskin);
        nodeL = new TextButton("Card Smith", corebringer.testskin);
        ///Fourth row (RANK 4) of nodes (M-P is also a static random of nodes)
        rank4table = new Table();
        nodeM = new TextButton("Rest", corebringer.testskin);
        nodeN = new TextButton("Merchant", corebringer.testskin);
        nodeO = new TextButton("Card Smith", corebringer.testskin);
        nodeP = new TextButton("Battle", corebringer.testskin);
        ///Fifth row (RANK 5) of nodes (Q-T is also a static random nodes)
        rank5table = new Table();
        nodeQ = new TextButton("Merchant", corebringer.testskin);
        nodeR = new TextButton("Battle", corebringer.testskin);
        nodeS = new TextButton("Battle", corebringer.testskin);
        nodeT = new TextButton("Rest", corebringer.testskin);

        ///This is where the Nodes and their specific ranks is added
        ///For Rank 1
        rank1table.add(nodeA).padBottom(20f).row();
        rank1table.add(nodeB).padBottom(20f).row();
        rank1table.add(nodeC).padBottom(20f).row();
        rank1table.add(nodeD).padBottom(20f).row();
        ///For Rank 2
        rank2table.add(nodeE).padBottom(20f).row();
        rank2table.add(nodeF).padBottom(20f).row();
        rank2table.add(nodeG).padBottom(20f).row();
        rank2table.add(nodeH).padBottom(20f).row();
        ///For Rank 3
        rank3table.add(nodeI).padBottom(20f).row();
        rank3table.add(nodeJ).padBottom(20f).row();
        rank3table.add(nodeK).padBottom(20f).row();
        rank3table.add(nodeL).padBottom(20f).row();
        ///For Rank 4
        rank4table.add(nodeM).padBottom(20f).row();
        rank4table.add(nodeN).padBottom(20f).row();
        rank4table.add(nodeO).padBottom(20f).row();
        rank4table.add(nodeP).padBottom(20f).row();
        ///For Rank 5
        rank5table.add(nodeQ).padBottom(20f).row();
        rank5table.add(nodeR).padBottom(20f).row();
        rank5table.add(nodeS).padBottom(20f).row();
        rank5table.add(nodeT).padBottom(20f).row();

        ///For the functionalities of the Rank 1 Battle Nodes
        nodeA.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameScreen);
                // Always reroll enemy and cards when starting
                if (corebringer.gameScreen != null) {
                    corebringer.corebringermapstartbgm.stop();
                    corebringer.corebringermapstartbgm.setVolume(1.0f);
                    corebringer.corebringergamescreenbgm.play();
                    corebringer.gameScreen.rerollEnemyAndCards();
                }
            }
        });

        nodeB.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameScreen);
                // Always reroll enemy and cards when starting
                if (corebringer.gameScreen != null) {
                    corebringer.corebringermapstartbgm.stop();
                    corebringer.corebringermapstartbgm.setVolume(1.0f);
                    corebringer.corebringergamescreenbgm.play();
                    corebringer.gameScreen.rerollEnemyAndCards();
                }
            }
        });

        nodeC.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameScreen);
                // Always reroll enemy and cards when starting
                if (corebringer.gameScreen != null) {
                    corebringer.corebringermapstartbgm.stop();
                    corebringer.corebringermapstartbgm.setVolume(1.0f);
                    corebringer.corebringergamescreenbgm.play();
                    corebringer.gameScreen.rerollEnemyAndCards();
                }
            }
        });

        nodeD.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameScreen);
                // Always reroll enemy and cards when starting
                if (corebringer.gameScreen != null) {
                    corebringer.corebringermapstartbgm.stop();
                    corebringer.corebringermapstartbgm.setVolume(1.0f);
                    corebringer.corebringergamescreenbgm.play();
                    corebringer.gameScreen.rerollEnemyAndCards();
                }
            }
        });

        ///This is where the messages and tips in the game will go
        gamemapmessagetable = new Table();
        gamemapmessages = new Label("Did you know?: Dying in this game is permanent. :D", corebringer.testskin);

        ///This is where the navigation buttons will go (Going back to title to forfeit the game etc.)
        gamemapbuttonstable = new Table();
        returnbutton = new TextButton("Return to Main Menu", corebringer.testskin);

        ///This is where the functionality of the buttons is located
        returnbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ///WARNING: Possible causes of memory leaks, stop() method isn't working for some reason
                ///pause() method doesn't work too, only the reduction of volume does
                ///This can be the cause of bugs or memory leak
                ///This is only a WORKAROUND AND NOT A SOLUTION on the problem of the background music not properly stopping
                corebringer.corebringermapstartbgm.stop();
                corebringer.corebringerstartmenubgm.setVolume(1.0f);
                corebringer.corebringerstartmenubgm.play();
                corebringer.setScreen(corebringer.mainMenuScreen);
            }
        });

        ///Placement of the messages in the Game Map Messages Table
        gamemapmessagetable.add(gamemapmessages);

        ///Placement of the buttons in the Game Map Buttons Table
        gamemapbuttonstable.add(returnbutton);

        ///Placement of Ranks to the Game Map Node Table (Not the Core Game Map Table, but the separate one)
        gamemapnodetable.add(rank1table).padLeft(90f);
        gamemapnodetable.add(rank2table).padLeft(90f);
        gamemapnodetable.add(rank3table).padLeft(90f);
        gamemapnodetable.add(rank4table).padLeft(90f);
        gamemapnodetable.add(rank5table).padLeft(90f);

        ///This is where the Game Map Node Table is then added to the Core Game Map Table
        coregamemaptable.add(gamemapmessagetable).padBottom(120f).row();
        coregamemaptable.add(gamemapnodetable).padBottom(120f).row();
        coregamemaptable.add(gamemapbuttonstable).padBottom(120f).row();

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(coregamemapstage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        coregamemapstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        coregamemapstage.draw();
    }

    @Override public void resize(int width, int height) {
        coregamemapstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(coregamemapstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        coregamemapstage.dispose();
    }
}
