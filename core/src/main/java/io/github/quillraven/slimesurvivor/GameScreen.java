package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final float ENEMY_SPAWN_INTERVAL = 1.5f;
    private static final float DAMAGE_PER_SECOND = 1.0f;
    private static final boolean DRAW_DEBUG = false;

    // General
    private final Batch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport gameViewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final Viewport uiViewport = new ScreenViewport();
    private final BitmapFont font;

    // Assets
    private final Texture bgdTexture = new Texture(Gdx.files.internal("bgd.png"));
    private final Texture playerTexture = new Texture(Gdx.files.internal("player.png"));
    private final Texture enemyTexture = new Texture(Gdx.files.internal("slime.png"));
    private final Array<Texture> attackTextures = new Array<>(14);
    {
        for (int i = 0; i <= 13; i++) {
            attackTextures.add(new Texture(Gdx.files.internal(String.format("slash_%02d.png", i))));
        }
    }
    private final Animation<Texture> attackAnimation = new Animation<>(1 / 12f, attackTextures);
    private final Music music = Gdx.audio.newMusic(Gdx.files.internal("nightsplitter.mp3"));
    private final Sound slashSfx = Gdx.audio.newSound(Gdx.files.internal("slash.wav"));

    // Player
    private final Player player = new Player(
        WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, // spawn location
        gameViewport, // boundary
        playerTexture, // graphic
        attackAnimation, // attack animation
        slashSfx // attack sound effect
    );
    private final Vector2 inputMovement = new Vector2();

    // Enemies
    private final Array<Enemy> enemies = new Array<>();
    private float enemySpawnTimer;

    // Game State
    private int score;

    public GameScreen(GdxGame game) {
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();
    }

    @Override
    public void show() {
        resetGame();
    }

    private void resetGame() {
        player.reset(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);

        enemies.clear();
        enemySpawnTimer = 0f;

        score = 0;

        music.stop();
        music.setLooping(true);
        music.play();
    }

    @Override
    public void render(float deltaTime) {
        if (!player.isDead()) {
            processInput();
            updateLogic(deltaTime);
            checkCollisions(deltaTime);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        draw();
    }

    private void processInput() {
        inputMovement.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            inputMovement.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            inputMovement.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputMovement.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            inputMovement.x += 1;
        }

        inputMovement.nor();
        player.changeDirection(inputMovement);
    }

    private void updateLogic(float deltaTime) {
        // Player (attacks + movement)
        player.update(deltaTime);

        // Enemy spawning
        enemySpawnTimer += deltaTime;
        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL) {
            enemySpawnTimer = 0f;
            Enemy newEnemy = Enemy.spawn(gameViewport, enemyTexture, player);
            enemies.add(newEnemy);
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
        }
    }

    private void checkCollisions(float deltaTime) {
        // Check attack hitbox vs enemies
        for (AttackHitbox attackHitbox : player.getAttackHitboxes()) {
            var iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                if (attackHitbox.overlaps(enemy)) {
                    iterator.remove();
                    score++;
                }
            }
        }

        // Check enemies vs player
        int numHits = 0;
        for (Enemy enemy : enemies) {
            if (player.overlaps(enemy)) {
                ++numHits;
            }
        }

        if (numHits > 0) {
            player.subLife(DAMAGE_PER_SECOND * numHits * deltaTime);
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);

        gameViewport.apply();

        // Draw textures
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        batch.draw(bgdTexture, 0, 0, gameViewport.getWorldWidth(), gameViewport.getWorldHeight());
        for (Enemy enemy : enemies) {
            enemy.draw(batch);
        }
        for (AttackHitbox attackHitbox : player.getAttackHitboxes()) {
            attackHitbox.draw(batch);
        }
        player.draw(batch);
        batch.end();

        drawDebug();

        // Draw UI
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, uiViewport.getWorldHeight() - 20);
        font.draw(batch, "Life: " + String.format("%.1f", player.getLife()), 20, uiViewport.getWorldHeight() - 60);
        if (player.isDead()) {
            font.draw(batch, "GAME OVER", uiViewport.getWorldWidth() / 2 - 100, uiViewport.getWorldHeight() / 2 + 40);
            font.draw(batch, "Press R to Restart", uiViewport.getWorldWidth() / 2 - 135, uiViewport.getWorldHeight() / 2 - 30);
        }
        batch.end();
    }

    private void drawDebug() {
        if (!DRAW_DEBUG) {
            return;
        }

        shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw player (green)
        player.drawDebug(shapeRenderer, Color.GREEN);

        // Draw enemies (red)
        for (Enemy enemy : enemies) {
            enemy.drawDebug(shapeRenderer, Color.RED);
        }

        // Draw attack hitbox (yellow)
        for (AttackHitbox attackHitbox : player.getAttackHitboxes()) {
            attackHitbox.drawDebug(shapeRenderer, Color.YELLOW);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        bgdTexture.dispose();
        playerTexture.dispose();
        enemyTexture.dispose();
        attackTextures.forEach(Texture::dispose);
        music.dispose();
        slashSfx.dispose();
    }
}
