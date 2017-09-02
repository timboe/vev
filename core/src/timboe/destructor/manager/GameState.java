package timboe.destructor.manager;


import timboe.destructor.DestructorGame;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

public class GameState {

  private static GameState ourInstance;
  public static GameState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new GameState(); }

  private TitleScreen theTitleScreen;
  private GameScreen theGameScreen;
  private DestructorGame game;

  private GameState() {
    theTitleScreen = new TitleScreen();
    theGameScreen = new GameScreen();
  }

  public void setGame(DestructorGame theGame) {
    game = theGame;
  }

  public void setToTitleScreen() {
    game.setScreen(theTitleScreen);
  }

  public void setToGameScreen() {
    game.setScreen(theGameScreen);
  }

  public void reset() {
  }

  public void dispose() {
    theGameScreen.dispose();
    theTitleScreen.dispose();
    ourInstance = null;
  }


}
