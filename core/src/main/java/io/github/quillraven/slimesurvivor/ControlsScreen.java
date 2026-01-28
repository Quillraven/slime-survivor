package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ControlsScreen extends ScreenAdapter {
    private final GdxGame game;
    private final Batch batch;
    private final BitmapFont font;
    private final Viewport viewport = new ScreenViewport();

    public ControlsScreen(GdxGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
    }

    @Override
    public void render(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }

        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float centerX = viewport.getWorldWidth() / 2;
        float y = viewport.getWorldHeight() / 2 + 100;

        font.draw(batch, "CONTROLS", centerX - 80, y);
        y -= 50;
        font.draw(batch, "W / A / S / D - Move", centerX - 140, y);
        y -= 40;
        font.draw(batch, "R - Restart (when dead)", centerX - 155, y);
        y -= 70;
        font.draw(batch, "Press SPACE to start", centerX - 145, y);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
