package timboe.destructor.manager;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

import java.util.Random;

public class GameState {

  public Vector3 selectStartScreen = new Vector3();
  public Vector3 selectStartWorld = new Vector3();
  public Vector3 selectEndScreen = new Vector3();
  public Vector3 selectEndWorld = new Vector3();

  private Stage tileStage;
  private Stage spriteStage;
  private Stage warpStage;
  private Stage uiStage;

  private final Random R = new Random();

  private float tickTime = 0;

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

  public void act(float delta) {
    tickTime += delta;
    if (tickTime < 1) return; // Tick every second
    tickTime -= 1;

    // Add a new sprite
    double rAngle = -Math.PI + (R.nextFloat() * Math.PI * 2);
    IVector2 warp = World.getInstance().warps.get( R.nextInt( World.getInstance().warps.size() ) );
    Sprite s = new Sprite((int)Math.round(warp.x + (3*Param.WARP_SIZE/4 * Math.cos(rAngle))),
                          (int)Math.round(warp.y + (3*Param.WARP_SIZE/4 * Math.sin(rAngle))));
    s.setTexture("ball", 6, false);
    spriteStage.addActor(s);

  }

  public void setToTitleScreen() {
    game.setScreen(theTitleScreen);
  }

  public void setToGameScreen() {
    game.setScreen(theGameScreen);
  }

  public Stage getTileStage() {
    return tileStage;
  }

  public Stage getUIStage() {
    return uiStage;
  }

  public Stage getWarpStage() { return warpStage; }

  public Stage getSpriteStage() {
    return spriteStage;
  }

  public void reset() {
    if (tileStage != null) tileStage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (uiStage != null) uiStage.dispose();
    if (warpStage!= null) warpStage.dispose();
    tileStage = new Stage(Camera.getInstance().getViewport());
    spriteStage = new Stage(Camera.getInstance().getViewport());
    uiStage = new Stage(Camera.getInstance().getViewport());
    warpStage = new Stage(Camera.getInstance().getViewport());
    Color warpStageC = warpStage.getBatch().getColor();
    warpStageC.a = Param.WARP_TRANSPARENCY;
    warpStage.getBatch().setColor(warpStageC);
  }

  public void dispose() {
    theGameScreen.dispose();
    theTitleScreen.dispose();
    tileStage.dispose();
    spriteStage.dispose();
    uiStage.dispose();
    warpStage.dispose();
    ourInstance = null;
  }


}
