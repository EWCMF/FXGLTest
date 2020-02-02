package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import game.RunAndGunFXGL;
import game.RunAndGunFXGLTypes;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerExplosionComponent extends Component {
    @Override
    public void onAdded() {
        double minX = entity.getPosition().getX();
        double minY = entity.getPosition().getY();
        int damage = entity.getProperties().getInt("damage");
        Rectangle2D range = new Rectangle2D(minX, minY, 200, 200);
        Entity posAtFiring = FXGL.getGameWorld().getSingleton(RunAndGunFXGLTypes.ROCKETDUMMY);
        Entity player = FXGL.getGameWorld().getSingleton(RunAndGunFXGLTypes.PLAYER);

        List<Entity> findWalls = FXGL.getGameWorld().getEntitiesInRange(range).stream()
                .filter(e -> e.hasComponent(SideDoorComponent.class)
                        && !e.getComponent(SideDoorComponent.class).isOpened()
                        || e.isType(RunAndGunFXGLTypes.WALL) && e.getWidth() <= 64).collect(Collectors.toList());

        List<Entity> findFloors = FXGL.getGameWorld().getEntitiesInRange(range).stream()
                .filter(e -> e.isType(RunAndGunFXGLTypes.WALL) && e.getWidth() > 64).collect(Collectors.toList());

        List<Entity> affected = FXGL.getGameWorld().getEntitiesInRange(range).stream()
                .filter(e -> e.hasComponent(PlayerComponent.class)
                        || e.hasComponent(MovingEnemyComponent.class)
                        || e.hasComponent(MovingEnemyComponentElite.class)
                        || e.hasComponent(TurretComponent.class)
                        || e.hasComponent(BreakableWallComponent.class)
                        || e.hasComponent(BaronOfHellComponent.class))
                .collect(Collectors.toList());

        // Check to remove affected on the other side of walls. Don't look at it too much.
        if (!affected.isEmpty() || !findWalls.isEmpty()) {
            for (int i = 0; i < affected.size(); i++) {
                if (!affected.get(i).isType(player)) {
                    for (Entity findWall : findWalls) {
                        if (affected.get(i).getX() > findWall.getX() && findWall.getX() > posAtFiring.getX()) {
                            for (Entity findFloor : findFloors) {
                                if (affected.get(i).getY() > findFloor.getY()) {
                                    affected.remove(i);
                                    break;
                                }
                            }
                            break;
                        }
                        if (affected.get(i).getX() < findWall.getX() && findWall.getX() < posAtFiring.getX()) {
                            for (Entity findFloor : findFloors) {
                                if (affected.get(i).getY() > findFloor.getY()) {
                                    affected.remove(i);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    for (Entity findFloor : findFloors) {
                        if (affected.get(i).getY() < findFloor.getY() && findFloor.getY() < posAtFiring.getY()) {
                            affected.remove(i);
                            break;
                        }
                        if (affected.get(i).getY() > findFloor.getY() && findFloor.getY() > posAtFiring.getY()) {
                            affected.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        FXGL.despawnWithScale(posAtFiring);
        for (Entity value : affected) {
            if (value.hasComponent(PlayerComponent.class)) {
                value.getComponent(FlickerComponent.class).flicker();
                if (value.getScaleX() == -1) {
                    value.getComponent(PlayerComponent.class).onHit(3 * RunAndGunFXGL.enemyDamageModifier, new Point2D(1, 0));
                } else {
                    value.getComponent(PlayerComponent.class).onHit(3 * RunAndGunFXGL.enemyDamageModifier, new Point2D(-1, 0));
                }
            }
            if (value.hasComponent(MovingEnemyComponent.class)) {
                value.getComponent(MovingEnemyComponent.class).onHit(damage);
                value.getComponent(FlickerComponent.class).flicker();
                if (value.getScaleX() == -1)
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(200, -200);
                else
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(-200, -200);
            }
            if (value.hasComponent(MovingEnemyComponentElite.class)) {
                value.getComponent(MovingEnemyComponentElite.class).onHit(damage);
                value.getComponent(FlickerComponent.class).flicker();
                if (value.getScaleX() == -1)
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(200, -200);
                else
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(-200, -200);
            }
            if (value.hasComponent(TurretComponent.class)) {
                value.getComponent(TurretComponent.class).onHit(damage);
                value.getComponent(FlickerComponent.class).flicker();
            }
            if (value.hasComponent(BreakableWallComponent.class)) {
                value.getComponent(BreakableWallComponent.class).onHit(damage);
            }
            if (value.hasComponent(BaronOfHellComponent.class)) {
                value.getComponent(BaronOfHellComponent.class).onHit(30);
            }
        }
    }
}
