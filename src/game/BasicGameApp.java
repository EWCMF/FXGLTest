package game;

import com.almasb.fxgl.app.*;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.core.collection.PropertyMap;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import game.characters.FlickerComponent;
import game.enemy.BaronOfHellComponent;
import game.enemy.TurretComponent;
import game.enemy.MovingEnemyComponent;
import game.level.*;
import game.characters.HPComponent;
import game.player.PlayerComponent;
import game.ui.BasicGameGameMenu;
import game.ui.BasicGameMainMenu;
import game.ui.BossHPIndicator;
import game.ui.HPIndicator;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

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
    public static int ammoRocket = 10;

    public static int enemyDamageModifier = 0;

    private String startLevel = "testBoss.tmx";
    private int startBoundX = 32 * 100;
    private int startBoundY = 32 * 70;

    private boolean allowPass = false;
    private boolean onSwitch = false;

    private String cameraPosX = "";
    private String cameraPosY = "";

    private VBox keysBox = new VBox();
    private AnchorPane bossHP = new AnchorPane();

    public static Music music;

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
                return new BasicGameMainMenu();
            }

            @NotNull
            @Override
            public FXGLMenu newGameMenu() {
                return new BasicGameGameMenu();
            }
        });
        settings.setDeveloperMenuEnabled(false);
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
                if (onSwitch) {
                    FXGL.getGameWorld().getEntitiesByType(EXITSWITCH).get(0).getComponent(ExitSwitchComponent.class).activate();
                    spawn("overheadText", new SpawnData(player.getPosition().add(0, -50)).put("text", "The exit is now open."));
                }
            }
        }, KeyCode.F);

        input.addAction(new UserAction("Test something") {
            @Override
            protected void onActionBegin() {
                if (getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).isActive()) {
                    getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).setActive(false);
                    getAudioPlayer().stopMusic(music);
                }
                else
                    getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).setActive(true);
            }
        }, KeyCode.C);

        input.addAction(new UserAction("Test something2") {
            @Override
            protected void onActionBegin() {
                getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).attackPurple();
            }
        }, KeyCode.V);

        input.addAction(new UserAction("Test something3") {
            @Override
            protected void onActionBegin() {
                getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).attack();
            }
        }, KeyCode.B);

        input.addAction(new UserAction("Test something4") {
            @Override
            protected void onActionBegin() {
                System.out.println(getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(PhysicsComponent.class).getBody().getContactList().contact);
                System.out.println(getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(PhysicsComponent.class).getBody().getFixtures().get(1).getHitBox());
            }
        }, KeyCode.N);

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
//                Point2D vector = FXGL.getInput().getVectorToMouse(player.getPosition());
//                if (vector.getX() < 0 && player.getTransformComponent().getScaleX() != -1.0)
//                    return;
//                if (vector.getX() > 0 && player.getTransformComponent().getScaleX() != 1.0)
//                    return;

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

        input.addAction(new UserAction("Camera left") {
            @Override
            protected void onActionBegin() {
                cameraPosX = "left";
            }
        }, KeyCode.J);

        input.addAction(new UserAction("Camera down") {
            @Override
            protected void onActionBegin() {
                cameraPosY = "down";
            }
        }, KeyCode.K);

        input.addAction(new UserAction("Camera right") {
            @Override
            protected void onActionBegin() {
                cameraPosX = "right";
            }
        }, KeyCode.L);

        input.addAction(new UserAction("Camera up") {
            @Override
            protected void onActionBegin() {
                cameraPosY = "up";
            }
        }, KeyCode.I);
    }

    private void FiringPosition(double mouseY, Point2D gunMiddle, Point2D gunUp, Point2D gunDown) {
        if (mouseY > aimUpVectorY && mouseY < aimDownVectorY) {
            Point2D gunOffset = player.getPosition().add(gunMiddle);
            Point2D mouseVector = FXGL.getInput().getVectorToMouse(gunOffset).normalize();
            player.getComponent(PlayerComponent.class).fire(mouseVector, gunOffset);
        } else if (mouseY < aimUpVectorY) {
            Point2D gunOffset = player.getPosition().add(gunUp);
            Point2D mouseVector = FXGL.getInput().getVectorToMouse(gunOffset).normalize();
            player.getComponent(PlayerComponent.class).fire(mouseVector, gunOffset);
        } else if (mouseY > aimDownVectorY) {
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
        vars.put("ammoRockets", ammoRocket);
        vars.put("weaponIndicatorPosition", 13);

        vars.put("hasKeycardBlue", false);
        vars.put("hasKeycardRed", false);
        vars.put("hasKeycardYellow", false);

        vars.put("isBossLevel", false);
        vars.put("isPlayingMusic", false);
    }

    public Entity player;

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BasicGameFactory());

        player = null;

        setLevel(gets("level"));
        getGameScene().setBackgroundColor(Color.color(0.2, 0.2, 0.2));

        Point2D start = FXGL.getGameWorld().getSingleton(START).getPosition();
        player = getGameWorld().spawn("player", start);

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
        var r = getUIFactory().newText("R", Color.WHITE, 20);
        r.setStroke(Color.BLACK);

        var ammoShotgun = getUIFactory().newText("", Color.WHITE, 15);
        ammoShotgun.textProperty().bind(getip("ammoShotgun").asString());
        var ammoMachineGun = getUIFactory().newText("", Color.WHITE, 15);
        ammoMachineGun.textProperty().bind(getip("ammoMachineGun").asString());
        var ammoRockets = getUIFactory().newText("", Color.WHITE, 15);
        ammoRockets.textProperty().bind(getip("ammoRockets").asString());

        var weaponIndicator = new Rectangle(13, 32, 20, 22);
        weaponIndicator.xProperty().bind(getip("weaponIndicatorPosition"));
        weaponIndicator.setStroke(Color.BLUE);
        weaponIndicator.setFill(null);

        addUINode(hp, hpInX, hpInY);

        addUINode(keysBox, 15, 120);
        addUINode(d, 15, 50);
        addUINode(s, 55, 50);
        addUINode(ammoShotgun, 55, 65);
        addUINode(m, 95, 50);
        addUINode(ammoMachineGun, 95, 65);
        addUINode(r, 135, 50);
        addUINode(ammoRockets, 135, 65);
        addUINode(weaponIndicator);
        addUINode(bossHP, 15, 35);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 760);
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, TURRET) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                bullet.removeFromWorld();
                enemy.getComponent(TurretComponent.class).onHit(bullet.getInt("damage"));
                enemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, ELITETURRET) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity eliteEnemy) {
                bullet.removeFromWorld();
                eliteEnemy.getComponent(TurretComponent.class).onHit(bullet.getInt("damage"));
                eliteEnemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, MOVINGENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity movingEnemy) {
                bullet.removeFromWorld();
                movingEnemy.getComponent(MovingEnemyComponent.class).onHit(bullet.getInt("damage"));
                movingEnemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, ELITEMOVINGENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity movingEnemy) {
                bullet.removeFromWorld();
                movingEnemy.getComponent(MovingEnemyComponent.class).onHit(bullet.getInt("damage"));
                movingEnemy.getComponent(FlickerComponent.class).flicker();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, BARONOFHELL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity baron) {
                if (bullet.getY() < getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(PhysicsComponent.class).getBody().getFixtures().get(1).getHitBox().getMaxYWorld()) {
                    baron.getComponent(BaronOfHellComponent.class).onHit(bullet.getInt("damage"));
                }
                bullet.removeFromWorld();

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

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, SIDEDOOR) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity door) {
                if (!door.getComponent(SideDoorComponent.class).isOpened())
                    enemyBullet.removeFromWorld();
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

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity player) {
                enemyBullet.removeFromWorld();
                Point2D deathVector = (player.getBoundingBoxComponent().getCenterWorld().subtract(enemyBullet.getBoundingBoxComponent().getCenterWorld()));
                player.getComponent(PlayerComponent.class).onHit(enemyBullet.getInt("damage"), deathVector);
                player.getComponent(FlickerComponent.class).flicker();
            }
        });

        playerRocketCollision(WALL);
        playerRocketCollision(SIDEDOOR);
        playerRocketCollision(TURRET);
        playerRocketCollision(ELITETURRET);
        playerRocketCollision(MOVINGENEMY);
        playerRocketCollision(ELITEMOVINGENEMY);
        playerRocketCollision(BREAKABLEWALL);
        playerRocketCollision(BARONOFHELL);

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
                } else
                    spawn("overheadText", new SpawnData(exit.getPosition().add(0, -50)).put("text", "Find a switch to open the door."));
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, KEYCARD) {
            @Override
            protected void onCollisionBegin(Entity player, Entity keycard) {
                keycard.removeFromWorld();
                switch (keycard.getProperties().getString("keyType")) {
                    case "blue":
                        set("hasKeycardBlue", true);
                        var key1 = getUIFactory().newText(keycard.getString("keyType").replace("b", "B"), Color.BLUE, 18);
                        keysBox.getChildren().add(key1);
                        break;
                    case "red":
                        set("hasKeycardRed", true);
                        var key2 = getUIFactory().newText(keycard.getString("keyType").replace("r", "R"), Color.RED, 18);
                        keysBox.getChildren().add(key2);
                        break;
                    case "yellow":
                        set("hasKeycardYellow", true);
                        var key3 = getUIFactory().newText(keycard.getString("keyType").replace("y", "Y"), Color.YELLOW, 18);
                        keysBox.getChildren().add(key3);
                }
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

        sideDoorTrigger(PLAYER);
        sideDoorTrigger(MOVINGENEMY);
        sideDoorTrigger(ELITEMOVINGENEMY);

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

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(NORMALBOH, WALL) {
            @Override
            protected void onCollisionBegin(Entity normalBOH, Entity wall) {
                getGameWorld().spawn("normalBOHExplosion", new SpawnData(normalBOH.getPosition().add(-160, -30)));
                normalBOH.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(NORMALBOH, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity normalBOH, Entity wall) {
                getGameWorld().spawn("normalBOHExplosion", new SpawnData(normalBOH.getPosition().add(-160, -30)));
                normalBOH.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PURPLEBOH, WALL) {
            @Override
            protected void onCollisionBegin(Entity purpleBOH, Entity wall) {
                getGameWorld().spawn("normalBOHExplosionPurple", new SpawnData(purpleBOH.getPosition().add(-360, -80)));
                purpleBOH.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PURPLEBOH, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity purpleBOH, Entity wall) {
                getGameWorld().spawn("normalBOHExplosionPurple", new SpawnData(purpleBOH.getPosition().add(-360, -80)));
                purpleBOH.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(PLAYER, BARONOFHELL) {
            @Override
            protected void onCollisionBegin(Entity player, Entity baron) {
                baron.getComponent(BaronOfHellComponent.class).melee();
            }
        });

        onCollisionOneTimeOnly(PLAYER, BOHTRIGGER, (player, bohTrigger) -> {
            getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).walkLeftCutscene();
            getGameWorld().getEntitiesByType(SIDEDOOR).get(0).getComponent(SideDoorComponent.class).lockDoor();
            getGameWorld().getEntitiesByType(PLAYER).get(0).getComponent(PlayerComponent.class).setPlayerControl(false);
            getGameWorld().getEntitiesByType(PLAYER).get(0).getComponent(PlayerComponent.class).completeStop();
            FXGL.runOnce(() -> {
                getGameWorld().getEntitiesByType(BARONOFHELL).get(0).getComponent(BaronOfHellComponent.class).setActive(true);
                activateBossHP();
                getGameWorld().getEntitiesByType(PLAYER).get(0).getComponent(PlayerComponent.class).setPlayerControl(true);
                music = FXGL.getAssetLoader().loadMusic("bossBGM.mp3");
                getAudioPlayer().loopMusic(music);
                set("isPlayingMusic", true);
            }, Duration.seconds(5));
        });
    }

    private void sideDoorTrigger(BasicGameTypes enemy) {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(enemy, SIDEDOORTRIGGER) {
            @Override
            protected void onCollisionBegin(Entity actor, Entity sideDoorTrigger) {
                Entity sideDoor = getGameWorld().getEntitiesAt(sideDoorTrigger.getPosition().add(90, 0)).get(0);
                sideDoor.getComponent(SideDoorComponent.class).checkCondition();
            }

            @Override
            protected void onCollisionEnd(Entity actor, Entity sideDoorTrigger) {
                Entity sideDoor = getGameWorld().getEntitiesAt(sideDoorTrigger.getPosition().add(90, 0)).get(0);
                sideDoor.getComponent(SideDoorComponent.class).checkCondition();
            }
        });
    }

    private void playerRocketCollision(BasicGameTypes hit) {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ROCKET, hit) {
            @Override
            protected void onCollisionBegin(Entity rocket, Entity hit) {
                if (hit.isType(SIDEDOOR))
                    if (hit.getComponent(SideDoorComponent.class).isOpened())
                        return;
                FXGL.spawn("playerExplosion", rocket.getPosition().add(-80, -85));
                rocket.removeFromWorld();
            }
        });
    }

    protected void onUpdate(double tpf) {
        if (player.getPosition().getY() > geti("currentBoundY")) {
            player.getComponent(PlayerComponent.class).setHP(0);
            playerDeath();
        }

        if (cameraPosX.equals("left"))
            getGameScene().getViewport().setX(player.getPosition().getX() - 900);
        if (cameraPosY.equals("down"))
            getGameScene().getViewport().setY(player.getPosition().getY() - 200);
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
        set("hasKeycardBlue", false);
        set("hasKeycardRed", false);
        set("hasKeycardYellow", false);
        keysBox.getChildren().clear();
        bossHP.getChildren().clear();
        bossHP.setOpacity(1);
        var keys = getUIFactory().newText("Keycards:", Color.WHITE, 20);
        keysBox.getChildren().add(keys);

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

                SpawnData data2 = new SpawnData(passable.getPosition());
                double typeCast3 = passable.getWidth();
                data2.put("width", (int) typeCast3);
                double typeCast4 = passable.getHeight();
                data2.put("height", (int) typeCast4);
                getGameWorld().spawn("passablePlatformEnemy", data2);
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

        if (!getGameWorld().getEntitiesByType(BARONOFHELL).isEmpty()) {
            Entity boss = getGameWorld().getEntitiesByType(BARONOFHELL).get(0);
            set("isBossLevel", true);
            var bossName = getUIFactory().newText("Boss", Color.WHITE, 22);
            bossName.setX(920);
            bossName.setY(25);
            var bossHPBar = new BossHPIndicator(boss.getComponent(HPComponent.class));
            bossHP.getChildren().addAll(bossName, bossHPBar);
            bossHP.setOpacity(0);
        }
    }

    public void playerDeath() {
        FXGL.runOnce(() -> {
            getDisplay().showMessageBox("You Died.", getGameController()::gotoMainMenu);
            if (getb("isPlayingMusic"))
                getAudioPlayer().stopMusic(music);
        }, Duration.seconds(0.45));
    }

    public void activateBossHP() {
        bossHP.setOpacity(1);
    }

    protected void initHP() {
        if (player != null) {
            player.getComponent(PlayerComponent.class).restoreHP();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
