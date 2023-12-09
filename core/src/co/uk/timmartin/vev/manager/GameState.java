package co.uk.timmartin.vev.manager;


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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.entity.Building;
import co.uk.timmartin.vev.entity.Entity;
import co.uk.timmartin.vev.entity.Firework;
import co.uk.timmartin.vev.entity.Sprite;
import co.uk.timmartin.vev.entity.Tile;
import co.uk.timmartin.vev.entity.Truck;
import co.uk.timmartin.vev.entity.Warp;
import co.uk.timmartin.vev.enums.BuildingType;
import co.uk.timmartin.vev.enums.Cardinal;
import co.uk.timmartin.vev.enums.FSM;
import co.uk.timmartin.vev.enums.Particle;
import co.uk.timmartin.vev.enums.QueueType;
import co.uk.timmartin.vev.enums.UIMode;
import co.uk.timmartin.vev.pathfinding.IVector2;
import co.uk.timmartin.vev.pathfinding.OrderlyQueue;
import co.uk.timmartin.vev.pathfinding.PathingCache;

import static co.uk.timmartin.vev.enums.Cardinal.kNONE;
import static co.uk.timmartin.vev.enums.Cardinal.kS;
import static co.uk.timmartin.vev.enums.Cardinal.kSW;

public class GameState {


  public Vector3 selectStartWorld = new Vector3();
  public Vector3 selectEndWorld = new Vector3();
  private Vector3 cursor = new Vector3();

  // Persistent
  private IVector2 placeLocation;
  private boolean buildingLocationGood = false;
  //
  public int queueSize;
  public QueueType queueType;
  //
  private float tickTime = 0;
  public Warp triggerSpawn; // Used to trigger a new wave now
  public float gameTime;
  public int difficulty;
  public int playerEnergy;
  public int playerTotalEnergy;
  public int inWorldParticles;
  public int warpParticles;
  protected float warpSpawnTime;
  protected float newParticlesMean;
  protected float newParticlesWidth;
  public int buildingsBuilt;
  public int buildingsDemolished;
  public int particlesDeconstructed;
  public int particleBounces;
  public int treesBulldozed;
  public int tiberiumMined;
  public int upgradesPurchased;
  public int taps;
  private boolean shownMidGame;
  public boolean cht;
  //
  public int debug;
  public boolean uiOn;
  public int entityID;
  private final HashMap<Integer, Sprite> particleMap = new HashMap<Integer, Sprite>(); // All movable sprites (exc. trucks)
  private final HashMap<Integer, Building> buildingMap = new HashMap<Integer, Building>(); // All buildings
  private final HashMap<Integer, Entity> buildingExtrasMap = new HashMap<Integer, Entity>(); // All building's extra sprites
  private final HashMap<Integer, Truck> trucksMap = new HashMap<Integer, Truck>(); // All Trucks
  private final HashMap<Integer, Warp> warpMap = new HashMap<Integer, Warp>(); // All warps

