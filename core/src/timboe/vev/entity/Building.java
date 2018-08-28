package timboe.vev.entity;

import com.badlogic.gdx.Gdx;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
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
import timboe.vev.pathfinding.OrderlyQueue;
import timboe.vev.pathfinding.PathFinding;

/**
 * Created by Tim on 28/12/2017.
 */

public class Building extends Entity {

  private OrderlyQueue myQueue = null;

  private final BuildingType type;
  private final Tile centre;
  private Tile pathingStartPoint;
  public float timeDisassemble;
  private float timeMove;
  private float timeBuild;
  private float timeHoldingPen;
  private float nextReleaseTime;
  public float timeUpgrade;
  public Sprite spriteProcessing = null;
  private Vector<Entity> childSprites = new Vector<Entity>();
  private EnumMap<Particle, List<Sprite>> holdingPen = new EnumMap<Particle, List<Sprite>>(Particle.class);
  private int built;
  private boolean updateBuildingTexture;
  public float getTimeDisassembleMax; // Used to get % complete
  private Patch myTiberiumPatch;
  private int buildingLevel;
  public boolean doUpgrade;

  public Building(Tile t, BuildingType type) {
    super(t.coordinates.x - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1),
          t.coordinates.y - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1));
    buildingPathingLists = new EnumMap<Particle, List<Tile>>(Particle.class);
    this.type = type;
    centre = t;
    for (Particle p : Particle.values()) holdingPen.put(p, new LinkedList<Sprite>());
    setTexture("build_3_3", 1, false);
    built = 0;
    buildingLevel = 0;
    doUpgrade = false;
    if (type == BuildingType.kWARP) return; // Warp does not need anything below
    ////////////////////////////////////////////////////////////////////////////
    centre.setBuilding(this);
    for (Cardinal D : Cardinal.n8) centre.n8.get(D).setBuilding(this);
    updatePathingGrid();
    updatePathingStartPoint();
    if (type != BuildingType.kMINE) {
      myQueue = new OrderlyQueue(centre.coordinates.x - 1, centre.coordinates.y - 2, null, this);
      built = myQueue.getQueue().size();
    } else {
      built = 1;
      for (Patch p : World.getInstance().tiberium) {
        if (myTiberiumPatch == null || p.coordinates.dst(coordinates) < myTiberiumPatch.coordinates.dst(coordinates)) {
          myTiberiumPatch = p;
        }
      }
      updateDemoPathingList(Particle.kH, World.getInstance().getTile(
          myTiberiumPatch.coordinates.x + (-Param.WARP_SIZE/2) + Util.R.nextInt( Param.WARP_SIZE ),
          myTiberiumPatch.coordinates.y + (-Param.WARP_SIZE/2) + Util.R.nextInt( Param.WARP_SIZE )
          ));
      savePathingList();
    }
    // Move any sprites which are here
    moveOn();
  }

  public BuildingType getType() {
    return type;
  }

  public void processSprite(Sprite s) {
    if (spriteProcessing != null) {
      Gdx.app.error("processSprite", "Already processing a sprite?! Logic error");
    }
    Particle p = s.getParticle();
    spriteProcessing = s;
    if (Camera.getInstance().onScreen(this)) Sounds.getInstance().poof();
    timeDisassemble = getDisassembleTime(p);
    getTimeDisassembleMax = timeDisassemble;
  }

  private float getDisassembleTime(Particle p) {
    return p.getDisassembleTime()
        * getUpgradeFactor()
        * type.getDissassembleBonus(p);
  }

  public float getDisassembleTime(int mode) {
    return getDisassembleTime(type.getInput(mode));
  }

  public float getUpgradeCost() {
    return type.getUpgradeBaseCost() * (1f/getUpgradeFactor());
  }

  public float getUpgradeTime() {
    return type.getUpgradeBaseTime() * (1f/getUpgradeFactor());
  }

  private float getUpgradeFactor() {
    return (float)Math.pow(Param.BUILDING_DISASSEMBLE_BONUS, buildingLevel);
  }

  public void updatePathingStartPoint() {
    pathingStartPoint = Sprite.findPathingLocation(centre, true, false, false, false); //reproducible=True, requireParking=False
    if (pathingStartPoint == null) {
      Gdx.app.error("updatePathingStartPoint", "Building could not find a pathing start point!");
      return;
    }
  }

  // Loop over the "demo" pathing and all stored pathing lists -
  public void updatePathingDestinations() {
    if (pathingList != null) {
      pathingList = PathFinding.doAStar(getPathingStartPoint(pathingParticle), getDestination(), null, null, GameState.getInstance().pathingCache);
    }
    for (Particle p : Particle.values()) {
      if (getBuildingPathingList(p) != null) {
        buildingPathingLists.put(p, PathFinding.doAStar(getPathingStartPoint(p), getBuildingDestination(p), null, null, GameState.getInstance().pathingCache) );
      }
    }
  }

  protected Tile getPathingStartPoint(Particle p) {
    return pathingStartPoint;
  }

  public void updateDemoPathingList(Particle p, Tile t) {
    if (getDestination() != t) {
      pathingList = PathFinding.doAStar(getPathingStartPoint(p), t, null, null, GameState.getInstance().pathingCache);
      Sounds.getInstance().click();
    }
    // The "pathingList" holds our speculative/demo destination
    pathingParticle = p;
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

  public Pair<Tile, Cardinal> getFreeLocationInQueue(Sprite s) {
    if (!type.accepts(s) || built > 0) return null;
    return myQueue.getFreeLocationInQueue();
  }

  public Tile getQueuePathingTarget() {
    if (myQueue != null) return myQueue.getQueuePathingTarget();
    return getPathingStartPoint(null);
  }

  // Moves on any sprites under the building
  private void moveOn() {
    if (myQueue != null) myQueue.moveOn();
    centre.moveOnSprites();
    for (Cardinal D1 : Cardinal.n8) {
      centre.n8.get(D1).moveOnSprites();
    }
  }

  private void addTruck() {
    // This can be NULL as WARP will never call this
    Truck t = new Truck(getPathingStartPoint(null), this);
    GameState.getInstance().getSpriteStage().addActor(t);
  }

  private void build(float delta) {
    timeBuild += delta;
    if (timeBuild < Param.BUILD_TIME) return;
    timeBuild -= Param.BUILD_TIME;
    if (--built == 0)  {
      GameState.getInstance().dustEffect( centre );
      for (Cardinal D : Cardinal.n8) GameState.getInstance().dustEffect( centre.n8.get(D) );
      updateBuildingTexture = true;
      if (type == BuildingType.kMINE) addTruck();
      // Introduce a small delay to let the cloud get into place
    }
    if (myQueue != null) {
      myQueue.getQueue().get(built).setQueueTexture();
      Tile t = myQueue.getQueue().get(built);
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
      childSprites.add(banner);
      GameState.getInstance().getBuildingStage().addActor(banner);
      for (int i = 0; i < BuildingType.N_MODES; ++i) {
        Entity p = new Entity(Param.SPRITE_SCALE*(coordinates.x), Param.SPRITE_SCALE*(coordinates.y + 1));
        p.moveBy(73, -5 + (20 * i)); // Fine tune-position of
        Particle input = type.getInput(i);
        p.setTexture("ball_" + input.getColourFromParticle().getString(), 1, false);
        childSprites.add(p);
        GameState.getInstance().getSpriteStage().addActor(p);
      }
    }
    Entity clock = new Entity(coordinates.x, coordinates.y);
    clock.setTexture("clock_small", 1, false);
    clock.moveBy(type == BuildingType.kMINE ? Param.TILE_S / 2f : 0, Param.TILE_S / 2f);
    childSprites.add(clock);
    clock.setVisible(false);
    GameState.getInstance().getBuildingStage().addActor(clock);
  }

  private boolean isSelected() {
    return (UI.getInstance().selectedBuilding == this);
  }

  @Override
  public void act(float delta) {
    if (built > 0) {
      build(delta);
      return;
    }

    // Upgrade. No spawning, no moving along (i.e. spriteProcessing will remain null until this ends)
    if (doUpgrade && spriteProcessing == null) {
      if (isSelected()) UI.getInstance().buildingSelectProgress.get(type).setValue(timeUpgrade / getUpgradeTime());
      timeUpgrade -= delta;
      if (timeUpgrade > 0) return;
      ++buildingLevel;
      // TODO update labels
      doUpgrade = false;
      childSprites.lastElement().setVisible(false);
      Sounds.getInstance().star();
      // Need to update multiple UI elements, so best to...
      if (isSelected()) UI.getInstance().showBuildingInfo(this);
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
      for (Particle p : Particle.values()) {
        if (holdingPen.get(p).isEmpty()) continue;
        int N = holdingPen.get(p).size() > 5 ? holdingPen.get(p).size() / 10 : 1; // If lots - place lots at a time
        for (int i = 0; i < N; ++i) {
          Sprite s = holdingPen.get(p).remove(0);
          s.moveBy( Util.R.nextInt(Param.TILE_S ), Util.R.nextInt(Param.TILE_S )  );
          GameState.getInstance().getSpriteStage().addActor(s);
          GameState.getInstance().getParticleSet().add(s);
          GameState.getInstance().dustEffect(s.myTile);
          if (Camera.getInstance().onScreen(s)) Sounds.getInstance().boop();
        }
      }
    }

    if (spriteProcessing == null) return;
    timeDisassemble -= delta;
    if (isSelected()) UI.getInstance().buildingSelectProgress.get(type).setValue(timeDisassemble / getTimeDisassembleMax);
    if (timeDisassemble > 0) return;
    Pair<Particle,Particle> myDecay = type.getOutputs( spriteProcessing.getParticle() );
    placeParticle( myDecay.getKey()   ); // Output #1
    placeParticle( myDecay.getValue() ); // Output #2
    GameState.getInstance().playerEnergy += type.getOutputEnergy( spriteProcessing.getParticle() );
    spriteProcessing = null; // Kill it
  }

  protected void placeParticle(Particle p) {
    if (p == null) return;
    Sprite s = new Sprite(getPathingStartPoint(p));
    s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);

    List<Tile> pList = getBuildingPathingList(p); // Do I have a standing order?
    if (pList == null) s.pathTo( s.findPathingLocation(getPathingStartPoint(p), true, true, true, false), null, null);  // random direction=True, needs parking=True, requireSameHeight=True
    else s.pathingList = new LinkedList<Tile>(pList); // Clone

    s.setTexture("ball_" + p.getColourFromParticle().getString(), 6, false);
    s.setUserObject(p);

    holdingPen.get(p).add(s); // Don't throw into world all at once
  }

  // Update my pathing orders as the world map has changed
  public void doRepath() {
    updatePathingStartPoint();
    updatePathingDestinations();
  }

  // Updates the pathing grid
  private void updatePathingGrid() {
    World w = World.getInstance();
    w.updateTilePathfinding(centre);
    for (Cardinal D1 : Cardinal.n8) {
      Tile t1 = centre.n8.get(D1);
      for (Cardinal D2 : Cardinal.n8) {
        Tile t2 = t1.n8.get(D2);
        w.updateTilePathfinding(t2);
      }
    }
  }

  public void upgradeBuilding() {
    doUpgrade = true;
    timeUpgrade = getUpgradeTime();
    GameState.getInstance().playerEnergy += getUpgradeCost();
    childSprites.lastElement().setVisible(true);
  }
}
