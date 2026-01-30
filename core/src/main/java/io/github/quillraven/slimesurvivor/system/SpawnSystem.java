package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.quillraven.slimesurvivor.component.Graphic;
import io.github.quillraven.slimesurvivor.component.Move;
import io.github.quillraven.slimesurvivor.component.Transform;

public class SpawnSystem extends EntitySystem implements Disposable {
    private static final float ENEMY_SPAWN_INTERVAL = 1.5f;
    private static final float ENEMY_SCALE = 1 / 32f;

    private final Texture enemyTexture = new Texture(Gdx.files.internal("slime.png"));
    private final Viewport gameViewport;
    private float enemySpawnTimer;

    public SpawnSystem(Viewport gameViewport) {
        this.gameViewport = gameViewport;
    }

    @Override
    public void update(float deltaTime) {
        enemySpawnTimer += deltaTime;
        if (!(enemySpawnTimer >= ENEMY_SPAWN_INTERVAL)) {
            return;
        }

        enemySpawnTimer = 0f;

        Entity enemy = getEngine().createEntity();
        Transform transform = new Transform(enemyTexture.getWidth() / 32f, enemyTexture.getHeight() / 32f);
        enemy.add(transform);
        enemy.add(new Graphic(enemyTexture));
        enemy.add(new Move(1.5f));
        getEngine().addEntity(enemy);

        int edge = MathUtils.random(3); // 0: top, 1: right, 2: bottom, 3: left
        switch (edge) {
            case 0 -> // Top
                transform.rect.setPosition(
                    MathUtils.random(0, 1) * gameViewport.getWorldWidth(),
                    gameViewport.getWorldHeight()
                );
            case 1 -> // Right
                transform.rect.setPosition(
                    gameViewport.getWorldWidth(),
                    MathUtils.random(0, 1) * gameViewport.getWorldHeight()
                );
            case 2 -> // Bottom
                transform.rect.setPosition(
                    MathUtils.random(0, 1) * gameViewport.getWorldWidth(),
                    -enemyTexture.getHeight() * ENEMY_SCALE
                );
            default -> // Left
                transform.rect.setPosition(
                    -enemyTexture.getWidth() * ENEMY_SCALE,
                    MathUtils.random(0, 1) * gameViewport.getWorldHeight()
                );
        }
    }

    public void resetTimer() {
        enemySpawnTimer = 0f;
    }

    @Override
    public void dispose() {
        enemyTexture.dispose();
    }
}
