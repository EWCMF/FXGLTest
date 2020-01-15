package game.enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.SensorCollisionHandler;
import com.almasb.fxgl.time.LocalTimer;
import game.BasicGameTypes;
import game.characters.HPComponent;
import game.level.SideDoorComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;


public class MovingEnemyComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    private PhysicsComponent physics;
    private boolean alerted = false;
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
            } else {
                if (checkLineOfSight()) {
                    if (!nearbyPitR)
                        moveRight();
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                    entity.setScaleX(1);
                }
            }
        }

        Rectangle2D checkPitLSelection = new Rectangle2D(entity.getX() - 20, entity.getY() + entity.getHeight(), 10, 10);
        List<Entity> checkPitL = FXGL.getGameWorld().getEntitiesInRange(checkPitLSelection);
        nearbyPitL = checkPitL.isEmpty();
        if (nearbyPitL && !movingRight)
            stop();

        Rectangle2D checkPitRSelection = new Rectangle2D(entity.getX() + entity.getWidth() + 10, entity.getY() + entity.getHeight(), 10, 10);
        List<Entity> checkPitR = FXGL.getGameWorld().getEntitiesInRange(checkPitRSelection);
        nearbyPitR = checkPitR.isEmpty();
        if (nearbyPitR && !movingLeft)
            stop();

//        alerted = distanceToPlayer(player) < entity.getInt("alertRange");
    }

//    public double distanceToPlayer(Entity player) {
//        return player.getPosition().distance(getEntity().getPosition());
//    }

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

//    private boolean checkForObstacles(double minX, boolean fromLeftSide) {
//        double minY = entity.getPosition().getY();
//        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, entity.getInt("alertRange"), 80));
//        double playerPos = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getBoundingBoxComponent().getCenterWorld().getX();
//        if (fromLeftSide) {
//            return list
//                    .stream()
//                    .filter(e -> e.getBoundingBoxComponent().getCenterWorld().getX() - playerPos > 0)
//                    .noneMatch(e -> e.hasComponent(SideDoorComponent.class) && !e.getComponent(SideDoorComponent.class).isOpened() || e.isType(BasicGameTypes.WALL));
//        }
//        return list
//                .stream()
//                .filter(e -> playerPos - e.getBoundingBoxComponent().getCenterWorld().getX() > 0)
//                .noneMatch(e -> e.hasComponent(SideDoorComponent.class) && !e.getComponent(SideDoorComponent.class).isOpened() || e.isType(BasicGameTypes.WALL));
//    }

//    public boolean stopCheckLeft() {
//        double minX = entity.getX() - 10;
//        double minY = entity.getPosition().getY();
//        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 10, 80));
//        return list
//                .stream()
//                .anyMatch(e -> e.hasComponent(SideDoorComponent.class)
//                        && !e.getComponent(SideDoorComponent.class).isOpened()
//                        || e.isType(BasicGameTypes.WALL)
//                        || e.isType(BasicGameTypes.MOVINGENEMY)
//                        || e.isType(BasicGameTypes.ELITEMOVINGENEMY));
//    }
//
//    public boolean stopCheckRight() {
//        double minX = entity.getX() + entity.getWidth();
//        double minY = entity.getPosition().getY();
//        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 20, 80));
//        return list
//                .stream()
//                .anyMatch(e -> e.hasComponent(SideDoorComponent.class)
//                        && !e.getComponent(SideDoorComponent.class).isOpened()
//                        || e.isType(BasicGameTypes.WALL)
//                        || e.isType(BasicGameTypes.MOVINGENEMY)
//                        || e.isType(BasicGameTypes.ELITEMOVINGENEMY));
//    }

    public void stop() {
        physics.setVelocityX(0);
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
