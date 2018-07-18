package timboe.destructor.manager;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.sun.org.apache.xpath.internal.operations.And;

import sun.security.krb5.internal.PAData;
import timboe.destructor.DestructorGame;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.entity.Warp;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Particle;
import timboe.destructor.enums.QueueType;
import timboe.destructor.enums.UIMode;
import timboe.destructor.pathfinding.OrderlyQueue;
import timboe.destructor.pathfinding.PathFinding;
import timboe.destructor.pathfinding.PathingCache;
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

  private Stage introTileStage;
  private Stage tileStage;
  private Stage spriteStage;
  private Stage introSpriteStage;
  private Stage introFoliageStage;
  private Stage foliageStage;
  private Stage warpStage;
  private Stage uiStage;
  private Stage buildingStage;

  public int queueSize;
  public QueueType queueType;

  private final Random R = new Random();

  private float tickTime = 0;

  public float playerEnergy;
  public float warpEnergy;

  private float warpSpawnTime;
  private float newParticlesMean;
  private float newParticlesWidth;

  public int debug;

  public final Set<Sprite> particleSet = new HashSet<Sprite>(); // All movable sprites
  public final Set<Building> buildingSet = new HashSet<Building>(); // All buildings

  public final Set<Sprite> selectedSet = new HashSet<Sprite>(); // Sub-set, selected sprites
  public Building selectedBuilding = null;

  public PathingCache<Tile> pathingCache = new PathingCache<Tile>();

  private static GameState ourInstance;
  public static GameState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new GameState(); }

  public ParticleEffectPool dustEffectPool;
  public Array<ParticleEffectPool.PooledEffect> dustEffects;

  private TitleScreen theTitleScreen;
  private GameScreen theGameScreen;
  private DestructorGame game;

  private boolean gameOn;

  private GameState() {
    reset(true);
  }

  public void setGame(DestructorGame theGame) {
    game = theGame;
    theTitleScreen = new TitleScreen();
    theGameScreen = new GameScreen();
  }

  public GameScreen getGameScreen() { return theGameScreen; }

  public void act(float delta) {
    tickTime += delta;

    if (!gameOn) {
      introSpriteStage.act(delta);
      return;
    }

    // Tile stage, foliage stage are static - does not need to be acted
    spriteStage.act(delta);
    warpStage.act(delta);
    uiStage.act(delta);
    buildingStage.act(delta);

    if (Param.IS_ANDROID) {
      cursor.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);
    } else {
      cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    }
    cursor = Camera.getInstance().unproject(cursor);
    cursor.scl(1f / (float) Param.TILE_S);
    Tile cursorTile = null;
    if (Util.inBounds((int)cursor.x, (int)cursor.y)) cursorTile = World.getInstance().getTile(cursor.x, cursor.y);

    if (UI.getInstance().doingPlacement) {
      if (UI.getInstance().uiMode == UIMode.kPLACE_BUILDING) {
        if (cursorTile != null && cursorTile.n8 != null && cursorTile.n8.get(kSW).n8 != null) {
          placeLocation = cursorTile;
          buildingLocationGood = cursorTile.setBuildableHighlight();
          for (Cardinal D : Cardinal.n8)
            buildingLocationGood &= cursorTile.n8.get(D).setBuildableHighlight();
          if (UI.getInstance().buildingBeingPlaced != BuildingType.kMINE) {
            buildingLocationGood &= cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight();
            if (buildingLocationGood) OrderlyQueue.hintQueue(cursorTile.n8.get(kSW).n8.get(kS));
            cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight(); // Re-apply green tint here
          }
        }
      } else if (UI.getInstance().uiMode == UIMode.kWITH_BUILDING_SELECTION) {
        if (cursorTile != null) {
          placeLocation = mapPathingDestination(cursorTile);
          if (placeLocation != null) {
            UI.getInstance().selectedBuilding.updateDemoPathingList(UI.getInstance().selectedBuildingStandingOrderParticle, placeLocation);
          }
        }
      }
    }

    if (tickTime < warpSpawnTime) return;
    tickTime -= warpSpawnTime;
    if (warpSpawnTime > Param.WARP_SPAWN_TIME_MIN) {
      warpSpawnTime -= Param.WARP_SPAWN_TIME_REDUCTION;
      newParticlesMean += Param.WARP_SPAWN_MEAN_INCREASE;
      newParticlesWidth += Param.WARP_SPAWN_WIDTH_INCREASE;
      Gdx.app.log("act","Warp: new SpawnTime: "+warpSpawnTime + " meanP: " + newParticlesMean + " widthP: " + newParticlesWidth);
    }

    tryNewParticles(false);
  }

  public void tryNewParticles(boolean stressTest) {
    // Add a new sprite
    List<Map.Entry<Warp,ParticleEffect>> entries = new ArrayList<Map.Entry<Warp,ParticleEffect>>(World.getInstance().warps.entrySet());
    Map.Entry<Warp,ParticleEffect> rWarp = entries.get( R.nextInt(entries.size()) );
    Warp warp = rWarp.getKey();

    int toPlace = Math.round(Util.clamp(newParticlesMean + ((float)R.nextGaussian() * newParticlesWidth), 1, Param.NEW_PARTICLE_MAX));
    if (stressTest) toPlace = 100000;
    boolean placed = warp.newParticles(toPlace);

    if (placed) {
      rWarp.getValue().start();
      Rectangle.tmp.set((warp.coordinates.x - Param.WARP_SIZE / 2) * Param.TILE_S,
          (warp.coordinates.y - Param.WARP_SIZE / 2) * Param.TILE_S,
          Param.WARP_SIZE * Param.TILE_S, Param.WARP_SIZE * Param.TILE_S);
      Camera.getInstance().addShake(Rectangle.tmp, Param.WARP_SHAKE);
    }
  }

  public void dustEffect(Tile t) {
    ParticleEffectPool.PooledEffect e = dustEffectPool.obtain();
    e.setPosition(t.centreScaleTile.x, t.centreScaleTile.y);
    dustEffects.add(e);
  }

  public void killSprite(Sprite s) {
    particleSet.remove(s);
    selectedSet.remove(s);
    dustEffect(s.myTile);
    s.remove(); // From its renderer
  }

  public boolean isSelecting() {
    if (Param.IS_ANDROID) {
      return  UI.getInstance().selectParticlesButton.isChecked();
    } else {
      return (!UI.getInstance().doingPlacement && selectStartWorld.dst(selectEndWorld) > 6);
    }
  }

  public boolean startSelectingAndroid() {
    if (UI.getInstance().doingPlacement) {
      UI.getInstance().selectParticlesButton.setChecked(false);
      return false; // Cannot do selection
    }
    UI.getInstance().selectParticlesButton.setChecked(true);
    selectEndWorld.setZero();
    selectStartWorld.setZero();
    return true;
  }

  public void placeBuilding() {
    if (!buildingLocationGood) return;
    Building b = new Building(placeLocation, UI.getInstance().buildingBeingPlaced);
    buildingStage.addActor(b);
    buildingSet.add(b);
    playerEnergy += UI.getInstance().buildingBeingPlaced.getCost();
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
    UI.getInstance().selectedBuilding.savePathingList(); // Save the pathing list
    UI.getInstance().doingPlacement = false;
    // Set all buttons to false
    for (Particle p : Particle.values()) {
      if (!UI.getInstance().buildingSelectStandingOrder.get(bt).containsKey(p)) continue;
      UI.getInstance().buildingSelectStandingOrder.get(bt).get(p).setChecked(false);
    }
  }


  public void reduceSelectedSet(Particle p, boolean invert) {
    Set<Sprite> toRemove = new HashSet<Sprite>();
    for (Sprite s : selectedSet) {
      boolean removeMe = (s.getParticle() != p);
      if (invert) removeMe = !removeMe;
      if (removeMe) {
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
      UI.getInstance().selectParticlesButton.setChecked(false);
      return !selectedSet.isEmpty();

    } else if (selectedSet.isEmpty()) {
      // We only let a building or individual particle be selected if no particles are selected
      // if particles are selected then we want to path to the building/location

      for (Building b : buildingSet) {
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

    }
    return false;
  }

  public void repath() {
    pathingCache.clear();
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
          if (Math.hypot(s.getX() - firstSprite.getX(), s.getY() - firstSprite.getY()) > Param.TILE_S * Param.WARP_SIZE) { // TODO try and make this larger to improve performance
            // Do this (and any others far away or different target) in another iteration of the do loop
            anotherRoundNeeded = true;
            continue;
          }

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

    if (doRepath) {
      for (Building b : buildingSet) b.doRepath();
    }
  }


  public void setToTitleScreen() {
    game.setScreen(theTitleScreen);
  }

  public void setToGameScreen() {
    UI.getInstance().reset();
    theGameScreen.setMultiplexerInputs();
    game.setScreen(theGameScreen);
    setGameOn(true);
  }

  public Stage getTileStage() {
    return tileStage;
  }

  public Stage getIntroTileStage() {
    return introTileStage;
  }

  public Stage getUIStage() {
    return uiStage;
  }

  public Stage getWarpStage() { return warpStage; }

  public Stage getSpriteStage() {
    return spriteStage;
  }

  public Stage getFoliageStage() { return foliageStage; }

  public Stage getIntroFoliageStage() { return introFoliageStage; }

  public Stage getBuildingStage() { return buildingStage; }

  public void reset(boolean includingIntro) {
    if (includingIntro) {
      if (introTileStage != null) introTileStage.dispose();
      if (introFoliageStage != null) introFoliageStage.dispose();
      if (introSpriteStage != null) introSpriteStage.dispose();
      introTileStage = new Stage(Camera.getInstance().getTileViewport());
      introSpriteStage = new Stage(Camera.getInstance().getSpriteViewport());
      introFoliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    }
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
    dustEffects = new Array<ParticleEffectPool.PooledEffect>();
    ParticleEffect dustEffect = new ParticleEffect();
    dustEffect.load(Gdx.files.internal("dust_effect.txt"), Textures.getInstance().getAtlas());
    dustEffectPool = new ParticleEffectPool(dustEffect, 10, 100);
    playerEnergy = Param.PLAYER_STARTING_ENERGY;
    warpEnergy = Param.WARP_STARTING_ENERGY;
    debug = Param.DEBUG_INTIAL;
    queueType = Param.QUEUE_INITIAL_TYPE;
    queueSize = Param.QUEUE_INITIAL_SIZE;
    warpSpawnTime = Param.WARP_SPAWN_TIME_INITIAL;
    newParticlesMean = Param.NEW_PARTICLE_MEAN;
    newParticlesWidth = Param.NEW_PARTICLE_WIDTH;
    pathingCache.clear();
    gameOn = false;
  }

  public void setGameOn(boolean gameOn) {
    this.gameOn = gameOn;
  }

  public void dispose() {
    theGameScreen.dispose();
    theTitleScreen.dispose();
    introTileStage.dispose();
    tileStage.dispose();
    spriteStage.dispose();
    foliageStage.dispose();
    introFoliageStage.dispose();
    uiStage.dispose();
    warpStage.dispose();
    buildingStage.dispose();
    ourInstance = null;
  }


  public Set<Sprite> getParticleSet() {
    return particleSet;
  }
}
