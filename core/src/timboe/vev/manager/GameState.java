package timboe.vev.manager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import timboe.vev.VEVGame;
import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Building;
import timboe.vev.entity.Entity;
import timboe.vev.entity.Patch;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.entity.Warp;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Particle;
import timboe.vev.enums.QueueType;
import timboe.vev.enums.UIMode;
import timboe.vev.pathfinding.IVector2;
import timboe.vev.pathfinding.OrderlyQueue;
import timboe.vev.pathfinding.PathingCache;
import timboe.vev.screen.GameScreen;
import timboe.vev.screen.TitleScreen;

import static timboe.vev.enums.Cardinal.kS;
import static timboe.vev.enums.Cardinal.kSW;

public class GameState {


  public final Vector3 selectStartScreen = new Vector3();
  public Vector3 selectStartWorld = new Vector3();
  public final Vector3 selectEndScreen = new Vector3();
  public Vector3 selectEndWorld = new Vector3();
  private Vector3 cursor = new Vector3();

  // Persistent
  private IVector2 placeLocation;
  private boolean buildingLocationGood = false;
  //
  public int queueSize;
  public int nMines;
  public QueueType queueType;
  //
  private float tickTime = 0;
  public float playerEnergy;
  public float warpEnergy;
  private float warpSpawnTime;
  private float newParticlesMean;
  private float newParticlesWidth;
  //
  public int debug;
  public int entitiyID;
  private final HashMap<Integer,Sprite> particleMap = new HashMap<Integer,Sprite>(); // All movable sprites
  private final HashMap<Integer,Building> buildingMap = new HashMap<Integer,Building>(); // All buildings
  private final HashMap<Integer,Entity> buildingExtrasMap = new HashMap<Integer,Entity>(); // All building's extra sprites

