package game;

import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
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
                .viewWithBBox(new Rectangle(12, 3, Color.RED))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 300))
                .with(new OffscreenCleanComponent())
                .with("damage", 1 * BasicGameApp.enemyDamageModifier)
                .build();
    }

    @Spawns("start")
    public Entity newStart(SpawnData data) {
        return entityBuilder()
                .type(START)
                .from(data)
                .build();
    }

    @Spawns("exit")
    public Entity newExit(SpawnData data) {
        return entityBuilder()
                .type(EXIT)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .from(data)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.DYNAMIC);
        physicsComponent.addGroundSensor(new HitBox(new Point2D(39, 84), BoundingShape.box(5, 10)));
        physicsComponent.setFixtureDef(new FixtureDef().friction(5));

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
                .with(new IrremovableComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.STATIC);
        physicsComponent.setFixtureDef(new FixtureDef().friction(0));

        return entityBuilder()
                .type(WALL)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("passablePlatform")
    public Entity newPassablePlatform(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.STATIC);
        physicsComponent.setFixtureDef(new FixtureDef().friction(0));

        return entityBuilder()
                .type(PASSABLE)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("movingPlatform")
    public Entity newMovingPlatform(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.KINEMATIC);
        physicsComponent.setOnPhysicsInitialized(() -> {
            Integer typeCast = data.get("startDir");
            physicsComponent.setVelocityX(typeCast.doubleValue());
        });

        physicsComponent.setFixtureDef(new FixtureDef().friction(5));

        return entityBuilder()
                .type(MOVING)
                .from(data)
                .view("testPlatform.png")
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .with(new MovingPlatformComponent())
                .build();
    }

    @Spawns("movingPlatformStop")
    public Entity newMovingPlatformStop(SpawnData data) {
        return entityBuilder()
                .type(MOVINGSTOP)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("turret")
    public Entity newTurret(SpawnData data) {
        return entityBuilder()
                .type(ENEMY)
                .from(data)
                .viewWithBBox(new Circle(16, 16, 15, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new HPComponent(6))
                .with(new EnemyComponent())
                .with(new FlickerComponent())
                .with("alertRange", 1000)
                .build();
    }

    @Spawns("eliteTurret")
    public Entity eliteTurret(SpawnData data) {
        return entityBuilder()
                .type(ELITEENEMY)
                .from(data)
                .viewWithBBox(new Circle(16, 16, 15, Color.DARKRED))
                .with(new CollidableComponent(true))
                .with(new HPComponent(30))
                .with(new EliteEnemyComponent())
                .with(new FlickerComponent())
                .with("alertRange", 1000)
                .build();
    }

    @Spawns("eliteEnemyBullet")
    public Entity newEliteEnemyBullet(SpawnData data) {
        return entityBuilder()
                .type(ENEMYBULLET)
                .from(data)
                .viewWithBBox(new Rectangle(20, 3, Color.DARKVIOLET))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 700))
                .with(new OffscreenCleanComponent())
                .with("damage", 3 * BasicGameApp.enemyDamageModifier)
                .build();
    }
}
