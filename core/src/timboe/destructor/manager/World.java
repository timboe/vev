package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import javafx.util.Pair;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Entity;
import timboe.destructor.entity.Tile;
import timboe.destructor.entity.Zone;
import timboe.destructor.enums.*;
import timboe.destructor.pathfinding.IVector2;

import java.util.*;

public class World {

  private static World ourInstance;
  public static World getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new World(); }
  public void dispose() { ourInstance = null; }
  private Random R = new Random();
  private Vector<Zone> allZones = new Vector<Zone>();
  public boolean isReady;

  private Tile[][] tiles;
  private Zone[][] zones;

  private World() {
    generate();
  }

  private void reset() {
    isReady = false;
    GameState.getInstance().reset();
    tiles = new Tile[Param.TILES_X][Param.TILES_Y];
    for (int x = 0; x < Param.TILES_X; ++x) {
      for (int y = 0; y < Param.TILES_Y; ++y) {
        tiles[x][y] = new Tile(x,y);
        if (Param.DEBUG > 1) tiles[x][y].setDebug(true);
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
    zones = new Zone[Param.ZONES_X][Param.ZONES_Y];
    allZones.clear();
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y] = new Zone(x,y);
        allZones.add(zones[x][y]);
        if (Param.DEBUG > 0) {
          zones[x][y].setDebug(true);
          GameState.getInstance().getStage().addActor(zones[x][y]);
        }
      }
    }
  }

  public void generate() {
    Gdx.app.log("World","Generating...");
    boolean success = false;
    int worldTry = 0;
    Vector<IVector2> edges = new Vector<IVector2>();
    edges.add(new IVector2(0,0));
    edges.add(new IVector2(0,Param.TILES_Y));
    edges.add(new IVector2(Param.TILES_X,Param.TILES_Y));
    edges.add(new IVector2(Param.TILES_X,0));
    edges.add(new IVector2(0,0));
    while (!success) {
      ++worldTry;
      reset();
      success = doZones();
      Gdx.app.log("doWorld","let there be land");
      if (success) success = doEdges(edges, allZones, Colour.kBLACK, Colour.kRED, 0, 1, 5, 10);
      if (success) success = doSpecialZones(true);
      if (success) success = removeGreenStubs();
      if (success) success = doSpecialZones(false);
    }
    applyTileGraphics();
    isReady = true;
    Gdx.app.log("World","Generation finished, try " + worldTry);
    for (int y = Param.ZONES_Y-1; y >= 0; --y) Gdx.app.log( "",(zones[0][y].colour == Colour.kRED ? "R " : "G ") + (zones[1][y].colour == Colour.kRED ? "R " : "G ") + (zones[2][y].colour == Colour.kRED ? "R " : "G ") );
    for (int y = Param.ZONES_Y-1; y >= 0; --y) Gdx.app.log( "",zones[0][y].level + " " + zones[1][y].level + " " + zones[2][y].level);
  }

  private boolean removeGreenStubs() {
    boolean removed;
    do { //Iterative
      removed = false;
      for (int x = 1; x < Param.TILES_X; ++x) {
        for (int y = 0; y < Param.TILES_Y; ++y) {
          if (tiles[x][y].colour != Colour.kGREEN) continue;
          Map<Cardinal, Tile> n = collateNeighbours(x, y);
          int c = 0;
          if (n.get(Cardinal.kN).colour == Colour.kRED) ++c;
          if (n.get(Cardinal.kE).colour == Colour.kRED) ++c;
          if (n.get(Cardinal.kS).colour == Colour.kRED) ++c;
          if (n.get(Cardinal.kW).colour == Colour.kRED) ++c;
          if (c >= 3) {
            tiles[x][y].colour = Colour.kRED;
            removed = true;
          }
        }
      }
    } while (removed);
    return true;
  }

  private boolean doZones() {
//    zones[0][0].colour = Colour.kGREEN;
//    zones[0][0].level = 2;
//    zones[0][0].hillWithinHill = true;
//    if (true) return true;
    int nGreen = 0;
    int nHill = 0;
    int nGreenHill = 0;
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y].colour = (R.nextBoolean() ? Colour.kGREEN : Colour.kRED);
        final int heightMod = (zones[x][y].colour == Colour.kGREEN ? 1 : -1);
        boolean hill = R.nextBoolean();
        zones[x][y].level = (hill ? 1 : 1 + heightMod);
        if (hill && R.nextFloat() < 1f) zones[x][y].hillWithinHill = true;
        if (zones[x][y].colour == Colour.kGREEN) ++nGreen;
        if (zones[x][y].level == 2) ++nHill;
        if (zones[x][y].colour == Colour.kGREEN && zones[x][y].level == 2) ++nGreenHill;
      }
    }
    return (nGreen >= Param.MIN_GREEN_ZONE);
  }

  private void increaseStep(IVector2 v, Cardinal D, Colour c, int level) {
    setGround(v, c, level);
    switch (D) {
      case kN: ++v.y; break;
      case kE: ++v.x; break;
      case kS: --v.y; break;
      case kW: --v.x; break;
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    if (Util.inBounds(v)) setGround(v, c, level);
  }

  private boolean reachedDestination(final IVector2 v, final Cardinal D, final int endOffset,
                                     final IVector2 destination, final boolean rightTurnNext) {
    final int flip = (rightTurnNext ? 1 : -1);
    switch (D) {
      case kN: return (v.y == destination.y - (endOffset * flip));
      case kE: return (v.x == destination.x - (endOffset * flip));
      case kS: return (v.y == destination.y + (endOffset * flip));
      case kW: return (v.x == destination.x + (endOffset * flip));
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    return false;
  }

  private boolean sameColourAndLevel(final IVector2 start, final int offX, final int offY,
                                     final Colour fromC, final Colour toC,
                                     final int fromLevel, final int toLevel) {
    //if (true) return true;
    IVector2 test = new IVector2(start.x + offX, start.y + offY);
    if (test.x < 0 || test.y < 0 || test.x >= Param.TILES_X || test.y >= Param.TILES_Y) return true;
    return ((tiles[test.x][test.y].colour == fromC && tiles[test.x][test.y].level == fromLevel)
            || (tiles[test.x][test.y].colour == toC && tiles[test.x][test.y].level == toLevel));
  }

  private Edge krinkle(IVector2 v, final Cardinal D, final Edge direction, final int maxIncursion, final IVector2 destination,
                       final Colour fromC, final Colour toC,
                       final int fromLevel, final int toLevel) {
    int dist = R.nextInt(Param.MAX_KRINKLE);
    switch (direction) {
      case kFLAT: dist -= Math.floor(Param.MAX_KRINKLE/2.); break;
      case kOUT: dist *= -1; break;
      case kIN: break;
      default: Gdx.app.error("krinkle","Unknown enum");
    }
    do {
      int step = 0;
      if      (dist > 0) { step = +1; --dist; }
      else if (dist < 0) { step = -1; ++dist; }

      int inTest = 0, inThreshold = 0, outTest = 0, outThreshold = 0, outModX = 0, outModY = 0;
      int outOfBoundX = 0, outOfBoundY = 0, outOfBoundModX = 0, outOfBoundModY = 0;
      int modX = 0, modY = 0;

      boolean testInvert = false;
      switch (D) {
        case kN:
          inTest  = v.x + step; inThreshold  = destination.x + 1;
          outTest = v.x + step; outThreshold = destination.x + maxIncursion; outModX = -1;
          outOfBoundX = -Param.NEAR_TO_EDGE; outOfBoundModX = +1;
          modX = step;
          break;
        case kS:
          inTest  = v.x - step; inThreshold  = destination.x - 2; testInvert = true;
          outTest = v.x - step; outThreshold = destination.x - maxIncursion; outModX = +1;
          outOfBoundX = Param.NEAR_TO_EDGE; outOfBoundModX = -1;
          modX = -step;
          break;
        case kW:
          inTest  = v.y + step; inThreshold  = destination.y + 2; // Space for cliff bottom
          outTest = v.y + step; outThreshold = destination.y + maxIncursion; outModY = -1;
          outOfBoundY = -Param.NEAR_TO_EDGE; outOfBoundModY = +1;
          modY = step;
          break;
        case kE:
          inTest  = v.y - step; inThreshold  = destination.y - 2; testInvert = true;
          outTest = v.y - step; outThreshold = destination.y - maxIncursion; outModY = +1;
          outOfBoundY = Param.NEAR_TO_EDGE; outOfBoundModY = -1;
          modY = -step;
          break;
      }

      if (testInvert) {
        int tempA = inTest;
        int tempB = outTest;
        inTest = inThreshold;
        outTest = outThreshold;
        inThreshold = tempA;
        outThreshold = tempB;
      }

      if (inTest < inThreshold ) {
        if (Param.DEBUG > 0) Gdx.app.log("      krinkle",v.toString() + " Too close to outer edge (" + inTest + " < " + inThreshold + ") -> kIN");
        return Edge.kIN; // Too close to edge of map
      } else if (outTest > outThreshold) { // Too close to centre
        v.x += outModX; v.y += outModY; setGround(v, toC, toLevel); // Correct now
        if (Param.DEBUG > 0) Gdx.app.log("      krinkle",v.toString() + " Too close to centre -> kOUT");
        return Edge.kOUT;
      } else if(!sameColourAndLevel(v, outOfBoundX, outOfBoundY, fromC, toC, fromLevel, toLevel)) {
        for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) {
          v.x += outOfBoundModX; v.y += outOfBoundModY;
          setGround(v, toC, toLevel);
        }
        if (Param.DEBUG > 0) Gdx.app.log("      krinkle",v.toString() + " Too close to other krinkle -> CORRECT");
      } else {
        if (Param.DEBUG > 1) Gdx.app.log("      krinkle",v.toString() + " Krinkle by x:" + modX + ", y:" + modY);
        v.x += modX; v.y += modY;
      }

      setGround(v, toC, toLevel);
    } while (dist != 0);
    return direction;
  }


  private void setGround(IVector2 v, Colour c, int level) {
    tiles[v.x][v.y].setType(TileType.kGROUND, c, level);
  }

  private void collateFloodFill(Vector<Pair<IVector2,IVector2>> edgePairs, Vector<Zone> zone) {
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (zones[x][y].mask) {
          zones[x][y].addEdgePairs(edgePairs);
          zones[x][y].merged = true;
          zone.add(zones[x][y]);
        }
        zones[x][y].mask = false;
      }
    }
  }

  private int pairFrequency(final Vector<Pair<IVector2,IVector2>> vec, final Pair<IVector2,IVector2> match) {
    // Insensitive to the direction of the 2-vector formed by the two points
    int n = 0;
    for (Pair<IVector2,IVector2> test : vec) {
      if      (test.getKey().equals(match.getKey()) && test.getValue().equals(match.getValue())) ++n;
      else if (test.getKey().equals(match.getValue()) && test.getValue().equals(match.getKey())) ++n;
    }
    return n;
  }

  private Vector<IVector2> edgePairsToEdges(Vector<Pair<IVector2,IVector2>> edgePairs) {
    Vector<Pair<IVector2,IVector2>> edgePairsReduced = new Vector<Pair<IVector2, IVector2>>();
    for (Pair<IVector2,IVector2> e : edgePairs) { // Remove duplicates
      if (pairFrequency(edgePairs, e) == 1) edgePairsReduced.add(e);
    }

    // Trace the edges
    Vector<IVector2> edges = new Vector<IVector2>();
    edges.add(edgePairsReduced.firstElement().getKey()); // Add starting location
    int infLoopProtection = 0;
    do { // Now get back to it
      IVector2 currentLocation = edges.lastElement();
      for (Pair<IVector2,IVector2> find : edgePairsReduced) {
        if (find.getKey().equals( currentLocation ) ) {
          edges.add( find.getValue() ); // Follow the vector
          break;
        }
      }
      if (++infLoopProtection > 1e3) {
        Gdx.app.error("edgePairsToEdges", "Encounterd non-cyclic pattern");
        for (Pair<IVector2, IVector2> find : edgePairsReduced) {
          Gdx.app.log("edgePairsToEdges", find.getKey().toString() + " - > " + find.getValue().toString());
        }
        return null;
      }
    } while ( !edges.firstElement().equals( edges.lastElement()) );

    // Remove un-needed elements
    Vector<IVector2> edgesReduced = new Vector<IVector2>();
    edgesReduced.add( edges.firstElement() );
    for (int i = 1; i < edges.size() - 1; ++i) {
      // Find the direction
      if (edges.elementAt(i - 1).x == edges.elementAt(i).x) { // Same x, therefore in y
        if (edges.elementAt(i + 1).x != edges.elementAt(i).x) edgesReduced.add(edges.elementAt(i));
      } else { // Same y, therefore in x
        if (edges.elementAt(i + 1).y != edges.elementAt(i).y) edgesReduced.add(edges.elementAt(i));
      }
    }
    edgesReduced.add( edges.lastElement() );
    return edgesReduced;
  }


  private boolean doSpecialZones(final boolean doGreenZones) {
    final boolean doHills = !doGreenZones;

    for (int x = 0; x < Param.ZONES_X; ++x) { // Reset merged status
      for (int y = 0; y < Param.ZONES_Y; ++y) zones[x][y].merged = false;
    }

    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (doGreenZones && zones[x][y].colour != Colour.kGREEN) continue;
        if (doHills      && zones[x][y].level == 1)
        if (zones[x][y].merged) continue;
        final boolean hwh = zones[x][y].hillWithinHill;
        final int hwh_level = (zones[x][y].level > 1 ? 3 : 0);

        Gdx.app.log("doSpecialZones","Start " + (doGreenZones ? "GreenZone" : "Hill")   + " z("+x+","+y+") L("+zones[x][y].getLowerX()+","+zones[x][y].getLowerY()+")");

        // Do a flood-fill
        final IVector2 floodStart = new IVector2(x,y);
        floodFillMask(floodStart, false, doHills); // False as onZones

        // Collate results of the flood fill
        Vector<Pair<IVector2,IVector2>> edgePairs = new Vector<Pair<IVector2, IVector2>>();
        Vector<Zone> zone = new Vector<Zone>();
        collateFloodFill(edgePairs, zone);

        final Vector<IVector2> edges = edgePairsToEdges(edgePairs);
        if (edges == null) return false;

//        for (Pair<IVector2,IVector2> e : edgePairs) Gdx.app.log("Edges Size " +edgePairs.size(),e.getKey().toString() + " -> " + e.getValue().toString());
//        for (Pair<IVector2,IVector2> e : edgePairsReduced) Gdx.app.log("EdgesReduced Size " + edgePairsReduced.size(),e.getKey().toString() + " -> " + e.getValue().toString());
//        for (IVector2 e : edges) Gdx.app.log("Edges Size " + edges.size(),e.toString());
//        for (IVector2 e : edgesReduced) Gdx.app.log("EdgesReduced Size " + edgesReduced.size(),e.toString());

        boolean success = true;
        if (doGreenZones) success = doEdges(edges, zone, Colour.kRED, Colour.kGREEN, 1, 1, 11, 15);
        if (doHills)      success = doEdges(edges, zone, zones[x][y].colour, zones[x][y].colour, 1, zones[x][y].level, 17, 21);
//        if (doHills && success && hwh) success = doEdges(edges, zone, zones[x][y].colour, zones[x][y].colour, zones[x][y].level, hwh_level, 23, 26);
        if (!success) return success;
      }
    }
    return true;
  }


  private void floodFillMask(final IVector2 start, final boolean onTiles, final boolean compareLevel) {
    Queue<IVector2> queue = new PriorityQueue<IVector2>();
    queue.add(start);
    Colour fill;
    int level;
    if (onTiles) {
      tiles[start.x][start.y].mask = true;
      fill = tiles[start.x][start.y].colour;
      level = tiles[start.x][start.y].level;
    } else {
      zones[start.x][start.y].mask = true;
      fill = zones[start.x][start.y].colour;
      level = zones[start.x][start.y].level;
    }

    while (!queue.isEmpty()) {
      IVector2 node = queue.remove();
      if (node.y < (onTiles ? Param.TILES_Y - 1 : Param.ZONES_Y - 1)) {
        Entity N = (onTiles ? tiles[node.x][node.y + 1] : zones[node.x][node.y + 1]);
        if (N.colour == fill && (!compareLevel || N.level == level) && !N.mask) {
          N.mask = true;
          queue.add(new IVector2(node.x, node.y + 1));
        }
      }
      if (node.x < (onTiles ? Param.TILES_X - 1 : Param.ZONES_X - 1)) {
        Entity E = (onTiles ? tiles[node.x + 1][node.y] : zones[node.x + 1][node.y]);
        if (E.colour == fill && (!compareLevel || E.level == level) && !E.mask) {
          E.mask = true;
          queue.add(new IVector2(node.x + 1, node.y));
        }
      }
      if (node.y > 0) {
        Entity S = (onTiles ? tiles[node.x][node.y - 1] : zones[node.x][node.y - 1]);
        if (S.colour == fill && (!compareLevel || S.level == level) && !S.mask) {
          S.mask = true;
          queue.add(new IVector2(node.x, node.y - 1));
        }
      }
      if (node.x > 0) {
        Entity W = (onTiles ? tiles[node.x - 1][node.y] : zones[node.x - 1][node.y]);
        if (W.colour == fill && (!compareLevel || W.level == level) && !W.mask) {
          W.mask = true;
          queue.add(new IVector2(node.x - 1, node.y));
        }
      }
    }
  }

  private void floodFill(final Zone z, final Colour fromC, final Colour toC, final int fromLevel, final int toLevel) {
    for (int x = z.lowerLeft.x; x < z.upperRight.x; ++x) {
      for (int y = z.lowerLeft.y; y < z.upperRight.y; ++y) {
        if (!tiles[x][y].mask && tiles[x][y].colour == fromC && tiles[x][y].level == fromLevel) {
          setGround(new IVector2(x,y), toC, toLevel);
        }
      }
    }
  }

  private Cardinal getDirection(final IVector2 from, final IVector2 to) {
    if (from.x == to.x)  return (to.y > from.y ? Cardinal.kN : Cardinal.kS);
    return (to.x > from.x ? Cardinal.kE : Cardinal.kW);
  }

  private boolean doEdges(final Vector<IVector2> edges, final Vector<Zone> zones,
                          final Colour fromC, final Colour toC, final int fromLevel, final int toLevel,
                          final int offset, final int maxIncursion) {

    Gdx.app.log("  doEdges", "FromC " + fromC + " toC " + toC + ", fromL " + fromLevel + " toL " + toLevel);


    IVector2 location = new IVector2(edges.firstElement().x + offset, edges.firstElement().y + offset);
    final int startX = location.x;
    final int startY = location.y;

    for (int section = 1; section < edges.size(); ++section) {
      final IVector2 current = edges.elementAt(section - 1).clone();
      final IVector2 destination = edges.elementAt(section).clone();
      final IVector2 next = (section < edges.size() - 1 ? edges.elementAt(section + 1).clone() : null);
      final Cardinal D = getDirection(current, destination);
      final boolean rightTurnNext = (next == null || getDirection(destination, next) == D.next90());
      Gdx.app.log("    Edge", "From " + edges.elementAt(section - 1).toString() + " to " + destination.toString() + ", D: " + D + ", RightTurnNext:" + rightTurnNext);
      boolean reachedDest = false;
      Edge direction = Edge.kOUT;
      while (!reachedDest) {
        int duration = Param.MIN_DIST + R.nextInt(Param.MAX_DIST);
        for (int step = 0; step < duration; ++step) {
          // Special block: to try and match up at the end
          if (section == edges.size() - 1 && location.x - startX < maxIncursion) { // TODO maxIncursions - offset?
            direction = (location.y > startY ? Edge.kOUT : Edge.kIN);
          }
          increaseStep(location, D, toC, toLevel);
          direction = krinkle(location, D, direction, maxIncursion, destination, fromC, toC, fromLevel, toLevel);
          if (!Util.inBounds(location)) return true; // TODO make this an error, return false
          reachedDest = reachedDestination(location, D, offset, destination, rightTurnNext);
          if (reachedDest) break;
        }
        direction = Edge.random();
      }
    }

    // Connect back up to the start
    while (location.y != startY) {
      location.y += (startY > location.y ? 1 : -1);
      setGround(location, toC, toLevel);
    }


    IVector2 floodStart = edges.firstElement().clone();
    while (tiles[floodStart.x][floodStart.y].colour != fromC || tiles[floodStart.x][floodStart.y].level != fromLevel) {
      floodStart.x++;
      floodStart.y++;
    }
    floodFillMask(floodStart, true, true);

    for (Zone z : zones) {
      floodFill(z, fromC, toC, fromLevel, toLevel);
    }
    for (int x = 0; x < Param.TILES_X; ++x) { // Can spread outside of the area
      for (int y = 0; y < Param.TILES_Y; ++y) tiles[x][y].mask = false;
    }

    return true;
  }

  private final Map<Cardinal, Tile> collateNeighbours(int x, int y) {
    Map<Cardinal, Tile> map = new EnumMap<Cardinal, Tile>(Cardinal.class);
    map.put(Cardinal.kN, tiles[x][y+1]);
    map.put(Cardinal.kNE, tiles[x+1][y+1]);
    map.put(Cardinal.kE, tiles[x+1][y]);
    map.put(Cardinal.kSE, tiles[x+1][y-1]);
    map.put(Cardinal.kS, tiles[x][y-1]);
    map.put(Cardinal.kSW, tiles[x-1][y-1]);
    map.put(Cardinal.kW, tiles[x-1][y]);
    map.put(Cardinal.kNW, tiles[x-1][y+1]);
    return map;
  }

  private void applyTileGraphics() {
    // Outer are forced to be void
    // Do these first so we don't try and access outside of the tilespace
    for (int x = 0; x < Param.TILES_X; ++x) {
      tiles[x][0].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
      tiles[x][Param.TILES_Y-1].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
    }
    for (int y = 0; y < Param.TILES_Y; ++y) {
      tiles[0][y].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
      tiles[Param.TILES_X-1][y].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
    }
    // Set the rest
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = 1; y < Param.TILES_Y-1; ++y) {
        tiles[x][y].setTexture( TileType.getTextureString(tiles[x][y], collateNeighbours(x,y)), 1 );
      }
    }
  }


}
