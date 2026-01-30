package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.quillraven.slimesurvivor.component.LifeSpan;
import io.github.quillraven.slimesurvivor.component.Move;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

public class EnemyAISystem extends IteratingSystem {
    private static final Vector2 TMP_VEC2 = new Vector2();

    private ImmutableArray<Entity> playerEntities = null;

    public EnemyAISystem() {
        super(Family.all(Transform.class, Move.class).exclude(Player.class, LifeSpan.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        playerEntities = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (playerEntities == null || playerEntities.size() == 0) return;

        Move move = entity.getComponent(Move.class);
        Rectangle rect = entity.getComponent(Transform.class).rect;
        Transform playerTransform = playerEntities.first().getComponent(Transform.class);

        Vector2 direction = playerTransform.getCenter(TMP_VEC2)
            .sub(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f) // direction to player
            .nor(); // normalize to avoid faster diagonal movement

        // move towards the player center
        move.direction.set(direction);
    }
}
