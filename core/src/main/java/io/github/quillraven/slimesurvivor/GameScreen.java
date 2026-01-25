package io.github.quillraven.slimesurvivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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

    // General
    private final Batch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport gameViewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final Viewport uiViewport = new ScreenViewport();
    private final BitmapFont font;

    // Player
    private final Player player = new Player(gameViewport.getWorldWidth() / 2f, gameViewport.getWorldHeight() / 2f);
    private final Vector2 inputMovement = new Vector2();

    // Combat
    private final Array<AttackHitbox> attackHitboxes = new Array<>();

    // Enemies
    private final Array<Enemy> enemies = new Array<>();
    private float enemySpawnTimer;

    // Game State
    private int score;

    public GameScreen(GdxGame game) {
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();

        // Generate font from TrueType file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("CherryCreamSoda-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void show() {
        resetGame();
    }

    private void resetGame() {
        player.reset(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2);

        attackHitboxes.clear();

        enemies.clear();
        enemySpawnTimer = 0f;

        score = 0;
    }

    @Override
    public void render(float delta) {
        if (!player.isDead()) {
            processInput(delta);
            updateLogic(delta);
            checkCollisions(delta);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        draw();
    }

    private void processInput(float delta) {
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

        if (!inputMovement.isZero()) {
            inputMovement.nor();
            player.move(inputMovement, delta, gameViewport.getWorldWidth(), gameViewport.getWorldHeight());
        }
    }

    private void updateLogic(float delta) {
        // Attack timer
        if (player.canAttack(delta)) {
            triggerAttack();
        }

        // Attack hitbox duration
        var iterator = attackHitboxes.iterator();
        while (iterator.hasNext()) {
            AttackHitbox attackHitbox = iterator.next();
            if (attackHitbox.updateLifeSpan(delta)) {
                iterator.remove();
            }
        }

        // Enemy spawning
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL) {
            enemySpawnTimer = 0f;
            spawnEnemy();
        }

        // Update enemies
        float playerCenterX = player.getRect().getX() + player.getRect().getWidth() * 0.5f;
        float playerCenterY = player.getRect().getY() + player.getRect().getHeight() * 0.5f;
        for (Enemy enemy : enemies) {
            enemy.update(playerCenterX, playerCenterY, delta);
        }
    }

    private void triggerAttack() {
        float hitboxX = player.getRect().getX() + player.getRect().getWidth() * 0.5f;
        float hitboxY = player.getRect().getY() + player.getRect().getHeight() * 0.5f;
        attackHitboxes.add(new AttackHitbox(hitboxX, hitboxY, player.getMoveDirection()));
    }

    private void spawnEnemy() {
        int edge = MathUtils.random(3); // 0: top, 1: right, 2: bottom, 3: left
        float x, y;

        switch (edge) {
            case 0 -> { // Top
                x = MathUtils.random(0, 1) * gameViewport.getWorldWidth();
                y = gameViewport.getWorldHeight();
            }
            case 1 -> { // Right
                x = gameViewport.getWorldWidth();
                y = MathUtils.random(0, 1) * gameViewport.getWorldHeight();
            }
            case 2 -> { // Bottom
                x = MathUtils.random(0, 1) * gameViewport.getWorldWidth();
                y = -Enemy.SIZE;
            }
            default -> { // Left
                x = -Enemy.SIZE;
                y = MathUtils.random(0, 1) * gameViewport.getWorldHeight();
            }
        }

        enemies.add(new Enemy(x, y));
    }

    private void checkCollisions(float delta) {
        // Check attack hitbox vs enemies
        for (AttackHitbox attackHitbox : attackHitboxes) {
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
            player.subLife(DAMAGE_PER_SECOND * numHits * delta);
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);

        gameViewport.apply();

        shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw player (green)
        player.drawDebug(shapeRenderer, Color.GREEN);

        // Draw enemies (red)
        for (Enemy enemy : enemies) {
            enemy.drawDebug(shapeRenderer, Color.RED);
        }

        // Draw attack hitbox (yellow)
        for (AttackHitbox attackHitbox : attackHitboxes) {
            attackHitbox.drawDebug(shapeRenderer, Color.YELLOW);
        }
        shapeRenderer.end();

        // Draw UI
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, uiViewport.getWorldHeight() - 20);
        font.draw(batch, "Life: " + String.format("%.1f", player.getLife()), 20, uiViewport.getWorldHeight() - 60);
        if (player.isDead()) {
            font.draw(batch, "GAME OVER", uiViewport.getWorldWidth() / 2 - 200, uiViewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to Restart", uiViewport.getWorldWidth() / 2 - 250, uiViewport.getWorldHeight() / 2 - 50);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
