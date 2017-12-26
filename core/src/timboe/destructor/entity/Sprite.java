package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.PathFinding;

import java.util.List;
import java.util.Set;

import static timboe.destructor.enums.Cardinal.kNE;
import static timboe.destructor.enums.Cardinal.kNW;
import static timboe.destructor.enums.Cardinal.kSE;

public class Sprite extends Entity {

  public List<Tile> pathingList;
  private Vector2 velocity = new Vector2();
  private Vector2 nudgeDestination = new Vector2();

  public Sprite(int x, int y) {
    super(x, y, Param.TILE_S * Param.SPRITE_SCALE);
  }

  public void pathTo(Tile target, Set<Tile> solutionKnownFrom, Set<Sprite> doneSet) {
    if (target == null) return;
    pathingList = PathFinding.doAStar(getMyTile(), target, solutionKnownFrom, doneSet);
    if (pathingList == null) Gdx.app.error("pathTo", "Warning, pathTo failed for " + this);
//    Gdx.app.log("pathTo", "Pathed in " + (pathingList != null ? pathingList.size() : " NULL ") + " steps");
  }

  public Tile getDestination() {
    if (pathingList == null || pathingList.isEmpty()) return null;
    return pathingList.get( pathingList.size() - 1 );
  }

  public Tile getMyTile() {
    return World.getInstance().getTile(Math.round(getX() / scale), Math.round(getY() / scale));
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
        Cardinal parking = pathingList.remove(0).regSprite(this);
        if (pathingList.isEmpty()) {
          if (parking == Cardinal.kNONE) { // I cannot stay here! Find me somewhere else
            doWanderFrom(next);
          } else { // Move to my resting location
            nudgeDestination.set(getX(), getY());
            nudgeDestination.add(parking == kSE || parking == kNE ? Param.TILE_S : 0, parking == kNW || parking == kNE ? Param.TILE_S : 0);
          }
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

  // Can I wander to this nearby space? is there room to park? (If so - initiate pathing, should be fast!)
  private boolean tryWanderDest(int x, int y) {
    if (!Util.inBounds(x,y)) return false;
    Tile tempTilePtr = World.getInstance().getTile(x, y);
    if (tempTilePtr.hasParkingSpace()) {
      pathTo( tempTilePtr, null, null );
      return true;
    }
    return false;
  }

  // I cannot park in my destination - find me a nearby spot
  private void doWanderFrom(final Tile t) {
    int tryRadius = 0;
    while (++tryRadius < 1e6) { // Danger! This number is in essence infinity
      for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
        if (tryWanderDest(x, t.coordinates.y + tryRadius)) return;
        if (tryWanderDest(x, t.coordinates.y - tryRadius)) return;
      }
      for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
        if (tryWanderDest(t.coordinates.x + tryRadius, y)) return;
        if (tryWanderDest(t.coordinates.x - tryRadius, y)) return;
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
    int off = 2 * (Param.FRAME % 360);
    for (float a = 0; a < 360; a += 120) {
      sr.arc( getX() + getWidth()/2, getY() + getWidth()/2, getWidth(), a + off, 30);
      sr.arc( getX() + getWidth()/2, getY() + getWidth()/2, getWidth(), a - off, 30);
    }
  }

}
