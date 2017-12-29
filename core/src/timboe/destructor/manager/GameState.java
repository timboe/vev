package timboe.destructor.manager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.TileType;
import timboe.destructor.enums.UIMode;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.pathfinding.OrderlyQueue;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

import java.util.*;

import static timboe.destructor.enums.Cardinal.kNE;
import static timboe.destructor.enums.Cardinal.kNW;
import static timboe.destructor.enums.Cardinal.kS;
import static timboe.destructor.enums.Cardinal.kSE;
import static timboe.destructor.enums.Cardinal.kSW;

public class GameState {

  // UI interactions
  public UIMode uiMode = UIMode.kNONE;

  public Vector3 selectStartScreen = new Vector3();
  public Vector3 selectStartWorld = new Vector3();
  public Vector3 selectEndScreen = new Vector3();
  public Vector3 selectEndWorld = new Vector3();

  public Vector3 cursor = new Vector3();

  private boolean buildingLocationGood = false;
  private Tile buildingLocation;

  private Stage tileStage;
  private Stage spriteStage;
  private Stage warpStage;
  private Stage uiStage;
  private Stage buildingStage;

  private final Random R = new Random();

  private float tickTime = 0;

  public Set<Sprite> particleSet = new HashSet<Sprite>(); // All movable sprites
  public Set<Sprite> selectedSet = new HashSet<Sprite>(); // Sub-set, selected sprites

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

  public GameScreen getGameScreen() { return theGameScreen; }

  public void act(float delta) {
    tickTime += delta;

    spriteStage.act(delta);
    warpStage.act(delta);
    uiStage.act(delta);
    // Tile stage is static - does not need to be acted


    if (uiMode == UIMode.kPLACE_BUILDING) {
      cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      cursor = Camera.getInstance().unproject(cursor);
      cursor.scl(1f / (float) Param.TILE_S);
      Gdx.app.log("act","Cursor (" + Gdx.input.getX() +"," + Gdx.input.getY() +") -> "  + cursor);
      Tile t = World.getInstance().getTile(cursor.x, cursor.y);
      if (t != null && t.n8 != null && t.n8.get(kSW).n8 != null) {
        buildingLocation = t;
        buildingLocationGood = t.setHighlight(false);
        for (Cardinal D : Cardinal.n8) buildingLocationGood &= t.n8.get(D).setHighlight(false);
        buildingLocationGood &= t.n8.get(kSW).n8.get(kS).setHighlight(true);
        if (buildingLocationGood && OrderlyQueue.canDoSimpleQueue(t.n8.get(kSW).n8.get(kS), false)) {
          OrderlyQueue.canDoSimpleQueue(t.n8.get(kSW).n8.get(kS), true); // doTint
        }
        t.n8.get(kSW).n8.get(kS).setHighlight(false); // Re-apply green tint here
      }
    }


    if (tickTime < 0.22) return; // Tick every second
    tickTime -= 0.22;

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
      Tile tryTile = World.getInstance().getTile(tryX, tryY);
      if (tryTile.getNeighbours().size() == 0) continue; // Non-pathable
      Sprite s = new Sprite(tryX, tryY, tryTile);
      s.moveBy(Param.TILE_S/2, Param.TILE_S/2);
      s.pathTo(tryTile, null, null);
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

  public boolean placeBuilding() {
    if (!buildingLocationGood) return false;
    uiMode = UIMode.kNONE;
    Building b = new Building(buildingLocation);
    b.setTexture("build_3_3", 1, false);
    buildingStage.addActor(b);
    repath();
    return true;
  }

  public void doRightClick() {
    for (Sprite s : selectedSet) s.selected = false;
    selectedSet.clear();
    uiMode = UIMode.kNONE;
  }

  public void doParticleSelect() {
    // TODO fix bounds
    tempRect.set(Math.min(selectStartWorld.x, selectEndWorld.x),
            Math.min(selectStartWorld.y, selectEndWorld.y),
            Math.abs(selectEndWorld.x - selectStartWorld.x),
            Math.abs(selectEndWorld.y - selectStartWorld.y));
    selectedSet.clear();
    for (Sprite s : particleSet) {
      s.selected = tempRect.contains(s.getX() / Param.SPRITE_SCALE, s.getY() / Param.SPRITE_SCALE);
      if (s.selected) selectedSet.add(s);
    }
    selectStartWorld.setZero();
  }

  public void repath() {
    pathingInternal(null, true);
  }

  public void doParticleMoveOrder(int x, int y) {
    Tile target = World.getInstance().getTile(x / Param.TILE_S, y / Param.TILE_S);
    if (target.mySprite != null && target.mySprite.getClass() == Building.class) {
      target = ((Building)target.mySprite).getPathingDestination();
    } else if (target.getPathFindNeighbours().isEmpty()) {
      return; // Cannot path here
    }
    pathingInternal(target, false);
  }

  // Used to send everyone to a particular destination or re-path everyone when the pathing grid mutates
  private void pathingInternal(Tile target, boolean doRepath) {
    Set<Sprite> pathed = new HashSet<Sprite>();
    boolean anotherRoundNeeded;
    int rounds = 0;
    do { // Pathfinding for a group of critters in the same location, caching the path between them.
      ++rounds;
      Set<Sprite> doneSet = new HashSet<Sprite>();
      Set<Tile> solutionKnownFrom = new HashSet<Tile>();
      anotherRoundNeeded = false;
      Sprite firstSprite = null;
      for (Sprite s : particleSet) {
        if (pathed.contains(s)) continue; // Already done in another loop
        if (doRepath) { // DOING REPATH

          if (s.pathingList.isEmpty()) continue;
          if (firstSprite == null) {
            firstSprite = s;
            target = s.getDestination();
          } else {
            if (s.getDestination() != target) {
              anotherRoundNeeded = true;
              continue; // I'm going elsewhere
            }
          }

        } else { // NOT doing repath

          if (!s.selected) continue; // If move order, critter must be selected
          if (firstSprite == null) firstSprite = s;

        }
        if (Math.hypot(s.getX() - firstSprite.getX(), s.getY() - firstSprite.getY()) > Param.TILE_S * Param.WARP_SIZE) { // TODO try and make this larger to improve performance
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

  public Stage getBuildingStage() { return buildingStage; }

  public void reset() {
    if (tileStage != null) tileStage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (uiStage != null) uiStage.dispose();
    if (warpStage != null) warpStage.dispose();
    if (buildingStage != null) buildingStage.dispose();
    tileStage = new Stage(Camera.getInstance().getViewport());
    spriteStage = new Stage(Camera.getInstance().getViewport());
    uiStage = new Stage(Camera.getInstance().getViewport());
    warpStage = new Stage(Camera.getInstance().getViewport());
    buildingStage = new Stage(Camera.getInstance().getViewport());
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
    buildingStage.dispose();;
    ourInstance = null;
  }


}
