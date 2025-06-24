package com.altf4studios.corebringer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DebugScreen implements Screen {
    private Main corebringer;
    private Stage coredebugscreenstage;
    private Table coredebugscreentable;
    private Table debugscreeninfotable;
    private Table debugscreenbuttons;
    private Label fpsdebug;
    private TextButton returntomainmenu;

    public DebugScreen(Main corebringer) {
        this.corebringer = corebringer; ///The Master Key that holds all screens together

        ///Here's everything that will initiate upon doing the secret combo
        coredebugscreenstage = new Stage(new FitViewport(1280, 720));
        coredebugscreentable = new Table();
        coredebugscreentable.setFillParent(true);
        coredebugscreenstage.addActor(coredebugscreentable);

        ///Parameters for the Debug Info Table
        debugscreeninfotable = new Table();
        debugscreeninfotable.top().left().pad(10f);

        ///Parameters for the Buttons in the Debug Screen
        debugscreenbuttons = new Table();
        debugscreenbuttons.bottom().center().padBottom(20f);

        ///Parameters for the FPS
        fpsdebug = new Label("FPS: ", corebringer.testskin);

        ///Parameters for the Return Button
        returntomainmenu = new TextButton("Return to Main Menu?", corebringer.testskin);

        ///This will give function to the Return Button
        returntomainmenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
            }
        });

        ///This is where the debug info and the return button will be called
        debugscreeninfotable.add(fpsdebug);
        debugscreenbuttons.add(returntomainmenu);

        ///Table calling here since IDE reads code per line
        coredebugscreentable.add(debugscreeninfotable).expand().top().left().pad(10f);
        coredebugscreentable.row();
        coredebugscreentable.add(debugscreenbuttons).expand().bottom().center().padBottom(20f);
    }
    @Override
    public void show() {
        Gdx.input.setInputProcessor(coredebugscreenstage);
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        coredebugscreenstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        coredebugscreenstage.draw();

        fpsdebug.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }
    @Override public void resize(int width, int height) {
        coredebugscreenstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(coredebugscreenstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        coredebugscreenstage.dispose();
    }
}
