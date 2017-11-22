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

  Tile[][] tiles;
  Zone[][] zones;

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
//        tiles[x][y].setDebug(true);
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
    zones = new Zone[Param.ZONES_X][Param.ZONES_Y];
    allZones.clear();
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y] = new Zone(x,y);
        allZones.add(zones[x][y]);
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
      if (success) success = doEdges(edges, allZones, Colour.kBLACK, Colour.kRED, 0, 1, 5, 10);
      if (success) success = doGreenZones();
      if (success) success = removeGreenStubs();
      //if (success) success = doHills();
    }
    applyTileGraphics();
    isReady = true;
    Gdx.app.log("World","Generation finished, try " + worldTry);
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
    int nGreen = 0;
    int nHill = 0;
    int nGreenHill = 0;
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y].colour = (R.nextBoolean() ? Colour.kGREEN : Colour.kRED);
        zones[x][y].hill = R.nextBoolean();
        if (zones[x][y].colour == Colour.kGREEN) ++nGreen;
        if (zones[x][y].hill) ++nHill;
        if (zones[x][y].colour == Colour.kGREEN && zones[x][y].hill) ++nGreenHill;
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
    setGround(v, c, level);
  }

  private boolean reachedDestination(final IVector2 v, final Cardinal D, final int endOffset, final IVector2 destination,
                                     final Colour fromC, final Colour toC,
                                     final int fromLevel, final int toLevel) {
    switch (D) {
      case kN: return (v.y == destination.y - endOffset);
      case kE: return (v.x == destination.x - endOffset);
      case kS: return (v.y == destination.y + endOffset); // || !sameColourAndLevel(v, 0, -Param.NEAR_TO_EDGE, fromC, toC, fromLevel, toLevel) );
      case kW: return (v.x == destination.x + endOffset);
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

  /*

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

      int xMin = 0, xMax = Param.TILES_X-1, yMin = 0, yMax = Param.TILES_Y-1;
      int xStepAdd = 0, yStepAdd = 0;
      switch (D) {
        case kN:
          xStepAdd = step;
          xMax = destination
      }


      if (!Util.needsClamp(v.x + xStepAdd, xMin, xMax) && !Util.needsClamp(v.y + yStepAdd, yMin, yMax))


      if(D == Cardinal.kN) {
        if (v.x + step < destination.x + 1) {
//          Gdx.app.log("","Dir=N: Force inwards" + " v (" + v.x + "," + v.y + ")");
          return Edge.kIN;
        } else if (v.x + step > destination.x + maxIncursion) {
//          Gdx.app.log("","Dir=N: Force outwards" + " v (" + v.x + "," + v.y + ")");
          v.x--; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, -Param.NEAR_TO_EDGE,0, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Dir=N: Force edge miss" + " v (" + v.x + "," + v.y + ")");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.x++; setGround(v, toC, toLevel); }
        } else {
//          Gdx.app.log("","Dir=N: v.x += " + step + " v (" + v.x + "," + v.y + ")");
          v.x += step;
        }
      }
      if (D == Cardinal.kS) {
//        if ( Util.needsClamp(v.x - step, destination.x - maxIncursion, destination.x - 2)) {
        if (v.x - step > destination.x - 2) {
          return Edge.kIN;
        } else if (v.x - step < destination.x - maxIncursion) {
          v.x++; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, Param.NEAR_TO_EDGE,0, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Force W");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.x--; setGround(v, toC, toLevel); }
        } else {
          v.x -= step;
        }
      }
      if (D == Cardinal.kW) {
//        if (Util.needsClamp(v.y + step, destination.y + 3, (destination.y+) maxIncursion)) { // Note - extra space for cliff
        if (v.y + step < destination.y + 3) {
          return Edge.kIN;
        } else if (v.y + step > destination.y + maxIncursion) {
          v.y--; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, 0, -Param.NEAR_TO_EDGE, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Force N");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.y++; setGround(v, toC, toLevel); }
        } else {
          v.y += step;
        }
      }
      if (D == Cardinal.kE) {
//        if (Util.needsClamp(v.y - step, destination.y - maxIncursion, destination.y - 2)) {
        if (v.y - step > destination.y - 2) {
//          Gdx.app.log("","Dir=E: Force inwards" + " v " + v.x + " " + v.y);
          return Edge.kIN;
        } else if (v.y - step < destination.y - maxIncursion) {
//          Gdx.app.log("","Dir=E: Force outwards" + " v " + v.x + " " + v.y);
          v.y++; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, 0, Param.NEAR_TO_EDGE, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Dir=E: Force S");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.y--; setGround(v, toC, toLevel); }
        } else {
//          Gdx.app.log("","Dir=E: y -= " + step);
          v.y -= step;
        }
      }
//      Gdx.app.log("",D.getString() + " dest " + destination.x + " " + destination.y + " v " + v.x + " " + v.y);
      setGround(v, toC, toLevel);
    } while (dist != 0);
    return direction;
  }

*/

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
      if(D == Cardinal.kN) {
        if (v.x + step < destination.x + 1) {
//          Gdx.app.log("","Dir=N: Force inwards" + " v (" + v.x + "," + v.y + ")");
          return Edge.kIN;
        } else if (v.x + step > destination.x + maxIncursion) {
//          Gdx.app.log("","Dir=N: Force outwards" + " v (" + v.x + "," + v.y + ")");
          v.x--; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, -Param.NEAR_TO_EDGE,0, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Dir=N: Force edge miss" + " v (" + v.x + "," + v.y + ")");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.x++; setGround(v, toC, toLevel); }
        } else {
//          Gdx.app.log("","Dir=N: v.x += " + step + " v (" + v.x + "," + v.y + ")");
          v.x += step;
        }
      }
      if (D == Cardinal.kS) {
//        if ( Util.needsClamp(v.x - step, destination.x - maxIncursion, destination.x - 2)) {
        if (v.x - step > destination.x - 2) {
          return Edge.kIN;
        } else if (v.x - step < destination.x - maxIncursion) {
          v.x++; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, Param.NEAR_TO_EDGE,0, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Force W");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.x--; setGround(v, toC, toLevel); }
        } else {
          v.x -= step;
        }
      }
      if (D == Cardinal.kW) {
//        if (Util.needsClamp(v.y + step, destination.y + 3, (destination.y+) maxIncursion)) { // Note - extra space for cliff
        if (v.y + step < destination.y + 3) {
          return Edge.kIN;
        } else if (v.y + step > destination.y + maxIncursion) {
          v.y--; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, 0, -Param.NEAR_TO_EDGE, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Force N");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.y++; setGround(v, toC, toLevel); }
        } else {
          v.y += step;
        }
      }
      if (D == Cardinal.kE) {
//        if (Util.needsClamp(v.y - step, destination.y - maxIncursion, destination.y - 2)) {
        if (v.y - step > destination.y - 2) {
//          Gdx.app.log("","Dir=E: Force inwards" + " v " + v.x + " " + v.y);
          return Edge.kIN;
        } else if (v.y - step < destination.y - maxIncursion) {
//          Gdx.app.log("","Dir=E: Force outwards" + " v " + v.x + " " + v.y);
          v.y++; setGround(v, toC, toLevel);
          return  Edge.kOUT;
        } else if(!sameColourAndLevel(v, 0, Param.NEAR_TO_EDGE, fromC, toC, fromLevel, toLevel)) {
//          Gdx.app.log("","Dir=E: Force S");
          for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) { v.y--; setGround(v, toC, toLevel); }
        } else {
//          Gdx.app.log("","Dir=E: y -= " + step);
          v.y -= step;
        }
      }
//      Gdx.app.log("",D.getString() + " dest " + destination.x + " " + destination.y + " v " + v.x + " " + v.y);
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
//          Gdx.app.log("collateFloodFill","add zone " + x + " " + y);
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

  private boolean doGreenZones() {
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (zones[x][y].colour != Colour.kGREEN) continue;
        if (zones[x][y].merged) continue;

        Gdx.app.log("doGreenZones","Start GreenZone z("+x+","+y+") L("+zones[x][y].getLowerX()+","+zones[x][y].getLowerY()+")");

        // Do a flood-fill
        IVector2 floodStart = new IVector2(x,y);
        floodFillMask(floodStart, false); // False as onZones

        // Collate results of the flood fill
        Vector<Pair<IVector2,IVector2>> edgePairs = new Vector<Pair<IVector2, IVector2>>();
        Vector<Zone> zone = new Vector<Zone>();
        collateFloodFill(edgePairs, zone);

        // Remove entries which are included more than once
        Vector<Pair<IVector2,IVector2>> edgePairsReduced = new Vector<Pair<IVector2, IVector2>>();
        for (Pair<IVector2,IVector2> e : edgePairs) {
          if (pairFrequency(edgePairs, e) == 1) edgePairsReduced.add(e);
        }

        // Trace the edges
        Vector<IVector2> edges = new Vector<IVector2>();
        edges.add(edgePairsReduced.firstElement().getKey()); // Add starting location
        do { // Now get back to it
          IVector2 currentLocation = edges.lastElement();
          for (Pair<IVector2,IVector2> find : edgePairsReduced) {
            if (find.getKey().equals( currentLocation ) ) {
              edges.add( find.getValue() ); // Follow the vector
              break;
            }
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


        for (Pair<IVector2,IVector2> e : edgePairs) Gdx.app.log("Edges Size " +edgePairs.size(),e.getKey().toString() + " -> " + e.getValue().toString());
        for (Pair<IVector2,IVector2> e : edgePairsReduced) Gdx.app.log("EdgesReduced Size " + edgePairsReduced.size(),e.getKey().toString() + " -> " + e.getValue().toString());
        for (IVector2 e : edges) Gdx.app.log("Edges Size " + edges.size(),e.toString());
        for (IVector2 e : edgesReduced) Gdx.app.log("EdgesReduced Size " + edgesReduced.size(),e.toString());

        boolean success = doEdges(edgesReduced, zone, Colour.kRED, Colour.kGREEN, 1, 1, 11, 15);
        if (!success) return false;
      }
    }
    return true;
  }

  private boolean doHills() {
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (!zones[x][y].hill) continue;
        Vector<IVector2> edges = new Vector<IVector2>();
        edges.add(new IVector2(zones[x][y].lowerLeft.x,zones[x][y].lowerLeft.y));
        edges.add(new IVector2(zones[x][y].lowerLeft.x,zones[x][y].lowerLeft.y + zones[x][y].h));
        edges.add(new IVector2(zones[x][y].upperRight.x,zones[x][y].upperRight.y));
        edges.add(new IVector2(zones[x][y].lowerLeft.x + zones[x][y].w,zones[x][y].lowerLeft.y));
        edges.add(new IVector2(zones[x][y].lowerLeft.x,zones[x][y].lowerLeft.y));
        Vector<Zone> zone = new Vector<Zone>();
        zone.add(zones[x][y]);

//        Gdx.app.log("doGreenZone","Start " + .toString() + " end " + zones[x][y].upperRight.toString() );
        boolean success = doEdges(edges, zone, Colour.kRED, Colour.kGREEN, 1, 1, 11, 15);
        if (!success) return false;
      }
    }
    return true;
  }

  private void floodFillMask(IVector2 start, boolean onTiles) {
    Queue<IVector2> queue = new PriorityQueue<IVector2>();
    queue.add(start);
    Colour fill;
    if (onTiles) {
      tiles[start.x][start.y].mask = true;
      fill = tiles[start.x][start.y].colour;
    } else {
      zones[start.x][start.y].mask = true;
      fill = zones[start.x][start.y].colour;
    }

    while (!queue.isEmpty()) {
      IVector2 node = queue.remove();
//      Gdx.app.log("",node.x + " " + node.y);
      if (node.y < (onTiles ? Param.TILES_Y - 1 : Param.ZONES_Y - 1)) {
        Entity N = (onTiles ? tiles[node.x][node.y + 1] : zones[node.x][node.y + 1]);
        if (N.colour == fill && !N.mask) {
          N.mask = true;
          queue.add(new IVector2(node.x, node.y + 1));
        }
      }
      if (node.x < (onTiles ? Param.TILES_X - 1 : Param.ZONES_X - 1)) {
        Entity E = (onTiles ? tiles[node.x + 1][node.y] : zones[node.x + 1][node.y]);
        if (E.colour == fill && !E.mask) {
          E.mask = true;
          queue.add(new IVector2(node.x + 1, node.y));
        }
      }
      if (node.y > 0) {
        Entity S = (onTiles ? tiles[node.x][node.y - 1] : zones[node.x][node.y - 1]);
        if (S.colour == fill && !S.mask) {
          S.mask = true;
          queue.add(new IVector2(node.x, node.y - 1));
        }
      }
      if (node.x > 0) {
        Entity W = (onTiles ? tiles[node.x - 1][node.y] : zones[node.x - 1][node.y]);
        if (W.colour == fill && !W.mask) {
          W.mask = true;
          queue.add(new IVector2(node.x - 1, node.y));
        }
      }
    }
  }

  private void floodFill(final Zone z, final Colour fromC, final Colour toC, final int level) {
    for (int x = z.lowerLeft.x; x < z.upperRight.x; ++x) {
      for (int y = z.lowerLeft.y; y < z.upperRight.y; ++y) {
        if (!tiles[x][y].mask && tiles[x][y].colour == fromC) setGround(new IVector2(x,y), toC, level);
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

    IVector2 location = new IVector2(edges.firstElement().x + offset, edges.firstElement().y + offset);
    final int startX = location.x;
    final int startY = location.y;

    for (int section = 1; section < edges.size(); ++section) {
      final IVector2 destination = edges.elementAt(section);
      final Cardinal D = getDirection(edges.elementAt(section-1), destination);
      boolean reachedDest = false;
      Edge direction = Edge.kOUT;
      while (!reachedDest) {
        int duration = Param.MIN_DIST + R.nextInt(Param.MAX_DIST);
        for (int step = 0; step < duration; ++step) {
          // Special block: to try and match up at the end
          if(section == edges.size()-1 && location.x - startX < maxIncursion) { // TODO maxIncursions - offset?
            direction = (location.y > startY ? Edge.kOUT : Edge.kIN);
          }
          increaseStep(location, D, toC, toLevel);
          direction = krinkle(location, D, direction, maxIncursion, destination, fromC, toC, fromLevel, toLevel);
          reachedDest = reachedDestination(location, D, offset, destination, fromC, toC, fromLevel, toLevel);
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

    IVector2 floodStart = edges.firstElement();
    while (tiles[floodStart.x][floodStart.y].colour != fromC && tiles[floodStart.x][floodStart.y].level != fromLevel) {
      floodStart.x++;
      floodStart.y++;
    }
    floodFillMask(floodStart, true);
    for (Zone z : zones) {
      floodFill(z, fromC, toC, toLevel);
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
