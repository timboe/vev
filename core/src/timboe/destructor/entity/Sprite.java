package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

  public List<Tile> pathingList;
  private Vector2 velocity = new Vector2();
  public Vector2 nudgeDestination = new Vector2();
  private List<Integer> walkSearchList = Arrays.asList(0,1,2,3);
  public Tile myTile;

  public Sprite(int x, int y, Tile t) {
    super(x, y, Param.TILE_S * Param.SPRITE_SCALE);
    myTile = t;
  }

  public void pathTo(Tile target, Set<Tile> solutionKnownFrom, Set<Sprite> doneSet) {
    if (target == null) return;
    if (myTile.getPathFindNeighbours().isEmpty()) {
      Tile jumpTo = findPathingNearbyLocation(myTile);
      jumpTo.tryRegSprite(this);
    }
    if (target.getPathFindNeighbours().isEmpty()) target = findPathingNearbyLocation(target);
    pathingList = PathFinding.doAStar(myTile, target, solutionKnownFrom, doneSet);
    if (pathingList == null) Gdx.app.error("pathTo", "Warning, pathTo failed for " + this);
//    Gdx.app.log("pathTo", "Pathed in " + (pathingList != null ? pathingList.size() : " NULL ") + " steps");
  }

  public Tile getDestination() {
    if (pathingList == null || pathingList.isEmpty()) return null;
    return pathingList.get( pathingList.size() - 1 );
  }

  private boolean doMove(float x, float y, float delta) {
    velocity.set(x - (getX() + getWidth()/2), y - (getY() + getWidth()/2));
    boolean atDestination = (velocity.len() < Param.SPRITE_AT_TARGET);
    velocity.setLength(Param.SPRITE_VELOCITY);
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
          doWanderFrom(next);
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

  // If I end up on an invalid tile, or my destination is invalidated - find a new start/stop point
  public static Tile findPathingNearbyLocation(final Tile t) {
    int tryRadius = 0;
    while (++tryRadius < Param.TILES_MAX) {
      for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
        for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
          if (!Util.inBounds(x,y)) continue;
          Tile tempTilePtr = World.getInstance().getTile(x, y);
          if (tempTilePtr == t) continue; // Do not return self as option
          if (tempTilePtr.mySprite != null) continue; // Building or queue or vegetation
          if (tempTilePtr.level != t.level) continue; // Different level
          if (!tempTilePtr.getPathFindNeighbours().isEmpty()) return tempTilePtr;
        }
      }
    }
    return null;
  }

  // Can I wander to this nearby space? is there room to park? (If so - initiate pathing, should be fast!)
  private boolean tryWanderDest(int x, int y, int level, int sameHeight) {
    Tile tempTilePtr = World.getInstance().getTile(x, y);
    // mySprite check to avoid vegetation, queues or buildings
    if (sameHeight == 1 && tempTilePtr.level != level) return false;
    if (tempTilePtr.hasParkingSpace() && tempTilePtr.mySprite == null) {
      pathTo( tempTilePtr, null, null );
      return true;
    }
    return false;
  }

  // I cannot park in my destination - find me a nearby spot. Note this randomises the direction
  private void doWanderFrom(final Tile t) {
    for (int sameHeight = 1; sameHeight >= 0; --sameHeight) { // Try and stay on the level
      int tryRadius = 0;
      while (++tryRadius < Param.TILES_MAX) {
        // Try these four in random order
        //      int invertX = Util.R.nextBoolean() ? 1 : -1, invertY = Util.R.nextBoolean() ? 1 : -1;
        //      for (int x = t.coordinates.x - (invertX * tryRadius); x <= t.coordinates.x + (invertX * tryRadius); x += invertX) {
        //        for (int y = t.coordinates.y - (invertY * tryRadius); y <= t.coordinates.y + (invertY * tryRadius); y += invertY) {
        //          if (tryWanderDest(x, y)) return;
        //        }
        //      }
        java.util.Collections.shuffle(walkSearchList); // TODO this is ugly
        for (final Integer s : walkSearchList) {
          if (s == 0) {
            for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
              if (!Util.inBounds(x, t.coordinates.y + tryRadius)) break;
              if (tryWanderDest(x, t.coordinates.y + tryRadius, t.level, sameHeight)) return;
            }
          } else if (s == 1) {
            for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
              if (!Util.inBounds(x, t.coordinates.y - tryRadius)) break;
              if (tryWanderDest(x, t.coordinates.y - tryRadius, t.level, sameHeight)) return;
            }
          } else if (s == 2) {
            for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
              if (!Util.inBounds(t.coordinates.x + tryRadius, y)) break;
              if (tryWanderDest(t.coordinates.x + tryRadius, y, t.level, sameHeight)) return;
            }
          } else if (s == 3) {
            for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
              if (!Util.inBounds(t.coordinates.x - tryRadius, y)) break;
              if (tryWanderDest(t.coordinates.x - tryRadius, y, t.level, sameHeight)) return;
            }
          }
        }
      }
    }
  }

  public void draw(ShapeRenderer sr) {
    if (Param.DEBUG > 0 && pathingList != null && pathingList.size() > 1) {
      for (int i = 1; i < pathingList.size(); ++i) {
        Tile previous = pathingList.get(i-1);
        Tile current = pathingList.get(i);
        sr.line(previous.centreScaleSprite, current.centreScaleSprite);
      }
    }
    if (!selected) return;
    float off = Param.FRAME * 0.25f / (float)Math.PI;
    final float xC = getX() + getWidth()/2f, yC = getY() + getHeight()/2f;
    for (float a = (float)-Math.PI; a < Math.PI; a += 2f*Math.PI/3f) {
      sr.line(xC + getWidth() * ((float) Math.cos(a + off)),
              yC + getHeight() * ((float) Math.sin(a + off)),
              xC + getWidth()/2f * ((float) Math.cos(a + off + Math.PI / 6f)),
              yC + getHeight()/2f * ((float) Math.sin(a + off + Math.PI / 6f)));
    }
  }

}
