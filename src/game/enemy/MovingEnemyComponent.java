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


public class MovingEnemyComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    private PhysicsComponent physics;
    private boolean alerted = false;
    private boolean nearbyPitL;
    private boolean nearbyPitR;
    private boolean movingLeft;
    private boolean movingRight;

    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();

        physics.addSensor(new HitBox(new Point2D(-100, entity.getHeight() + 5), BoundingShape.box(50, 20)), new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
                nearbyPitL = false;
            }

            @Override
            protected void onCollisionEnd(Entity other) {
                nearbyPitL = true;
                stop();
            }
        });

        physics.addSensor(new HitBox(new Point2D(entity.getWidth()+100, entity.getHeight() + 5), BoundingShape.box(32, 20)), new SensorCollisionHandler() {
            @Override
            protected void onCollisionBegin(Entity other) {
                nearbyPitR = false;
            }

            @Override
            protected void onCollisionEnd(Entity other) {
                nearbyPitR = true;
                stop();
            }
        });
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (alerted) {
                if (player.getBoundingBoxComponent().getCenterWorld().getX() - entity.getBoundingBoxComponent().getCenterWorld().getX() < 0) {
                    if (checkLineOfSight(true)) {
                        basicEnemyAttack(player);
                        enemyAttackInterval.capture();
                        if (!nearbyPitL)
                            moveLeft();
                    }
                }
                else {
                    if (checkLineOfSight(false)) {
                        basicEnemyAttack(player);
                        enemyAttackInterval.capture();
                        if (!nearbyPitR)
                            moveRight();
                    }
                }
            }
        }

        if (stopCheckLeft() && movingLeft)
            stop();
        if (stopCheckRight() && movingRight)
            stop();

        alerted = distanceToPlayer(player) < entity.getInt("alertRange");
    }

    public double distanceToPlayer(Entity player) {
        return player.getPosition().distance(getEntity().getPosition());
    }

    public boolean checkLineOfSight(boolean fromLeftSide) {
        if (fromLeftSide) {
            double minX = entity.getPosition().getX() - entity.getInt("alertRange");
            return checkForObstacles(minX, true);
        }
        else {
            double minX = entity.getPosition().getX() + entity.getWidth();
            return checkForObstacles(minX, false);
        }
    }

    private boolean checkForObstacles(double minX, boolean fromLeftSide) {
        double minY = entity.getPosition().getY();
        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, entity.getInt("alertRange"), 80));
        double playerPos = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getBoundingBoxComponent().getCenterWorld().getX();
        if (fromLeftSide) {
            return list
                    .stream()
                    .filter(e -> e.getBoundingBoxComponent().getCenterWorld().getX() - playerPos > 0)
                    .noneMatch(e -> e.hasComponent(SideDoorComponent.class) && !e.getComponent(SideDoorComponent.class).isOpened() || e.isType(BasicGameTypes.WALL));
        }
        return list
                .stream()
                .filter(e -> playerPos - e.getBoundingBoxComponent().getCenterWorld().getX() > 0)
                .noneMatch(e -> e.hasComponent(SideDoorComponent.class) && !e.getComponent(SideDoorComponent.class).isOpened() || e.isType(BasicGameTypes.WALL));
    }

    public boolean stopCheckLeft() {
        double minX = entity.getX() - 10;
        double minY = entity.getPosition().getY();
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
        double minY = entity.getPosition().getY();
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
        movingLeft = false;
        movingRight = false;
    }

    public void moveLeft() {
        physics.setVelocityX(-100);
        movingLeft = true;
    }

    public void moveRight() {
        physics.setVelocityX(100);
        movingRight = true;
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        if (entity.isType(BasicGameTypes.MOVINGENEMY)) {
            FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
        }
        else FXGL.getGameWorld().spawn("eliteEnemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
    }

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            entity.removeFromWorld();
        }
    }

    public void initHP() {
        hp.setValue(hp.getMaxHP());
    }
}
