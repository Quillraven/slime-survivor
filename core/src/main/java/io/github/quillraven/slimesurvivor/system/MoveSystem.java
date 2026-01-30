package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.quillraven.slimesurvivor.component.Move;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

public class MoveSystem extends IteratingSystem {
    private final Viewport gameViewport;

    public MoveSystem(Viewport gameViewport) {
        super(Family.all(Move.class, Transform.class).get());
        this.gameViewport = gameViewport;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = entity.getComponent(Move.class);
        if (move.direction.isZero()) return;

        Rectangle rect = entity.getComponent(Transform.class).rect;
        float newX = rect.getX() + move.direction.x * move.speed * deltaTime;
        float newY = rect.getY() + move.direction.y * move.speed * deltaTime;

        if (entity.getComponent(Player.class) != null) {
            // Clamp to screen bounds
            newX = Math.clamp(newX, 0, gameViewport.getWorldWidth() - rect.getWidth());
            newY = Math.clamp(newY, 0, gameViewport.getWorldHeight() - rect.getHeight());
        }

        rect.setPosition(newX, newY);
    }
}
