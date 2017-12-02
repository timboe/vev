package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import javafx.util.Pair;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Entity;
import timboe.destructor.entity.Sprite;
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
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
    zones = new Zone[Param.ZONES_X][Param.ZONES_Y];
    allZones.clear();
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y] = new Zone(x,y);
        allZones.add(zones[x][y]);
        GameState.getInstance().getStage().addActor(zones[x][y]);
      }
    }
  }

  public void generate() {
    Gdx.app.log("World","Generating...");
    if (Param.KRINKLE_OFFSET <= Param.KRINKLE_GAP) {
      Gdx.app.error("generate","Kinnkle offset "+Param.KRINKLE_OFFSET+" less than gap "+Param.KRINKLE_GAP);
      System.exit(0);
    }
    boolean success = false;
    int worldTry = 0;
    Vector<IVector2> edges = new Vector<IVector2>();
    edges.add(new IVector2(0,0));
    edges.add(new IVector2(0,Param.TILES_Y));
    edges.add(new IVector2(Param.TILES_X,Param.TILES_Y));
    edges.add(new IVector2(Param.TILES_X,0));
    edges.add(new IVector2(0,0));
    while (!success) {
      if (++worldTry > 1e3) {
        Gdx.app.error("generate", "Reached maximum number of wordlGen tries");
        System.exit(0);
      }
      reset();
      success = doZones();
      Gdx.app.log("doWorld","let there be land");
      if (success) success = doEdges(edges, allZones, Colour.kBLACK, Colour.kRED, 0, 1, Param.KRINKLE_OFFSET, (2*Param.KRINKLE_OFFSET)-Param.KRINKLE_GAP);
      if (success) success = doSpecialZones(true);
      if (success) success = removeGreenStubs();
      if (success) success = doSpecialZones(false);
      if (success) success = removeEWSingleStairs();
      if (success) success = markCliffs();
      if (success) success = addFoliage();
      if (success) success = addForests();
      if (success) success = doPathGrid();
    }
    applyTileGraphics();
    isReady = true;
    Gdx.app.log("World","Generation finished, try " + worldTry);
    for (int y = Param.ZONES_Y-1; y >= 0; --y) Gdx.app.log( "",(zones[0][y].colour == Colour.kRED ? "R " : "G ") + (zones[1][y].colour == Colour.kRED ? "R " : "G ") + (zones[2][y].colour == Colour.kRED ? "R " : "G ") );
    for (int y = Param.ZONES_Y-1; y >= 0; --y) Gdx.app.log( "",zones[0][y].level + " " + zones[1][y].level + " " + zones[2][y].level);
  }

  private boolean shouldLink(Tile A, Tile B) {
    if (A.colour == Colour.kBLACK || B.colour == Colour.kBLACK) return false;
    if (A.type == TileType.kFOILAGE || A.type == TileType.kCLIFF) return false;
    if (B.type == TileType.kFOILAGE || B.type == TileType.kCLIFF) return false;
    if (A.type == TileType.kSTAIRS && B.type == TileType.kSTAIRS) return true;
    if (B.type == TileType.kSTAIRS) return shouldLink(B, A); // Do it from the perspective of the stairs
    if (A.type == TileType.kSTAIRS) {
      Map<Cardinal, Tile> n = collateNeighbours(A.x, A.y);
      if (A.direction == Cardinal.kN || A.direction == Cardinal.kS) { // I'm N-S stairs
        if      (B.y > A.y) return (n.get(Cardinal.kN).type == TileType.kSTAIRS);
        else if (B.y < A.y) return (n.get(Cardinal.kS).type == TileType.kSTAIRS);
      } else { // E-W stairs
        if      (B.x > A.x) return (n.get(Cardinal.kE).type == TileType.kSTAIRS);
        else if (B.x < A.x) return (n.get(Cardinal.kW).type == TileType.kSTAIRS);
      }
      return true;
    }
    return (A.level == B.level);
  }

  private void updateTilePathfinding(int x, int y) {
    tiles[x][y].pathFindDebug.clear();
    tiles[x][y].pathFindNeighbours.clear();
    Map<Cardinal, Tile> n = collateNeighbours(x, y);
    for (Cardinal D : Cardinal.n8) {
      if (shouldLink(tiles[x][y], n.get(D))) {
        tiles[x][y].pathFindDebug.add(D);
        tiles[x][y].pathFindNeighbours.add(n.get(D));
      }
    }
  }

  private boolean doPathGrid() {
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = 1; y < Param.TILES_Y-1; ++y) {
        updateTilePathfinding(x,y);
      }
    }
    return true;
  }

  private String randomFoliage(Colour c) {
    if (R.nextFloat() < Param.TREE_PROB) return "tree_" + c.getString() + "_" + R.nextInt(Param.N_TREE);
    return "bush_" + c.getString() + "_" + R.nextInt(Param.N_BUSH);
  }

  private void newSprite(int x, int y, String name) {
    Sprite s = new Sprite(x,y);
    GameState.getInstance().getSpriteStage().addActor(s);
    s.setTexture(name, 1);
    //          if (R.nextBoolean()) s.flip(); //TODO not working
  }

  private boolean tryPatchOfStuff(int _x, int _y, String stuff) {
    for (int x = _x - Param.FOREST_SIZE; x < _x + Param.FOREST_SIZE; ++x) {
      if (tiles[x][_y].type != TileType.kGROUND || tiles[x][_y].colour != tiles[_x][_y].colour) return false;
    }
    for (int y = _y - Param.FOREST_SIZE; y < _y + Param.FOREST_SIZE; ++y) {
      if (tiles[_x][y].type != TileType.kGROUND || tiles[_x][y].colour != tiles[_x][_y].colour) return false;
    }
    final double maxD = Math.sqrt(2*Math.pow(Param.FOREST_SIZE,2));
    for (int x = _x - Param.FOREST_SIZE; x < _x + Param.FOREST_SIZE; ++x) {
      for (int y = _y + Param.FOREST_SIZE - 1; y >= _y - Param.FOREST_SIZE; --y) {
        double d = Math.sqrt( Math.pow(x - _x, 2) + Math.pow(y - _y, 2) );
        if (tiles[x][y].colour != tiles[_x][_y].colour
                || tiles[x][y].type != TileType.kGROUND
                || d > Math.abs(R.nextGaussian() * Param.FOREST_DENSITY * maxD)) continue;
        newSprite(x, y, stuff);
        tiles[x][y].type = TileType.kFOILAGE;
      }
    }
    return true;
  }

  private boolean addForests() {
    int fTry = 0;
    int fPlaced = 0;
    do {
      final int xExtent = (Param.FOREST_SIZE/2) + R.nextInt(Param.FOREST_SIZE);
      final int yExtent = (Param.FOREST_SIZE/2) + R.nextInt(Param.FOREST_SIZE);
      final int x = xExtent + R.nextInt(Param.TILES_X - (2*xExtent));
      final int y = yExtent + R.nextInt(Param.TILES_Y - (2*yExtent));
      if (tiles[x][y].type != TileType.kGROUND || tiles[x][y].colour != Colour.kGREEN) continue;
      if (tryPatchOfStuff(x, y, "tree_" + tiles[x][y].colour.getString() + "_" + R.nextInt(Param.N_TREE))) ++fPlaced;
    } while (++fTry < Param.N_FOREST_TRIES && fPlaced < Param.N_FOREST);
    return true;
  }


  private boolean addFoliage() {
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = Param.TILES_Y-1; y >= 0 ; --y) {
        if (tiles[x][y].type != TileType.kGROUND || tiles[x][y].colour == Colour.kBLACK) continue;
        if (R.nextFloat() < Param.FOLIAGE_PROB) {
          newSprite(x, y, randomFoliage(tiles[x][y].colour));
          tiles[x][y].type = TileType.kFOILAGE;
        } else if (tiles[x][y].colour == Colour.kGREEN && R.nextFloat() < 0.01) {
//          Tile s = new Tile(x,y);
//          GameState.getInstance().getStage().addActor(s);
//          s.setTexture("building_" + R.nextInt(5), 1);
        }
      }
    }
    return true;
  }

  private boolean markCliffs() {
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = 1; y < Param.TILES_Y-1; ++y) {
        if (tiles[x][y].type != TileType.kGROUND) continue;
        Map<Cardinal, Tile> n = collateNeighbours(x, y);
        if (n.get(Cardinal.kN).level > tiles[x][y].level && n.get(Cardinal.kN).type != TileType.kSTAIRS) {
          tiles[x][y].type = TileType.kCLIFF;
        } else {
          for (Cardinal D : Cardinal.NESW) {
            if (n.get(D).level < tiles[x][y].level) {
              tiles[x][y].type = TileType.kCLIFF_EDGE;
              break;
            } else if (tiles[x][y].colour == Colour.kGREEN && n.get(D).colour != Colour.kGREEN) {
              tiles[x][y].type = TileType.kGRASS_EDGE;
              break;
            }
          }
        }
      }
    }
    return true;
  }

  private boolean removeGreenStubs() {
    boolean removed;
    do { //Iterative
      removed = false;
      for (int x = 1; x < Param.TILES_X-1; ++x) {
        for (int y = 1; y < Param.TILES_Y-1; ++y) {
          if (tiles[x][y].colour != Colour.kGREEN) continue;
          Map<Cardinal, Tile> n = collateNeighbours(x, y);
          int c = 0;
          for (Cardinal D : Cardinal.NESW) if (n.get(D).colour == Colour.kRED) ++c;
          if (c >= 3) {
            tiles[x][y].colour = Colour.kRED;
            removed = true;
          }
        }
      }
    } while (removed);
    return true;
  }

  // The way stairs work, we cannot have a single stair tile in the E-W orientation
  private boolean removeEWSingleStairs() {
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = 1; y < Param.TILES_Y-1; ++y) {
        if (tiles[x][y].type != TileType.kSTAIRS) continue;
        int count = 0;
        Map<Cardinal, Tile> n = collateNeighbours(x, y);
        for (Cardinal D : Cardinal.NESW) if (n.get(D).type == TileType.kSTAIRS) ++count;
        if (count > 0) continue;
        // Check if E-W or N-S
        if (n.get(Cardinal.kE).level != n.get(Cardinal.kW).level) {
          setGround(new IVector2(x, y), tiles[x][y].colour, tiles[x][y].level, Edge.kFLAT);
          if (Param.DEBUG > 0) Gdx.app.log("removeEWSingleStairs", "Removed single stair at "+x+","+y);
        }
      }
    }
    return true;
  }

  private boolean doZones() {
    int nGreen = 0;
    int nRed = 0;
    int nRedHill = 0;
    int nGreenHill = 0;
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y].colour = (R.nextBoolean() ? Colour.kGREEN : Colour.kRED);
        final int heightMod = (zones[x][y].colour == Colour.kGREEN ? 1 : -1);
        boolean hill = R.nextBoolean();
        zones[x][y].level = (hill ? 1 + heightMod : 1);
        if (hill && R.nextFloat() < Param.HILL_IN_HILL_PROB) zones[x][y].hillWithinHill = true;

        if (Param.DEBUG > 0) Gdx.app.log("doZones","x:" + x + " y:" + y + " C:" + zones[x][y].colour + " L:" +  zones[x][y].level + " HWH:"+zones[x][y].hillWithinHill);

        if (zones[x][y].colour == Colour.kGREEN) ++nGreen;
        else ++nRed;
        if (zones[x][y].level != 1) {
          if (zones[x][y].colour == Colour.kGREEN) ++nGreenHill;
          else ++nRedHill;
        }
      }
    }
    if (Util.needsClamp(nGreen, Param.MIN_GREEN_ZONE, Param.MAX_GREEN_ZONE)) return false;
    if (Util.needsClamp(nGreenHill, Param.MIN_GREEN_HILL, nGreen - 1)) return false;
    return !(Util.needsClamp(nRedHill, Param.MIN_RED_HILL, nRed - 1));
  }

  private void increaseStep(IVector2 v, Cardinal D, Colour c, int level) {
    switch (D) {
      case kN: ++v.y; break;
      case kE: ++v.x; break;
      case kS: --v.y; break;
      case kW: --v.x; break;
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    if (Util.inBounds(v)) setGround(v, c, level, Edge.kFLAT);
  }

  private int distanceToDestination(final IVector2 v, final Cardinal D, final int endOffset,
                                    final IVector2 destination, final boolean rightTurnNext) {
    final int flip = (rightTurnNext ? 1 : -1);
    switch (D) {
      case kN: return Math.abs(v.y - (destination.y - (endOffset * flip)));
      case kE: return Math.abs(v.x - (destination.x - (endOffset * flip)));
      case kS: return Math.abs(v.y - (destination.y + (endOffset * flip)));
      case kW: return Math.abs(v.x - (destination.x + (endOffset * flip)));
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    return 0;
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

  private Edge krinkle(IVector2 v, final Cardinal D, Edge direction, final int distanceToDest,
                       final int maxIncursion, final IVector2 destination,
                       final Colour fromC, final Colour toC,
                       final int fromLevel, final int toLevel) {
    int dist = R.nextInt(Param.MAX_KRINKLE);
    switch (direction) {
      case kFLAT: dist -= Math.floor(Param.MAX_KRINKLE/2.); break;
      case kOUT: dist *= -1; break;
      case kIN: break;
      case kSTAIRS: case kSTAIRS_IN: case kSTAIRS_OUT: dist = 0; break;
      default: Gdx.app.error("krinkle","Unknown enum");
    }
    do {
      int step = 0;
      if      (dist > 0) { step = +1; --dist; }
      else if (dist < 0) { step = -1; ++dist; }

      int inTest = 0, inThreshold = 0; // Proposed krinkle vs. outermost allowed value
      int outTest = 0, outThreshold = 0; // Proposed krinkle vs. innermost allowed value
      int outModX = 0, outModY = 0; // X and Y needed to force us outwards if innermost criteria met
      int outOfBoundX = 0, outOfBoundY = 0; // Distance to check for out-of-bounds due to meeting another krinkle
      int outOfBoundModX = 0, outOfBoundModY = 0; // X and Y needed to force us inwards away from the OOB
      int lookAheadOutOfBoundsX = 0, lookAheadOutOfBoundsY = 0; // Looking ahead to make sure we don't hit a right-angle we cannot krinkle around
      int modX = 0, modY = 0; // The krinkle to apply - if all is good

      boolean testInvert = false;
      switch (D) {
        case kN:
          inTest  = v.x + step; inThreshold  = destination.x + 1;
          outTest = v.x + step; outThreshold = destination.x + maxIncursion; outModX = -1;
          outOfBoundX = -Param.NEAR_TO_EDGE; outOfBoundModX = +1;
          lookAheadOutOfBoundsY = Param.NEAR_TO_EDGE;
          modX = step;
          break;
        case kS:
          inTest  = v.x - step; inThreshold  = destination.x - 2; testInvert = true;
          outTest = v.x - step; outThreshold = destination.x - maxIncursion; outModX = +1;
          outOfBoundX = Param.NEAR_TO_EDGE; outOfBoundModX = -1;
          lookAheadOutOfBoundsY = -Param.NEAR_TO_EDGE;
          modX = -step;
          break;
        case kW:
          inTest  = v.y + step; inThreshold  = destination.y + 2; // Space for cliff bottom
          outTest = v.y + step; outThreshold = destination.y + maxIncursion; outModY = -1;
          outOfBoundY = -(Param.NEAR_TO_EDGE+1); outOfBoundModY = +1; // Space for cliff bottom
          lookAheadOutOfBoundsX = Param.NEAR_TO_EDGE;
          modY = step;
          break;
        case kE:
          inTest  = v.y - step; inThreshold  = destination.y - 2; testInvert = true;
          outTest = v.y - step; outThreshold = destination.y - maxIncursion; outModY = +1;
          outOfBoundY = Param.NEAR_TO_EDGE; outOfBoundModY = -1;
          lookAheadOutOfBoundsX = -Param.NEAR_TO_EDGE;
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
        v.x += outModX; v.y += outModY; setGround(v, toC, toLevel, Edge.kFLAT); // Correct now
        if (Param.DEBUG > 0) Gdx.app.log("      krinkle",v.toString() + " Too close to centre -> kOUT");
        return Edge.kOUT;
      } else if (!sameColourAndLevel(v, outOfBoundX, outOfBoundY, fromC, toC, fromLevel, toLevel)
              || (distanceToDest > Param.MAX_DIST && !sameColourAndLevel(v, lookAheadOutOfBoundsX, lookAheadOutOfBoundsY, fromC, toC, fromLevel, toLevel)) ) {
        for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) {
          v.x += outOfBoundModX; v.y += outOfBoundModY;
          setGround(v, toC, toLevel, Edge.kFLAT);
        }
        if (direction == Edge.kSTAIRS || direction == Edge.kSTAIRS_IN || direction == Edge.kSTAIRS_OUT) direction = Edge.kIN;// Kill any on-going stairs at this point
        if (Param.DEBUG > 0) Gdx.app.log("      krinkle",v.toString() + " Too close to other krinkle -> CORRECT");
      } else {
        if (Param.DEBUG > 1) Gdx.app.log("      krinkle",v.toString() + " Krinkle by x:" + modX + ", y:" + modY);
        v.x += modX; v.y += modY;
      }

      IVector2 v2 = v.clone();
      if (direction == Edge.kSTAIRS && toLevel > fromLevel) { // We are going UPHILL. Need to tweak stair placement
        setGround(v, toC, toLevel, Edge.kFLAT);
        switch (D) {
          case kN: v2.x--; break;
          case kS: v2.x++; break;
          case kE: v2.y++; break;
          case kW: v2.y--; break;
          default: Gdx.app.error("krinkle","Unknown direction " + D);
        }
      }
      setGround(v2, toC, toLevel, direction);
      tiles[v2.x][v2.y].direction = D;

    } while (dist != 0);
    return direction;
  }


  private void setGround(IVector2 v, Colour c, int level, Edge direction) {
    tiles[v.x][v.y].setType(direction == Edge.kSTAIRS ? TileType.kSTAIRS : TileType.kGROUND, c, level);
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
        if (doHills      && zones[x][y].level == 1) continue;
        if (zones[x][y].merged) continue;
        final boolean hwh = zones[x][y].hillWithinHill;
        final int hwh_level = zones[x][y].level + (zones[x][y].level > 1 ? 1 : -1);

        Gdx.app.log("doSpecialZones - " + (doGreenZones ? "GreenZone" : "Hill"),
                "Start z("+x+","+y+") L("+zones[x][y].getLowerX()+","+zones[x][y].getLowerY()+
                        ") HWH:" + zones[x][y].hillWithinHill);

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
        if (doGreenZones) success = doEdges(edges, zone, Colour.kRED, Colour.kGREEN, 1, 1, 2*Param.KRINKLE_OFFSET, (3*Param.KRINKLE_OFFSET)-Param.KRINKLE_GAP);
        if (doHills)      success = doEdges(edges, zone, zones[x][y].colour, zones[x][y].colour, 1, zones[x][y].level, 3*Param.KRINKLE_OFFSET, (4*Param.KRINKLE_OFFSET)-Param.KRINKLE_GAP);
        if (doHills && success && hwh) {
          Gdx.app.log("doSpecialZones - HillWithinHill",
                  "Start z("+x+","+y+") L("+zones[x][y].getLowerX()+","+zones[x][y].getLowerY()+")");
          success = doEdges(edges, zone, zones[x][y].colour, zones[x][y].colour, zones[x][y].level, hwh_level, 4*Param.KRINKLE_OFFSET, (5*Param.KRINKLE_OFFSET)-Param.KRINKLE_GAP);
        }
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
//        Gdx.app.log("DBG","x " + x + " y " + y + " | " + z.upperRight.x + " " + z.upperRight.y);
        if (!tiles[x][y].mask && tiles[x][y].colour == fromC && tiles[x][y].level == fromLevel) {
          setGround(new IVector2(x,y), toC, toLevel, Edge.kFLAT);
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
    final IVector2 start = new IVector2(location);
    final boolean doingHill = (fromLevel != toLevel && fromC != Colour.kBLACK);
    int stairCount = 0;

    for (int section = 1; section < edges.size(); ++section) {
      final IVector2 current = edges.elementAt(section - 1).clone();
      final IVector2 destination = edges.elementAt(section).clone();
      final IVector2 next = (section < edges.size() - 1 ? edges.elementAt(section + 1).clone() : null);
      final Cardinal D = getDirection(current, destination);
      final boolean rightTurnNext = (next == null || getDirection(destination, next) == D.next90());
      Gdx.app.log("    Edge",
              "From " + edges.elementAt(section - 1).toString() +
                      " to " + destination.toString() +
                      ", D: " + D + ", RightTurnNext:" + rightTurnNext);
      int distanceToDest = 999;
      Edge direction = Edge.kOUT;
      while (distanceToDest > 0) {
        int duration = Param.MIN_DIST + R.nextInt(Param.MAX_DIST - Param.MIN_DIST + 1);
        if (direction == Edge.kSTAIRS_IN || direction == Edge.kSTAIRS_OUT) duration = 2 + R.nextInt(Param.MIN_DIST); // Keep flat buffers shorter
        for (int step = 0; step < duration; ++step) {
          // Special block: to try and match up at the end
          if (section == edges.size() - 1 && location.x - start.x < maxIncursion) { // TODO maxIncursions - offset?
            direction = (location.y > start.y ? Edge.kOUT : Edge.kIN);
          }
          increaseStep(location, D, toC, toLevel);
          distanceToDest = distanceToDestination(location, D, offset, destination, rightTurnNext);
          direction = krinkle(location, D, direction, distanceToDest, maxIncursion, destination, fromC, toC, fromLevel, toLevel);
          if (!Util.inBounds(location)) {
            Gdx.app.error("doEdges", "Serious krinkle error, gone out-of-bounds");
            return true; // TODO make this an error, return false
          }
          if (distanceToDest == 0) break;
        }
        if (direction == Edge.kSTAIRS_IN) {
          direction = Edge.kSTAIRS;
        } else if (direction == Edge.kSTAIRS) {
          direction = Edge.kSTAIRS_OUT;
          ++stairCount;
        } else {
          direction = Edge.random(doingHill
                  && direction != Edge.kSTAIRS_OUT
                  && distanceToDest > Param.MAX_DIST*2);
        }
      }
    }

    if (doingHill && stairCount < Param.MIN_STAIRCASES) {
      Gdx.app.error("doEdges", "Staircases " + stairCount + " smaller than min " + Param.MIN_STAIRCASES);
      return false;
    }

    // Connect back up to the start
    while (location.y != start.y) {
      location.y += (start.y > location.y ? 1 : -1);
      setGround(location, toC, toLevel, Edge.kFLAT);
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