  // Transient
  public final Set<Integer> selectedSet = new HashSet<Integer>(); // Sub-set, selected sprites
  public int selectedBuilding = 0;
  public Particle selectedBuildingStandingOrderParticle;
  public BuildingType buildingBeingPlaced = null;
  public boolean doingPlacement = false;

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("placeLocation", placeLocation == null ? JSONObject.NULL : placeLocation.serialise());
    json.put("buildingLocationGood", buildingLocationGood);
    json.put("queueSize", queueSize);
    json.put("nMines", nMines);
    json.put("tickTime", tickTime);
    json.put("playerEnergy", playerEnergy);
    json.put("warpEnergy", warpEnergy);
    json.put("warpSpawnTime", warpSpawnTime);
    json.put("newParticlesMean", newParticlesMean);
    json.put("newParticlesWidth", newParticlesWidth);
    json.put("debug", debug);
    json.put("entityID", entitiyID);
    //
    JSONObject particles = new JSONObject();
    for (Map.Entry<Integer,Sprite> entry : particleMap.entrySet()) {
      particles.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("particleMap", particles);
    //
    JSONObject buildings = new JSONObject();
    for (Map.Entry<Integer,Building> entry : buildingMap.entrySet()) {
      buildings.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("buildingMap", buildings);
    //
    JSONObject buildingExtras = new JSONObject();
    for (Map.Entry<Integer,Entity> entry : buildingExtrasMap.entrySet()) {
      buildingExtras.put(entry.getKey().toString(), entry.getValue().serialise(false));
    }
    json.put("buildingExtrasMap", buildingExtras);
    return json;
  }

  public void deserialise(JSONObject json) throws JSONException {
    if (json.get("placeLocation") == JSONObject.NULL) {
      placeLocation = null;
    } else {
      placeLocation = new IVector2( json.getJSONObject("placeLocation") );
    }
    buildingLocationGood = json.getBoolean("buildingLocationGood");
    queueSize = json.getInt("queueSize");
    nMines = json.getInt("nMines");
    tickTime = (float) json.getDouble("tickTime");
    playerEnergy = (float) json.getDouble("playerEnergy");
    warpEnergy = (float) json.getDouble("warpEnergy");
    warpSpawnTime = (float) json.getDouble("warpSpawnTime");
    newParticlesMean = (float) json.getDouble("newParticlesMean");
    newParticlesWidth = (float) json.getDouble("newParticlesWidth");
    debug = json.getInt("debug");
    entitiyID = json.getInt("entityID");
    //
    JSONObject jsonParticles = json.getJSONObject("particleMap");
    Iterator particlesIt = jsonParticles.keys();
    while (particlesIt.hasNext()) {
      String key = (String) particlesIt.next();
      Sprite s = new Sprite( jsonParticles.getJSONObject( key ) );
      particleMap.put(s.id, s);
      getSpriteStage().addActor(s);
      if (s.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    JSONObject jsonBuildings = json.getJSONObject("buildingMap");
    Iterator buildingsIt = jsonBuildings.keys();
    while (buildingsIt.hasNext()) {
      String key = (String) buildingsIt.next();
      Building b = new Building( jsonBuildings.getJSONObject( key ) );
      buildingMap.put(b.id, b);
      getBuildingStage().addActor(b);
      if (b.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    JSONObject jsonBuildingExtras = json.getJSONObject("buildingExtrasMap");
    Iterator buildingExtrasIt = jsonBuildingExtras.keys();
    while (buildingExtrasIt.hasNext()) {
      String key = (String) buildingExtrasIt.next();
      Entity e = new Entity( jsonBuildingExtras.getJSONObject( key ) );
      buildingExtrasMap.put(e.id, e);
      addBuildingExtraEntity(e);
      if (e.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    for (Building b : buildingMap.values()) {
      if (b.clock != 0) {
        buildingExtrasMap.get( b.clock ).setVisible( b.clockVisible );
      }
    }
  }


  public PathingCache<IVector2> pathingCache = new PathingCache<IVector2>();
  private final Random R = new Random();

  private Stage introTileStage;
  private Stage tileStage;
  private Stage spriteStage;
  private Stage introSpriteStage;
  private Stage introFoliageStage;
  private Stage foliageStage;
  private Stage warpStage;
  private Stage uiStage;
  private Stage buildingStage;

  private static GameState ourInstance = null;
  public static GameState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new GameState(); }
  public static boolean constructed() { return ourInstance != null; }

  private ParticleEffectPool dustEffectPool, upgradeDustEffectPool;
  public Array<ParticleEffectPool.PooledEffect> dustEffects;

  TitleScreen theTitleScreen;
  private GameScreen theGameScreen;
  private VEVGame game;

  public boolean isGameOn() {
    return gameOn;
  }

  private boolean gameOn;

  private GameState() {
    reset(true);
  }

  public void setGame(VEVGame theGame) {
    game = theGame;
    theTitleScreen = new TitleScreen();
    theGameScreen = new GameScreen();
  }

  public GameScreen getGameScreen() { return theGameScreen; }

  public void act(float delta) {

    uiStage.act(delta);

    if (!isGameOn()) {
      introSpriteStage.act(delta);
      return;
    }

    // Tile stage, foliage stage are static - does not need to be acted
    spriteStage.act(delta);
    warpStage.act(delta);
    buildingStage.act(delta);

    if (Param.IS_ANDROID) {
      cursor.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);
    } else {
      cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    }
    cursor = Camera.getInstance().unproject(cursor);
    cursor.scl(1f / (float) Param.TILE_S);
    Tile cursorTile = null;
    if (Util.inBounds((int)cursor.x, (int)cursor.y, false)) cursorTile = World.getInstance().getTile(cursor.x, cursor.y);

    if (doingPlacement) {
      if (UI.getInstance().uiMode == UIMode.kPLACE_BUILDING) {
        if (cursorTile != null
            && cursorTile.n8 != null
            && cursorTile.n8.get(kSW).n8 != null) {
          if (placeLocation != cursorTile.coordinates) Sounds.getInstance().click();
          placeLocation = cursorTile.coordinates;
          buildingLocationGood = cursorTile.setBuildableHighlight();
          for (Cardinal D : Cardinal.n8)
            buildingLocationGood &= cursorTile.n8.get(D).setBuildableHighlight();
          if (buildingBeingPlaced != BuildingType.kMINE) {
            buildingLocationGood &= cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight();
            if (buildingLocationGood) OrderlyQueue.hintQueue(cursorTile.n8.get(kSW).n8.get(kS));
            cursorTile.n8.get(kSW).n8.get(kS).setBuildableHighlight(); // Re-apply green tint here
          }
        }
      } else if (UI.getInstance().uiMode == UIMode.kWITH_BUILDING_SELECTION) {
        if (cursorTile != null) {
          Tile t = mapPathingDestination(cursorTile);
          if (t != null) {
            placeLocation = t.coordinates;
            // Don't allow loop
            if (t.mySprite != 0 && t.mySprite == selectedBuilding) placeLocation = null;
            if (placeLocation != null) {
              Building b = buildingMap.get( selectedBuilding );
              b.updateDemoPathingList(selectedBuildingStandingOrderParticle, t);
            }
          }
        }
      }
    }

    tickTime += delta;
    if (tickTime < warpSpawnTime) return;
    tickTime -= warpSpawnTime;
    if (warpSpawnTime > Param.WARP_SPAWN_TIME_MIN) {
      warpSpawnTime -= Param.WARP_SPAWN_TIME_REDUCTION;
      newParticlesMean += Param.WARP_SPAWN_MEAN_INCREASE;
      newParticlesWidth += Param.WARP_SPAWN_WIDTH_INCREASE;
    }

    tryNewParticles(false);
  }

  public void tryNewParticles(boolean stressTest) {
    // Add a new sprite
    List<Map.Entry<Integer,Warp>> entries = new ArrayList<Map.Entry<Integer,Warp>>(World.getInstance().warps.entrySet());
    Map.Entry<Integer,Warp> rWarp = entries.get( R.nextInt(entries.size()) );
    Warp warp = rWarp.getValue();

    int toPlace = Math.round(
        Util.clamp(newParticlesMean + ((float)R.nextGaussian() * newParticlesWidth), 1, Param.NEW_PARTICLE_MAX)
    );
    if (stressTest) toPlace = 1000000;
    boolean placed = warp.newParticles(toPlace, stressTest);
    Gdx.app.log("act","Warp: SpawnTime: "+warpSpawnTime + " meanP: "
        + newParticlesMean + " widthP: " + newParticlesWidth
        + " place:" + toPlace + " placed:" + placed);

    if (placed) {
      warp.zap.start(); // Lightning
      if (Camera.getInstance().addShake(warp, Param.WARP_SHAKE)) {
        // If did shake, then also do zap
        Sounds.getInstance().zap();
      }
    }
  }

  public void upgradeDustEffect(Tile t) {
    ParticleEffectPool.PooledEffect e = upgradeDustEffectPool.obtain();
    e.setPosition(t.centreScaleTile.x, t.centreScaleTile.y);
    dustEffects.add(e);
  }

  public void dustEffect(Tile t) {
    ParticleEffectPool.PooledEffect e = dustEffectPool.obtain();
    e.setPosition(t.centreScaleTile.x, t.centreScaleTile.y);
    dustEffects.add(e);
  }

  public void killSprite(Sprite s) {
    particleMap.remove(s.id);
    selectedSet.remove(s.id);
    dustEffect(s.getTile());
    s.remove(); // From its renderer
  }

  public void addSprite(Sprite s) {
    spriteStage.addActor(s);
    particleMap.put(s.id, s);
    dustEffect( s.getTile() );
    if (Camera.getInstance().onScreen(s)) Sounds.getInstance().boop();
  }

  public void addBuildingExtraEntity(Entity e) {
    buildingExtrasMap.put(e.id, e);
    if (e.texString.equals("clock") || e.texString.equals("board_vertical")) {
      buildingStage.addActor(e);
    } else if (e.texString.contains("ball_")) {
      spriteStage.addActor(e);
    } else {
      Gdx.app.log("addBuildingExtraEntity","Do no know about:" + e.texString);
    }
  }

  public HashMap<Integer, Entity> getBuildingExtrasMap() {
    return buildingExtrasMap;
  }

  public boolean isSelecting() {
    if (Param.IS_ANDROID) {
      return  UI.getInstance().selectParticlesButton.isChecked();
    } else {
      return (!doingPlacement && selectStartWorld.dst(selectEndWorld) > 6);
    }
  }

  public boolean startSelectingAndroid() {
    if (doingPlacement) {
      UI.getInstance().selectParticlesButton.setChecked(false);
      return false; // Cannot do selection
    }
    UI.getInstance().selectParticlesButton.setChecked(true);
    selectEndWorld.setZero();
    selectStartWorld.setZero();
    return true;
  }

  public void placeBuilding() {
    if (!buildingLocationGood || placeLocation == null) return;
    Building b = new Building(World.getInstance().getTile(placeLocation), buildingBeingPlaced);
    buildingStage.addActor(b);
    buildingMap.put(b.id, b);
    playerEnergy += buildingBeingPlaced.getCost();
    if (buildingBeingPlaced == BuildingType.kMINE) ++nMines;
    Camera.getInstance().addShake(Param.BUILDING_SHAKE);
    Sounds.getInstance().thud();
    Sounds.getInstance().OK();
    repath();
    UI.getInstance().showMain();
  }

  public void doRightClick() {
    if (!gameOn) { // Canceling settings window
      UI.getInstance().resetTitle("main");
      return;
    }
    if (selectedSet.size() > 0 || selectedBuilding != 0) {
      clearSelect();
    }
    selectEndWorld.setZero();
    selectStartWorld.setZero();
    UI.getInstance().showMain();
  }

  public void doConfirmStandingOrder() {
    Building b = buildingMap.get( selectedBuilding );
    BuildingType bt = b.getType();
    boolean didSave = b.savePathingList(); // Save the pathing list
    if (!didSave) return;
    doingPlacement = false;
    Sounds.getInstance().OK();
    // Set all buttons to false
    for (Particle p : Particle.values()) {
      if (!UI.getInstance().buildingSelectStandingOrder.get(bt).containsKey(p)) continue;
      UI.getInstance().buildingSelectStandingOrder.get(bt).get(p).setChecked(false);
    }
  }


  public void reduceSelectedSet(Particle p, boolean invert) {
    Set<Integer> toRemove = new HashSet<Integer>();
    for (int sID : selectedSet) {
      Sprite s = particleMap.get(sID);
      boolean removeMe = (s.getParticle() != p);
      if (invert) removeMe = !removeMe;
      if (removeMe) {
        toRemove.add(sID);
        s.selected = false;
      }
    }
    selectedSet.removeAll(toRemove);
    if (selectedSet.isEmpty()) UI.getInstance().showMain();
    else UI.getInstance().doSelectParticle(selectedSet);
  }

  private void clearSelect() {
    for (int sID : selectedSet) {
      Sprite s = particleMap.get(sID);
      s.selected = false;
    }
    selectedSet.clear();
    if (selectedBuilding != 0) {
      Building b = buildingMap.get(selectedBuilding);
      b.selected = false;
    }
    selectedBuilding = 0;
  }

  public boolean doParticleSelect(boolean rangeBased) {

    if (rangeBased) {

      clearSelect();
      Rectangle.tmp.set(Math.min(selectStartWorld.x, selectEndWorld.x),
          Math.min(selectStartWorld.y, selectEndWorld.y),
          Math.abs(selectEndWorld.x - selectStartWorld.x),
          Math.abs(selectEndWorld.y - selectStartWorld.y));
      for (Sprite s : particleMap.values()) {
        s.selected = Rectangle.tmp.contains(s.getX() / Param.SPRITE_SCALE, s.getY() / Param.SPRITE_SCALE);
        if (s.selected) selectedSet.add(s.id);
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

      for (Building b : buildingMap.values()) {
        Rectangle.tmp.set(b.getX(), b.getY(), b.getWidth(), b.getHeight());
        if (Rectangle.tmp.contains(selectStartWorld.x, selectStartWorld.y)) {
          clearSelect();
          b.selected = true;
          selectedBuilding = b.id;
          UI.getInstance().showBuildingInfo(b);
          return true;
        }
      }
      for (Sprite s : particleMap.values()) {
        Rectangle.tmp.set(s.getX(), s.getY(), s.getWidth(), s.getHeight());
        if (Rectangle.tmp.contains(selectStartWorld.x * Param.SPRITE_SCALE, selectStartWorld.y * Param.SPRITE_SCALE)) {
          clearSelect();
          s.selected = true;
          selectedSet.add(s.id);
          UI.getInstance().doSelectParticle(selectedSet);
          return true;
        }
      }

    }
    return false;
  }

  private void repath() {
    pathingCache.clear();
    pathingInternal(null, true);
  }

  public Tile mapPathingDestination(Tile target) {
//    Gdx.app.log("mapPathingDestination","For "+target.coordinates.toString());
    Entity targetSprite = target.getMySprite();
    if (targetSprite != null && targetSprite.getClass() == Building.class) {
      return ((Building)targetSprite).getQueuePathingTarget();
    } else if (target.getPathFindNeighbours().isEmpty()) {
      return null; // Cannot path here
    }
    return target;
  }

  public void retextureSprites() {
    for (Actor a : spriteStage.getActors()) {
      ((Entity)a).loadTexture();
    }
    for (Actor a : introSpriteStage.getActors()) {
      ((Entity)a).loadTexture();
    }
  }

  public void doParticleMoveOrder(int x, int y) {
    if (!Util.inBounds(x / Param.TILE_S, y / Param.TILE_S, false)) return;
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
      Set<IVector2> solutionKnownFrom = new HashSet<IVector2>();
      anotherRoundNeeded = false;
      Sprite firstSprite = null;
      for (Sprite s : particleMap.values()) {
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
          // TODO - cancel existing pathing on these fellas?
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
    if (!doRepath && pathed.size() > 0) {
      Sounds.getInstance().moveOrder();
      Sounds.getInstance().OK();
    }

    if (doRepath) {
      for (Building b : buildingMap.values()) b.doRepath();
    }
  }


  public void transitionToGameScreen() {
    theTitleScreen.fadeTimer = 1f;
    Sounds.getInstance().pulse();
  }

  public void setToTitleScreen() {
    pathingCache.clear();
    UI.getInstance().resetTitle("main");
    game.setScreen(theTitleScreen);
    setGameOn(false);
  }

  public void setToGameScreen() {
    pathingCache.clear();
    UI.getInstance().resetGame();
    theGameScreen.setMultiplexerInputs();
    theGameScreen.fadeIn = 100f;
    Actor toFocusOn = warpStage.getActors().first();
    Camera.getInstance().setCurrentPos(
        toFocusOn.getX() + (Param.WARP_SIZE/2 * Param.TILE_S),
        toFocusOn.getY() + (Param.WARP_SIZE/2 * Param.TILE_S));
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

  public Stage getIntroSpriteStage() {
    return introSpriteStage;
  }

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
      if (uiStage != null) uiStage.dispose();
      introTileStage = new Stage(Camera.getInstance().getTileViewport());
      introSpriteStage = new Stage(Camera.getInstance().getSpriteViewport());
      introFoliageStage = new Stage(Camera.getInstance().getSpriteViewport());
      uiStage = new Stage(Camera.getInstance().getUiViewport());

    }
    if (tileStage != null) tileStage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (foliageStage != null) foliageStage.dispose();
    if (warpStage != null) warpStage.dispose();
    if (buildingStage != null) buildingStage.dispose();
    tileStage = new Stage(Camera.getInstance().getTileViewport());
    spriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    foliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    warpStage = new Stage(Camera.getInstance().getTileViewport());
    buildingStage = new Stage(Camera.getInstance().getTileViewport());
    Color warpStageC = warpStage.getBatch().getColor();
    warpStageC.a = Param.WARP_TRANSPARENCY;
    warpStage.getBatch().setColor(warpStageC);
    particleMap.clear();
    buildingMap.clear();
    dustEffects = new Array<ParticleEffectPool.PooledEffect>();
    ParticleEffect dustEffect = new ParticleEffect();
    dustEffect.load(Gdx.files.internal("dust_effect.txt"), Textures.getInstance().getAtlas());
    ParticleEffect upgradeDustEffect = new ParticleEffect();
    upgradeDustEffect.load(Gdx.files.internal("upgrade_dust_effect.txt"), Textures.getInstance().getAtlas());
    dustEffectPool = new ParticleEffectPool(dustEffect, 10, 100);
    upgradeDustEffectPool = new ParticleEffectPool(upgradeDustEffect, 10, 150);
    playerEnergy = Param.PLAYER_STARTING_ENERGY;
    warpEnergy = Param.WARP_STARTING_ENERGY;
    debug = Param.DEBUG_INTIAL;
    queueType = Param.QUEUE_INITIAL_TYPE;
    queueSize = Param.QUEUE_INITIAL_SIZE;
    warpSpawnTime = Param.WARP_SPAWN_TIME_INITIAL;
    newParticlesMean = Param.NEW_PARTICLE_MEAN;
    newParticlesWidth = Param.NEW_PARTICLE_WIDTH;
    pathingCache.clear();
    nMines = 0;
    entitiyID = 0;
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
    introSpriteStage.dispose();
    foliageStage.dispose();
    introFoliageStage.dispose();
    uiStage.dispose();
    warpStage.dispose();
    buildingStage.dispose();
    ourInstance = null;
  }


  public HashMap<Integer, Sprite> getParticleMap() {
    return particleMap;
  }

  public HashMap<Integer, Building> getBuildingMap() {
    return buildingMap;
  }
}
