package timboe.vev.pathfinding;

import com.badlogic.gdx.Gdx;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import timboe.vev.Pair;
import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Building;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.TileType;
import timboe.vev.manager.GameState;
import timboe.vev.manager.World;

/**
 * Created by Tim on 28/12/2017.
 */

public class OrderlyQueue {
  final IVector2 queueStart;
  private List<IVector2> queue = new LinkedList<IVector2>();
  final int myBuilding;
  final boolean isIntro;

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("queueStart", queueStart.serialise());
    Integer count = 0;
    JSONObject q = new JSONObject();
    for (IVector2 v : queue) {
      q.put(count.toString(), v.serialise());
      ++count;
    }
    json.put("queue", q);
    json.put("myBuilding", myBuilding);
    json.put("isIntro", isIntro);
    return json;
  }

  public OrderlyQueue(JSONObject json) throws JSONException {
    myBuilding = json.getInt("myBuilding");
    isIntro = json.getBoolean("isIntro");
    JSONObject q = json.getJSONObject("queue");
    Iterator qIt = q.keys();
    int maxKey = -1;
    while (qIt.hasNext()) {
      maxKey = Math.max(maxKey, Integer.valueOf((String) qIt.next()));
    }
    if (maxKey >= 0) {
      for (Integer i = 0; i <= maxKey; ++i) {
        queue.add(new IVector2(q.getJSONObject(i.toString())));
      }
    }
    queueStart = new IVector2(json.getJSONObject("queueStart"));
  }


  public OrderlyQueue(int x, int y, List<IVector2> customQueue, Building b, boolean isIntro) {
    myBuilding = (b == null ? 0 : b.id);
    this.isIntro = isIntro;
    if (customQueue == null) doQueue(x, y);
    else queue = customQueue;
    queueStart = queue.get(0);
    repath();
    if (isIntro) {
      setAllTextures();
    }
  }

  public void deconstruct() {
    for (IVector2 v : queue) {
      Tile t = tileFromCoordinate(v);
      t.removeBuilding();
    }
    repath();
    queue.clear();
  }

  public List<IVector2> getQueue() {
    return queue;
  }


  public IVector2 getQueuePathingTarget() {
    return queue.get(queue.size() - 1);
  }

  private Tile tileFromCoordinate(IVector2 v) {
    return World.getInstance().getTile(v, isIntro);
  }

  private Building getMyBuilding() {
    return GameState.getInstance().getBuildingMap().get(myBuilding);
  }

  private void setAllTextures() {
    for (IVector2 v : getQueue()) {
      Tile t = tileFromCoordinate(v);
      t.setQueueTexture();
    }
  }

  public void moveAlongMoveAlong() {
    // Now try and move everyone along
    for (int i = 0; i < queue.size(); ++i) {
      final Tile tile = tileFromCoordinate( queue.get(i) );
      Sprite toRemove = null;
      assert tile != null;
      for (int id : tile.containedSprites) {
        Sprite s = GameState.getInstance().getParticleMap().get(id);
//        Gdx.app.log("moveAlongMoveAlong","tile " + tile.coordinates + " contains "+id+" sprite=" + s + " parked=" + tile.parkingSpaces.get(s.id));
        // Get parking space
        final Cardinal parking = tile.parkingSpaces.get(s.id);
        if (parking == null) continue; // I'm not parked here, e.g. moving over the entrance tile

        if (parking == tile.queueExit) { // Is this the final space of the tile?
          if (i == 0) { // Is this the final tile?
            // Is the sprite *actually here*
            Building b = getMyBuilding();
//            Gdx.app.log("moveAlongMoveAlong", "b.spriteProcessing="+b.spriteProcessing+" nudgeDest="+s.nudgeDestination);
            if (b.spriteProcessing == 0 && s.nudgeDestination.isZero()) { // Arrived
              if (toRemove != null) Gdx.app.error("moveAlongMoveAlong", "should only ever be one toRemove");
              // Goodby - this particle is now DEAD
              toRemove = s; // from its tile
              b.processSprite(s);
            }
          } else { // Not the final tile.
            // Is the entrance of the next tile free?
            Tile nextTile = tileFromCoordinate( queue.get(i - 1) );
            Cardinal nextTileEntrance = nextTile.queueExit.next90( nextTile.queueClockwise );
            if (!nextTile.parkingSpaces.containsValue(nextTileEntrance)) {
              // Move me to here
              if (toRemove != null) Gdx.app.error("moveAlongMoveAlong", "should only ever be one toRemove");
              toRemove = s;
              // Move on to next tile
              nextTile.parkSprite(s, nextTileEntrance);
            }
          }
        } else { // This is *NOT* the final space on this tile, can we move on?
          Cardinal nextParking = parking.next90( tile.queueClockwise );
          if (!tile.parkingSpaces.containsValue(nextParking)) {
            // Move me to here
            tile.parkSprite(s, nextParking);
          }
        }
      }
      // Can only remove reference from the tile at the end
      if (toRemove != null) tile.deRegSprite(toRemove);
    }
  }

  // New sprite is trying to enter the queue
  public Pair<Tile, Cardinal> getFreeLocationInQueue() {
    // Back iterate over the queue
    Tile previousT = null;
    Cardinal previousD = null;
    ListIterator<IVector2> liTile = queue.listIterator( queue.size() );

    while(liTile.hasPrevious()) {
      final Tile t = tileFromCoordinate( liTile.previous() );
      final Cardinal queueStart = t.queueExit.next90( t.queueClockwise );
      Cardinal D = queueStart;
      do {
        if (t.parkingSpaces.containsValue(D)) { // Someone is here - go for the previous place
          Gdx.app.debug("getFreeLocationInQueue","Accepted sprite to "+t.coordinates+" "+D.getString());
          if (previousT != null) return new Pair<Tile, Cardinal>(previousT, previousD);
          return null;
        } else { // We can put the sprite here! Make a note
          previousT = t;
          previousD = D;
        }
        D = D.next90( t.queueClockwise ); // Not as we are reverse iterating
      } while (D != queueStart);
    }

    // Check final slot
    Tile queueFinal = tileFromCoordinate( queue.get(0) );
    if (!queueFinal.parkingSpaces.containsValue(queueFinal.queueExit)) {
      return new Pair<Tile,Cardinal>(queueFinal, queueFinal.queueExit);
    }

    // If we made it to the end - we can also place in the first slot
    if (previousT != null) return new Pair<Tile, Cardinal>(previousT, previousD);
    return null;
  }

    // Moves on any sprites under the queue
  public void moveOn() {
    for (IVector2 v : queue) {
      tileFromCoordinate( v ).moveOnSprites();
    }
  }

  // Re-do the pathing grid for all tiles in the queue and all touching it
  private void repath() {
    for (IVector2 v : queue) {
      Tile t = tileFromCoordinate(v);
      for (Cardinal D : Cardinal.n8) {
        World.getInstance().updateTilePathfinding( t.n8.get(D));
      }
    }
  }

  public static void hintQueue(final Tile start) {
    switch (GameState.getInstance().queueType) {
      case kSIMPLE: hintSimpleQueue(start); break;
      case kSPIRAL: hintSpiralQueue(start); break;
      default: Gdx.app.error("hintQueue","Unknown - " + GameState.getInstance().queueType);
    }
  }

  private static void hintSpiralQueue(final Tile start) {
    Tile t = start;
    int step = 0, move = 3, toAdd =3;
    boolean inc = true;
    Cardinal D = Cardinal.kE;
    while (step++ < GameState.getInstance().queueSize) {
      if (!t.buildable()) return;
      t.setHighlightColour(Param.HIGHLIGHT_YELLOW, (move == toAdd ? D.next90(true) : D));
      t = t.n8.get(D);
      if (--move == 0) {
        if (inc) ++toAdd;
        inc = !inc;
        move += toAdd;
        D = D.next90(false);
      }
    }
  }

  private static void hintSimpleQueue(final Tile start) {
    int step = 0, x = start.coordinates.x, y = start.coordinates.y, move = 3;
    Cardinal D = Cardinal.kE;
    World w = World.getInstance();
    while (step++ < GameState.getInstance().queueSize) {
      if (!Util.inBounds(x,y,false) || !w.getTile(x,y,false).buildable()) return;
      w.getTile(x,y,false).setHighlightColour(Param.HIGHLIGHT_YELLOW, D);
      if (Math.abs(move) > 1) {
        x += 1 * Math.signum(move);
        move -= 1 * Math.signum(move);
        D = (move > 0 ? Cardinal.kE : Cardinal.kW);
      } else {
        --y;
        move *= -3;
        D = Cardinal.kS;
      }
    }
  }

  private void doQueue(int xStart, int yStart) {
    switch (GameState.getInstance().queueType) {
      case kSIMPLE: doSimpleQueue(xStart, yStart); break;
      case kSPIRAL: doSpiralQueue(xStart, yStart); break;
      default: Gdx.app.error("hintQueue","Unknown - " + GameState.getInstance().queueType);
    }
    tileFromCoordinate( queue.get( queue.size()-1 )).type = TileType.kGROUND; // Re-set to ground to make path-able
  }

  private void doSpiralQueue(int xStart, int yStart) {
    Tile t = World.getInstance().getTile(xStart, yStart, isIntro);
    int step = 0, move = 3, toAdd =3;
    boolean inc = true;
    Cardinal D = Cardinal.kE, previousD = Cardinal.kN;
    while (step++ < GameState.getInstance().queueSize) {
      if (!t.buildable()) return;
      Cardinal from, to;
      if (D == previousD) {
        from = D;
        to = D.next90(true).next90(true); // 180deg
      } else { // We just turned a corner
        from = D;
        to = D.next90(false);
      }
      Cardinal exit = getExitLocation(to);
      boolean isClockwise = getQueueClockwise(from, to);
      t.setQueue(from, to, myBuilding, exit, isClockwise);
      queue.add(t.coordinates);

      t = t.n8.get(D);
      previousD = D;
      if (--move == 0) {
        if (inc) ++toAdd;
        inc = !inc;
        move += toAdd;
        D = D.next90(false);
      }
    }
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
    while (step++ < GameState.getInstance().queueSize) {
      Cardinal D = getExitLocation(v.get(element).getValue());
      boolean isClockwise = getQueueClockwise(v.get(element).getKey(), v.get(element).getValue());
      Tile t = w.getTile(x, y, isIntro);
      if (!t.buildable()) break;
      t.setQueue(v.get(element).getKey(), v.get(element).getValue(), myBuilding, D, isClockwise);
      queue.add( w.getTile(x, y, isIntro).coordinates );
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

  // TODO figure out the pattern!
  private boolean getQueueClockwise(Cardinal from, Cardinal to) {
    if      (from == Cardinal.kE && to == Cardinal.kN) return true;
    // E -> S false
    // E -> W false

    else if (from == Cardinal.kW && to == Cardinal.kS) return true;
    // W -> N fase
    // W -> E false

    else if (from == Cardinal.kS && to == Cardinal.kE) return true;
    // S -> W false
    else if (from == Cardinal.kS && to == Cardinal.kN) return true;

    else if (from == Cardinal.kN && to == Cardinal.kW) return true;
    // N -> E false
    else if (from == Cardinal.kN && to == Cardinal.kS) return true;

    return false;
  }

  private Cardinal getExitLocation(Cardinal to) {
    if (to == Cardinal.kN || to == Cardinal.kE) return Cardinal.kNE;
    return Cardinal.kSW;
  }
}
