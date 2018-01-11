package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.PathFinding;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static timboe.destructor.enums.Cardinal.kNE;
import static timboe.destructor.enums.Cardinal.kNW;
import static timboe.destructor.enums.Cardinal.kSE;

public class Sprite extends Entity {

  private final Vector2 velocity = new Vector2();
  public final Vector2 nudgeDestination = new Vector2();
  private static final List<Integer> walkSearchRandom = Arrays.asList(0,1,2,3);
  private static final List<Integer> walkSearchReproducible = Arrays.asList(0,1,2,3);
  public Tile myTile;

  public Sprite(Tile t) {
    super(t.coordinates.x, t.coordinates.y, Param.TILE_S * Param.SPRITE_SCALE);


//    this.addListener(new InputListener() {
//      public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//        Gdx.app.log("SPRITE","clicked");
//      }
//    });

    myTile = t;
  }

  public void pathTo(Tile target, Set<Tile> solutionKnownFrom, Set<Sprite> doneSet) {
    if (target == null) return;
    if (myTile.getPathFindNeighbours().isEmpty()) {
      Tile jumpTo = findPathingLocation(myTile, true, false, true); // Find nearby, reproducible TRUE, require parking FALSE, sameHeight TRUE
      jumpTo.tryRegSprite(this);
    }
    if (target.getPathFindNeighbours().isEmpty()) target = findPathingLocation(target, true, false, true); // Find nearby, reproducible TRUE, require parking FALSE, sameHeight TRUE
    pathingList = PathFinding.doAStar(myTile, target, solutionKnownFrom, doneSet);
    if (pathingList == null) Gdx.app.error("pathTo", "Warning, pathTo failed for " + this);
//    Gdx.app.log("pathTo", "Pathed in " + (pathingList != null ? pathingList.size() : " NULL ") + " steps");
  }

  private boolean doMove(float x, float y, float delta) {
    velocity.set(x - (getX() + getWidth()/2), y - (getY() + getWidth()/2));
    boolean atDestination = (velocity.len() < Param.PARTICLE_AT_TARGET);
    velocity.setLength(Param.PARTICLE_VELOCITY);
    moveBy(velocity.x * delta, velocity.y * delta);
    return atDestination;
  }

  @Override
  public void act(float delta) {
    time += delta;
    // Pathing
    if(pathingList != null && !pathingList.isEmpty()) { // We've got some walkin' to do
      Tile next = pathingList.get(0);
      boolean atDestination = doMove(next.centreScaleSprite.x, next.centreScaleSprite.y, delta);
      if (atDestination) { // Reached destination
        boolean wasParked = pathingList.remove(0).tryRegSprite(this);
        if (pathingList.isEmpty() && !wasParked) { // I cannot stay here! Find me somewhere else
          Tile newDest = findPathingLocation(next, false, true, true); // Wander from "next", random direction, needs parking, requireSameHeight=True
          if (newDest != null) pathTo(newDest, null, null);
        }
      }
    } else if (!nudgeDestination.isZero()) { // Nudge
      boolean atDestination = doMove(nudgeDestination.x, nudgeDestination.y, delta);
      if (atDestination) nudgeDestination.setZero();
    }
    if (frames == 1 || time < Param.ANIM_TIME) return;
    time -= Param.ANIM_TIME;
    if (++frame == frames) {
      frame = 0;
//      Sounds.getInstance().foot();
    }
  }

  public void setNudgeDestination(Tile t, Cardinal D) {
    nudgeDestination.set(t.getX() * Param.SPRITE_SCALE + getWidth()/2, t.getY() * Param.SPRITE_SCALE + getHeight()/2);
    nudgeDestination.add(D == kSE || D == kNE ? Param.TILE_S : 0, D == kNW || D== kNE ? Param.TILE_S : 0);
  }

  // Check a (nearby) space for its connectedness, suitability for particle and if it has parking space
  private static Tile tryWanderDest(int x, int y, int level, boolean requireSameHeight, boolean requireParking) {
    Tile tempTilePtr = World.getInstance().getTile(x, y);
    if (tempTilePtr.mySprite != null) return null; // Building or queue or vegetation
    if (tempTilePtr.getPathFindNeighbours().isEmpty()) return null; // Unpathable
    if (requireSameHeight && tempTilePtr.level != level) return null; // Different elevation
    if (requireParking && !tempTilePtr.hasParkingSpace()) return null;
    return tempTilePtr;
  }

  // Find a nearby spot. Note this randomises the direction if reproducible == false
  public static Tile findPathingLocation(final Tile t, final boolean reproducible, final boolean requireParking, final boolean requireSameHeight) {
    int tryRadius = 0;
    while (++tryRadius < Param.TILES_MAX) {
      boolean sameHeight = (tryRadius < Param.TILES_MAX / 4); // TODO tune radius at which we should try and get the same height
      if (!requireSameHeight) sameHeight = false;
      if (!reproducible) java.util.Collections.shuffle(walkSearchRandom);
      // TODO this is ugly - looking around outside of box radius tryRadius
      for (final Integer s : reproducible ? walkSearchReproducible : walkSearchRandom) {
        if (s == 0) {
          for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
            if (!Util.inBounds(x, t.coordinates.y + tryRadius)) break;
            Tile t2 = tryWanderDest(x, t.coordinates.y + tryRadius, t.level, sameHeight, requireParking);
            if (t2 != null) return t2;
          }
        } else if (s == 1) {
          for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
            if (!Util.inBounds(x, t.coordinates.y - tryRadius)) break;
            Tile t2 = tryWanderDest(x, t.coordinates.y - tryRadius, t.level, sameHeight, requireParking);
            if (t2 != null) return t2;
          }
        } else if (s == 2) {
          for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
            if (!Util.inBounds(t.coordinates.x + tryRadius, y)) break;
            Tile t2 = tryWanderDest(t.coordinates.x + tryRadius, y, t.level, sameHeight, requireParking);
            if (t2 != null) return t2;
          }
        } else if (s == 3) {
          for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
            if (!Util.inBounds(t.coordinates.x - tryRadius, y)) break;
            Tile t2 = tryWanderDest(t.coordinates.x - tryRadius, y, t.level, sameHeight, requireParking);
            if (t2 != null) return t2;
          }
        }
      }
    }
    return null;
  }

}