  // Transient
  public final Set<Integer> selectedSet = new HashSet<Integer>(); // Sub-set, selected sprites
  public int selectedBuilding = 0;
  public Particle selectedBuildingStandingOrderParticle;
  public BuildingType buildingBeingPlaced = null;
  public boolean doingPlacement = false;
  private EnumMap<BuildingType, Integer> buildingPrices = new EnumMap<BuildingType, Integer>(BuildingType.class);
  private EnumMap<BuildingType, Integer> buildingQueuePrices = new EnumMap<BuildingType, Integer>(BuildingType.class);
  public Warp toFocusOn;
  private float saveTimer = 0f;
  private boolean saveTimerActivated = false;

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("placeLocation", placeLocation == null ? JSONObject.NULL : placeLocation.serialise());
    json.put("buildingLocationGood", buildingLocationGood);
    json.put("queueSize", queueSize);
    json.put("tickTime", tickTime);
    json.put("playerEnergy", playerEnergy);
    json.put("playerTotalEnergy", playerTotalEnergy);
    json.put("buildingsBuilt", buildingsBuilt);
    json.put("buildingsDemolished", buildingsDemolished);
    json.put("particlesDeconstructed", particlesDeconstructed);
    json.put("particleBounces", particleBounces);
    json.put("tiberiumMined", tiberiumMined);
    json.put("treesBulldozed", treesBulldozed);
    json.put("upgradesPurchased", upgradesPurchased);
    json.put("taps", taps);
    json.put("shownMidGame", shownMidGame);
    json.put("inWorldParticles", inWorldParticles);
    json.put("warpParticles", warpParticles);
    json.put("warpSpawnTime", warpSpawnTime);
    json.put("newParticlesMean", newParticlesMean);
    json.put("newParticlesWidth", newParticlesWidth);
    json.put("entityID", entityID);
    json.put("difficulty", difficulty);
    json.put("gameTime", gameTime);
    json.put("cht", cht);
    //
    JSONObject particles = new JSONObject();
    for (Map.Entry<Integer, Sprite> entry : particleMap.entrySet()) {
      particles.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("particleMap", particles);
    //
    JSONObject buildings = new JSONObject();
    for (Map.Entry<Integer, Building> entry : buildingMap.entrySet()) {
      buildings.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("buildingMap", buildings);
    //
    JSONObject buildingExtras = new JSONObject();
    for (Map.Entry<Integer, Entity> entry : buildingExtrasMap.entrySet()) {
      buildingExtras.put(entry.getKey().toString(), entry.getValue().serialise(false));
    }
    json.put("buildingExtrasMap", buildingExtras);
    //
    JSONObject trucks = new JSONObject();
    for (Map.Entry<Integer, Truck> entry : trucksMap.entrySet()) {
      trucks.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("trucksMap", trucks);
    //
    JSONObject jsonWarps = new JSONObject();
    for (Map.Entry<Integer, Warp> entry : warpMap.entrySet()) {
      jsonWarps.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("warpMap", jsonWarps);
    //
    return json;
  }

  public void deserialise(JSONObject json) throws JSONException {
    if (json.get("placeLocation") == JSONObject.NULL) {
      placeLocation = null;
    } else {
      placeLocation = new IVector2(json.getJSONObject("placeLocation"));
    }
    buildingLocationGood = json.getBoolean("buildingLocationGood");
    queueSize = json.getInt("queueSize");
    tickTime = (float) json.getDouble("tickTime");
    inWorldParticles = json.getInt("inWorldParticles");
    playerEnergy = json.getInt("playerEnergy");
    playerTotalEnergy = json.getInt("playerTotalEnergy");
    buildingsBuilt = json.getInt("buildingsBuilt");
    buildingsDemolished = json.getInt("buildingsDemolished");
    particlesDeconstructed = json.getInt("particlesDeconstructed");
    particleBounces = json.getInt("particleBounces");
    upgradesPurchased = json.getInt("upgradesPurchased");
    taps = json.getInt("taps");
    shownMidGame = json.getBoolean("shownMidGame");
    tiberiumMined = json.getInt("tiberiumMined");
    treesBulldozed = json.getInt("treesBulldozed");
    warpParticles = json.getInt("warpParticles");
    warpSpawnTime = (float) json.getDouble("warpSpawnTime");
    newParticlesMean = (float) json.getDouble("newParticlesMean");
    newParticlesWidth = (float) json.getDouble("newParticlesWidth");
    entityID = json.getInt("entityID");
    difficulty = json.getInt("difficulty");
    gameTime = (float) json.getDouble("gameTime");
    cht = json.getBoolean("cht");
    //
    JSONObject jsonParticles = json.getJSONObject("particleMap");
    Iterator particlesIt = jsonParticles.keys();
    while (particlesIt.hasNext()) {
      String key = (String) particlesIt.next();
      Sprite s = new Sprite(jsonParticles.getJSONObject(key));
      particleMap.put(s.id, s);
      getSpriteStage().addActor(s);
      if (s.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    JSONObject jsonBuildings = json.getJSONObject("buildingMap");
    Iterator buildingsIt = jsonBuildings.keys();
    while (buildingsIt.hasNext()) {
      String key = (String) buildingsIt.next();
      Building b = new Building(jsonBuildings.getJSONObject(key));
      buildingMap.put(b.id, b);
      getBuildingStage().addActor(b);
      if (b.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    JSONObject jsonBuildingExtras = json.getJSONObject("buildingExtrasMap");
    Iterator buildingExtrasIt = jsonBuildingExtras.keys();
    while (buildingExtrasIt.hasNext()) {
      String key = (String) buildingExtrasIt.next();
      Entity e = new Entity(jsonBuildingExtras.getJSONObject(key));
      buildingExtrasMap.put(e.id, e);
      addBuildingExtraEntity(e);
      if (e.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    for (Building b : buildingMap.values()) {
      if (b.clock != 0) {
        buildingExtrasMap.get(b.clock).setVisible(b.clockVisible);
      }
    }
    //
    JSONObject jsonTrucks = json.getJSONObject("trucksMap");
    Iterator trucksIt = jsonTrucks.keys();
    while (trucksIt.hasNext()) {
      String key = (String) trucksIt.next();
      Truck t = new Truck(jsonTrucks.getJSONObject(key));
      trucksMap.put(t.id, t);
      getSpriteStage().addActor(t);
      if (t.id != Integer.valueOf(key)) throw new AssertionError();
    }
    //
    JSONObject jsonWarps = json.getJSONObject("warpMap");
    Iterator warpIt = jsonWarps.keys();
    while (warpIt.hasNext()) {
      String key = (String) warpIt.next();
      Warp w = new Warp(jsonWarps.getJSONObject(key));
      warpMap.put(w.id, w);
      getWarpStage().addActor(w);
      if (w.id != Integer.valueOf(key)) throw new AssertionError();
    }
  }

  public PathingCache<IVector2> pathingCache = new PathingCache<IVector2>();
  public final Random R = new Random();

  private Stage tileStage;
  private Stage spriteStage;
  private Stage foliageStage;
  private Stage warpStage;
  private Stage buildingStage;
  private Stage uiStage;
  private Stage fireworkStage;
  private Stage saveStage;


  private static GameState ourInstance = null;

  public static GameState getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new GameState();
  }

  public static boolean constructed() {
    return ourInstance != null;
  }

  private ParticleEffectPool dustEffectPool, upgradeDustEffectPool, boomParticleEffectPool;
  public Array<ParticleEffectPool.PooledEffect> dustEffects;

  private GameState() {
    reset();
  }

  public void act(float delta) {
    if (!World.getInstance().getGenerated()) return;

    uiStage.act(delta);

    if (StateManager.getInstance().fsm == FSM.kGAME_OVER || StateManager.getInstance().fsm == FSM.kTRANSITION_TO_INTRO_NOSAVE) {
      if (R.nextInt(100) == 0) {
        fireworkStage.addActor(new Firework(R.nextInt(Param.DISPLAY_X)));
      }
      fireworkStage.act(delta);
      World.getInstance().paintFin(delta);
    }

    if (StateManager.getInstance().fsm != FSM.kGAME || UI.getInstance().uiMode == UIMode.kSETTINGS) {
      return;
    }

    gameTime += delta;

    if (saveTimerActivated) {
      Persistence.getInstance().trySaveGame();
      Persistence.getInstance().flushSaveGame();
      UI.getInstance().saving.setVisible(false);
      saveTimerActivated = false;
    }

    saveTimer += delta;
    if (Persistence.getInstance().autoSave > 0 && saveTimer >= 60 * Persistence.getInstance().autoSave) {
      saveTimer = 0f;
      UI.getInstance().saving.setVisible(true);
      saveTimerActivated = true;
    }

    // Tile stage, foliage stage are static - does not need to be acted
    spriteStage.act(delta);
    warpStage.act(delta);
    buildingStage.act(delta);

    boolean processingOre = false;
    for (Building b : getBuildingMap().values()) {
      if (b.truckWithLock != 0 && Camera.getInstance().onScreen(b)) {
        processingOre = true;
        break;
      }
    }
    Sounds.getInstance().processing(processingOre);

    if (Param.IS_ANDROID) {
      cursor.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
    } else {
      cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    }
    cursor = Camera.getInstance().unproject(cursor);
    cursor.scl(1f / (float) Param.TILE_S);
    Tile cursorTile = null;
    if (Util.inBounds((int) cursor.x, (int) cursor.y, false)) {
      cursorTile = World.getInstance().getTile(cursor.x, cursor.y, false);
    }

    if (UI.getInstance().uiMode == UIMode.kPLACE_BUILDING) {
      hintBuildingPlacement(cursorTile);
    } else if (UI.getInstance().uiMode == UIMode.kWITH_BUILDING_SELECTION && doingPlacement && cursorTile != null) {
      hintStandingOrder(cursorTile);
    }

    if (UI.getInstance().uiMode == UIMode.kWITH_PARTICLE_SELECTION && cursorTile != null) {
      if (cursorTile.mySprite != 0) {
        Building b = GameState.getInstance().buildingMap.get(cursorTile.mySprite);
        if (b != null && b.getType() != BuildingType.kMINE) {
          cursorTile = b.getQueuePathingTarget();
        }
      }
      if (cursorTile.coordinates.pathFindNeighbours.size() > 0 && !Param.IS_ANDROID) {
        cursorTile.setHighlightColour(Param.HIGHLIGHT_YELLOW, kNONE);
      }
    }

    if (!shownMidGame && warpParticles == 0) {
      UI.getInstance().spawnOverDiag(inWorldParticles);
      shownMidGame = true;
    }

    tickTime += delta;
    boolean won = (inWorldParticles == 0 && warpParticles == 0 && toFocusOn.holdinPenEmpty());
    if (won) {
      StateManager.getInstance().gameOver();
      return;
    }
    boolean tooFew = (inWorldParticles < 10 && warpParticles > 0 && toFocusOn.holdinPenEmpty());
    if (!tooFew && tickTime < warpSpawnTime && triggerSpawn == null) return;
    if (tooFew) {
      Gdx.app.log("act", "Spawning due to 'too few'");
    }
    tickTime -= warpSpawnTime;
    if (warpSpawnTime > Param.WARP_SPAWN_TIME_MIN) {
      warpSpawnTime -= Param.WARP_SPAWN_TIME_REDUCTION;
      newParticlesMean += Param.WARP_SPAWN_MEAN_INCREASE;
      newParticlesWidth += Param.WARP_SPAWN_WIDTH_INCREASE;
    }

    tryNewParticles(false, triggerSpawn, 1);
    triggerSpawn = null;
  }

  public void addEnergy(int e) {
    playerEnergy += e;
    playerTotalEnergy += e;
  }

  private void hintBuildingPlacement(Tile cursorTile) {
    buildingLocationGood = false;
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
    UI.getInstance().selectTickIsEnabled(buildingLocationGood);
  }

  private void hintStandingOrder(Tile cursorTile) {
    if (cursorTile != null && selectedBuildingStandingOrderParticle != null) {
      Tile t = mapPathingDestination(cursorTile);
      if (t != null) {
        placeLocation = t.coordinates;
        // Don't allow loop
        if (t.mySprite != 0 && t.mySprite == selectedBuilding) placeLocation = null;
        if (placeLocation != null) {
          Building b = getSelectedBuilding();
          b.updateDemoPathingList(selectedBuildingStandingOrderParticle, t);
          t.setHighlightColour(Param.HIGHLIGHT_BLUE, kNONE);
        }
      }
    }
  }

  public int tryNewParticles(boolean stressTest, Warp from, int floor) {
    // Add a new sprite
    if (from == null) {
      List<Map.Entry<Integer, Warp>> entries = new ArrayList<Map.Entry<Integer, Warp>>(warpMap.entrySet());
      if (entries.size() == 0) return 0;
      Map.Entry<Integer, Warp> rWarp = entries.get(R.nextInt(entries.size()));
      from = rWarp.getValue();
    }

    int toPlace = floor + Math.round(
            Util.clamp(newParticlesMean + ((float) R.nextGaussian() * newParticlesWidth), 1, Param.NEW_PARTICLE_MAX)
    );
    if (stressTest) toPlace = warpParticles;
    int placed = from.newParticles(toPlace, stressTest);
    Gdx.app.debug("GameState:act", "Warp: SpawnTime: " + warpSpawnTime + " meanP: "
            + newParticlesMean + " widthP: " + newParticlesWidth
            + " place:" + toPlace + " placed:" + placed);

    if (placed > 0) {
      from.zap.start(); // Lightning
      if (Camera.getInstance().addShake(from, Param.WARP_SHAKE)) {
        // If did shake, then also do zap
        Sounds.getInstance().zap();
      }
    }
    return placed;
  }

  public void boomDustEffect(Tile t) {
    ParticleEffectPool.PooledEffect e = boomParticleEffectPool.obtain();
    e.setPosition(t.centreScaleTile.x, t.centreScaleTile.y);
    dustEffects.add(e);
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
    --inWorldParticles;
    ++particlesDeconstructed;
    s.remove(); // From its renderer
  }

  // Note: this is a cheat fn
  public void killSelectedParticles() {
    Set<Integer> selectedSetCopy = new HashSet<Integer>(selectedSet);
    for (int p : selectedSetCopy) {
      Sprite s = particleMap.get(p);
      killSprite(s);
    }
    if (selectedSet.isEmpty()) showMainUITable(false);
    else UI.getInstance().doSelectParticle(selectedSet, false);
  }

  public void killSelectedBuilding() {
    Building selected = getBuildingMap().remove(selectedBuilding);
    if (selected == null) {
      Gdx.app.error("killSelectedBuilding", "Trying to kill NULL selected building?! selectedBuilding is " + selectedBuilding);
      return;
    }
    // Run deconstructor
    selected.deconstruct();
    // Refund
    playerEnergy += selected.refund;
    selectedBuilding = 0;
    // Remove upgrade clock
    if (selected.clock != 0) {
      Entity clock = buildingExtrasMap.remove(selected.clock);
      clock.remove(); // From stage
    }
    // Remove extras
    for (Integer extraID : selected.childElements) {
      Entity extraEntity = buildingExtrasMap.remove(extraID);
      extraEntity.remove(); // From stage
    }
    // Do animation
    Tile t = selected.getCentreTile();
    for (Cardinal D : Cardinal.n8) {
      boomDustEffect(t.n8.get(D));
    }
    if (selected.myQueue != null) {
      for (IVector2 v : selected.myQueue.getQueue()) {
        boomDustEffect(World.getInstance().getTile(v, false));
      }
    }
    Camera.getInstance().addShake(selected, Param.WARP_SHAKE);
    buildingsDemolished += 1;
    // Remove building from stage
    selected.remove();
    // If it was a mine, try and re-allocate miners
    if (selected.getType() == BuildingType.kMINE) {
      reallocateTrucks();
    }
    // Return to main menu
    showMainUITable(false);
  }

  private void reallocateTrucks() {
    for (Building b : buildingMap.values()) {
      if (b.getType() != BuildingType.kMINE || b.myPatch == 0) {
        continue;
      }
      for (Truck t : trucksMap.values()) {
        if (t.myBuilding == 0) {
          t.myBuilding = b.id;
        }
      }
      break;
    }
  }

  public void addSprite(Sprite s) {
    spriteStage.addActor(s);
    particleMap.put(s.id, s);
    dustEffect(s.getTile());
    if (Camera.getInstance().onScreen(s)) Sounds.getInstance().boop();
  }

  public void addBuildingExtraEntity(Entity e) {
    buildingExtrasMap.put(e.id, e);
    if (e.texString.equals("clock") || e.texString.equals("board_vertical")) {
      buildingStage.addActor(e);
    } else if (e.texString.contains("ball_")) {
      spriteStage.addActor(e);
    } else {
      Gdx.app.log("addBuildingExtraEntity", "Do no know about:" + e.texString);
    }
  }

  public HashMap<Integer, Entity> getBuildingExtrasMap() {
    return buildingExtrasMap;
  }

  public HashMap<Integer, Truck> getTrucksMap() {
    return trucksMap;
  }

  public boolean isSelecting() {
    if (Param.IS_ANDROID) {
      return UI.getInstance().selectParticlesButton.isChecked();
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
    Sounds.getInstance().foot();
    return true;
  }

  public int getBuildingPrice(boolean base, BuildingType type) {
    int price = buildingPrices.get(type);
    if (base) return price;
    if (queueSize > 9) {
      price += buildingQueuePrices.get(type) * (queueSize - 9);
    }
    return price;
  }

  public void placeBuilding() {
    if (!buildingLocationGood || placeLocation == null) return;
    Building b = new Building(World.getInstance().getTile(placeLocation, false), buildingBeingPlaced);
    buildingStage.addActor(b);
    buildingMap.put(b.id, b);
    final int price = getBuildingPrice(false, buildingBeingPlaced);
    playerEnergy -= price;
    ++buildingsBuilt;
    b.addCost(price);
    Camera.getInstance().addShake(Param.BUILDING_SHAKE);
    Sounds.getInstance().thud();
    Sounds.getInstance().OK();
    repath();
    if (b.getType() == BuildingType.kMINE) {
      reallocateTrucks(); // In case we had any orphaned
    }
    updateBuildingPrices();
    showMainUITable(false);
  }

  public void showMainUITable(boolean allowDismissSettingsWindow) {
    if (StateManager.getInstance().fsm != FSM.kGAME) {
      return;
    }
    if (UI.getInstance().uiMode == UIMode.kSETTINGS && !allowDismissSettingsWindow) {
      return;
    }
    if (selectedSet.size() > 0 || selectedBuilding != 0) {
      clearSelect();
    }
    selectEndWorld.setZero();
    selectStartWorld.setZero();
    doingPlacement = false;
    buildingBeingPlaced = null;
    UI.getInstance().showMain();
  }

  public void doConfirmStandingOrder() {
    Building b = getSelectedBuilding();
    BuildingType bt = b.getType();
    boolean didSave = b.savePathingList(); // Save the pathing list
    if (!didSave) return;
    doingPlacement = false;
    UI.getInstance().selectTickIsEnabled(false);
    Sounds.getInstance().OK();
    // Set all buttons to false
    for (Particle p : Particle.values()) {
      if (!UI.getInstance().buildingSelectStandingOrder.get(bt).containsKey(p)) continue;
      UI.getInstance().buildingSelectStandingOrder.get(bt).get(p).setChecked(false);
    }
  }

  public void removeFromSelectedSet(Sprite s) {
    if (!s.selected) {
      return;
    }
    selectedSet.remove(s.id);
    s.selected = false;
    if (selectedSet.isEmpty()) showMainUITable(false);
    else UI.getInstance().doSelectParticle(selectedSet, false);
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
    if (selectedSet.isEmpty()) showMainUITable(false);
    else UI.getInstance().doSelectParticle(selectedSet, false);
  }

  private void clearSelect() {
    for (int sID : selectedSet) {
      Sprite s = particleMap.get(sID);
      s.selected = false;
    }
    selectedSet.clear();
    if (selectedBuilding != 0) {
      Building b = getSelectedBuilding();
      b.selected = false;
      b.cancelUpdatePathingList();
    }
    selectedBuilding = 0;
    selectedBuildingStandingOrderParticle = null;
  }

  private boolean contained(Entity e, int scale) {
    Rectangle.tmp.set(e.getX(), e.getY(), e.getWidth(), e.getHeight());
    if (Rectangle.tmp.contains(selectStartWorld.x * scale, selectStartWorld.y * scale)) {
      clearSelect();
      e.selected = true;
      if (e.getClass() == Building.class || e.getClass() == Warp.class) {
        selectedBuilding = e.id;
        UI.getInstance().showBuildingInfo(e.getClass() == Building.class ? (Building) e : (Warp) e);
      } else if (e.getClass() == Sprite.class) {
        selectedSet.add(e.id);
        UI.getInstance().doSelectParticle(selectedSet, true);
      }
      return true;
    }
    return false;
  }

  public boolean doParticleSelect(boolean rangeBased) {

    if (rangeBased) {

      clearSelect();
      Rectangle.tmp.set(Math.min(selectStartWorld.x, selectEndWorld.x),
              Math.min(selectStartWorld.y, selectEndWorld.y),
              Math.abs(selectEndWorld.x - selectStartWorld.x),
              Math.abs(selectEndWorld.y - selectStartWorld.y));
      for (Sprite s : particleMap.values()) {
        if (s.getTile().queueTexSet) { // Do not allow user to select particles in the queue
          continue;
        }
        s.selected = Rectangle.tmp.contains(s.getX() / Param.SPRITE_SCALE, s.getY() / Param.SPRITE_SCALE);
        if (s.selected) selectedSet.add(s.id);
      }
      UI.getInstance().uiMode = UIMode.kNONE; // Remove "selecting"
      UI.getInstance().selectParticlesButton.setChecked(false);
      if (!selectedSet.isEmpty()) UI.getInstance().doSelectParticle(selectedSet, true);
      selectStartWorld.setZero();
      selectEndWorld.setZero();
      return !selectedSet.isEmpty();

    } else if (selectedSet.isEmpty()) {
      // We only let a building or individual particle be selected if no particles are selected
      // if particles are selected then we want to path to the building/location

      for (Building b : buildingMap.values()) {
        if (contained(b, 1)) {
          return true;
        }
      }
      for (Warp w : warpMap.values()) {
        if (contained(w, 1)) {
          return true;
        }
      }
      for (Sprite s : particleMap.values()) {
        if (s.getTile().queueTexSet) { // Do not allow user to select particles in the queue
          continue;
        }
        if (contained(s, Param.SPRITE_SCALE)) {
          return true;
        }
      }
    }
    return false;
  }

  private void repath() {
    clearPathingCache();
    pathingInternal(null, true);
  }

  public Building getSelectedBuilding() {
    Building b = buildingMap.get(selectedBuilding);
    if (b == null) {
      b = warpMap.get(selectedBuilding);
    }
    return b;
  }

  public Tile mapPathingDestination(Tile target) {
    Entity targetSprite = target.getMySprite();
    if (targetSprite != null && targetSprite.getClass() == Building.class) {
      return ((Building) targetSprite).getQueuePathingTarget();
    } else if (target.getPathFindNeighbours().isEmpty()) {
      return null; // Cannot path here
    }
    return target;
  }

  public void retextureSprites() {
    for (Actor a : spriteStage.getActors()) {
      ((Entity) a).loadTexture();
    }
  }

  public void doParticleMoveOrder(int x, int y) {
    if (!Util.inBounds(x / Param.TILE_S, y / Param.TILE_S, false)) return;
    Tile target = World.getInstance().getTile(x / Param.TILE_S, y / Param.TILE_S, false);
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
    Gdx.app.log("pathingInternal", "Pathing of " + pathed.size() + " sprites took " + rounds + " rounds");
    if (!doRepath && pathed.size() > 0) {
      Sounds.getInstance().moveOrder();
      Sounds.getInstance().OK();
    }

    if (doRepath) {
      for (Building b : buildingMap.values()) b.doRepath();
      for (Warp w : warpMap.values()) w.doRepath();
    }
  }

  public void clearPathingCache() {
    pathingCache.clear();
  }

  public void initialZap() {
    toFocusOn = warpMap.values().iterator().next();
    if (inWorldParticles > 0) {
      // Only for a new game
      return;
    }
    int placed = 0;
    for (int i = 0; i < R.nextInt(3) + 1; ++i) {
      placed += tryNewParticles(false, toFocusOn, 20);
    }
    UI.getInstance().newGameDiag(placed, warpParticles);
  }

  public Stage getTileStage() {
    return tileStage;
  }

  public Stage getUIStage() {
    return uiStage;
  }

  public Stage getFireworkStage() {
    return fireworkStage;
  }

  public Stage getWarpStage() {
    return warpStage;
  }

  public Stage getSpriteStage() {
    return spriteStage;
  }

  public Stage getFoliageStage() {
    return foliageStage;
  }

  public Stage getBuildingStage() {
    return buildingStage;
  }

  public Stage getSaveStage() {
    return saveStage;
  }

  public void reset() {
    if (tileStage != null) tileStage.dispose();
    if (spriteStage != null) spriteStage.dispose();
    if (foliageStage != null) foliageStage.dispose();
    if (warpStage != null) warpStage.dispose();
    if (buildingStage != null) buildingStage.dispose();
    if (uiStage != null) uiStage.dispose();
    if (fireworkStage != null) fireworkStage.dispose();
    if (saveStage != null) saveStage.dispose();
    tileStage = new Stage(Camera.getInstance().getTileViewport());
    spriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    foliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    warpStage = new Stage(Camera.getInstance().getTileViewport());
    buildingStage = new Stage(Camera.getInstance().getTileViewport());
    uiStage = new Stage(Camera.getInstance().getUiViewport());
    fireworkStage = new Stage(Camera.getInstance().getUiViewport());
    saveStage = new Stage(Camera.getInstance().getUiViewport());
    Color warpStageC = warpStage.getBatch().getColor();
    warpStageC.a = Param.WARP_TRANSPARENCY;
    warpStage.getBatch().setColor(warpStageC);
    particleMap.clear();
    buildingMap.clear();
    buildingExtrasMap.clear();
    trucksMap.clear();
    warpMap.clear();
    buildingPrices.clear();
    buildingQueuePrices.clear();
    selectedSet.clear();
    dustEffects = new Array<ParticleEffectPool.PooledEffect>();
    ParticleEffect dustEffect = new ParticleEffect();
    dustEffect.load(Gdx.files.internal("dust_effect.txt"), Textures.getInstance().getAtlas());
    ParticleEffect upgradeDustEffect = new ParticleEffect();
    upgradeDustEffect.load(Gdx.files.internal("upgrade_dust_effect.txt"), Textures.getInstance().getAtlas());
    ParticleEffect boomParticleEffect = new ParticleEffect();
    boomParticleEffect.load(Gdx.files.internal("boom_dust_effect.txt"), Textures.getInstance().getAtlas());
    dustEffectPool = new ParticleEffectPool(dustEffect, 10, 100);
    upgradeDustEffectPool = new ParticleEffectPool(upgradeDustEffect, 10, 150);
    boomParticleEffectPool = new ParticleEffectPool(boomParticleEffect, 10, 150);
    playerEnergy = Param.PLAYER_STARTING_ENERGY;
    playerTotalEnergy = 0;
    inWorldParticles = 0;
    buildingsBuilt = 0;
    buildingsDemolished = 0;
    particlesDeconstructed = 0;
    particleBounces = 0;
    tiberiumMined = 0;
    upgradesPurchased = 0;
    selectedBuilding = 0;
    selectedBuildingStandingOrderParticle = null;
    buildingBeingPlaced = null;
    doingPlacement = false;
    taps = 0;
    shownMidGame = false;
    treesBulldozed = 0;
    warpParticles = 0;
    debug = Param.DEBUG_INITIAL;
    uiOn = true;
    queueType = Param.QUEUE_INITIAL_TYPE;
    queueSize = Param.QUEUE_INITIAL_SIZE;
    warpSpawnTime = Param.WARP_SPAWN_TIME_INITIAL;
    newParticlesMean = Param.NEW_PARTICLE_MEAN;
    newParticlesWidth = Param.NEW_PARTICLE_WIDTH;
    clearPathingCache();
    entityID = 0;
    gameTime = 0;
    //difficulty = 0; Do NOT reset difficulty if re-doing worldgen
    if (Param.WORLD_SEED > 0) {
      R.setSeed(Param.WORLD_SEED);
    }
    updateBuildingPrices();
  }

  public void updateBuildingPrices() {
    for (BuildingType bt : BuildingType.values()) {
      int number = 0;
      for (Building b : buildingMap.values()) {
        if (b.getType() == bt) ++number;
      }
      if (bt == BuildingType.kMINE) {
        number += trucksMap.size();
      }
      buildingPrices.put(bt, (int) Math.round(bt.getBaseCost() * Math.pow(bt.getCostIncrease(), number)));
      buildingQueuePrices.put(bt, (int) Math.round(bt.getQueueBaseCost() * Math.pow(bt.getCostIncrease(), number)));
    }
    if (UI.constructed()) {
      UI.getInstance().updateButtonPriceStatus();
    }
  }

  public void dispose() {
    tileStage.dispose();
    spriteStage.dispose();
    foliageStage.dispose();
    warpStage.dispose();
    buildingStage.dispose();
    uiStage.dispose();
    fireworkStage.dispose();
    saveStage.dispose();
    ourInstance = null;
  }

  public HashMap<Integer, Sprite> getParticleMap() {
    return particleMap;
  }

  public HashMap<Integer, Building> getBuildingMap() {
    return buildingMap;
  }

  public HashMap<Integer, Warp> getWarpMap() {
    return warpMap;
  }
}
