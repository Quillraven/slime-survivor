package io.github.quillraven.slimesurvivor;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.quillraven.slimesurvivor.component.Controls;
import io.github.quillraven.slimesurvivor.component.Graphic;
import io.github.quillraven.slimesurvivor.component.Move;
import io.github.quillraven.slimesurvivor.component.Player;
import io.github.quillraven.slimesurvivor.component.Transform;
import io.github.quillraven.slimesurvivor.system.AnimationSystem;
import io.github.quillraven.slimesurvivor.system.AttackSystem;
import io.github.quillraven.slimesurvivor.system.AudioSystem;
import io.github.quillraven.slimesurvivor.system.CollisionSystem;
import io.github.quillraven.slimesurvivor.system.ControlsSystem;
import io.github.quillraven.slimesurvivor.system.DebugRenderSystem;
import io.github.quillraven.slimesurvivor.system.EnemyAISystem;
import io.github.quillraven.slimesurvivor.system.LifeSpanSystem;
import io.github.quillraven.slimesurvivor.system.MoveSystem;
import io.github.quillraven.slimesurvivor.system.RenderSystem;
import io.github.quillraven.slimesurvivor.system.SpawnSystem;

public class GameScreen extends ScreenAdapter {
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;

    // General
    private final Batch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport gameViewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final Viewport uiViewport = new ScreenViewport();
    private final BitmapFont font;
    private final Engine engine;

    // Assets
    private final Texture playerTexture = new Texture(Gdx.files.internal("player.png"));

    public GameScreen(GdxGame game) {
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();
        this.engine = createEngine();
    }

    private Engine createEngine() {
        Engine engine = new Engine();

        engine.addSystem(new ControlsSystem());
        engine.addSystem(new SpawnSystem(gameViewport));
        engine.addSystem(new AttackSystem());
        engine.addSystem(new EnemyAISystem());
        engine.addSystem(new MoveSystem(gameViewport));
        engine.addSystem(new CollisionSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem(batch, gameViewport, uiViewport, font));
        engine.addSystem(new DebugRenderSystem(shapeRenderer, gameViewport));
        engine.addSystem(new LifeSpanSystem());
        engine.addSystem(new AudioSystem());

        return engine;
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        Entity player = engine.createEntity();
        player.add(new Transform(playerTexture.getWidth() / 32f, playerTexture.getHeight() / 32f));
        player.add(new Graphic(playerTexture));
        player.add(new Controls());
        player.add(new Player(5f));
        player.add(new Move(2f));
        engine.addEntity(player);

        resetGame();
    }

    private void resetGame() {
        // reset player
        ImmutableArray<Entity> playerEntities = engine.getEntitiesFor(Family.all(Player.class).get());
        Entity player = playerEntities.first();
        player.getComponent(Transform.class).rect.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
        player.getComponent(Player.class).reset();

        // remove any non-player entities
        engine.getEntitiesFor(Family.exclude(Player.class).get()).forEach(engine::removeEntity);

        // reset enemy spawn and enable relevant systems again
        engine.getSystem(SpawnSystem.class).setProcessing(true);
        engine.getSystem(MoveSystem.class).setProcessing(true);
        engine.getSystem(SpawnSystem.class).resetTimer();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        engine.update(deltaTime);
    }

    @Override
    public void dispose() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        playerTexture.dispose();
    }
}
