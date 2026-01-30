package io.github.quillraven.slimesurvivor.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.quillraven.slimesurvivor.component.Graphic;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;

import java.util.Comparator;

import static io.github.quillraven.slimesurvivor.GameScreen.WORLD_HEIGHT;
import static io.github.quillraven.slimesurvivor.GameScreen.WORLD_WIDTH;

public class RenderSystem extends SortedIteratingSystem implements Disposable {
    // General
    private final Batch batch;
    private final Viewport gameViewport;
    private final Viewport uiViewport;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private ImmutableArray<Entity> playerEntities = null;

    // Assets
    private final Texture bgdTexture = new Texture(Gdx.files.internal("bgd.png"));

    public RenderSystem(Batch batch,
                        Viewport gameViewport,
                        Viewport uiViewport,
                        BitmapFont font) {
        super(
            Family.all(Graphic.class, Transform.class).get(),
            Comparator.comparing(entity -> entity.getComponent(Transform.class))
        );

        this.batch = batch;
        this.gameViewport = gameViewport;
        this.uiViewport = uiViewport;
        this.font = font;

        bgdTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.playerEntities = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    @Override
    public void update(float deltaTime) {
        forceSort();

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        drawBackground();
        super.update(deltaTime);
        batch.end();

        drawUI();
    }

    private void drawBackground() {
        // Calculate how many times the texture fits into the world dimensions
        float u2 = gameViewport.getWorldWidth() / WORLD_WIDTH;
        float v2 = gameViewport.getWorldHeight() / WORLD_HEIGHT;

        // Draw with custom UV coordinates to correctly repeat the texture
        batch.draw(bgdTexture,
            0, 0,
            gameViewport.getWorldWidth(),
            gameViewport.getWorldHeight(),
            0, 0,
            u2, v2
        );
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Graphic graphic = entity.getComponent(Graphic.class);
        if (graphic.texture == null) {
            return;
        }

        Transform transform = entity.getComponent(Transform.class);
        Rectangle rect = transform.rect;
        batch.draw(graphic.texture, rect.x, rect.y, rect.width, rect.height);
    }

    private void drawUI() {
        if (playerEntities == null || playerEntities.size() == 0) {
            return;
        }

        final Player player = playerEntities.first().getComponent(Player.class);

        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();
        font.draw(batch, "Score: " + player.score, 20, uiViewport.getWorldHeight() - 20);
        font.draw(batch, "Life: " + String.format("%.1f", Math.max(0f, player.lifes)), 20, uiViewport.getWorldHeight() - 60);
        if (player.isDead()) {
            layout.setText(font, "GAME OVER");
            font.draw(batch, layout, uiViewport.getWorldWidth() / 2 - layout.width / 2, uiViewport.getWorldHeight() / 2 + 40);
            layout.setText(font, "Press R to Restart");
            font.draw(batch, layout, uiViewport.getWorldWidth() / 2 - layout.width / 2, uiViewport.getWorldHeight() / 2 - 30);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        this.bgdTexture.dispose();
    }
}
