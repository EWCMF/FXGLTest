package game;

import com.almasb.fxgl.app.*;
import com.almasb.fxgl.core.collection.PropertyMap;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import game.characters.FlickerComponent;
import game.enemy.EliteEnemyComponent;
import game.enemy.EnemyComponent;
import game.enemy.MovingEnemyComponent;
import game.level.*;
import game.characters.HPComponent;
import game.player.PlayerComponent;
import game.ui.BasicGameMenu;
import game.ui.HPIndicator;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Map;

import static game.BasicGameTypes.*;
import static com.almasb.fxgl.dsl.FXGL.*;


public class BasicGameApp extends GameApplication {
    public static double aimUpVectorY = -150;
    public static double aimDownVectorY = 150;

    public static int hpInX = 15;
    public static int hpInY = 35;

    private Point2D gunRightUp = new Point2D(46, 13);
    private Point2D gunRightMiddle = new Point2D(52, 35);
    private Point2D gunRightDown = new Point2D(45, 70);

    private Point2D gunLeftUp = new Point2D(6, 13);
    private Point2D gunLeftMiddle = new Point2D(0, 35);
    private Point2D gunLeftDown = new Point2D(7, 70);

    public static int ammoShotgun = 20;
    public static int ammoMachineGun = 200;

    public static int enemyDamageModifier = 0;

    private String startLevel = "test2.tmx";
    private int startBoundX = 32 * 100;
    private int startBoundY = 32 * 70;

