package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class StartGameMapScreen implements Screen{
    /// Declaration of variables and elements here.
    private Main corebringer;
    private Stage startgamemapstage;
    private Table startgamemapcoretable;
    private Table sidebarmenutable;
    private TextButton optionsbutton;
    private TextButton returnbutton;
    private Label testlabel;
    private Label testlabel2;
    private Table tiertable1;
    private Table tiertable2;
    private Table tiertable3;
    private Table tiertable4;
    private Table tiertable5;
    private Table tiertable6;
    private Table tiertable7;

    private Stage battleStage;
    private Stage cardStage;
    private Stage editorStage;


    public StartGameMapScreen(Main corebringer) {
        this.corebringer = corebringer; /// The Master Key that holds all screens together

        ///This stages are separated to lessen complications
        battleStage = new Stage(new ScreenViewport());
        editorStage = new Stage(new ScreenViewport());
//        cardStage = new Stage(new ScreenViewport());

        ///Every stages provides a main method for them
        ///They also have local variables and objects for them to not interact with other methods
        battleStageUI();
        editorStageUI();
        cardStageUI();
        /// Here's all the things that will initialize once the Start Button is clicked.

        /// This provides lines to be able to monitor the objects' boundaries
        battleStage.setDebugAll(true);
        editorStage.setDebugAll(true);
//        startgamemapstage = new Stage(new FitViewport(1280, 720));
//        startgamemapcoretable = new Table();
//        startgamemapcoretable.setFillParent(true);
//        sidebarmenutable = new Table();
//        sidebarmenutable.setFillParent(true);
//        startgamemapstage.addActor(startgamemapcoretable);
//        startgamemapstage.addActor(sidebarmenutable);
//
//        ///Tier-based Logic System
//        tiertable1 = new Table();
//        TextButton nodeA = new TextButton("A", corebringer.testskin);
//        TextButton nodeB = new TextButton("B", corebringer.testskin);
//        TextButton nodeC = new TextButton("C", corebringer.testskin);
//        TextButton nodeD = new TextButton("D", corebringer.testskin);
//        tiertable1.add(nodeA).pad(20f);
//        tiertable1.add(nodeB).pad(20f);
//        tiertable1.add(nodeC).pad(20f);
//        tiertable1.add(nodeD).pad(20f);
//
//        tiertable2 = new Table();
//        TextButton nodeE = new TextButton("E", corebringer.testskin);
//        TextButton nodeF = new TextButton("F", corebringer.testskin);
//        TextButton nodeG = new TextButton("G", corebringer.testskin);
//        TextButton nodeH = new TextButton("H", corebringer.testskin);
//        tiertable2.add(nodeE).pad(20f);
//        tiertable2.add(nodeF).pad(20f);
//        tiertable2.add(nodeG).pad(20f);
//        tiertable2.add(nodeH).pad(20f);
//
//        tiertable3 = new Table();
//        TextButton nodeI = new TextButton("I", corebringer.testskin);
//        TextButton nodeJ = new TextButton("J", corebringer.testskin);
//        TextButton nodeK = new TextButton("K", corebringer.testskin);
//        TextButton nodeL = new TextButton("L", corebringer.testskin);
//        tiertable3.add(nodeI).pad(20f);
//        tiertable3.add(nodeJ).pad(20f);
//        tiertable3.add(nodeK).pad(20f);
//        tiertable3.add(nodeL).pad(20f);
//
//        /// Buttons for the sidebar menu here
//        optionsbutton = new TextButton("Options", corebringer.testskin);
//        returnbutton = new TextButton("Main Menu", corebringer.testskin);
//        sidebarmenutable.left();
//        sidebarmenutable.add(optionsbutton).width(250f).height(50f).pad(10f).row();
//        sidebarmenutable.add(returnbutton).width(250f).height(50f).pad(10f).row();
//
//        ///This is the sidebar menu for returning to main menu and tweaking things in options
//        ///This gives function to the Options Button
//        optionsbutton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                corebringer.setScreen(corebringer.optionsScreen);
//            }
//        });
//
//        ///This gives function to the Return Button
//        returnbutton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                corebringer.setScreen(corebringer.mainMenuScreen);
//            }
//        });
//
//
//        ///Test function to Node A
//        nodeA.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                testlabel = new Label(" Nigga, I just got ", corebringer.testskin);
//                testlabel2 = new Label(" raped :(", corebringer.testskin);
//                sidebarmenutable.add(testlabel).row();
//                sidebarmenutable.add(testlabel2).row();
//            }
//        });
//
//        /// Calling of tables here because IDE reads things code per line
//        startgamemapcoretable.bottom();
//        startgamemapcoretable.add(tiertable3).padTop(10f).row();
//        startgamemapcoretable.add(tiertable2).padTop(10f).row();
//        startgamemapcoretable.add(tiertable1).padTop(10f);
    }

    private void cardStageUI() {

    }

    private void editorStageUI() {
        /// This is where the code editor will be
        Table editorTable = new Table();
        Label codeLabel = new Label("This is where the code will be!", corebringer.testskin);
        codeLabel.setAlignment(Align.center);
        editorTable.bottom();
        editorTable.setFillParent(true);


        /// These now will become the submenu buttons when created
        Table submenuTable = new Table();
        submenuTable.bottom();
        submenuTable.setFillParent(false);
        TextButton btnOptions = new TextButton("Options", corebringer.testskin);
        TextButton btnLog = new TextButton("Logs", corebringer.testskin);
        TextButton btnCheckDeck = new TextButton("Deck", corebringer.testskin);
        TextButton btnCharacter = new TextButton("Character", corebringer.testskin);

        /// Default format for submenuTable
        submenuTable.defaults().space(25).pad(5).fill().uniform();
        /// Row 1
        submenuTable.add(btnOptions);
        submenuTable.add(btnCheckDeck).row();
        /// Row 2
        submenuTable.add(btnLog);
        submenuTable.add(btnCharacter);
        /// Everything is now added into the editorTable
        editorTable.add(codeLabel).height(200).growX().bottom();
        editorTable.add(submenuTable).height(200).bottom().right();

        editorStage.addActor(editorTable);

        /// This Click Listener opens the option window
        btnOptions.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindowUI();
            }
        });
    }

    private void battleStageUI() {

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setFillParent(true);

        /// These labels provides placeholders for the actuals objects later on
        Label userHpLabel = new Label("100", corebringer.testskin);
        Label enemyHpLabel = new Label("100", corebringer.testskin);
        Label userTemplate = new Label("Player", corebringer.testskin);
        Label enemyTemplate = new Label("Enemy", corebringer.testskin);

        /// This edits the alignment of the labels
        userHpLabel.setAlignment(Align.center);
        userTemplate.setAlignment(Align.center);
        enemyHpLabel.setAlignment(Align.center);
        enemyHpLabel.setAlignment(Align.center);

        /// This is the table configurations to be able to locate them
        actionTable.defaults().padTop(50);
        actionTable.add(userHpLabel).height(25).width(500).padLeft(50);
        actionTable.add(enemyHpLabel).height(25).width(500).padRight(50).row();
        actionTable.defaults().reset();
        actionTable.defaults().padTop(65);
        actionTable.add(userTemplate).height(50).pad(100).center();
        actionTable.add(enemyTemplate).height(50).pad(100).center();
        battleStage.addActor(actionTable);
    }

    private void optionsWindowUI(){
        /// This creates an option window
        Window optionsWindow = new Window("Options", corebringer.testskin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        optionsWindow.setSize(640, 480);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() / 2 /2,
            Gdx.graphics.getHeight() /2 / 2
        );
        /// This button will close the options menu
        TextButton btnClose = new TextButton("Close", corebringer.testskin);
        btnClose.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.remove();
            }
        });
        /// This is where we add objects into the optionsWindow
        optionsWindow.add(new Label("Options go here", corebringer.testskin)).top().row();
        optionsWindow.add(btnClose).padTop(20).bottom();

        /// This is where it is added into the stage
        editorStage.addActor(optionsWindow);
    }

    @Override
    public void show() {
        /// This gives the button functions to become clickable
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(editorStage);
        Gdx.input.setInputProcessor(editorStage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        /// Since there are multiple stages, they are needed to be drawn separately
        battleStage.act(delta);
        battleStage.draw();

        editorStage.act(delta);
        editorStage.draw();
//        startgamemapstage.act(delta); ////Used to call the Stage and render the elements that is inside it
//        startgamemapstage.draw();     bat
    }

    @Override public void resize(int width, int height) {
//        startgamemapstage.getViewport().update(width, height, true);
//        Gdx.input.setInputProcessor(startgamemapstage);
        battleStage.getViewport().update(width, height, true);
        editorStage.getViewport().update(width, height, true);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        battleStage.dispose();
        editorStage.dispose();

    }
}
