package timboe.destructor.manager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

import java.util.*;

import static timboe.destructor.enums.Cardinal.kNE;
import static timboe.destructor.enums.Cardinal.kNW;
import static timboe.destructor.enums.Cardinal.kSE;

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

    spriteStage.act(delta);


    if (tickTime < 1) return; // Tick every second
    tickTime -= 1;

    // Add a new sprite
    double rAngle = -Math.PI + (R.nextFloat() * Math.PI * 2);
    List<Map.Entry<IVector2,ParticleEffect>> entries = new ArrayList<Map.Entry<IVector2,ParticleEffect>>(World.getInstance().warps.entrySet());
    Map.Entry<IVector2,ParticleEffect> rWarp = entries.get( R.nextInt(entries.size()) );
    IVector2 warp = rWarp.getKey();
    ParticleEffect zap = rWarp.getValue();

    int placeTry = 0;
    do {
      int tryX = (int) Math.round(warp.x + (2 * Param.WARP_SIZE / 3 * Math.cos(rAngle)));
      int tryY = (int) Math.round(warp.y + (2 * Param.WARP_SIZE / 3 * Math.sin(rAngle)));
      if (!World.getInstance().getTile(tryX, tryY).hasParkingSpace()) continue;
      if (World.getInstance().getTile(tryX, tryY).getNeighbours().size() == 0) continue; // Non-pathable
      Sprite s = new Sprite(tryX, tryY);
      Cardinal pSpace = World.getInstance().getTile(tryX, tryY).regSprite(s);
      s.moveBy(pSpace == kSE || pSpace == kNE ? Param.TILE_S : 0, pSpace == kNW || pSpace == kNE ? Param.TILE_S : 0);
      s.setTexture("ball_" + Colour.random().getString(), 6, false);
      spriteStage.addActor(s);
      particleSet.add(s);
      Rectangle r = new Rectangle((warp.x - Param.WARP_SIZE / 2) * Param.TILE_S,
              (warp.y - Param.WARP_SIZE / 2) * Param.TILE_S,
              Param.WARP_SIZE * Param.TILE_S, Param.WARP_SIZE * Param.TILE_S);
      if (Camera.getInstance().onScrean(r)) {
        //      Camera.getInstance().addShake(30f / Camera.getInstance().getZoom());
      }
      zap.start();
      placeTry = Param.N_PATCH_TRIES; // To break the loop
    } while (++placeTry < Param.N_PATCH_TRIES);
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

  public void repath() {
    pathingInternal(0, 0, true);
  }

  public void doParticleMoveOrder(int x, int y) {
    pathingInternal(x, y, false);
  }

  // Used to send everyone to a particular destination or re-path everyone when the pathing grid mutates
  private void pathingInternal(int x, int y, boolean doRepath) {
    Set<Sprite> pathed = new HashSet<Sprite>();
    boolean anotherRoundNeeded = false;
    int rounds = 0;
    do { // Pathfinding for a group of critters in the same location, caching the path between them.
      ++rounds;
      Tile target = World.getInstance().getTile(x / Param.TILE_S, y / Param.TILE_S);
      Set<Sprite> doneSet = new HashSet<Sprite>();
      Set<Tile> solutionKnownFrom = new HashSet<Tile>();
      anotherRoundNeeded = false;
      Sprite firstSprite = null;
      for (Sprite s : particleSet) {
        if (pathed.contains(s)) continue; // Already done in another loop
        if (!doRepath && !s.selected) continue; // If move order, critter must be selected
        if (doRepath && s.pathingList.isEmpty()) continue; // If repath, critter must be moving
        if (firstSprite == null) { // have not yet hit the first of this batch of critters to path - this will be it
          firstSprite = s;
          if (doRepath) target = s.getDestination(); // Update target
        }
        if ((doRepath && s.getDestination() != target) || // I'm going somewhere else
                Math.hypot(s.getX() - firstSprite.getX(), s.getY() - firstSprite.getY()) > Param.TILE_S * Param.WARP_SIZE) { // TODO try and make this larger to improve performance
          // Do this (and any others far away or different target) in another iteration of the do loop
          anotherRoundNeeded = true;
          continue;
        }
        s.pathTo(target, solutionKnownFrom, doneSet);
        pathed.add(s);
        if (s.pathingList != null) {
          doneSet.add(s);
          solutionKnownFrom.addAll(s.pathingList);
        }
      }
    } while (anotherRoundNeeded);
    Gdx.app.log("pathingInternal","Pathing of " + pathed.size() + " sprites took " + rounds + " rounds");
    if (!doRepath && pathed.size() > 0) Sounds.getInstance().moveOrder();
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
