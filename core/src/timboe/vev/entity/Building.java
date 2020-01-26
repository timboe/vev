package timboe.vev.entity;

import com.badlogic.gdx.Gdx;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import timboe.vev.Pair;
import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Particle;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;
import timboe.vev.pathfinding.OrderlyQueue;
import timboe.vev.pathfinding.PathFinding;

/**
 * Created by Tim on 28/12/2017.
 */

public class Building extends Entity {

  // Persistent properties
  private final BuildingType type;
  private final IVector2 centre;
  public OrderlyQueue myQueue = null;
  public int myPatch;
  private IVector2 pathingStartPoint;
  public float timeDisassemble;
  private float timeMove;
  private float timeBuild;
  private float timeHoldingPen;
  private float nextReleaseTime;
  public float timeUpgrade;
  public int spriteProcessing;
  public Vector<Integer> childElements = new Vector<Integer>();
  public int clock;
  public boolean clockVisible;
  private EnumMap<Particle, Integer> holdingPen = new EnumMap<Particle, Integer>(Particle.class);
  public int built;
  private boolean updateBuildingTexture;
  public float getTimeDisassembleMax; // Used to get % complete
  private int buildingLevel;
  public boolean doUpgrade;
  public int refund;

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(false);
    json.put("type", type.name());
    json.put("centre", centre.serialise());
    json.put("myQueue", myQueue == null ? JSONObject.NULL : myQueue.serialise());
    json.put("myPatch", this.myPatch);
    json.put("pathingStartPoint", pathingStartPoint == null ? JSONObject.NULL : pathingStartPoint.serialise());
    json.put("timeDisassemble", timeDisassemble);
    json.put("timeMove", timeMove);
    json.put("timeBuild", timeBuild);
    json.put("timeHoldingPen", timeHoldingPen);
    json.put("nextReleaseTime", nextReleaseTime);
    json.put("timeUpgrade", timeUpgrade);
    json.put("spriteProcessing", spriteProcessing);
    Integer extraCount = 0;
    JSONObject extra = new JSONObject();
    for (Integer e : childElements) {
      extra.put(extraCount.toString(), e);
      ++extraCount;
    }
    json.put("childElements", extra);
    JSONObject holding = new JSONObject();
    for (Particle p : Particle.values()) {
      holding.put(p.name(), holdingPen.get(p));
    }
    json.put("holdingPen", holding);
    json.put("clock", clock);
    json.put("clockVisible", clockVisible);
    json.put("built", built);
    json.put("updateBuildingTexture", updateBuildingTexture);
    json.put("getTimeDisassembleMax", getTimeDisassembleMax);
    json.put("buildingLevel", buildingLevel);
    json.put("doUpgrade", doUpgrade);
    json.put("refund", refund);

