package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import io.github.quillraven.slimesurvivor.component.LifeSpan;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

public class CollisionSystem extends EntitySystem {
    private static final float DAMAGE_PER_SECOND = 1.0f;

    private ImmutableArray<Entity> attackEntities = null;
    private ImmutableArray<Entity> playerEntities = null;
    private ImmutableArray<Entity> enemyEntities = null;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        attackEntities = engine.getEntitiesFor(Family.all(Transform.class, LifeSpan.class).get());
        playerEntities = engine.getEntitiesFor(Family.all(Transform.class, Player.class).get());
        enemyEntities = engine.getEntitiesFor(Family.all(Transform.class).exclude(Player.class, LifeSpan.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Player player = playerEntities.first().getComponent(Player.class);

        // Check attack hitbox vs enemies
        for (Entity attackEntity : attackEntities) {
            Rectangle attackRect = attackEntity.getComponent(Transform.class).rect;
            for (Entity enemyEntity : enemyEntities) {
                Rectangle enemyRect = enemyEntity.getComponent(Transform.class).rect;
                if (attackRect.overlaps(enemyRect)) {
                    getEngine().removeEntity(enemyEntity);
                    player.score++;
                }
            }
        }

        // Check enemies vs player
        int numHits = 0;
        Rectangle playerRect = playerEntities.first().getComponent(Transform.class).rect;
        for (Entity enemyEntity : enemyEntities) {
            Rectangle enemyRect = enemyEntity.getComponent(Transform.class).rect;
            if (playerRect.overlaps(enemyRect)) {
                ++numHits;
            }
        }

        if (numHits > 0) {
            player.lifes -= (DAMAGE_PER_SECOND * numHits * deltaTime);
            if (player.isDead()) {
                getEngine().getSystem(SpawnSystem.class).setProcessing(false);
                getEngine().getSystem(MoveSystem.class).setProcessing(false);
                getEngine().getSystem(AttackSystem.class).setProcessing(false);
            }
        }
    }
}
