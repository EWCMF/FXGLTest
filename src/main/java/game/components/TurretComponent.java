package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.time.LocalTimer;
import game.RunAndGunFXGLTypes;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;


@Required(HPComponent.class)
public class TurretComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    boolean alertedRight = false;
    boolean alertedLeft = false;

    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(RunAndGunFXGLTypes.PLAYER);
        int alertRange = entity.getInt("alertRange");

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (alertedRight || alertedLeft) {
                if (checkLineOfSight()) {
                    basicEnemyAttack(player);
                    enemyAttackInterval.capture();
                }
            }
        }

        if (distanceToPlayerX(player) > -alertRange && distanceToPlayerX(player) < 0) {
            if (distanceToPlayerY(player) > 0 && distanceToPlayerY(player) < alertRange / 1.5
                || distanceToPlayerY(player) < 0 && distanceToPlayerY(player) > -alertRange / 1.5) {
                alertedLeft = true;
            }
            else alertedLeft = false;
        }
        else alertedLeft = false;

        if (distanceToPlayerX(player) < alertRange && distanceToPlayerX(player) > 0) {
            if (distanceToPlayerY(player) > 0 && distanceToPlayerY(player) < alertRange / 1.5
                || distanceToPlayerY(player) < 0 && distanceToPlayerY(player) > -alertRange / 1.5){
                alertedRight = true;
            }
            else alertedRight = false;
        }
        else alertedRight = false;
    }

    public double distanceToPlayerX(Entity player) {
        return player.getPosition().getX() - entity.getPosition().getX();
    }

    public double distanceToPlayerY(Entity player) {
        return player.getPosition().getY() - entity.getPosition().getY();
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
                .filter(e -> e.isType(RunAndGunFXGLTypes.WALL) && e.getWidth() >= 64).collect(Collectors.toList());

        if (player.getPosition().getX() - entity.getPosition().getX() < 0) {
            List<Entity> leftWalls = findWalls.stream().filter(e -> e.getPosition().getX() < entity.getPosition().getX()).collect(Collectors.toList());
            List<Entity> leftFloors = findFloors.stream().filter(e -> e.getX() < entity.getX() && e.getX() > player.getX()).collect(Collectors.toList());
            if (player.getPosition().getY() - entity.getPosition().getY() < 0) {
                List<Entity> upFloors = leftFloors.stream().filter(e -> e.getPosition().getY() < entity.getPosition().getY()).collect(Collectors.toList());

                if (!leftWalls.isEmpty() || !upFloors.isEmpty()) {
                    List<Entity> remainingWalls = leftWalls.stream().filter(e -> e.getY() < entity.getY()).collect(Collectors.toList());
                    for (Entity remainingWall : remainingWalls) {
                        if (entity.getX() > remainingWall.getX() && remainingWall.getX() > player.getX() && player.getY() > remainingWall.getY()) {
//                            System.out.println("LU P W T");
                            return false;
                        }
                    }
                    for (Entity upFloor : upFloors) {
                        if (entity.getY() > upFloor.getY() && upFloor.getY() > player.getY()) {
//                            System.out.println("LU P F T");
                            return false;
                        }
                    }
                }
            }
            else {
                List<Entity> downFloors = leftFloors.stream().filter(e -> e.getPosition().getY() > entity.getPosition().getY()).collect(Collectors.toList());

                if (!leftWalls.isEmpty() || !downFloors.isEmpty()) {
                    List<Entity> remainingWalls = leftWalls.stream().filter(e -> e.getBottomY() > entity.getY()).collect(Collectors.toList());
                    for (Entity remainingWall : remainingWalls) {
                        if (entity.getX() > remainingWall.getX() && remainingWall.getX() > player.getX() && player.getY() > remainingWall.getY()) {
//                            System.out.println("LD P W T");
//                            System.out.println(remainingWall.toString());
                            return false;
                        }
                    }
                    for (Entity downFloor : downFloors) {
                        if (entity.getY() < downFloor.getY() && downFloor.getY() < player.getY()) {
//                            System.out.println("LD P F T");
//                            System.out.println(downFloor.toString());
                            return false;
                        }
                    }
                }
            }
        }
        else {
            List<Entity> rightWalls = findWalls.stream().filter(e -> e.getPosition().getX() > entity.getPosition().getX()).collect(Collectors.toList());
            List<Entity> rightFloors = findFloors.stream().filter(e -> e.getX() > entity.getX() && e.getX() < player.getX()).collect(Collectors.toList());
            if (player.getPosition().getY() - entity.getPosition().getY() < 0) {
                List<Entity> upFloors = rightFloors.stream().filter(e -> e.getPosition().getY() < entity.getPosition().getY()).collect(Collectors.toList());

                if (!rightWalls.isEmpty() || !upFloors.isEmpty()) {
                    List<Entity> remainingWalls = rightWalls.stream().filter(e -> e.getY() < entity.getY()).collect(Collectors.toList());
                    for (Entity remainingWall : remainingWalls) {
                        if (entity.getX() > remainingWall.getX() && remainingWall.getX() > player.getX() && player.getY() > remainingWall.getY()) {
//                            System.out.println("RU P W T");
                            return false;
                        }
                    }
                    for (Entity upFloor : upFloors) {
                        if (entity.getY() > upFloor.getY() && upFloor.getY() > player.getY()) {
//                            System.out.println("RU P F T");
                            return false;
                        }
                    }
                }
            }
            else {
                List<Entity> downFloors = rightFloors.stream().filter(e -> e.getPosition().getY() > entity.getPosition().getY()).collect(Collectors.toList());

                if (!rightWalls.isEmpty() || !downFloors.isEmpty()) {
                    List<Entity> remainingWalls = rightWalls.stream().filter(e -> e.getBottomY() > entity.getY()).collect(Collectors.toList());
                    for (Entity remainingWall : remainingWalls) {
                        if (entity.getX() > remainingWall.getX() && remainingWall.getX() > player.getX() && player.getY() > remainingWall.getY()) {
//                            System.out.println("RD P W T");
                            return false;
                        }
                    }
                    for (Entity downFloor : downFloors) {
                        if (entity.getY() < downFloor.getY() && downFloor.getY() < player.getY()) {
//                            System.out.println("RD P F T");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget =  player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        if (entity.isType(RunAndGunFXGLTypes.TURRET))
            FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
        else
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
