package game;

import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;
import static game.BasicGameTypes.*;

public class BasicGameFactory implements EntityFactory {

    @Spawns("defaultBullet")
    public Entity newDefault(SpawnData data) {
        return entityBuilder()
                .type(BULLET)
                .at((Point2D) data.get("position"))
                .viewWithBBox(new Rectangle(12,3, Color.GOLD))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 500))
                .with(new OffscreenCleanComponent())
                .with("damage", 3)
                .build();
    }

    @Spawns("shotgunPellet")
    public Entity newPellet(SpawnData data) {
        return entityBuilder()
                .type(BULLET)
                .at((Point2D) data.get("position"))
                .viewWithBBox(new Rectangle(10,3, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 850))
                .with(new OffscreenCleanComponent())
                .with("damage", 2)
                .build();
    }

    @Spawns("machineGunBullet")
    public Entity newMachineGun(SpawnData data) {
        return entityBuilder()
                .type(BULLET)
                .at((Point2D) data.get("position"))
                .viewWithBBox(new Rectangle(14,3, Color.ORANGERED))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 650))
                .with(new OffscreenCleanComponent())
                .with("damage", 2)
                .build();
    }

    @Spawns("enemyBullet")
    public Entity newEnemyBullet(SpawnData data) {
        return entityBuilder()
                .type(ENEMYBULLET)
                .from(data)
                .viewWithBBox(new Rectangle(12, 3, Color.DARKRED))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 300))
                .with(new OffscreenCleanComponent())
                .with("damage", 1)
                .build();
    }

    @Spawns("start")
    public Entity newStart(SpawnData data) {
        return entityBuilder()
                .type(START)
                .from(data)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.DYNAMIC);
        physicsComponent.addGroundSensor(new HitBox(BoundingShape.box(42, 94)));
        physicsComponent.setFixtureDef(new FixtureDef().friction(0.0f));

        Point2D hitboxOffset = new Point2D(7, 14);

        return entityBuilder()
                .type(PLAYER)
                .from(data)
                //.view(new Rectangle(25, 25, Color.RED))
                .bbox(new HitBox(hitboxOffset, BoundingShape.box(42, 80)))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .with(new HPComponent(12))
                .with(new PlayerComponent())
                .with(new FlickerComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        return entityBuilder()
                .type(WALL)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("target")
    public Entity newTarget(SpawnData data) {
        return entityBuilder()
                .type(TARGET)
                .from(data)
                .viewWithBBox(new Circle(16, 16, 15, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new HPComponent(6))
                .with(new EnemyComponent())
                .with(new FlickerComponent())
                .with("alertRange", 600)
                .build();
    }
}
