package timboe.destructor.entity;

import timboe.destructor.Pair;
import timboe.destructor.enums.BuildingType;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.TileType;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.OrderlyQueue;

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
  private float timeDissasemble;
  private float timeMove;
  public Sprite spriteProcessing = null;

  public Building(Tile t, BuildingType type) {
    super(t.coordinates.x - 1, t.coordinates.y - 1);
    this.type = type;
    centre = t;
    centre.setBuilding(this);
    for (Cardinal D : Cardinal.n8) centre.n8.get(D).setBuilding(this);
    repath();
    myQueue = new OrderlyQueue(centre.coordinates.x - 1, centre.coordinates. y - 2, null, this);
    // Move any sprites which are here
    moveOn();
    myQueue.moveOn();
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
    timeDissasemble += delta;
    if (timeMove > 0.2f) {
      timeMove -= 0.2f;
      myQueue.moveAlongMoveAlong();
    }
    if (timeDissasemble < 1f) return;
    timeDissasemble -= 1f;
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
