package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.time.LocalTimer;
import game.RunAndGunFXGLTypes;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

@Required(HPComponent.class)
public class MovingEnemyComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;

    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    private PhysicsComponent physics;
    private boolean nearbyPitL = false;
    private boolean nearbyPitR = false;
    private boolean movingLeft;
    private boolean movingRight;

    public MovingEnemyComponent() {
        Image image = FXGL.image("testMoveEnemy2.png");

        animIdle = new AnimationChannel(image, 4, 85, 96, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(image, 4, 85, 96, Duration.seconds(1), 1,3);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(42, entity.getHeight() / 2));
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(RunAndGunFXGLTypes.PLAYER);
        double distance = player.getPosition().getX() - entity.getPosition().getX();

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (distance > -entity.getProperties().getInt("alertRange") && distance < 0) {
                if (checkLineOfSight()) {
                    if (!nearbyPitL)
                        moveLeft();
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                    entity.setScaleX(1);
                }
                else
                    completeStop();
            } else if (distance > 0 && distance < entity.getProperties().getInt("alertRange")) {
                if (checkLineOfSight()) {
                    if (!nearbyPitR)
                        moveRight();
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                    entity.setScaleX(-1);
                }
                else
                    completeStop();
            }
        }

        Rectangle2D checkPitLSelection = new Rectangle2D(entity.getX() - 32, entity.getY() + entity.getHeight(), 16, 32);
        List<Entity> checkPitL = FXGL.getGameWorld().getEntitiesInRange(checkPitLSelection).stream().filter(e -> !e.isType(RunAndGunFXGLTypes.MOVINGSTOP) && !e.isType(RunAndGunFXGLTypes.TURRET)).collect(Collectors.toList());
        nearbyPitL = checkPitL.isEmpty();
        if (nearbyPitL && !movingRight)
            stop();

        Rectangle2D checkPitRSelection = new Rectangle2D(entity.getX() + entity.getWidth() + 16, entity.getY() + entity.getHeight(), 16, 32);
        List<Entity> checkPitR = FXGL.getGameWorld().getEntitiesInRange(checkPitRSelection).stream().filter(e -> !e.isType(RunAndGunFXGLTypes.MOVINGSTOP) && !e.isType(RunAndGunFXGLTypes.TURRET)).collect(Collectors.toList());
        nearbyPitR = checkPitR.isEmpty();
        if (nearbyPitR && !movingLeft)
            stop();

        if (entity.getTransformComponent().getScaleX() == -1)
            if (stopCheckLeft())
                completeStop();
        else if (entity.getTransformComponent().getScaleX() == 1)
            if (stopCheckRight())
                completeStop();

    }

    public boolean checkLineOfSight() {
        Entity player = FXGL.getGameWorld().getSingleton(RunAndGunFXGLTypes.PLAYER);
        Integer alertRange = entity.getProperties().getInt("alertRange");
        Rectangle2D selection = new Rectangle2D(entity.getX() - alertRange.doubleValue(), entity.getY() - alertRange.doubleValue(), alertRange.doubleValue() * 2 + entity.getWidth(), alertRange.doubleValue() * 2 + entity.getHeight());
        List<Entity> findWalls = FXGL.getGameWorld().getEntitiesInRange(selection).stream()
                .filter(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(RunAndGunFXGLTypes.WALL) && e.getWidth() < 64).collect(Collectors.toList());

        List<Entity> findFloors = FXGL.getGameWorld().getEntitiesInRange(selection).stream()
                .filter(e -> e.isType(RunAndGunFXGLTypes.WALL) && e.getWidth() > 64).collect(Collectors.toList());

        List<Entity> findWallsFilterLeftRight;
        if (entity.getX() < player.getX())
            findWallsFilterLeftRight = findWalls.stream().filter(e -> e.getX() < player.getX() && e.getX() > entity.getX()).collect(Collectors.toList());
        else
            findWallsFilterLeftRight = findWalls.stream().filter(e -> e.getX() > player.getX() && e.getX() < entity.getX()).collect(Collectors.toList());

        List<Entity> findWallsFilterUpDown;
        List<Entity> findFloorsFilterUpDown;
        if (entity.getBottomY() >= player.getBottomY()) {
            findWallsFilterUpDown = findWallsFilterLeftRight.stream().filter(e -> e.getY() <= entity.getY() && e.getBottomY() > player.getY()).collect(Collectors.toList());
        }
        else {
            findWallsFilterUpDown = findWallsFilterLeftRight.stream().filter(e -> e.getBottomY() >= entity.getY() && e.getY() < player.getY()).collect(Collectors.toList());
        }

        if (!findWallsFilterUpDown.isEmpty() || !findFloors.isEmpty()) {
            for (Entity findWall : findWallsFilterUpDown) {
                if (entity.getX() > findWall.getX() && findWall.getX() > player.getX()) {
                    return false;
                }
                if (entity.getX() < findWall.getX() && findWall.getX() < player.getX()) {
                    return false;
                }
            }
            for (Entity findFloor : findFloors) {
                if (entity.getY() > findFloor.getY() && findFloor.getY() > player.getY()) {
                    return false;
                }
                if (entity.getY() < findFloor.getY() && findFloor.getY() < player.getY()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean stopCheckLeft() {
        double minX = entity.getX() - 10;
        double minY = entity.getY();
        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 10, 80));
        return list
                .stream()
                .anyMatch(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(RunAndGunFXGLTypes.WALL)
                        || e.isType(RunAndGunFXGLTypes.MOVINGENEMY)
                        || e.isType(RunAndGunFXGLTypes.ELITEMOVINGENEMY));
    }

    public boolean stopCheckRight() {
        double minX = entity.getX() + entity.getWidth();
        double minY = entity.getY();
        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 20, 80));
        return list
                .stream()
                .anyMatch(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(RunAndGunFXGLTypes.WALL)
                        || e.isType(RunAndGunFXGLTypes.MOVINGENEMY)
                        || e.isType(RunAndGunFXGLTypes.ELITEMOVINGENEMY));
    }

    public void stop() {
        texture.loopAnimationChannel(animIdle);
        physics.setVelocityX(0);
    }

    public void completeStop() {
        texture.loopAnimationChannel(animIdle);
        physics.setVelocityX(0);
        movingLeft = false;
        movingRight = false;
    }

    public void moveLeft() {
        texture.loopAnimationChannel(animWalk);
        physics.setVelocityX(-100);
        movingLeft = true;
        movingRight = false;
    }

    public void moveRight() {
        texture.loopAnimationChannel(animWalk);
        physics.setVelocityX(100);
        movingRight = true;
        movingLeft = false;
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld().add(0, -12);
        Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
    }

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            FXGL.spawn("enemyDeathEffect", entity.getPosition());
            entity.removeFromWorld();
        }
    }

    public void initHP() {
        hp.setValue(hp.getMaxHP());
    }
}
