package timboe.destructor.manager;


import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

public class GameState {

  private Stage stage;
  private Stage spriteStage;
  private Stage uiStage;

  private static GameState ourInstance;
  public static GameState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new GameState(); }

  private TitleScreen theTitleScreen;
  private GameScreen theGameScreen;
  private DestructorGame game;

  private GameState() {
    reset();
  }

  public void setGame(DestructorGame theGame) {
    game = theGame;
    theTitleScreen = new TitleScreen();
    theGameScreen = new GameScreen();
  }

  public void setToTitleScreen() {
    game.setScreen(theTitleScreen);
  }

  public void setToGameScreen() {
    game.setScreen(theGameScreen);
  }

  public Stage getStage() {
    return stage;
  }

  public Stage getUIStage() {
    return uiStage;
  }

  public Stage getSpriteStage() {
    return spriteStage;
  }

  public void reset() {
    if (stage != null) stage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (uiStage != null) uiStage.dispose();
    stage = new Stage(Camera.getInstance().getViewport());
    spriteStage = new Stage(Camera.getInstance().getViewport());
    uiStage = new Stage(Camera.getInstance().getViewport());
  }

  public void dispose() {
    theGameScreen.dispose();
    theTitleScreen.dispose();
    stage.dispose();
    spriteStage.dispose();
    uiStage.dispose();
    ourInstance = null;
  }


}
