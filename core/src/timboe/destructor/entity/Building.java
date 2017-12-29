package timboe.destructor.entity;

import timboe.destructor.Pair;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.TileType;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.OrderlyQueue;

/**
 * Created by Tim on 28/12/2017.
 */

public class Building extends Entity {

  private OrderlyQueue myQueue;
  private Tile centre;
  private float time;
  private Sprite spriteProcessing;

  public Building(Tile t) {
    super(t.coordinates.x - 1, t.coordinates.y - 1);
    centre = t;
    centre.setBuilding(this);
    for (Cardinal D : Cardinal.n8) centre.n8.get(D).setBuilding(this);
    repath();
    myQueue = new OrderlyQueue(centre.coordinates.x - 1, centre.coordinates. y - 2, null, this);
    // Move any sprites which are here
    moveOn();
    myQueue.moveOn();
  }

  public Pair<Tile, Cardinal> getFreeLocationInQueue() {
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
    time += delta;
    if (time < 1f) return;
    time -= 1f;
    if (spriteProcessing != null) {
      spriteProcessing.remove();
      spriteProcessing = null; // Kill it
    }
    myQueue.moveAlongMoveAlong();
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
