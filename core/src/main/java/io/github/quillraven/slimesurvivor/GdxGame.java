package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
