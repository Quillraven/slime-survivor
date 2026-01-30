package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.quillraven.slimesurvivor.component.LifeSpan;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

import java.util.Comparator;

public class DebugRenderSystem extends SortedIteratingSystem {
    private static final boolean DRAW_DEBUG = true;

    private final ShapeRenderer shapeRenderer;
    private final Viewport gameViewport;

    public DebugRenderSystem(ShapeRenderer shapeRenderer, Viewport gameViewport) {
        super(
            Family.all(Transform.class).get(),
            Comparator.comparing(entity -> entity.getComponent(Transform.class))
        );

        this.shapeRenderer = shapeRenderer;
        this.gameViewport = gameViewport;
        setProcessing(DRAW_DEBUG);
    }

    @Override
    public void update(float deltaTime) {
        gameViewport.apply();
        shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        super.update(deltaTime);
        shapeRenderer.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = entity.getComponent(Transform.class);
        Rectangle rect = transform.rect;
        final Color color;
        if (entity.getComponent(Player.class) != null) {
            color = Color.GREEN;
        } else if (entity.getComponent(LifeSpan.class) != null) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }

        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
    }
}
