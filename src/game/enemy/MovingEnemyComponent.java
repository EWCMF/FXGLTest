package game.enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import game.BasicGameTypes;
import game.characters.HPComponent;
import game.level.SideDoorComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


public class MovingEnemyComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    private PhysicsComponent physics;
    boolean alerted = false;
    private ArrayList<Entity> OIS = new ArrayList<>();


    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {

        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (alerted && checkLineOfSight()) {
                moveLeft();
                basicEnemyAttack(player);
                enemyAttackInterval.capture();
            }
        }

        alerted = distanceToPlayer(player) < entity.getInt("alertRange");
    }

    public double distanceToPlayer(Entity player) {
        return player.getPosition().distance(getEntity().getPosition());
    }

    public boolean checkLineOfSight() {
        double minX = entity.getPosition().getX() - 500;
        double minY = entity.getPosition().getY();
        List<Entity> list = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 500, 80));
        for (Entity value : list) {
            if (value.hasComponent(SideDoorComponent.class))
                if (!value.getComponent(SideDoorComponent.class).isOpened())
                    return false;
            if (value.isType(BasicGameTypes.WALL))
                return false;
        }
        return true;
    }

    public void moveLeft() {
        if (physics.getVelocityX() == 0)
        physics.setVelocityX(-100);
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget =  player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
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
