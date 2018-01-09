package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;

import java.util.EnumMap;
import java.util.List;

import timboe.destructor.Pair;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Particle;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.OrderlyQueue;
import timboe.destructor.pathfinding.PathFinding;

/**
 * Created by Tim on 28/12/2017.
 */

public class Building extends Entity {

  private final OrderlyQueue myQueue;

  public BuildingType getType() {
    return type;
  }

  private final BuildingType type;
  private final Tile centre;
  private Tile pathingStartPoint;
  private float timeDisassemble;
  private float timeMove;
  public Sprite spriteProcessing = null;

  public Building(Tile t, BuildingType type) {
    super(t.coordinates.x - 1, t.coordinates.y - 1);
    buildingPathingLists = new EnumMap<Particle, List<Tile>>(Particle.class);
    this.type = type;
    centre = t;
    centre.setBuilding(this);
    for (Cardinal D : Cardinal.n8) centre.n8.get(D).setBuilding(this);
    repath();
    myQueue = new OrderlyQueue(centre.coordinates.x - 1, centre.coordinates. y - 2, null, this);
    // Move any sprites which are here
    moveOn();
    myQueue.moveOn();
    updatePathingStartPoint();
  }

  public void updatePathingStartPoint() {
    pathingStartPoint = Sprite.findPathingLocation(centre, true, false); //reproducible=True, requireParking=False
    if (pathingStartPoint == null) {
      Gdx.app.error("updatePathingStartPoint", "Building could not find a pathing start point!");
      return;
    }
    // TODO update all pathingLists to use this now start point
  }

  public void updateDemoPathingList(Particle p, Tile t) {
    if (getPathingDestination() != t) pathingList = PathFinding.doAStar(pathingStartPoint, t, null, null);
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

  public Tile getPathingDestination() {
    return myQueue.getPathingDestination();
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
    timeDisassemble += delta;
    if (timeMove > 0.2f) {
      timeMove -= 0.2f;
      myQueue.moveAlongMoveAlong();
    }
    if (timeDisassemble < 1f) return;
    timeDisassemble -= 1f;
    spriteProcessing = null; // Kill it
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