    private boolean allowPass = false;
    private boolean onSwitch = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Contra Knockoff");
        settings.setVersion("0.3");
        settings.setMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new BasicGameMenu();
            }
        });
        //settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
                player.getComponent(PlayerComponent.class).setHoldingMoveDirection(true);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
                player.getComponent(PlayerComponent.class).setHoldingMoveDirection(false);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
                player.getComponent(PlayerComponent.class).setHoldingMoveDirection(true);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
                player.getComponent(PlayerComponent.class).setHoldingMoveDirection(false);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Prone/Through platform") {
            @Override
            protected void onActionBegin() {
                allowPass = true;
            }

            @Override
            protected void onActionEnd() {
                allowPass = false;
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).jump();
            }
        }, KeyCode.SPACE);

        input.addAction(new UserAction("Press switch") {
            @Override
            protected void onActionBegin() {
                if (onSwitch)
                    FXGL.getGameWorld().getEntitiesByType(EXITSWITCH).get(0).getComponent(ExitSwitchComponent.class).activate();
            }
        }, KeyCode.F);

        input.addAction(new UserAction("Test something") {
            @Override
            protected void onActionBegin() {
                System.out.println(getGameWorld().getEntitiesInRange(new Rectangle2D(player.getX()-1, player.getY(), player.getWidth()+2, player.getHeight())).stream()
                .filter(entity -> entity.isType(WALL))
                .findAny()
                .get().getPosition());
            }
        }, KeyCode.C);

        input.addAction(new UserAction("Change weapon") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).changeWeapon();
            }
        }, KeyCode.E);

        input.addAction(new UserAction("Change weapon back") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).changeWeaponReverse();
            }
        }, KeyCode.Q);

        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onAction() {
                Point2D vector = FXGL.getInput().getVectorToMouse(player.getPosition());
                if (vector.getX() < 0 && player.getTransformComponent().getScaleX() != -1.0)
                    return;
                if (vector.getX() > 0 && player.getTransformComponent().getScaleX() != 1.0)
                    return;

                double mouseY = FXGL.getInput().getVectorToMouse(player.getPosition()).getY();

                // Facing left.
                if (player.getTransformComponent().getScaleX() == -1.0) {
                    // Middle position, Up position, down position.
                    FiringPosition(mouseY, gunLeftMiddle, gunLeftUp, gunLeftDown);
                }
                // Facing right.
                else {
                    // Middle position, Up position, down position.
                    FiringPosition(mouseY, gunRightMiddle, gunRightUp, gunRightDown);
                }
            }
        }, MouseButton.PRIMARY);
    }

    private void FiringPosition(double mouseY, Point2D gunMiddle, Point2D gunUp, Point2D gunDown) {
        if (mouseY > aimUpVectorY && mouseY < aimDownVectorY) {
            Point2D gunOffset = player.getPosition().add(gunMiddle);
            Point2D mouseVector = FXGL.getInput().getVectorToMouse(gunOffset).normalize();
            player.getComponent(PlayerComponent.class).fire(mouseVector, gunOffset);
        }
        else if (mouseY < aimUpVectorY) {
            Point2D gunOffset = player.getPosition().add(gunUp);
            Point2D mouseVector = FXGL.getInput().getVectorToMouse(gunOffset).normalize();
            player.getComponent(PlayerComponent.class).fire(mouseVector, gunOffset);
        }
        else if (mouseY > aimDownVectorY) {
            Point2D gunOffset = player.getPosition().add(gunDown);
            Point2D mouseVector = FXGL.getInput().getVectorToMouse(gunOffset).normalize();
            player.getComponent(PlayerComponent.class).fire(mouseVector, gunOffset);
        }
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("level", startLevel);
        vars.put("currentBoundX", startBoundX);
        vars.put("currentBoundY", startBoundY);

        vars.put("ammoShotgun", ammoShotgun);
        vars.put("ammoMachineGun", ammoMachineGun);
        vars.put("weaponIndicatorPosition", 13);

        vars.put("hasKeycard", false);
    }

    public Entity player;

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BasicGameFactory());

        player = null;
        setLevel(gets("level"));
        getGameScene().setBackgroundColor(Color.color(0.2, 0.2, 0.2));

        Point2D start = FXGL.getGameWorld().getSingleton(START).getPosition();
        player = FXGL.getGameWorld().spawn("player", start);

        initHP();

        Viewport viewport = FXGL.getGameScene().getViewport();

        viewport.setBounds(32, 0, startBoundX, startBoundY);
        viewport.bindToEntity(player, FXGL.getAppWidth() / 4, FXGL.getAppHeight() / 1.5);
    }

    @Override
    protected void initUI() {
        var hp = new HPIndicator(player.getComponent(HPComponent.class));

        var d = getUIFactory().newText("D", Color.WHITE, 20);
        d.setStroke(Color.BLACK);
        var s = getUIFactory().newText("S", Color.WHITE, 20);
        s.setStroke(Color.BLACK);
        var m = getUIFactory().newText("M", Color.WHITE, 20);
        m.setStroke(Color.BLACK);

        var ammoShotgun = getUIFactory().newText("", Color.WHITE, 15);
        ammoShotgun.textProperty().bind(getip("ammoShotgun").asString());
        var ammoMachineGun = getUIFactory().newText("", Color.WHITE, 15);
        ammoMachineGun.textProperty().bind(getip("ammoMachineGun").asString());

        var weaponIndicator = new Rectangle(13, 32, 20, 22);
        weaponIndicator.xProperty().bind(getip("weaponIndicatorPosition"));
        weaponIndicator.setStroke(Color.BLUE);
        weaponIndicator.setFill(null);

        addUINode(hp, hpInX, hpInY);

        addUINode(d, 15, 50);
        addUINode(s, 55, 50);
        addUINode(ammoShotgun, 55, 65);
        addUINode(m, 95, 50);
        addUINode(ammoMachineGun, 95, 65);
        addUINode(weaponIndicator);

    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 760);
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, ENEMY) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                bullet.removeFromWorld();
                enemy.getComponent(EnemyComponent.class).onHit(bullet.getInt("damage"));
                enemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, ELITEENEMY) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bullet, Entity eliteEnemy) {
                bullet.removeFromWorld();
                eliteEnemy.getComponent(EliteEnemyComponent.class).onHit(bullet.getInt("damage"));
                eliteEnemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, MOVINGENEMY) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bullet, Entity movingEnemy) {
                bullet.removeFromWorld();
                movingEnemy.getComponent(MovingEnemyComponent.class).onHit(bullet.getInt("damage"));
                movingEnemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, WALL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity wall) {
                bullet.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, WALL) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity wall) {
                enemyBullet.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, SIDEDOOR) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity door) {
                if (!door.getComponent(SideDoorComponent.class).isOpened())
                    bullet.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, BREAKABLEWALL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity wall) {
                bullet.removeFromWorld();
                wall.getComponent(BreakableWallComponent.class).onHit(bullet.getInt("damage"));
                wall.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, SIDEDOOR) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity door) {
                if (!door.getComponent(SideDoorComponent.class).isOpened())
                    enemyBullet.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity player) {
                enemyBullet.removeFromWorld();
                Point2D deathVector = (player.getBoundingBoxComponent().getCenterWorld().subtract(enemyBullet.getBoundingBoxComponent().getCenterWorld()));
                player.getComponent(PlayerComponent.class).onHit(enemyBullet.getInt("damage"), deathVector);
                player.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EXIT) {
            @Override
            protected void onCollisionBegin(Entity player, Entity exit) {
                if (exit.getComponent(ExitDoorComponent.class).isOpened()) {
                    PropertyMap exitP = exit.getProperties();
                    getDisplay().showMessageBox("Level Complete.", () -> {
                        if (exitP.exists("next")) {
                            if (exitP.exists("newBoundX"))
                                setLevel(exitP.getString("next"), exitP.getInt("newBoundX"), exitP.getInt("newBoundY"));
                            else
                                setLevel(exitP.getString("next"));
                        } else
                            setLevel(gets("level"));
                    });
                }
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, KEYCARD) {
            @Override
            protected void onCollisionBegin(Entity player, Entity keycard) {
                keycard.removeFromWorld();
                set("hasKeycard", true);
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(MOVING, MOVINGSTOP) {
            @Override
            protected void onCollisionBegin(Entity moving, Entity movingStop) {
                moving.getComponent(MovingPlatformComponent.class).checkDirection();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, AMMOBOX) {
            @Override
            protected void onCollisionBegin(Entity player, Entity ammobox) {
                ammobox.removeFromWorld();
                player.getComponent(PlayerComponent.class).restoreAmmo(ammobox.getProperties().getInt("amount"));
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, PASSABLETRIGGER) {
            @Override
            protected void onCollisionBegin(Entity player, Entity passableTrigger) {
                getGameWorld().getEntitiesAt(passableTrigger.getPosition().add(0, 81)).get(0).getComponent(PhysicsComponent.class).getBody().setActive(true);
            }

            @Override
            protected void onCollision(Entity player, Entity passableTrigger) {
                if (allowPass)
                    getGameWorld().getEntitiesAt(passableTrigger.getPosition().add(0, 81)).get(0).getComponent(PhysicsComponent.class).getBody().setActive(false);

            }

            @Override
            protected void onCollisionEnd(Entity player, Entity passableTrigger) {
                getGameWorld().getEntitiesAt(passableTrigger.getPosition().add(0, 81)).get(0).getComponent(PhysicsComponent.class).getBody().setActive(false);
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, SIDEDOORTRIGGER) {
            @Override
            protected void onCollisionBegin(Entity player, Entity sideDoorTrigger) {
                Entity sideDoor = getGameWorld().getEntitiesAt(sideDoorTrigger.getPosition().add(90, 0)).get(0);
                sideDoor.getComponent(SideDoorComponent.class).checkCondition();
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity sideDoorTrigger) {
                Entity sideDoor = getGameWorld().getEntitiesAt(sideDoorTrigger.getPosition().add(90, 0)).get(0);
                sideDoor.getComponent(SideDoorComponent.class).checkCondition();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, TELEPORTER) {
            @Override
            protected void onCollisionBegin(Entity player, Entity teleport) {
                var teleport1 = teleport.getComponent(TeleportComponent.class);

                if (!teleport1.isActive()) {
                    return;
                }

                var teleport2 = getGameWorld().getEntitiesByType(TELEPORTER)
                        .stream()
                        .filter(e -> e != teleport)
                        .findAny()
                        .get()
                        .getComponent(TeleportComponent.class);

                teleport2.activate();

                animationBuilder()
                        .duration(Duration.seconds(0.5))
                        .onFinished(() -> {
                            player.getComponent(PhysicsComponent.class).overwritePosition(teleport2.getEntity().getPosition());
                            spawn("teleportEffect", teleport2.getEntity().getPosition());
                            animationBuilder()
                                    .fadeIn(player)
                                    .buildAndPlay();
                        })
                        .fadeOut(player)
                        .buildAndPlay();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, ONEWAYTELEPORT) {
            @Override
            protected void onCollisionBegin(Entity player, Entity teleport) {
                String connected1 = teleport.getProperties().getString("connected");
                String connected2 = "";

                for (int i = 0; i < FXGL.getGameWorld().getEntitiesByType(OWTDROPOFF).size(); i++) {
                    if (FXGL.getGameWorld().getEntitiesByType(OWTDROPOFF).get(i).getProperties().exists("connected")) {
                        connected2 = FXGL.getGameWorld().getEntitiesByType(OWTDROPOFF).get(i).getProperties().getString("connected");
                        if (connected1.equals(connected2)) {
                            var teleport2 = FXGL.getGameWorld().getEntitiesByType(OWTDROPOFF).get(i);
                            animationBuilder()
                                    .duration(Duration.seconds(0.5))
                                    .onFinished(() -> {
                                        player.getComponent(PhysicsComponent.class).overwritePosition(teleport2.getPosition());
                                        spawn("teleportEffect", teleport2.getPosition());
                                        animationBuilder()
                                                .fadeIn(player)
                                                .buildAndPlay();
                                    })
                                    .fadeOut(player)
                                    .buildAndPlay();
                        }
                    }
                }
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, EXITSWITCH) {
            @Override
            protected void onCollisionBegin(Entity player, Entity exitSwitch) {
                onSwitch = true;
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity exitSwitch) {
                onSwitch = false;
            }
        });
    }

    protected void onUpdate(double tpf) {
        if (player.getPosition().getY() > geti("currentBoundY")) {
            player.getComponent(PlayerComponent.class).setHP(0);
            playerDeath();
        }
    }

    protected void setLevel(String level) {
        setLevelFromMap(level);
        set("level", level);

        initialLevelActions();
    }

    protected void setLevel(String level, int newMaxX, int newMaxY) {
        setLevelFromMap(level);
        set("level", level);
        set("currentBoundX", newMaxX);
        set("currentBoundY", newMaxY);

        getGameScene().getViewport().setBounds(-32, 0, newMaxX, newMaxY);

        initialLevelActions();
    }

    private void initialLevelActions() {
        set("hasKeycard", false);

        if (player != null) {
            player.getComponent(PhysicsComponent.class).overwritePosition(getGameWorld().getSingleton(START).getPosition());
            player.setZ(Integer.MAX_VALUE);
            player.getComponent(PlayerComponent.class).restoreHP();
        }

        if (!getGameWorld().getEntitiesByType(PASSABLE).isEmpty()) {
            for (int i = 0; i < getGameWorld().getEntitiesByType(PASSABLE).size(); i++) {
                Entity passable = getGameWorld().getEntitiesByType(PASSABLE).get(i);
                SpawnData data = new SpawnData(passable.getPosition().add(0, -81));
                double typeCast = passable.getWidth();
                data.put("width", (int) typeCast);
                double typeCast2 = passable.getHeight();
                data.put("height", (int) typeCast2);
                getGameWorld().spawn("passablePlatformTrigger", data);
            }
        }

        if (!getGameWorld().getEntitiesByType(SIDEDOOR).isEmpty()) {
            for (int i = 0; i < getGameWorld().getEntitiesByType(SIDEDOOR).size(); i++) {
                Entity sideDoor = getGameWorld().getEntitiesByType(SIDEDOOR).get(i);
                SpawnData data = new SpawnData(sideDoor.getPosition().add(-90, 0));
                data.put("width", 212);
                Double typeCast = sideDoor.getHeight();
                data.put("height", typeCast.intValue());
                getGameWorld().spawn("sideDoorTrigger", data);
            }
        }
    }

    public void playerDeath() {
        FXGL.runOnce(() -> {
            getDisplay().showMessageBox("You Died.", getGameController()::gotoMainMenu);
        }, Duration.seconds(0.45));
    }

    protected void initHP() {
        if (player != null) {
            player.getComponent(PlayerComponent.class).restoreHP();
        }
        for (int i = 0; i < getGameWorld().getEntitiesByType(ENEMY).size(); i++) {
            getGameWorld().getEntitiesByType(ENEMY).get(i).getComponent(EnemyComponent.class).initHP();
        }
        for (int i = 0; i < getGameWorld().getEntitiesByType(ELITEENEMY).size(); i++) {
            getGameWorld().getEntitiesByType(ELITEENEMY).get(i).getComponent(EliteEnemyComponent.class).initHP();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
