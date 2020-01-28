package timboe.vev.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;
import timboe.vev.pathfinding.PathFinding;

import static timboe.vev.enums.Cardinal.kNE;
import static timboe.vev.enums.Cardinal.kNW;
import static timboe.vev.enums.Cardinal.kSE;

public class Sprite extends Entity {

  private static final List<Integer> walkSearchRandom = Arrays.asList(0, 1, 2, 3);
  private static final List<Integer> walkSearchReproducible = Arrays.asList(0, 1, 2, 3);

  // Persistent
  private final Vector2 velocity;
  public final Vector2 nudgeDestination;
  public IVector2 myTile;
  public float idleTime;
  public final float boredTime; // Leave at least 10 seconds
  private Particle myParticle;
  public HashSet<Integer> bouncedBuildings = new HashSet<Integer>();

  public Sprite(Tile t) {
    super(t.coordinates.x, t.coordinates.y, Param.TILE_S * Param.SPRITE_SCALE);
    myTile = t.coordinates;
    idleTime = 0;
    myParticle = null;
    boredTime = 10f + (Param.PARTICLE_BORED_TIME * Util.R.nextFloat()); // Leave at least 10 seconds
    velocity = new Vector2();
    nudgeDestination = new Vector2();
  }

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(false);
    json.put("velocity", Util.serialiseVec2(velocity));
    json.put("nudgeDestination", Util.serialiseVec2(nudgeDestination));
    json.put("myTile", myTile.serialise());
    json.put("idleTime", idleTime);
    json.put("boredTime", boredTime);
    json.put("myParticle", myParticle == null ? JSONObject.NULL : myParticle.name());
    Integer count = 0;
    JSONObject jsonBB = new JSONObject();
    for (Integer i : bouncedBuildings) {
      jsonBB.put(count.toString(), i.toString());
      count += 1;
    }
    json.put("bouncedBuildings", jsonBB);
    return json;
  }

  public Sprite(JSONObject json) throws JSONException {
    super(json);
    JSONObject jsonBB = json.getJSONObject("bouncedBuildings");
    bouncedBuildings.clear();
    Iterator bbIt = jsonBB.keys();
    while (bbIt.hasNext()) {
      String key = (String) bbIt.next();
      bouncedBuildings.add(jsonBB.getInt(key));
    }
    if (json.get("myParticle") == JSONObject.NULL) {
      myParticle = null;
    } else {
      myParticle = Particle.valueOf(json.getString("myParticle"));
    }
    boredTime = (float) json.getDouble("boredTime");
    idleTime = (float) json.getDouble("idleTime");
    myTile = new IVector2(json.getJSONObject("myTile"));
    nudgeDestination = Util.deserialiseVec2(json.getJSONObject("nudgeDestination"));
    velocity = Util.deserialiseVec2(json.getJSONObject("velocity"));
  }

  public Particle getParticle() {
    return myParticle;
  }

  public void setParticle(Particle p) {
    myParticle = p;
  }

  public void pathTo(Tile target, Set<IVector2> solutionKnownFrom, Set<Sprite> doneSet) {
    if (target == null) return;
    Tile t = getTile();
    if (t.getPathFindNeighbours().isEmpty()) {
      Tile jumpTo = findPathingLocation(t, true, false, true, isIntro); // Find nearby, reproducible TRUE, require parking FALSE, sameHeight TRUE
      if (jumpTo != null) jumpTo.tryRegSprite(this);
    }
    if (target.getPathFindNeighbours().isEmpty())
      target = findPathingLocation(target, true, false, true, isIntro); // Find nearby, reproducible TRUE, require parking FALSE, sameHeight TRUE
    pathingList = (t != null && target != null ? PathFinding.doAStar(t.coordinates, target.coordinates, solutionKnownFrom, doneSet, GameState.getInstance().pathingCache) : null);
    if (pathingList == null)
      Gdx.app.error("pathTo", "Warning, pathTo failed for " + this + ", t=" + (t != null ? t.coordinates : "!") + " target=" + (target != null ? target.coordinates : "!"));
    if (!isIntro) idleTime = 0;
    Gdx.app.debug("pathTo", "Pathed in " + (pathingList != null ? pathingList.size() : " NULL ") + " steps");
  }

  protected float spriteVelocity() {
    return Param.PARTICLE_VELOCITY * getParticle().getSpeedMod();
  }

  private boolean doMove(float x, float y, float delta) {
    velocity.set(x - (getX() + getWidth() / 2), y - (getY() + getWidth() / 2));
    boolean atDestination = (velocity.len() < Param.PARTICLE_AT_TARGET);
    velocity.setLength(spriteVelocity());
    moveBy(velocity.x * delta, velocity.y * delta);
    return atDestination;
  }

  protected void atFinalDestination(Tile next, boolean wasParked) {
    if (!wasParked) { // I cannot stay here! Find me somewhere else
      Tile newDest = findPathingLocation(next, false, true, true, isIntro); // Wander from "next", random direction, needs parking, requireSameHeight=True
      if (newDest != null) pathTo(newDest, null, null);
    }
  }

  public Tile getTile() {
    return World.getInstance().getTile(myTile, isIntro);
  }

  protected void actMovement(float delta) {
    // Pathing
    if (pathingList != null && !pathingList.isEmpty()) { // We've got some walkin' to do
      Tile next = coordinateToTile(pathingList.get(0));
      boolean atDestination = doMove(next.centreScaleSprite.x, next.centreScaleSprite.y, delta);
      if (atDestination) { // Reached destination
        boolean wasParked = coordinateToTile(pathingList.remove(0)).tryRegSprite(this);
        if (pathingList.isEmpty()) atFinalDestination(next, wasParked);
      }
    } else if (!nudgeDestination.isZero()) { // Nudge
      boolean atDestination = doMove(nudgeDestination.x, nudgeDestination.y, delta);
      if (atDestination) nudgeDestination.setZero();
    } else if (getTile().mySprite == 0) { // I.e. I am not in a queue, otherwise this would be a building
      idleTime += delta;
    }
  }

  @Override
  public void act(float delta) {
    time += delta;
    actMovement(delta);
    if (frames == 1 || time < Param.ANIM_TIME) return;
    time = 0; // No need to catch up if we cannot maintain desired FPS
    if (idleTime > boredTime && Util.R.nextFloat() < Param.PARTICLE_WANDER_CHANCE) {
      bouncedBuildings.clear(); // Another safe place to reset the buildings that I have visited
      int newX = Util.clamp(myTile.x - (Param.PARTICLE_WANDER_R / 2) + Util.R.nextInt(Param.PARTICLE_WANDER_R), 1, Param.TILES_X - 2);
      int newY = Util.clamp(myTile.y - (Param.PARTICLE_WANDER_R / 2) + Util.R.nextInt(Param.PARTICLE_WANDER_R), 1, Param.TILES_Y - 2);
      Tile idleWander = null;
      if (Util.inBounds(newX, newY, isIntro)) {
        idleWander = GameState.getInstance().mapPathingDestination(World.getInstance().getTile(newX, newY, isIntro));
      }
      if (idleWander != null) {
        pathTo(idleWander, null, null);
        idleTime = 0;
      }
    }
    if (++frame == frames) {
      frame = 0;
    }
    ++GameState.getInstance().particleBounces;
  }

  public void setNudgeDestination(Tile t, Cardinal D) {
    nudgeDestination.set(t.getX() * Param.SPRITE_SCALE + getWidth() / 2, t.getY() * Param.SPRITE_SCALE + getHeight() / 2);
    nudgeDestination.add(D == kSE || D == kNE ? Param.TILE_S : 0, D == kNW || D == kNE ? Param.TILE_S : 0);
    if (!isIntro) idleTime = 0;
  }

  // Check a (nearby) space for its connectedness, suitability for particle and if it has parking space
  private static Tile tryWanderDest(int x, int y, int level, boolean requireSameHeight, boolean requireParking, boolean isIntroSprite) {
    Tile tempTilePtr = World.getInstance().getTile(x, y, isIntroSprite);
    if (tempTilePtr.mySprite != 0) return null; // Building or queue or vegetation
    if (tempTilePtr.getPathFindNeighbours() == null) {
      Gdx.app.error("tryWanderDest", "Tile " + tempTilePtr.coordinates.x + "," + tempTilePtr.coordinates.y + " doesn't have p.f. N. Investigate this!");
      return null;
    }
    if (tempTilePtr.getPathFindNeighbours().isEmpty()) return null; // Unpathable
    if (requireSameHeight && tempTilePtr.level != level) return null; // Different elevation
    if (requireParking && !tempTilePtr.hasParkingSpace()) return null;
    return tempTilePtr;
  }

  // Find a nearby spot. Note this randomises the direction if reproducible == false
  public static Tile findPathingLocation(final Tile t, final boolean reproducible, final boolean requireParking, final boolean requireSameHeight, final boolean isIntroSprite) {
    int tryRadius = 0;
    while (++tryRadius < Param.TILES_MAX) {
      boolean sameHeight = (tryRadius < Param.TILES_MAX / 4); // TODO tune radius at which we should try and get the same height
      if (!requireSameHeight) sameHeight = false;
      if (!reproducible) java.util.Collections.shuffle(walkSearchRandom);
      // TODO this is ugly - looking around outside of box radius tryRadius
      for (final Integer s : reproducible ? walkSearchReproducible : walkSearchRandom) {
        if (s == 0) {
          for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
            if (!Util.inBounds(x, t.coordinates.y + tryRadius, isIntroSprite)) break;
            Tile t2 = tryWanderDest(x, t.coordinates.y + tryRadius, t.level, sameHeight, requireParking, isIntroSprite);
            if (t2 != null) return t2;
          }
        } else if (s == 1) {
          for (int x = t.coordinates.x - tryRadius; x <= t.coordinates.x + tryRadius; ++x) {
            if (!Util.inBounds(x, t.coordinates.y - tryRadius, isIntroSprite)) break;
            Tile t2 = tryWanderDest(x, t.coordinates.y - tryRadius, t.level, sameHeight, requireParking, isIntroSprite);
            if (t2 != null) return t2;
          }
        } else if (s == 2) {
          for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
            if (!Util.inBounds(t.coordinates.x + tryRadius, y, isIntroSprite)) break;
            Tile t2 = tryWanderDest(t.coordinates.x + tryRadius, y, t.level, sameHeight, requireParking, isIntroSprite);
            if (t2 != null) return t2;
          }
        } else if (s == 3) {
          for (int y = t.coordinates.y - tryRadius; y <= t.coordinates.y + tryRadius; ++y) {
            if (!Util.inBounds(t.coordinates.x - tryRadius, y, isIntroSprite)) break;
            Tile t2 = tryWanderDest(t.coordinates.x - tryRadius, y, t.level, sameHeight, requireParking, isIntroSprite);
            if (t2 != null) return t2;
          }
        }
      }
    }
    Gdx.app.error("findPathingLocation", "Failed to find anywhere good for " + t.coordinates.toString() + " :(");
    return null;
  }

}
