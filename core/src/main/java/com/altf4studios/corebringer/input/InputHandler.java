package com.altf4studios.corebringer.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;
import java.util.Map;

public class InputHandler extends InputAdapter {
    private final Map<String, Integer> keyBindings;
    private ActionCallback callback;

    public interface ActionCallback {
        void onPlayCard();
        void onCancel();
        void onMoveUp();
        void onMoveDown();
        void onDebug(); // D key
    }

    public InputHandler(ActionCallback callback) {
        this.callback = callback;
        keyBindings = new HashMap<>();
        keyBindings.put("PLAY_CARD", Input.Keys.SPACE);
        keyBindings.put("CANCEL", Input.Keys.ESCAPE);
        keyBindings.put("MOVE_UP", Input.Keys.UP);
        keyBindings.put("MOVE_DOWN", Input.Keys.DOWN);
        keyBindings.put("DEBUG", Input.Keys.D);
    }

    @Override
    public boolean keyDown(int keycode) {
        System.out.println("[InputHandler] keyDown called with keycode: " + keycode);
        if (keycode == keyBindings.get("PLAY_CARD")) {
            if (callback != null) callback.onPlayCard();
            return true;
        }
        if (keycode == keyBindings.get("CANCEL")) {
            if (callback != null) callback.onCancel();
            return true;
        }
        if (keycode == keyBindings.get("MOVE_UP")) {
            if (callback != null) callback.onMoveUp();
            return true;
        }
        if (keycode == keyBindings.get("MOVE_DOWN")) {
            if (callback != null) callback.onMoveDown();
            return true;
        }
        if (keycode == keyBindings.get("DEBUG")) {
            if (callback != null) callback.onDebug();
            return true;
        }
        return false;
    }
}
