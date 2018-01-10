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
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.UIMode;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.pathfinding.OrderlyQueue;
import timboe.destructor.screen.GameScreen;
import timboe.destructor.screen.TitleScreen;

import java.util.*;

import static timboe.destructor.enums.Cardinal.kS;
import static timboe.destructor.enums.Cardinal.kSW;

public class GameState {

  // UI interactions

  public final Vector3 selectStartScreen = new Vector3();
  public Vector3 selectStartWorld = new Vector3();
  public final Vector3 selectEndScreen = new Vector3();
  public Vector3 selectEndWorld = new Vector3();

  public Vector3 cursor = new Vector3();

  private boolean buildingLocationGood = false;

  private Tile placeLocation;

  private Stage tileStage;
  private Stage spriteStage;
  private Stage foliageStage;
  private Stage warpStage;
  private Stage uiStage;
  private Stage buildingStage;

  private final Random R = new Random();

  private float tickTime = 0;

  public final Set<Sprite> particleSet = new HashSet<Sprite>(); // All movable sprites
  public final Set<Building> buildingSet = new HashSet<Building>(); // All buildings

  public final Set<Sprite> selectedSet = new HashSet<Sprite>(); // Sub-set, selected sprites
  public Building selectedBuilding = null;

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
    buildingStage.act(delta);
    // Tile stage is static - does not need to be acted

    cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    cursor = Camera.getInstance().unproject(cursor);
    cursor.scl(1f / (float) Param.TILE_S);
    Tile cursorTile = null;
    if (Util.inBounds((int)cursor.x, (int)cursor.y)) cursorTile = World.getInstance().getTile(cursor.x, cursor.y);

    if (UI.getInstance().doingPlacement) {
      if (UI.getInstance().uiMode == UIMode.kPLACE_BUILDING) {
        if (cursorTile != null && cursorTile.n8 != null && cursorTile.n8.get(kSW).n8 != null) {
          placeLocation = cursorTile;
          buildingLocationGood = cursorTile.setBuildableHighlight();
          for (Cardinal D : Cardinal.n8) buildingLocationGood &= cursorTile.n8.get(D).setBuildableHighlight();
          buildingLocationGood &= cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight();
          if (buildingLocationGood) OrderlyQueue.hintQueue(cursorTile.n8.get(kSW).n8.get(kS));
          cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight(); // Re-apply green tint here
        }
      } else if (UI.getInstance().uiMode == UIMode.kWITH_BUILDING_SELECTION) {
        if (cursorTile != null && !cursorTile.getPathFindNeighbours().isEmpty()) {
          placeLocation = mapPathingDestination(cursorTile); //TODO this is broken? why?
          if (placeLocation != null) {
            UI.getInstance().selectedBuilding.updateDemoPathingList(UI.getInstance().selectedBuildingStandingOrderParticle, placeLocation);
          }
        }
      }
    }

    if (tickTime < 5) return; // Tick every second
    tickTime -= 5;

