package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Particle;
import timboe.destructor.manager.GameState;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.OrderlyQueue;
import timboe.destructor.pathfinding.PathFinding;

/**
 * Created by Tim on 28/12/2017.
 */

public class Building extends Entity {

  private OrderlyQueue myQueue = null;

  private final BuildingType type;
  private final Tile centre;
  private Tile pathingStartPoint;
  private float timeDisassemble;
  private float timeMove;
  public Sprite spriteProcessing = null;
  private EnumMap<Particle, List<Sprite>> holdingPen = new EnumMap<Particle, List<Sprite>>(Particle.class);

  public Building(Tile t, BuildingType type) {
    super(t.coordinates.x - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1),
          t.coordinates.y - (type == BuildingType.kWARP ? (Param.WARP_SIZE/2) - 2 : 1));
    buildingPathingLists = new EnumMap<Particle, List<Tile>>(Particle.class);
    this.type = type;
    centre = t;
    for (Particle p : Particle.values()) holdingPen.put(p, new LinkedList<Sprite>());
    if (type == BuildingType.kWARP) return; // Warp does not need anything below
    centre.setBuilding(this);
    for (Cardinal D : Cardinal.n8) centre.n8.get(D).setBuilding(this);
    repath();
    myQueue = new OrderlyQueue(centre.coordinates.x - 1, centre.coordinates. y - 2, null, this);
    // Move any sprites which are here
    moveOn();
    myQueue.moveOn();
    updatePathingStartPoint();
  }

  public BuildingType getType() {
    return type;
  }

  public void updatePathingStartPoint() {
    pathingStartPoint = Sprite.findPathingLocation(centre, true, false, false); //reproducible=True, requireParking=False
    if (pathingStartPoint == null) {
      Gdx.app.error("updatePathingStartPoint", "Building could not find a pathing start point!");
      return;
    }
    // TODO update all pathingLists to use this now start point
  }

  protected Tile getPathingStartPoint(Particle p) {
    return pathingStartPoint;
  }

  public void updateDemoPathingList(Particle p, Tile t) {
    if (getDestination() != t) pathingList = PathFinding.doAStar(getPathingStartPoint(p), t, null, null);
    // The "pathingList" holds our speculative/demo destination
    pathingParticle = p;
  }

  public void updatePathingList() {
    if (pathingParticle == null) {
      Gdx.app.error("updatePathingList","Called with pathingParticle = null?!");
      return;
    }
    if (pathingList == null) {
      Gdx.app.error("updatePathingList","Called with pathingList = null?!");
      return;
    }
    Gdx.app.log("updatePathingList","Set pathing " + pathingParticle + " to " + pathingList.get(0).toString());
    buildingPathingLists.put(pathingParticle, pathingList);
    pathingList = null;
    pathingParticle = null;
  }

  public void cancelUpdatePathingList() {
    pathingParticle = null;
    pathingList = null;
  }



  public Pair<Tile, Cardinal> getFreeLocationInQueue(Sprite s) {
    if (!type.accepts(s)) return null;
    return myQueue.getFreeLocationInQueue();
  }

  public Tile getQueuePathingTarget() {
    return myQueue.getQueuePathingTarget();
  }

  // Moves on any sprites under the building
  private void moveOn() {
    centre.moveOnSprites();
    for (Cardinal D1 : Cardinal.n8) {
      centre.n8.get(D1).moveOnSprites();
    }
  }

  @Override
  public void act(float delta) {
    timeMove += delta;
    if (timeMove > 0.2f) {
      timeMove -= 0.2f;
      if (myQueue != null) myQueue.moveAlongMoveAlong();
      for (Particle p : Particle.values()) {
        if (holdingPen.get(p).isEmpty()) continue;
        Sprite s = holdingPen.get(p).remove(0);
        GameState.getInstance().getSpriteStage().addActor(s);
        GameState.getInstance().getParticleSet().add(s);
      }
    }
    if (spriteProcessing == null) return;
    timeDisassemble += delta;
    if (timeDisassemble < 1f) return;
    timeDisassemble -= 1f;
    Pair<Particle,Particle> myDecay = type.getOutputs( (Particle) spriteProcessing.getUserObject() );
    placeParticle( myDecay.getKey() );
    placeParticle( myDecay.getValue() );
    spriteProcessing = null; // Kill it
  }

  protected void placeParticle(Particle p) {
    if (p == null) return;
    Sprite s = new Sprite(getPathingStartPoint(p));
    s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);

    List<Tile> pList = getPathingList(p); // Do I have a standing order?
    if (pList == null) s.pathTo(getPathingStartPoint(p), null, null);
    else s.pathingList = new LinkedList<Tile>(pList); // Clone

    s.setTexture("ball_" + p.getColourFromParticle().getString(), 6, false);
    s.setUserObject(p);
    holdingPen.get(p).add(s); // Don't throw into world all at once
  }

  // Updates the pathing grid
  private void repath() {
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
}
