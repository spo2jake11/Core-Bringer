package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.LoggingUtils;
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

import java.util.ArrayList;
import java.util.Random;

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
    private Table rank6table;
    private Table rank7table;
    private Table rank8table;
    private Table rank9table;
    private Table rank10table;
    private Image mapbackgroundimg;
    private ImageButton staticbattlenodeA;
    private ImageButton staticbattlenodeB;
    private ImageButton staticbattlenodeC;
    private ImageButton staticbattlenodeD;
    private ImageButton randombattlenode;
    private ImageButton restnode;
    private ImageButton merchantnode;
    private ImageButton cardsmithnode;
    private ImageButton searchnode;
    private ImageButton bossnodeA;
    private TextButton returnbutton;
    private Label gamemapmessages;
    private Label gamemapmessages2;
    private Label gamemapmessages3;
    private Image gamemapmessagesdisplay;
    private Random counter;
    private int totalnodescounter;
    private TextureAtlas gamemapatlas;
    private TextureAtlas gamemapbackgroundatlas;

    public GameMapScreen(Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        coregamemapstage = new Stage(new FitViewport(1280, 720));
        coregamemaptable = new Table();
        gamemapatlas = new TextureAtlas(Utils.getInternalPath("assets/icons/nodes/200node_atlas.atlas")); ///Change to 200 or 300node_atlas.atlas if needed
        gamemapbackgroundatlas = new TextureAtlas(Utils.getInternalPath("assets/icons/nodes/100node_atlas.atlas"));
        coregamemaptable.setFillParent(true);
        coregamemapstage.addActor(coregamemaptable);

        ///Core Table Parameters
        mapbackgroundimg = new Image(new Texture("backgrounds/map_table.png"));
        mapbackgroundimg.setFillParent(true);
        coregamemaptable.addActor(mapbackgroundimg);

        ///Code for the initialization of the Game Map Node Table
        gamemapnodetable = new Table();

        ///Code for the Nodes (Rank represents Columns, meaning Rank 1 is First Node Column of 4)
        ///First row (RANK 1)
        rank1table = new Table();
        staticbattlenodeA = createAtlasButton("combat_node");
        staticbattlenodeB = createAtlasButton("combat_node");
        staticbattlenodeC = createAtlasButton("combat_node");
        staticbattlenodeD = createAtlasButton("combat_node");
        staticbattlenodeA.getImage().setScaling(Scaling.stretch);
        staticbattlenodeB.getImage().setScaling(Scaling.stretch);
        staticbattlenodeC.getImage().setScaling(Scaling.stretch);
        staticbattlenodeD.getImage().setScaling(Scaling.stretch);

        ///Rank 2 to 9 Tables Initialized here
        rank2table = new Table();
        rank3table = new Table();
        rank4table = new Table();
        rank5table = new Table();
        rank6table = new Table();
        rank7table = new Table();
        rank8table = new Table();
        rank9table = new Table();

        ///Initialization of the Button List of RANK 2 and RANK 9 to help with Button Presence Validation
        ArrayList<TextButton> nodesinrank2 = new ArrayList<>();

        ///Initialization of the Random Counter to help with the Random Node Generator
        counter = new Random();

        ///Initialization of the counter for Total Nodes to help with the Random Node Generator
        totalnodescounter = 4;

        ///Tenth row (RANK 10)
        rank10table = new Table();
        bossnodeA = createAtlasButton("boss_node");
        bossnodeA.getImage().setScaling(Scaling.fit);

        /*bossnodeB = new TextButton("BF", corebringer.testskin);
        bossnodeC = new TextButton("BF", corebringer.testskin);
        bossnodeD = new TextButton("BF", corebringer.testskin);*/

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank2table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        // Create a fresh GameScreen instance (previous may have been disposed)
                        corebringer.gameScreen = new GameScreen(corebringer);
                        corebringer.setScreen(corebringer.gameScreen);
                        corebringer.gameScreen.rerollEnemyAndCards();
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank2table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank2table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank2table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank2table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank2table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 2 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank3table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.gameScreen = new GameScreen(corebringer);
                        corebringer.setScreen(corebringer.gameScreen);
                        corebringer.gameScreen.rerollEnemyAndCards();
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank3table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank3table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank3table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank3table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank3table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 3 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank4table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.gameScreen = new GameScreen(corebringer);
                        corebringer.setScreen(corebringer.gameScreen);
                        corebringer.gameScreen.rerollEnemyAndCards();
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank4table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank4table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank4table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank4table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank4table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 4 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank5table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.gameScreen = new GameScreen(corebringer);
                        corebringer.setScreen(corebringer.gameScreen);
                        corebringer.gameScreen.rerollEnemyAndCards();
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank5table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank5table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank5table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank5table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank5table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 5 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank6table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.setScreen(corebringer.gameScreen);
                        if (corebringer.gameScreen != null) {
                            corebringer.gameScreen.rerollEnemyAndCards();
                        }
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank6table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank6table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank6table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank6table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank6table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 6 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank7table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.setScreen(corebringer.gameScreen);
                        if (corebringer.gameScreen != null) {
                            corebringer.gameScreen.rerollEnemyAndCards();
                        }
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank7table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank7table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank7table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank7table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank7table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 7 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank8table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.setScreen(corebringer.gameScreen);
                        if (corebringer.gameScreen != null) {
                            corebringer.gameScreen.rerollEnemyAndCards();
                        }
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank8table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank8table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank8table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank8table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank8table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 8 has nodes already or has problems.");
        }

        ///This is for the Node Map Generator for Random Nodes at RANK 2 and RANK 9
        if (rank9table.getChildren().size == 0) {
            ///Nodes to be Randomized between RANK 2 and RANK 9
            randombattlenode = createAtlasButton("combat_node");
            restnode = createAtlasButton("rest_node");
            merchantnode = createAtlasButton("shop_node");
            searchnode = createAtlasButton("search_node");
            randombattlenode.getImage().setScaling(Scaling.stretch);
            restnode.getImage().setScaling(Scaling.stretch);
            merchantnode.getImage().setScaling(Scaling.stretch);
            searchnode.getImage().setScaling(Scaling.stretch);

            // Add click listener for search node to show puzzle screen
            searchnode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    corebringer.setScreen(corebringer.puzzleScreen);
                }
            });

            ///Functionalities for the Randomized Nodes
            randombattlenode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Fade out map music, fade in game music
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                        corebringer.setScreen(corebringer.gameScreen);
                        if (corebringer.gameScreen != null) {
                            corebringer.gameScreen.rerollEnemyAndCards();
                        }
                    });
                }
            });

            for (int x = 0; x < totalnodescounter; x++) {
                int nodeselection = counter.nextInt(5) + 1; /// rolls between 1 to 5
                switch (nodeselection) {
                    case 1:
                        rank9table.add(randombattlenode).padBottom(20f).row();
                        break;
                    case 2:
                        rank9table.add(restnode).padBottom(20f).row();
                        break;
                    case 3:
                        rank9table.add(merchantnode).padBottom(20f).row();
                        break;
                    case 4:
                        rank9table.add(cardsmithnode).padBottom(20f).row();
                        break;
                    case 5:
                        rank9table.add(searchnode).padBottom(20f).row();
                        break;
                }
            }
        } else {
            LoggingUtils.log("NodeGeneration","Rank 9 has nodes already or has problems.");
        }

        ///For the functionalities of the Rank 1 Battle Nodes
        staticbattlenodeA.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Fade out map music, fade in game music
                corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                    corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                    corebringer.gameScreen = new GameScreen(corebringer);
                    corebringer.setScreen(corebringer.gameScreen);
                    corebringer.gameScreen.rerollEnemyAndCards();
                });
            }
        });

        staticbattlenodeB.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                    corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                    corebringer.gameScreen = new GameScreen(corebringer);
                    corebringer.setScreen(corebringer.gameScreen);
                    corebringer.gameScreen.rerollEnemyAndCards();
                });
            }
        });

        staticbattlenodeC.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                    corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                    corebringer.gameScreen = new GameScreen(corebringer);
                    corebringer.setScreen(corebringer.gameScreen);
                    corebringer.gameScreen.rerollEnemyAndCards();
                });
            }
        });

        staticbattlenodeD.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                    corebringer.fadeInMusic(corebringer.corebringergamescreenbgm, 1f);
                    corebringer.gameScreen = new GameScreen(corebringer);
                    corebringer.setScreen(corebringer.gameScreen);
                    corebringer.gameScreen.rerollEnemyAndCards();
                });
            }
        });

        ///This is where the Nodes and their specific ranks is added
        ///For Rank 1
        rank1table.add(staticbattlenodeA).padBottom(20f).row();
        rank1table.add(staticbattlenodeB).padBottom(20f).row();
        rank1table.add(staticbattlenodeC).padBottom(20f).row();
        rank1table.add(staticbattlenodeD).padBottom(20f).row();

        ///For Rank 10
        rank10table.add(bossnodeA).padBottom(20f).row();

        /*rank10table.add(bossnodeB).padBottom(20f).row();
        rank10table.add(bossnodeC).padBottom(20f).row();
        rank10table.add(bossnodeD).padBottom(20f).row();*/


        ///This is where the messages and tips in the game will go
        gamemapmessagetable = new Table();

        gamemapmessages = new Label("Did you know?: Dying in this game is permanent. :D", corebringer.testskin);
        gamemapmessages2 = new Label("Here's what the nodes mean: ", corebringer.testskin);
        gamemapmessagesdisplay = new Image(new Texture("assets/icons/nodes/100node_atlas.png"));
        gamemapmessagesdisplay.setSize(20f, 20f);
        gamemapmessages3 = new Label("Boss, Battle, Search, Rest, Merchant", corebringer.testskin);

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
        /*gamemapmessagetable.add(gamemapmessages2).row();
        gamemapmessagetable.add(gamemapmessagesdisplay).row();
        gamemapmessagetable.add(gamemapmessages3).row();*/

        ///Placement of the buttons in the Game Map Buttons Table
        gamemapbuttonstable.add(returnbutton);

        ///Placement of Ranks to the Game Map Node Table (Not the Core Game Map Table, but the separate one)
        gamemapnodetable.add(rank1table).padLeft(40f);
        gamemapnodetable.add(rank2table).padLeft(40f);
        gamemapnodetable.add(rank3table).padLeft(40f);
        gamemapnodetable.add(rank4table).padLeft(40f);
        gamemapnodetable.add(rank5table).padLeft(40f);
        gamemapnodetable.add(rank6table).padLeft(40f);
        gamemapnodetable.add(rank7table).padLeft(40f);
        gamemapnodetable.add(rank8table).padLeft(40f);
        gamemapnodetable.add(rank9table).padLeft(40f);
        gamemapnodetable.add(rank10table).padLeft(40f);

        ///This is where the Game Map Node Table is then added to the Core Game Map Table
        coregamemaptable.add(gamemapmessagetable).padBottom(50f).row();
        coregamemaptable.add(gamemapnodetable).padBottom(50f).row();
        coregamemaptable.add(gamemapbuttonstable).padBottom(50f).row();

    }

    private ImageButton createAtlasButton(String regionName) {
        TextureRegionDrawable base = new TextureRegionDrawable(gamemapatlas.findRegion(regionName));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = base;
        style.over = base.tint(new Color(1f, 1f, 1f, 0.9f));
        style.down = base.tint(new Color(0.85f, 0.85f, 0.85f, 1f));
        style.checked = base.tint(new Color(0.9f, 0.9f, 0.9f, 1f));
        return new ImageButton(style);
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
