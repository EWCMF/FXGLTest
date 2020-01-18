package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import game.BasicGameTypes;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

@Required(HPComponent.class)
public class MovingEnemyComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    private PhysicsComponent physics;
    private boolean nearbyPitL = false;
    private boolean nearbyPitR = false;
    private boolean movingLeft;
    private boolean movingRight;

    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(entity.getWidth() / 2, entity.getHeight() / 2));
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (player.getBoundingBoxComponent().getMaxXWorld() - entity.getBoundingBoxComponent().getMaxXWorld() < 0) {
                if (checkLineOfSight()) {
                    if (!nearbyPitL)
                        moveLeft();
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                    entity.setScaleX(-1);
                }
                else
                    completeStop();
            } else {
                if (checkLineOfSight()) {
                    if (!nearbyPitR)
                        moveRight();
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                    entity.setScaleX(1);
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
                        || e.isType(BasicGameTypes.WALL) && e.getWidth() < 64
                        && e.getY() > entity.getY()).collect(Collectors.toList());

        List<Entity> findFloors = FXGL.getGameWorld().getEntitiesInRange(selection).stream()
                .filter(e -> e.isType(BasicGameTypes.WALL) && e.getWidth() > 64).collect(Collectors.toList());

        if (!findWalls.isEmpty() || !findFloors.isEmpty()) {
            for (Entity findWall : findWalls) {
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
        physics.setVelocityX(0);
    }

    public void completeStop() {
        physics.setVelocityX(0);
        movingLeft = false;
        movingRight = false;
    }

    public void moveLeft() {
        physics.setVelocityX(-100);
        movingLeft = true;
        movingRight = false;
    }

    public void moveRight() {
        physics.setVelocityX(100);
        movingRight = true;
        movingLeft = false;
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        if (entity.isType(BasicGameTypes.MOVINGENEMY)) {
            FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
        } else
            FXGL.getGameWorld().spawn("eliteEnemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
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
