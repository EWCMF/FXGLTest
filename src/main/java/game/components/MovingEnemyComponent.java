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
import game.BasicGameTypes;
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
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
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

        Rectangle2D checkPitLSelection = new Rectangle2D(entity.getX() - 20, entity.getY() + entity.getHeight(), 10, 10);
        List<Entity> checkPitL = FXGL.getGameWorld().getEntitiesInRange(checkPitLSelection).stream().filter(e -> !e.isType(BasicGameTypes.MOVINGSTOP)).collect(Collectors.toList());
        nearbyPitL = checkPitL.isEmpty();
        if (nearbyPitL && !movingRight)
            stop();

        Rectangle2D checkPitRSelection = new Rectangle2D(entity.getX() + entity.getWidth() + 10, entity.getY() + entity.getHeight(), 10, 10);
        List<Entity> checkPitR = FXGL.getGameWorld().getEntitiesInRange(checkPitRSelection);
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
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        Integer alertRange = entity.getProperties().getInt("alertRange");
        Rectangle2D selection = new Rectangle2D(entity.getX() - alertRange.doubleValue(), entity.getY() - alertRange.doubleValue(), alertRange.doubleValue() * 2 + entity.getWidth(), alertRange.doubleValue() * 2 + entity.getHeight());
        List<Entity> findWalls = FXGL.getGameWorld().getEntitiesInRange(selection).stream()
                .filter(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(BasicGameTypes.WALL) && e.getWidth() < 64).collect(Collectors.toList());

        List<Entity> findFloors = FXGL.getGameWorld().getEntitiesInRange(selection).stream()
                .filter(e -> e.isType(BasicGameTypes.WALL) && e.getWidth() > 64).collect(Collectors.toList());

        if (!findWalls.isEmpty() || !findFloors.isEmpty()) {
            for (Entity findWall : findWalls) {
                if (entity.getX() > findWall.getX() && findWall.getX() > player.getX()) {
                    if (entity.getY() >= findWall.getBottomY() && player.getY() - entity.getY() < 0)
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
                        || e.isType(BasicGameTypes.WALL)
                        || e.isType(BasicGameTypes.MOVINGENEMY)
                        || e.isType(BasicGameTypes.ELITEMOVINGENEMY));
    }

    public boolean stopCheckRight() {
        double minX = entity.getX() + entity.getWidth();
        double minY = entity.getY();
        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 20, 80));
        return list
                .stream()
                .anyMatch(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(BasicGameTypes.WALL)
                        || e.isType(BasicGameTypes.MOVINGENEMY)
                        || e.isType(BasicGameTypes.ELITEMOVINGENEMY));
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