    return json;
  }

  public Building(JSONObject json) throws JSONException {
    super(json);
    this.refund = json.getInt("refund");
    this.doUpgrade = json.getBoolean("doUpgrade");
    this.buildingLevel = json.getInt("buildingLevel");
    this.getTimeDisassembleMax = (float) json.getDouble("getTimeDisassembleMax");
    this.updateBuildingTexture = json.getBoolean("updateBuildingTexture");
    this.built = json.getInt("built");
    this.clock = json.getInt("clock");
    this.clockVisible = json.getBoolean("clockVisible");
    //
    JSONObject jsonHolding = json.getJSONObject("holdingPen");
    for (Particle p : Particle.values()) {
      int n = jsonHolding.getInt( p.name() );
      this.holdingPen.put(p, n);
    }
    //
    JSONObject extra = json.getJSONObject("childElements");
    Iterator extraIt = extra.keys();
    while (extraIt.hasNext()) {
      this.childElements.add( extra.getInt((String) extraIt.next()) );
    }
    //
    this.spriteProcessing = json.getInt("spriteProcessing");
    this.timeUpgrade = (float) json.getDouble("timeUpgrade");
    this. nextReleaseTime = (float) json.getDouble("nextReleaseTime");
    this.timeHoldingPen = (float) json.getDouble("timeHoldingPen");
    this.timeBuild = (float) json.getDouble("timeBuild");
    this.timeMove = (float) json.getDouble("timeMove");
    this.timeDisassemble = (float) json.getDouble("timeDisassemble");
    this.myPatch = json.getInt("myPatch");
    if (json.get("pathingStartPoint") == JSONObject.NULL) {
      this.pathingStartPoint = null;
    } else {
      this.pathingStartPoint = new IVector2(json.getJSONObject("pathingStartPoint"));
    }
    if (json.get("myQueue") == JSONObject.NULL) {
      this.myQueue = null;
    } else {
      this.myQueue = new OrderlyQueue( json.getJSONObject("myQueue") );
    }
    this.centre = new IVector2( json.getJSONObject("centre") );
    this.type = BuildingType.valueOf( json.getString("type") );
    if (this.type != BuildingType.kWARP) doRepath();
    // Warp calls repath after having unpacked its pathing start data
  }

  public Building(Tile t, BuildingType type) {
    super(t.coordinates.x - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1),
          t.coordinates.y - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1));
    buildingPathingLists = new EnumMap<Particle, List<IVector2>>(Particle.class);
    this.refund = 0;
    this.type = type;
    this.doUpgrade = false;
    this.spriteProcessing = 0;
    this.clockVisible = false;
    this.clock = 0;
    this.centre = t.coordinates;
    for (Particle p : Particle.values()) holdingPen.put(p, 0);
    this.built = 0;
    this.buildingLevel = 0;
    this.myPatch = 0;
    if (type == BuildingType.kWARP) return; // Warp does not need anything below
    ////////////////////////////////////////////////////////////////////////////
    setTexture("build_3_3", 1, false);
    t.setBuilding(this);
    for (Cardinal D : Cardinal.n8) t.n8.get(D).setBuilding(this);
    updatePathingGrid();
    if (type != BuildingType.kMINE) {
      this.myQueue = new OrderlyQueue(t.coordinates.x - 1, t.coordinates.y - 2, null, this, isIntro);
      this.built = myQueue.getQueue().size();
    } else {
      this.built = 1;
      updatePathingStartPoint();
      updateMyPatch();
      addTruck();
    }
    // Move any sprites which are here
    moveOn();
  }

  public void deconstruct() {
    Sounds.getInstance().demolish();
    finishProcessingParticle();
    Tile t = World.getInstance().getTile( coordinates.x + 1, coordinates.y + 1, isIntro); // Assuming 3x3
    t.removeBuilding();
    for (Cardinal D : Cardinal.n8) {
      t.n8.get(D).removeBuilding();
    }
    if (myQueue != null) {
      myQueue.deconstruct();
      myQueue = null;
    }
    updatePathingGrid();
    if (type == BuildingType.kMINE) {
      for (Truck truck : GameState.getInstance().getTrucksMap().values()) {
        if (truck.myBuilding == this.id) {
          truck.orphan();
        }
      }
    }
  }

  public void addCost(int cost) {
    this.refund += Math.round( Param.BUILDING_REFUND_AMOUND * cost );
  }

  public void updateMyPatch() {
    Patch patch = null;
    for (Patch p : World.getInstance().tiberiumPatches.values()) {
      if (p.remaining() == 0) {
        continue;
      }
      if ( patch == null || p.coordinates.dst(coordinates) < patch.coordinates.dst(coordinates)) {
        patch = p;
      }
    }
    if (patch == null) {
      this.buildingPathingLists.remove(Particle.kBlank);
      this.myPatch = 0;
      return;
    }
    this.myPatch = patch.id;
    // kBLank pathing is used for the truck
    Tile tibTile = World.getInstance().getTile(
        patch.coordinates.x + (-Param.WARP_SIZE/2) + Util.R.nextInt( Param.WARP_SIZE ),
        patch.coordinates.y + (-Param.WARP_SIZE/2) + Util.R.nextInt( Param.WARP_SIZE ),
            isIntro);
    Tile tibGoal = Sprite.findPathingLocation(tibTile, true, false, false, false);
    updateDemoPathingList(Particle.kBlank, tibGoal);
    savePathingList();
  }

  public BuildingType getType() {
    return type;
  }

  public void processSprite(Sprite s) {
    if (spriteProcessing != 0) {
      Gdx.app.error("processSprite", "Already processing a sprite?! Logic error");
    }
    Particle p = s.getParticle();
    spriteProcessing = s.id;
    GameState.getInstance().dustEffect(s.getTile());
    if (Camera.getInstance().onScreen(this)) Sounds.getInstance().poof();
    s.setVisible(false);
    timeDisassemble = getDisassembleTime(p);
    getTimeDisassembleMax = timeDisassemble;
  }

  private float getDisassembleTime(Particle p) {
    return p.getDisassembleTime()
        * getUpgradeFactor()
        * type.getDisassembleMod(p);
  }

  public float getDisassembleTime(int mode) {
    return getDisassembleTime(type.getInput(mode));
  }

  public int getUpgradeCost() {
    return Math.round(type.getUpgradeBaseCost() * (1f/getUpgradeFactor()));
  }

  public float getUpgradeTime() {
    return type.getUpgradeBaseTime() * (1f/getUpgradeFactor());
  }

  public float getUpgradeFactor() {
    if (type == BuildingType.kMINE) {
      int totalLevels = 0;
      for (Truck t : GameState.getInstance().getTrucksMap().values()) {
        if (t.myBuilding == this.id) {
          totalLevels =+ t.level;
        }
      }
      return (float)Math.pow(Param.BUILDING_DISASSEMBLE_BONUS, totalLevels);
    }
    // Else
    return (float)Math.pow(Param.BUILDING_DISASSEMBLE_BONUS, buildingLevel);
  }

  public float getAverageNextUpgradeFactor() {
    if (type == BuildingType.kMINE) {
      int nTruck = 0;
      float bonus = 0;
      for (Truck t : GameState.getInstance().getTrucksMap().values()) {
        if (t.myBuilding == this.id) {
          nTruck += 1;
          bonus += t.getNextUpgradeFactor();
        }
      }
      if (nTruck == 0) {
        Gdx.app.error("getAverageNextUpgradeFactor", "Encountered a mine with zero miners?");
        return 0;
      }
      return (bonus / nTruck);
    }
    // Else
    return 1f/(float)Math.pow(Param.BUILDING_DISASSEMBLE_BONUS, buildingLevel + 1);
  }

  public void updatePathingStartPoint() {
//    Gdx.app.log("updatePathingStartPoint", "CALLED myQueue:" + myQueue)
    Tile queueStart = myQueue != null ? tileFromCoordinate( myQueue.getQueuePathingTarget() ) : getCentreTile();
    // TODO graphically, appears to be starting from within the queue?
    Tile pathingStartPointTile = Sprite.findPathingLocation(queueStart, true, false, false, false); //reproducible=True, requireParking=False
    if (pathingStartPointTile == null) {
      Gdx.app.error("updatePathingStartPoint", "Building could not find a pathing start point!");
      return;
    }
    this.pathingStartPoint = pathingStartPointTile.coordinates;
  }

  // Loop over the "demo" pathing and all stored pathing lists -
  public void updatePathingDestinations() {
    if (pathingList != null) {
      pathingList = PathFinding.doAStar(getPathingStartPoint(pathingParticle), getDestination().coordinates, null, null, GameState.getInstance().pathingCache);
    }
    for (Particle p : Particle.values()) {
      if (getBuildingPathingList(p) != null) {
//        Gdx.app.log("DBG","From " + p.getString() + " " + getPathingStartPoint(p) + " " + getBuildingDestination(p).coordinates + " " + getBuildingDestination(p).coordinates.getNeighbours());
        buildingPathingLists.put(p, PathFinding.doAStar(getPathingStartPoint(p), getBuildingDestination(p).coordinates, null, null, GameState.getInstance().pathingCache) );
      }
    }
  }

  protected IVector2 getPathingStartPoint(Particle p) {
//    Gdx.app.log("getPathingStartPoint BASE","Returning "+pathingStartPoint);
    // Note: p is only used in Warp's override of this function.
    // Note: We fetch the World version as this has the getNeighbours property set.
    return World.getInstance().getTile(pathingStartPoint, isIntro).coordinates;
  }

  public void updateDemoPathingList(Particle p, Tile t) {
    if (getDestination() != t) {
//      Gdx.app.log("DEBUG updateDemoPathingList","Start for "+p+" is "+getPathingStartPoint(p)+" with target t.coordinates " + t.coordinates);
      this.pathingList = PathFinding.doAStar(getPathingStartPoint(p), t.coordinates, null, null, GameState.getInstance().pathingCache);
      Sounds.getInstance().click();
    }
    // The "pathingList" holds our speculative/demo destination
    this.pathingParticle = p;
  }

  public boolean savePathingList() {
    if (pathingParticle == null) {
      Gdx.app.log("savePathingList","Called with pathingParticle = null. Maybe OK was chosen with no pathing list in progress?");
      return false;
    }
    if (pathingList == null) {
      Gdx.app.error("savePathingList","Called with pathingList = null?!");
      return false;
    }
    Gdx.app.log("savePathingList","Set pathing " + pathingParticle + " to " + pathingList.get(0).toString());
    buildingPathingLists.put(pathingParticle, pathingList);
    pathingList = null;
    pathingParticle = null;
    return true;
  }

  public void cancelUpdatePathingList() {
    pathingParticle = null;
    pathingList = null;
  }

  public boolean canJoinQueue(Sprite s) {
    // Note: built==0 is built
    return (type.accepts(s) && built == 0);
  }

  public Pair<Tile, Cardinal> getFreeLocationInQueue(Sprite s) {
//    Gdx.app.log("getFreeLocationInQueue","Sprite "+s+" is accepted " + type.accepts(s));
    // NOTE: Now expect canJoinQueue to be called independently of this, first
    return myQueue.getFreeLocationInQueue();
  }

  public Tile getQueuePathingTarget() {
    if (myQueue != null) return tileFromCoordinate( myQueue.getQueuePathingTarget() );
    return tileFromCoordinate( getPathingStartPoint(null) );
  }

  private Tile tileFromCoordinate(IVector2 v) {
    return World.getInstance().getTile(v, isIntro);
  }

  public Tile getCentreTile() {
    return World.getInstance().getTile(centre, isIntro);
  }

  // Moves on any sprites under the building
  private void moveOn() {
    if (myQueue != null) myQueue.moveOn();
    Tile t = getCentreTile();
    t.moveOnSprites();
    for (Cardinal D1 : Cardinal.n8) {
      t.n8.get(D1).moveOnSprites();
    }
  }

  public Vector<Truck> getTrucks() {
    Vector<Truck> v = new Vector<Truck>();
    for (Truck truck : GameState.getInstance().getTrucksMap().values()) {
      if (truck.myBuilding == this.id) {
        v.add(truck);
      }
    }
    return v;
  }

  private void addTruck() {
    // This can be NULL as WARP will never call this
    Truck t = new Truck(tileFromCoordinate(getPathingStartPoint(null)), this);
    t.setParticle( Particle.kBlank ); // This is so that Sprite::doMove does not crash
    GameState.getInstance().getSpriteStage().addActor(t);
    GameState.getInstance().getTrucksMap().put(t.id, t);
  }

  private void build(float delta) {
    timeBuild += delta;
    if (timeBuild < Param.BUILD_TIME) return;
    timeBuild -= Param.BUILD_TIME;
    if (--built == 0)  {
      Tile t = getCentreTile();
      GameState.getInstance().dustEffect( t );
      for (Cardinal D : Cardinal.n8) GameState.getInstance().dustEffect( t.n8.get(D) );
      updateBuildingTexture = true;
      // Introduce a small delay to let the cloud get into place
    }
    if (myQueue != null) {
      Tile t = tileFromCoordinate( myQueue.getQueue().get(built) );
      t.setQueueTexture();
      GameState.getInstance().dustEffect(t);
      if (Camera.getInstance().onScreen(t)) Sounds.getInstance().foot();
    }
  }

  private void setBuiltTexture() {
    setTexture("building_" + type.ordinal(), 1, false);
    if (Camera.getInstance().onScreen(this)) Sounds.getInstance().star();
    if (type != BuildingType.kMINE) {
      Entity banner = new Entity(coordinates.x + 2, coordinates.y);
      banner.setTexture("board_vertical", 1, false);
      GameState.getInstance().addBuildingExtraEntity(banner);
      childElements.add(banner.id);
      for (int i = 0; i < BuildingType.N_MODES; ++i) {
        Entity p = new Entity(Param.SPRITE_SCALE*(coordinates.x), Param.SPRITE_SCALE*(coordinates.y + 1));
        p.moveBy(73, -5 + (20 * i)); // Fine tune-position of
        Particle input = type.getInput(BuildingType.N_MODES - i - 1);
        assert input != null;
        assert input.getColourFromParticle() != null;
        p.setTexture("ball_" + input.getColourFromParticle().getString(), 1, false);
        childElements.add(p.id);
        GameState.getInstance().addBuildingExtraEntity(p);
      }
    }
    Entity clock = new Entity(coordinates.x, coordinates.y);
    clock.setTexture("clock", 1, false);
    clock.moveBy(type == BuildingType.kMINE ? Param.TILE_S / 2f : 0, Param.TILE_S / 2f);
    this.clock = clock.id;
    clock.setVisible(false);
    clockVisible = false;
    GameState.getInstance().addBuildingExtraEntity(clock);
  }

  private boolean isSelected() {
    if (GameState.getInstance().selectedBuilding != 0) {
      Building b = GameState.getInstance().getSelectedBuilding();
      return (b == this);
    }
    return false;
  }

  @Override
  public void act(float delta) {
    if (built > 0) {
      build(delta);
      return;
    }

    // Upgrade. No spawning, no moving along (i.e. spriteProcessing will remain null until this ends)
    if (doUpgrade && spriteProcessing == 0) {
      if (isSelected()) UI.getInstance().buildingSelectProgress.get(type).setValue(timeUpgrade / getUpgradeTime());
      timeUpgrade -= delta;
      if (timeUpgrade > 0) return;
      ++buildingLevel;
      if (type == BuildingType.kMINE) {
        for (Truck truck : GameState.getInstance().getTrucksMap().values()) {
          if (truck.myBuilding == this.id) {
            truck.level++;
          }
        }
      }
      // TODO update labels
      doUpgrade = false;
      clockVisible = false;
      GameState.getInstance().getBuildingExtrasMap().get( clock ).setVisible(false); // clock
      Sounds.getInstance().star();
      Tile t = getCentreTile();
      for (Cardinal D : Cardinal.n8) GameState.getInstance().upgradeDustEffect(t.n8.get(D));
      if (myQueue != null) {
        for (IVector2 v : myQueue.getQueue()) {
          GameState.getInstance().upgradeDustEffect( tileFromCoordinate(v) );
        }
      }
      // Need to update multiple UI elements, so best to...
      if (isSelected()) UI.getInstance().refreshBuildingLabels();
    }

    timeMove += delta;
    if (timeMove > Param.BUILDING_QUEUE_MOVE_TIME) {
      timeMove -= Param.BUILDING_QUEUE_MOVE_TIME;
      if (myQueue != null) myQueue.moveAlongMoveAlong();
      // When built - set my final texture
      if (updateBuildingTexture) {
        updateBuildingTexture = false;
        setBuiltTexture();
      }
    }

    // Mostly used by Warps, this block of code spawns new particles into the world
    // (used also by buildings where a particle is deconstructed to another particle)
    timeHoldingPen += delta;
    if (timeHoldingPen > nextReleaseTime) {
      timeHoldingPen -= nextReleaseTime;
      nextReleaseTime = Util.R.nextFloat() * Param.NEW_PARTICLE_TIME;
      doRelease();
    }

    if (spriteProcessing == 0) return;
    timeDisassemble -= delta;
    if (isSelected()) UI.getInstance().buildingSelectProgress.get(type).setValue(timeDisassemble / getTimeDisassembleMax);
    if (timeDisassemble > 0) return;
    finishProcessingParticle();
  }

  private void doRelease() {
    for (Particle p : Particle.values()) {
      if (holdingPen.get(p) == 0) continue;
      Integer toPlace = holdingPen.get(p);
      final int N = (toPlace > 10 ? Math.round(toPlace / 10f) : 1); // If lots - place lots at a time
      assert N <= toPlace;
      holdingPen.put(p, toPlace - N);
      for (int i = 0; i < N; ++i) {
        Sprite s = new Sprite( tileFromCoordinate( getPathingStartPoint(p) ) );
        List<IVector2> pList = getBuildingPathingList(p); // Do I have a standing order?
        if (pList == null) s.pathTo( Sprite.findPathingLocation(tileFromCoordinate( getPathingStartPoint(p) ), true, true, true, false), null, null);  // random direction=True, needs parking=True, requireSameHeight=True
        else s.pathingList = new LinkedList<IVector2>(pList); // Clone
        assert p.getColourFromParticle() != null;
        s.setTexture("ball_" + p.getColourFromParticle().getString(), 6, false);
        s.setParticle(p);
        s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);
        s.moveBy( Util.R.nextInt(Param.TILE_S ), Util.R.nextInt(Param.TILE_S )  );
        GameState.getInstance().addSprite(s);
        ++GameState.getInstance().inWorldParticles;
      }
    }
  }

  private void finishProcessingParticle() {
    if (spriteProcessing == 0) return;
    Sprite s = GameState.getInstance().getParticleMap().get(spriteProcessing);
    Pair<Particle,Particle> myDecay = type.getOutputs( s.getParticle() );
    assert myDecay != null;
    if (myDecay.getKey() != null) {
      placeParticle( myDecay.getKey(), true); // Output #1
    }
    if (myDecay.getValue() != null) {
      placeParticle(myDecay.getValue(), true); // Output #2
    }
    GameState.getInstance().addEnergy( type.getOutputEnergy( s.getParticle() ) );
    GameState.getInstance().killSprite(s);
    spriteProcessing = 0;
  }

  public boolean holdinPenEmpty() {
    for (Particle p : Particle.values()) {
      if (holdingPen.get(p) > 0) {
        return false;
      }
    }
    return true;
  }

  void placeParticle(Particle p, boolean now) {
    if (p == null) {
      Gdx.app.log("placeParticle", "Passed NULL");
      return;
    }
    Integer current = holdingPen.get(p);
    holdingPen.put(p, current + 1);
    if (now) {
      doRelease();
    }
  }

  // Update my pathing orders as the world map has changed
  public void doRepath() {
    updatePathingStartPoint();
    updatePathingDestinations();
  }

  // Updates the pathing grid
  private void updatePathingGrid() {
    World w = World.getInstance();
    Tile t = getCentreTile();
    w.updateTilePathfinding(t);
    for (Cardinal D1 : Cardinal.n8) {
      Tile t1 = t.n8.get(D1);
      for (Cardinal D2 : Cardinal.n8) {
        Tile t2 = t1.n8.get(D2);
        w.updateTilePathfinding(t2);
      }
    }
  }

  public boolean upgradeBuilding() {
    if (built > 0 || this.clock == 0) return false; // Not built yet
    final int cost = getUpgradeCost();
    if (GameState.getInstance().playerEnergy < cost) return false; // Cannot afford
    doUpgrade = true;
    timeUpgrade = getUpgradeTime();
    GameState.getInstance().playerEnergy -= cost;
    addCost(cost);
    clockVisible = true;
    Entity c = GameState.getInstance().getBuildingExtrasMap().get( clock );
    if (c != null) {
      c.setVisible(true);
    }
    Sounds.getInstance().OK();
    ++GameState.getInstance().upgradesPurchased;
    return true;
  }
}
