package timboe.destructor.pathfinding;

import com.badlogic.gdx.Gdx;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import sun.awt.geom.AreaOp;
import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Building;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.TileType;
import timboe.destructor.manager.World;

/**
 * Created by Tim on 28/12/2017.
 */

public class OrderlyQueue {
  Tile queueStart;
  List<Tile> queue = new LinkedList<Tile>();
  Building myBuilding;

  public OrderlyQueue(int x, int y, List<Tile> customQueue, Building b) {
    myBuilding = b;
    if (customQueue == null) doSimpleQueue(x, y);
    else queue = customQueue;
    queueStart = queue.get(0);
    repath();
  }

  public Tile getPathingDestination() {
    return queue.get( queue.size()-1 );
  }

  public void moveAlongMoveAlong() {
    // If at 1st - move to building
//    if (queueStart.parkingSpaces.containsValue( queueStart.queue.get(0) )) {
//      Sprite s = queueStart.parkingSpaces.
//    }
//    if (queue.get(0).queue.get(0))

      // get if sprite at end - add it to the building
    // move all sprites down one

    // Forward iterator - if free, move next to me


  }

  // New sprite is trying to enter the queue
  public Pair<Tile, Cardinal> getFreeLocationInQueue() {
    // Back iterate over the queue
    Tile previousT = null;
    Cardinal previousD = null;
    ListIterator<Tile> liTile = queue.listIterator( queue.size() );
    while(liTile.hasPrevious()) {
      Tile t = liTile.previous();
      ListIterator<Cardinal> liCardinal = t.queue.listIterator( t.queue.size() );
      while (liCardinal.hasPrevious()) {
        Cardinal D = liCardinal.previous();
        if (t.parkingSpaces.containsValue(D)) { // Someone is here - go for the previous place
          Gdx.app.log("getFreeLocationInQueue","Accepted sprite to "+t.coordinates+" "+D.getString());
          if (previousT != null) return new Pair<Tile, Cardinal>(previousT, previousD);
          return null;
        } else { // We can put the sprite here! Make a note
          previousT = t;
          previousD = D;
        }
      }
    }
    // If we made it to the end - we can place in the final slot
    if (previousT != null) return new Pair<Tile, Cardinal>(previousT, previousD);
    return null;
  }

    // Moves on any sprites under the queue
  public void moveOn() {
    for (Tile t : queue) {
      t.moveOnSprites();
    }
  }

  // Re-do the pathing grid for all tiles in the queue and all touching it
  private void repath() {
    for (Tile t : queue) {
      for (Cardinal D : Cardinal.n8) {
        World.getInstance().updateTilePathfinding(t.n8.get(D));
      }
    }
  }

  public static boolean canDoSimpleQueue(final Tile start, boolean doTint) {
    int step = 0, x = start.coordinates.x, y = start.coordinates.y, move = 3;
    World w = World.getInstance();
    while (step++ < Param.QUEUE_SIZE) {
      if (!Util.inBounds(x,y)) return false;
      if (!w.getTile(x,y).buildable()) return false;
      if (doTint) w.getTile(x,y).setHighlight(true);
      if (Math.abs(move) > 1) {
        x += 1 * Math.signum(move);
        move -= 1 * Math.signum(move);
      } else {
        --y;
        move *= -3;
      }
    }
    return true;
  }

  private void doSimpleQueue(int xStart, int yStart) {
    int step = 0, x = xStart, y = yStart, move = 3, element = 0;
    World w = World.getInstance();
    Vector<Pair<Cardinal, Cardinal>> v = new Vector<Pair<Cardinal, Cardinal>>();
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kE, Cardinal.kN));
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kE, Cardinal.kW));
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kS, Cardinal.kW));
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kW, Cardinal.kN));
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kW, Cardinal.kE));
    v.add(new Pair<Cardinal, Cardinal>(Cardinal.kS, Cardinal.kE));
    while (step++ < Param.QUEUE_SIZE) {
      List<Cardinal> tileQueue = getTilesQueue(v.get(element).getKey(), v.get(element).getValue());
      w.getTile(x, y).setQueue(v.get(element).getKey(), v.get(element).getValue(), myBuilding, tileQueue, step < Param.QUEUE_SIZE);
      queue.add( w.getTile(x, y) );
      if (++element == v.size()) element = 0;
      if (Math.abs(move) > 1) {
        x += 1 * Math.signum(move);
        move -= 1 * Math.signum(move);
      } else {
        --y;
        move *= -3;
      }
    }
  }

  private List<Cardinal> getTilesQueue(Cardinal from, Cardinal to) {
    List<Cardinal> l = new LinkedList<Cardinal>();
    boolean clockwise = true;
    if (from == Cardinal.kE && to == Cardinal.kN) {
      l.add(Cardinal.kNE);
      clockwise = false;
    } else if (from == Cardinal.kW && to == Cardinal.kN) {
      l.add(Cardinal.kNE);
    } else if (from == Cardinal.kE && to == Cardinal.kW) {
      l.add(Cardinal.kSW);
    } else if (from == Cardinal.kW && to == Cardinal.kE) {
      l.add(Cardinal.kNE);
    } else if (from == Cardinal.kS && to == Cardinal.kW) {
      l.add(Cardinal.kSW);
    } else if (from == Cardinal.kS && to == Cardinal.kE) {
      l.add(Cardinal.kNE);
      clockwise = false;
    } else {
      Gdx.app.error("getTilesQueue", "Need to add for from:" + from.getString() + " to:" + to.getString());
      l.add(Cardinal.kNE); // any random one
    }

    while (l.size() < Cardinal.corners.size()) {
      l.add( clockwise ? l.get(l.size()-1).next90() : l.get(l.size()-1).minus90() );
    }
    return l;
  }
}
