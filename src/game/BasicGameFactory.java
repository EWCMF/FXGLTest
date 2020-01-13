package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import game.characters.FlickerComponent;
import game.enemy.EliteEnemyComponent;
import game.enemy.EnemyComponent;
import game.enemy.MovingEnemyComponent;
import game.level.*;
import game.characters.HPComponent;
import game.player.PlayerComponent;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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
                .view("startDoor.png")
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
                .with(new ExitDoorComponent())
                .with("startOpened", data.get("startOpened"))
                .build();
    }

    @Spawns("exitSwitch")
    public Entity newExitSwitch(SpawnData data) {
        return entityBuilder()
                .type(EXITSWITCH)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .from(data)
                .with(new ExitSwitchComponent())
                .build();
    }

    @Spawns("sideDoor")
    public Entity newSideDoor(SpawnData data) {
        return entityBuilder()
                .type(SIDEDOOR)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .from(data)
                .with(new PhysicsComponent())
                .with(new SideDoorComponent())
                .with("startOpened", data.get("startOpened"))
                .with("openType", data.get("openType"))
                .build();
    }

    @Spawns("sideDoorTrigger")
    public Entity newSideDoorTrigger(SpawnData data) {
        return entityBuilder()
                .type(SIDEDOORTRIGGER)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
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

    @Spawns("breakWall")
    public Entity newBreakWall(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.STATIC);
        physicsComponent.setFixtureDef(new FixtureDef().friction(0));

        return entityBuilder()
                .type(BREAKABLEWALL)
                .from(data)
                .view("breakableWall.png")
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physicsComponent)
                .collidable()
                .with(new HPComponent(20))
                .with(new FlickerComponent())
                .with(new BreakableWallComponent())
                .build();
    }

    @Spawns("hidden")
    public Entity newHidden(SpawnData data) {
        return entityBuilder()
                .type(HIDDEN)
                .from(data)
                .viewWithBBox(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.color(0.2, 0.2, 0.2)))
                .collidable()
                .build();
    }

    @Spawns("passablePlatform")
    public Entity newPassablePlatform(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.STATIC);
        physicsComponent.setFixtureDef(new FixtureDef().friction(0));
        physicsComponent.setOnPhysicsInitialized(() -> {
            physicsComponent.getBody().setActive(false);
        });
        
        return entityBuilder()
                .type(PASSABLE)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), 1)))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("passablePlatformTrigger")
    public Entity newPassablePlatformTrigger(SpawnData data) {
        return entityBuilder()
                .type(PASSABLETRIGGER)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("movingPlatform")
    public Entity newMovingPlatform(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.KINEMATIC);
        physicsComponent.setOnPhysicsInitialized(() -> {
            Integer typeCast = data.get("startDirX");
            Integer typeCast2 = data.get("startDirY");
            physicsComponent.setLinearVelocity(typeCast.doubleValue(), typeCast2.doubleValue());
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
                .with("stopTime", data.get("stopTime"))
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

    @Spawns("movingEnemy")
    public Entity newMovingEnemy(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.DYNAMIC);

        return entityBuilder()
                .type(MOVINGENEMY)
                .from(data)
                .viewWithBBox(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.BLUE))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .with(new HPComponent(12))
                .with(new MovingEnemyComponent())
                .with(new FlickerComponent())
                .with("alertRange", 500)
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

    @Spawns("ammoBox")
    public Entity newAmmoBox(SpawnData data) {
        return entityBuilder()
                .type(AMMOBOX)
                .from(data)
                .view("ammoBox.png")
                .bbox(new HitBox(new Point2D(0, 27), BoundingShape.box(64, 37)))
                .with(new CollidableComponent(true))
                .with("amount", data.get("amount"))
                .build();
    }

    @Spawns("keycard")
    public Entity newKeycard(SpawnData data) {
        return entityBuilder()
                .type(KEYCARD)
                .from(data)
                .viewWithBBox("keycard.png")
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("teleporter")
    public Entity newTeleporter(SpawnData data) {
        return entityBuilder()
                .type(TELEPORTER)
                .from(data)
                .view(texture("teleporter.png").toAnimatedTexture(3, Duration.seconds(0.8)).loop())
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new TeleportComponent())
                .build();
    }

    @Spawns("oneWayTeleport")
    public Entity newOWTeleporter(SpawnData data) {
        return entityBuilder()
                .type(ONEWAYTELEPORT)
                .from(data)
                .view(texture("teleporter.png").toAnimatedTexture(3, Duration.seconds(0.8)).loop())
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with("connected", data.get("connected"))
                .build();
    }

    @Spawns("oneWayTeleportDropOff")
    public Entity newOWTDropOff(SpawnData data) {
        return entityBuilder()
                .type(OWTDROPOFF)
                .from(data)
                .with("connected", data.get("connected"))
                .build();
    }

    @Spawns("teleportEffect")
    public Entity newTeleportEffect(SpawnData data) {
        play("teleport.wav");

        var emitter = ParticleEmitters.newExplosionEmitter(100);
        emitter.setMaxEmissions(1);
        emitter.setColor(Color.BLUE);

        return entityBuilder()
                .from(data)
                .view(texture("teleportEffect.png").toAnimatedTexture(2, Duration.seconds(0.33)).loop())
                .with(new ExpireCleanComponent(Duration.seconds(0.66)))
                .with(new ParticleComponent(emitter))
                .build();
    }
}