    tryNewParticles(false);

  }

  public void tryNewParticles(boolean stressTest) {
    // Add a new sprite
    List<Map.Entry<IVector2,ParticleEffect>> entries = new ArrayList<Map.Entry<IVector2,ParticleEffect>>(World.getInstance().warps.entrySet());
    Map.Entry<IVector2,ParticleEffect> rWarp = entries.get( R.nextInt(entries.size()) );
    IVector2 warp = rWarp.getKey();
    rWarp.getValue().start();
    Rectangle.tmp.set((warp.x - Param.WARP_SIZE / 2) * Param.TILE_S,
        (warp.y - Param.WARP_SIZE / 2) * Param.TILE_S,
        Param.WARP_SIZE * Param.TILE_S, Param.WARP_SIZE * Param.TILE_S);
    if (Camera.getInstance().onScrean(Rectangle.tmp)) Camera.getInstance().addShake(5f);

    int toPlace = Math.round(Util.clamp(Param.NEW_PARTICLE_MEAN + ((float)R.nextGaussian() * Param.NEW_PARTICLE_WIDTH), 1, Param.NEW_PARTICLE_MAX));
    if (stressTest) toPlace = 100000;
    for (int tp = 0; tp < toPlace; ++tp) {
      int placeTry = 0;
      do {
        double rAngle = -Math.PI + (R.nextFloat() * Math.PI * 2);
        int tryX = (int) Math.round(warp.x + (2 * Param.WARP_SIZE / 3 * Math.cos(rAngle)));
        int tryY = (int) Math.round(warp.y + (2 * Param.WARP_SIZE / 3 * Math.sin(rAngle)));
        Tile tryTile = World.getInstance().getTile(tryX, tryY);
        if (tryTile.getNeighbours().size() == 0) continue; // Non-pathable
        Sprite s = new Sprite(tryX, tryY, tryTile);
        s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);
        s.pathTo(tryTile, null, null);
        Colour c = Colour.random();
        s.setTexture("ball_" + c.getString(), 6, false);
        s.setUserObject(c);
        spriteStage.addActor(s);
        particleSet.add(s);
        break;
      } while (++placeTry < Param.N_PATCH_TRIES);
    }
  }

  public void killSprite(Sprite s) {
    particleSet.remove(s);
    selectedSet.remove(s);
    s.remove(); // From its renderer
  }

  public boolean isSelecting() {
    UIMode mode = UI.getInstance().uiMode;
    if ((mode == UIMode.kNONE || mode == UIMode.kWITH_PARTICLE_SELECTION) && selectStartWorld.dst(selectEndWorld) > 6) {
      UI.getInstance().uiMode = UIMode.kMAKING_SELECTION;
      UI.getInstance().selectParticlesButton.setChecked( true );
    }
    return (UI.getInstance().uiMode == UIMode.kMAKING_SELECTION);
  }

  public void placeBuilding() {
    if (!buildingLocationGood) return;
    Building b = new Building(placeLocation, UI.getInstance().buildingBeingPlaced);
    b.setTexture("build_3_3", 1, false);
    buildingStage.addActor(b);
    buildingSet.add(b);
    repath();
    UI.getInstance().showMain();
  }

  public void doRightClick() {
    if (selectedSet.size() > 0 || selectedBuilding != null) {
      clearSelect();
    }
    selectEndWorld.setZero();
    selectStartWorld.setZero();
    UI.getInstance().showMain();
  }

  public void doConfirmStandingOrder() {
    BuildingType bt = UI.getInstance().selectedBuilding.getType();
    UI.getInstance().selectedBuilding.updatePathingList(); // Save the pathing list
    UI.getInstance().doingPlacement = false;
    // Set all buttons to false
    for (Particle p : Particle.values()) {
      if (!UI.getInstance().buildingSelectStandingOrder.get(bt).containsKey(p)) continue;
      UI.getInstance().buildingSelectStandingOrder.get(bt).get(p).setChecked(false);
    }
  }


  public void reduceSelectedSet() {
    Set<Sprite> toRemove = new HashSet<Sprite>();
    for (Sprite s : selectedSet) {
      if (!UI.getInstance().selectButton.get( s.getUserObject() ).isChecked()) {
        toRemove.add(s);
        s.selected = false;
      }
    }
    selectedSet.removeAll(toRemove);
    if (selectedSet.isEmpty()) UI.getInstance().showMain();
    else UI.getInstance().doSelectParticle(selectedSet);
  }

  private void clearSelect() {
    for (Sprite s : selectedSet) s.selected = false;
    selectedSet.clear();
    if (selectedBuilding != null) selectedBuilding.selected = false;
    selectedBuilding = null;
  }

  public boolean doParticleSelect(boolean rangeBased) {

    if (rangeBased) {

      clearSelect();
      Rectangle.tmp.set(Math.min(selectStartWorld.x, selectEndWorld.x),
          Math.min(selectStartWorld.y, selectEndWorld.y),
          Math.abs(selectEndWorld.x - selectStartWorld.x),
          Math.abs(selectEndWorld.y - selectStartWorld.y));
      for (Sprite s : particleSet) {
        s.selected = Rectangle.tmp.contains(s.getX() / Param.SPRITE_SCALE, s.getY() / Param.SPRITE_SCALE);
        if (s.selected) selectedSet.add(s);
      }
      UI.getInstance().uiMode = UIMode.kNONE; // Remove "selecting"
      UI.getInstance().selectParticlesButton.setChecked( false );
      if (!selectedSet.isEmpty()) UI.getInstance().doSelectParticle(selectedSet);
      selectStartWorld.setZero();
      selectEndWorld.setZero();
      return !selectedSet.isEmpty();

    } else {

      for (Building b : buildingSet) {
        // We only let a building be selected if no particles are selected
        // if particles are selected then we want to path to the building
        if (!selectedSet.isEmpty()) break;
        Rectangle.tmp.set(b.getX(), b.getY(), b.getWidth(), b.getHeight());
        if (Rectangle.tmp.contains(selectStartWorld.x, selectStartWorld.y)) {
          clearSelect();
          b.selected = true;
          selectedBuilding = b;
          UI.getInstance().showBuildingInfo(b);
          return true;
        }
      }
      for (Sprite s : particleSet) {
        Rectangle.tmp.set(s.getX(), s.getY(), s.getWidth(), s.getHeight());
        if (Rectangle.tmp.contains(selectStartWorld.x * Param.SPRITE_SCALE, selectStartWorld.y * Param.SPRITE_SCALE)) {
          clearSelect();
          s.selected = true;
          selectedSet.add(s);
          UI.getInstance().doSelectParticle(selectedSet);
          return true;
        }
      }
      return false;

    }
  }

  public void repath() {
    pathingInternal(null, true);
  }

  public Tile mapPathingDestination(Tile target) {
    if (target.mySprite != null && target.mySprite.getClass() == Building.class) {
      return ((Building)target.mySprite).getQueuePathingTarget();
    } else if (target.getPathFindNeighbours().isEmpty()) {
      return null; // Cannot path here
    }
    return target;
  }

  public void doParticleMoveOrder(int x, int y) {
    if (!Util.inBounds(x / Param.TILE_S, y / Param.TILE_S)) return;
    Tile target = World.getInstance().getTile(x / Param.TILE_S, y / Param.TILE_S);
    target = mapPathingDestination(target);
    if (target == null) return;
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

          if (s.getPathingList() == null || s.getPathingList().isEmpty()) continue;
          if (firstSprite == null) {
            firstSprite = s;
            target = s.getDestination();
            // Note - if this turns out to not be valid anymore, pathTo will choose a new one
            // in a reproducible way. So we can continue to use "target" here, even though everyone's
            // target will mutate to a new location
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

        if (s != firstSprite && firstSprite.getPathingList() == null) { // Pathing failed for the firstSprite :(
          // Hence it will (very likley) fail here too. And failed pathing is EXPENSIVE
          // So don't run it.
          pathed.add(s);
          continue;
        }

        s.pathTo(target, solutionKnownFrom, doneSet);
        pathed.add(s);
        if (s.getPathingList() != null) {
          doneSet.add(s);
          solutionKnownFrom.addAll(s.getPathingList());
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

  public Stage getFoliageStage() { return foliageStage; }

  public Stage getBuildingStage() { return buildingStage; }

  public void reset() {
    if (tileStage != null) tileStage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (foliageStage != null) foliageStage.dispose();
    if (uiStage != null) uiStage.dispose();
    if (warpStage != null) warpStage.dispose();
    if (buildingStage != null) buildingStage.dispose();
    tileStage = new Stage(Camera.getInstance().getTileViewport());
    spriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    foliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    uiStage = new Stage(Camera.getInstance().getUiViewport());
    warpStage = new Stage(Camera.getInstance().getTileViewport());
    buildingStage = new Stage(Camera.getInstance().getTileViewport());
    Color warpStageC = warpStage.getBatch().getColor();
    warpStageC.a = Param.WARP_TRANSPARENCY;
    warpStage.getBatch().setColor(warpStageC);
    particleSet.clear();
    buildingSet.clear();
  }

  public void dispose() {
    theGameScreen.dispose();
    theTitleScreen.dispose();
    tileStage.dispose();
    spriteStage.dispose();
    foliageStage.dispose();
    uiStage.dispose();
    warpStage.dispose();
    buildingStage.dispose();
    ourInstance = null;
  }


}
