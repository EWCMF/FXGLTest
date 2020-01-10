package game;

import com.almasb.fxgl.app.FXGLMenu;
import com.almasb.fxgl.app.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class BasicGameMenu extends FXGLMenu {

    public BasicGameMenu() {
        super(MenuType.MAIN_MENU);

        VBox vBox1 = new VBox();
        VBox vBox2 = new VBox();

        double centerX = FXGL.getAppWidth() / 2 - 200 / 2;
        double centerY = FXGL.getAppHeight() / 2;

        // First menu.
        var start = new GameMenuButton("Start", () -> {
            getMenuRoot().getChildren().set(0, vBox2);
        });

        start.setTranslateX(centerX);
        start.setTranslateY(centerY - 80);

        var exit = new GameMenuButton("Exit", this::fireExit);

        vBox1.getChildren().addAll(start, exit);

        exit.setTranslateX(centerX);
        exit.setTranslateY(centerY - 60);

        // Difficulty menu.
        var easy = new GameMenuButton("Easy", () -> {
            this.fireNewGame();
            BasicGameApp.enemyDamageModifier = 1;
            getMenuRoot().getChildren().set(0, vBox1);
        });

        easy.setTranslateX(centerX);
        easy.setTranslateY(centerY - 80);

        var normal = new GameMenuButton("Normal", () -> {
            this.fireNewGame();
            BasicGameApp.enemyDamageModifier = 2;
            getMenuRoot().getChildren().set(0, vBox1);
        });

        normal.setTranslateX(centerX);
        normal.setTranslateY(centerY - 60);

        var hard = new GameMenuButton("Hard", () -> {
            this.fireNewGame();
            BasicGameApp.enemyDamageModifier = 3;
            getMenuRoot().getChildren().set(0, vBox1);
        });

        hard.setTranslateX(centerX);
        hard.setTranslateY(centerY - 40);

        var lunatic = new GameMenuButton("Lunatic", () -> {
            this.fireNewGame();
            BasicGameApp.enemyDamageModifier = 4;
            getMenuRoot().getChildren().set(0, vBox1);
        });

        lunatic.setTranslateX(centerX);
        lunatic.setTranslateY(centerY - 20);

        var back = new GameMenuButton("Back", () -> {
            getMenuRoot().getChildren().set(0, vBox1);
        });

        back.setTranslateX(centerX);
        back.setTranslateY(centerY + 80);

        vBox2.getChildren().addAll(easy, normal, hard, lunatic, back);

        getMenuRoot().getChildren().add(vBox1);
    }

    @Override
    protected Button createActionButton(StringBinding stringBinding, Runnable runnable) {
        return new Button();
    }

    @Override
    protected Button createActionButton(String s, Runnable runnable) {
        return new Button();
    }

    @Override
    protected Node createBackground(double width, double height) {
        return new Rectangle(width, height, Color.BLACK);
    }

    @Override
    protected Node createProfileView(String s) {
        return new Text();
    }

    @Override
    protected Node createTitleView(String title) {
        return new Text(title);
    }

    @Override
    protected Node createVersionView(String s) {
        return new Text();
    }

    private static class GameMenuButton extends StackPane {
        public GameMenuButton(String name, Runnable action) {
            var bg = new Rectangle(200, 40);
            bg.setStroke(Color.WHITE);

            var text = FXGL.getUIFactory().newText(name, Color.WHITE, 18);

            bg.fillProperty().bind(
                    Bindings.when(hoverProperty()).then(Color.WHITE).otherwise(Color.BLACK)
            );

            text.fillProperty().bind(
                    Bindings.when(hoverProperty()).then(Color.BLACK).otherwise(Color.WHITE)
            );

            setOnMouseClicked(e -> action.run());

            getChildren().addAll(bg, text);
        }
    }
}
