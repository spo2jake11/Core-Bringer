package com.altf4studios.corebringer.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;
import java.util.Map;

public class InputHandler extends InputAdapter {
    private final Map<String, Integer> keyBindings;

    public InputHandler() {
        keyBindings = new HashMap<>();
        keyBindings.put("PLAY_CARD", Input.Keys.SPACE);
        keyBindings.put("CANCEL", Input.Keys.ESCAPE);
        keyBindings.put("MOVE_UP", Input.Keys.UP);
        keyBindings.put("MOVE_DOWN", Input.Keys.DOWN);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == keyBindings.get("PLAY_CARD")) {
            System.out.println("SPACE pressed (Play Card)");
            return true;
        }
        if (keycode == keyBindings.get("CANCEL")) {
            System.out.println("ESCAPE pressed (Cancel)");
            return true;
        }
        if (keycode == keyBindings.get("MOVE_UP")) {
            System.out.println("UP Arrow pressed");
            return true;
        }
        if (keycode == keyBindings.get("MOVE_DOWN")) {
            System.out.println("DOWN Arrow pressed");
            return true;
        }
        return false;
    }
}
