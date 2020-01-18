package game.ui;

import com.almasb.fxgl.app.FXGLMenu;
import com.almasb.fxgl.app.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class BasicGameGameMenu extends FXGLMenu {

    public BasicGameGameMenu() {
        super(MenuType.GAME_MENU);

        var mainMenu = new GameMenuButton("Return to main menu", this::fireExitToMainMenu);
        mainMenu.setTranslateX(FXGL.getAppWidth()/2 - 100);
        mainMenu.setTranslateY(FXGL.getAppHeight()/2 - 20);

        var stackP = new StackPane();

        stackP.setTranslateX(FXGL.getAppWidth()/2 - 100);
        stackP.setTranslateY(FXGL.getAppHeight()/2 - 100);

        var textRect = new Rectangle(200, 40);
        var text = FXGL.getUIFactory().newText("Game Paused", Color.WHITE, 24);
        text.setUnderline(true);

        stackP.getChildren().addAll(textRect, text);

        getMenuContentRoot().getChildren().addAll(mainMenu, stackP);
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
    protected Node createTitleView(String s) {
        return new Text();
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
