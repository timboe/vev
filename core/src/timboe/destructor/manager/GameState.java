package timboe.destructor.manager;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

  public Set<Sprite> particleSet = new HashSet<Sprite>();

  private Rectangle tempRect = new Rectangle();

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
    if (tickTime < .2) return; // Tick every second
    tickTime -= .2;

    // Add a new sprite
    double rAngle = -Math.PI + (R.nextFloat() * Math.PI * 2);
    IVector2 warp = World.getInstance().warps.get( R.nextInt( World.getInstance().warps.size() ) );
    Sprite s = new Sprite((int)Math.round(warp.x + (2*Param.WARP_SIZE/3 * Math.cos(rAngle))),
                          (int)Math.round(warp.y + (2*Param.WARP_SIZE/3 * Math.sin(rAngle))));
    s.setTexture("ball", 6, false);
    spriteStage.addActor(s);
    particleSet.add(s);
  }

  public boolean isSelecting() {
    return selectStartWorld.dst(selectEndWorld) > 6; // Min pixels to count as a selection
  }

  public void doParticleSelect() {
    tempRect.set(Math.min(selectStartWorld.x, selectEndWorld.x),
            Math.min(selectStartWorld.y, selectEndWorld.y),
            Math.abs(selectEndWorld.x - selectStartWorld.x),
            Math.abs(selectEndWorld.y - selectStartWorld.y));
    for (Sprite s : particleSet) {
      s.selected = tempRect.contains(s.getX() / Param.SPRITE_SCALE, s.getY() / Param.SPRITE_SCALE);
    }
    selectStartWorld.setZero();
  }

  public void doParticleMoveOrder(int x, int y) {
    Tile target = World.getInstance().getTile(x / Param.TILE_S, y / Param.TILE_S);
    Set<Sprite> doneSet = new HashSet<Sprite>();
    Set<Tile> solutionKnownFrom = new HashSet<Tile>();
    for (Sprite s : particleSet) {
      if (s.selected) {
        s.pathTo(target, solutionKnownFrom, doneSet);
        if (s.pathingList != null) {
          doneSet.add(s);
          solutionKnownFrom.addAll(s.pathingList);
        }
      }
    }
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
    particleSet.clear();
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
