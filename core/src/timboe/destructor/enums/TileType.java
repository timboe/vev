package timboe.destructor.enums;

import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;

import java.util.Map;
import java.util.Random;

import static timboe.destructor.enums.Colour.kGREEN;
import static timboe.destructor.enums.Colour.kRED;

public enum TileType {
  // Main types - assigned to the Tile
  kGROUND,
  kSTAIRS,
  kGRASS_EDGE,
  kCLIFF,
  kCLIFF_EDGE,
  kFOILAGE,
  // Specialisation - used only when assigning texture
  kSTAIRS_1, // single-width stairs
  kGROUND_CORNER,
  kGRASS_CORNER,
  kGRASS_INNER_CORNER,
  kGRASS_INNER_CORNER_DOUBLE,
  kCLIFF_EDGE_2,
  kCLIFF_EDGE_3,
  kCLIFF_EDGE_4,
  kCLIFF_3;

  private static Random R = new Random();

  public static String getTextureString(TileType tt, Colour c) {
    return getTextureString(tt, c, Cardinal.kN, Colour.kBLACK, Cardinal.kN);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D) {
    return getTextureString(tt, c, D, Colour.kBLACK, Cardinal.kN);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D, Cardinal D2) {
    return getTextureString(tt, c, D, Colour.kBLACK, D2);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D, Colour c2) {
    return getTextureString(tt, c, D, c2, Cardinal.kN);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D, Colour c2, Cardinal D2) {
    switch (tt) {
      case kGROUND:
        if (c == Colour.kBLACK) return "b";
        else return "floor_" + c.getString() + "_" + (R.nextFloat() < .9f ? "2" : Util.R.nextInt(Param.N_GRASS_VARIANTS));
        // 90% chance of having "plain" ground
      case kGROUND_CORNER:
        return "floor_c_" + c.getString() + "_" + D.getString();
      case kGRASS_EDGE:
        return "border_" + c.getString() + "_" + D.getString() + "_" + Util.R.nextInt(Param.N_BORDER_VARIANTS);
      case kGRASS_CORNER:
        return "border_" + c.getString() + "_" + D.getString();
      case kGRASS_INNER_CORNER:
        return "border_inner_" + c.getString() + "_" + D.getString();
      case kGRASS_INNER_CORNER_DOUBLE:
        return "border_inner_double_" + c.getString() + "_" + D.getString();
      case kCLIFF_EDGE:
        return "h_" + c.getString() + "_" + D.getString();
      case kCLIFF_EDGE_2:
        return "h2_" + c.getString() + "_" + D.getString();
      case kCLIFF_EDGE_3:
        return "h3_" + c.getString() + "_" + D.getString();
      case kCLIFF_EDGE_4:
        return "h4_" + c.getString();
      case kCLIFF:
        return "h_" + c.getString() + "_" + D.getString() + "_" + c2.getString();
      case kCLIFF_3:
        return "h3_" + c.getString() + "_" + D.getString() + "_" + c2.getString();
      case kSTAIRS_1:
        return "s_" + c.getString() + "_" + D.getString();
      case kSTAIRS:
        return "s_" + c.getString() + "_" + D.getString() + "_" + D2.getString();
    }
    return "missing2";
  }


  private static boolean testStairs(final Tile t, final Map<Cardinal, Tile> neighbours, int N, int E, int S, int W) {
    if (t.type != kSTAIRS) return false;
    for (Cardinal D : Cardinal.NESW) {
      Tile neighbour = neighbours.get(D);
      int test;
      switch (D) {
        case kN:
          test = N;
          break;
        case kE:
          test = E;
          break;
        case kS:
          test = S;
          break;
        default:
          test = W;
          break;
      }
      switch (test) {
        case 0:
          if (neighbour.type != kSTAIRS) return false;
          break;
        case +1:
          if (neighbour.level < t.level) return false;
          break;
        case -1:
          if (neighbour.level > t.level) return false;
          break;
        case 9: // Do not care
          break;
      }
    }
    return true;
  }

  private static boolean testCliffEdge(final Tile t, final Map<Cardinal, Tile> neighbours, boolean N, boolean E, boolean S, boolean W) {
    for (Cardinal D : Cardinal.NESW) if (neighbours.get(D).type == kSTAIRS) return false;
    return (t.level - (N ? 1 : 0) == neighbours.get(Cardinal.kN).level
            && t.level - (E ? 1 : 0) == neighbours.get(Cardinal.kE).level
            && t.level - (S ? 1 : 0) == neighbours.get(Cardinal.kS).level
            && t.level - (W ? 1 : 0) == neighbours.get(Cardinal.kW).level);
  }

  private static boolean testCliff(final Tile t, final Map<Cardinal, Tile> neighbours, boolean E, boolean N, boolean W) {
    if (neighbours.get(Cardinal.kN).type == kSTAIRS) return false;
    return (t.level + (E ? 1 : 0)  == neighbours.get(Cardinal.kNE).level
            && t.level + (N ? 1 : 0)  == neighbours.get(Cardinal.kN).level
            && t.level + (W ? 1 : 0)  == neighbours.get(Cardinal.kNW).level);
  }

  private static boolean testGrass(final Tile T, final Map<Cardinal, Tile> neighbours, boolean N, boolean E, boolean S, boolean W) {
    if (T.colour != kGREEN) return false;
    return (neighbours.get(Cardinal.kN).colour == (N ? kGREEN : kRED)
            && neighbours.get(Cardinal.kE).colour == (E ? kGREEN : kRED)
            && neighbours.get(Cardinal.kS).colour == (S ? kGREEN : kRED)
            && neighbours.get(Cardinal.kW).colour == (W ? kGREEN : kRED));
  }


  // Large test case for all possible tiles
  public static String getTextureString(final Tile t, final Map<Cardinal, Tile> neighbours) {

    // Temp
//    if (t.mask) return "missing3";
//    if (true) return getTextureString(kGROUND, t.colour);

//    if (t.type == kSTAIRS) return "missing2";


    // Stairs N
    if (testStairs(t, neighbours, 1, 0, -1, 0)) return getTextureString(kSTAIRS, t.colour, Cardinal.kN, Cardinal.kN);
    if (testStairs(t, neighbours, 1, 9, -1, 0)) return getTextureString(kSTAIRS, t.colour, Cardinal.kN, Cardinal.kE);
    if (testStairs(t, neighbours, 1, 0, -1, 9)) return getTextureString(kSTAIRS, t.colour, Cardinal.kN, Cardinal.kW);
    // Stairs S
    if (testStairs(t, neighbours, -1, 0, 1, 0)) return getTextureString(kSTAIRS, t.colour, Cardinal.kS, Cardinal.kS);
    if (testStairs(t, neighbours, -1, 9, 1, 0)) return getTextureString(kSTAIRS, t.colour, Cardinal.kS, Cardinal.kE);
    if (testStairs(t, neighbours, -1, 0, 1, 9)) return getTextureString(kSTAIRS, t.colour, Cardinal.kS, Cardinal.kW);
    // Stairs E
    if (testStairs(t, neighbours, 0, -1, 0, 1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kE, Cardinal.kE);
    if (testStairs(t, neighbours, 9, -1, 0, 1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kE, Cardinal.kN);
    if (testStairs(t, neighbours, 0, -1, 9, 1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kE, Cardinal.kS);
    // Stairs W
    if (testStairs(t, neighbours, 0, 1, 0, -1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kW, Cardinal.kW);
    if (testStairs(t, neighbours, 9, 1, 0, -1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kW, Cardinal.kN);
    if (testStairs(t, neighbours, 0, 1, 9, -1)) return getTextureString(kSTAIRS, t.colour, Cardinal.kW, Cardinal.kS);
    // Single stair
    if (testStairs(t, neighbours, 1, 9, -1, 9)) return getTextureString(kSTAIRS_1, t.colour, Cardinal.kN);
    if (testStairs(t, neighbours, -1, 9, 1, 9)) return getTextureString(kSTAIRS_1, t.colour, Cardinal.kS);
    if (testStairs(t, neighbours, 9, -1, 9, 1)) return getTextureString(kSTAIRS_1, t.colour, Cardinal.kE);
    if (testStairs(t, neighbours, 9, 1, 9, -1)) return getTextureString(kSTAIRS_1, t.colour, Cardinal.kW);

    // Two special cases with clif edges reaching stairs
    if (neighbours.get(Cardinal.kE).type == kSTAIRS
            && neighbours.get(Cardinal.kSE).type == kGROUND
            && neighbours.get(Cardinal.kSE).level < t.level) {
      return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kE);
    }
    if (neighbours.get(Cardinal.kW).type == kSTAIRS
            && neighbours.get(Cardinal.kSW).type == kGROUND
            && neighbours.get(Cardinal.kSW).level < t.level) {
      return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kW);
    }
    // TODO could also do kSE and kSW cases here

    // Cliff Edges (x8)
    if (testCliffEdge(t, neighbours, true,false,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kN);
    if (testCliffEdge(t, neighbours, true,true,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kNE);
    if (testCliffEdge(t, neighbours, false,true,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kE);
    if (testCliffEdge(t, neighbours, false,true,true,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kSE);
    if (testCliffEdge(t, neighbours, false,false,true,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kS);
    if (testCliffEdge(t, neighbours, false,false,true,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kSW);
    if (testCliffEdge(t, neighbours, false,false,false,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kW);
    if (testCliffEdge(t, neighbours, true,false,false,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kNW);
    // Two-sides (x2)
    if (testCliffEdge(t, neighbours, true,false,true,false)) return getTextureString(kCLIFF_EDGE_2, t.colour, Cardinal.kE);
    if (testCliffEdge(t, neighbours, false,true,false,true)) return getTextureString(kCLIFF_EDGE_2, t.colour, Cardinal.kN);
    // Three-sides (x4)
    if (testCliffEdge(t, neighbours, true,true,false,true)) return getTextureString(kCLIFF_EDGE_3, t.colour, Cardinal.kN);
    if (testCliffEdge(t, neighbours, true,true,true,false)) return getTextureString(kCLIFF_EDGE_3, t.colour, Cardinal.kE);
    if (testCliffEdge(t, neighbours, false,true,true,true)) return getTextureString(kCLIFF_EDGE_3, t.colour, Cardinal.kS);
    if (testCliffEdge(t, neighbours, true,false,true,true)) return getTextureString(kCLIFF_EDGE_3, t.colour, Cardinal.kW);
    // Four sides
    if (testCliffEdge(t, neighbours, true,true,true,true)) return getTextureString(kCLIFF_EDGE_4, t.colour);

    // Bottom of cliff. This colour block keeps the correct void fade around tight corners
    Colour lowerC = neighbours.get(Cardinal.kS).colour;
    if      (neighbours.get(Cardinal.kSE).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kSE).colour;
    else if (neighbours.get(Cardinal.kSW).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kSW).colour;
    else if (neighbours.get(Cardinal.kE).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kE).colour;
    else if (neighbours.get(Cardinal.kW).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kW).colour;
    // Bottom of cliff.
    if (testCliff(t, neighbours,true, true, true)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kS, lowerC);
    if (testCliff(t, neighbours,false, true, true)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kSE, lowerC);
    if (testCliff(t, neighbours,true, true, false)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kSW, lowerC);
    // Three sides
    if (testCliff(t, neighbours,false, true, false)) return getTextureString(kCLIFF_3, neighbours.get(Cardinal.kN).colour, Cardinal.kS, lowerC);


    // Grass-Dirt boundary
    // sides
    if (testGrass(t, neighbours, false,true,true,true)) return getTextureString(kGRASS_EDGE, t.colour, Cardinal.kN);
    if (testGrass(t, neighbours, true,false,true,true)) return getTextureString(kGRASS_EDGE, t.colour, Cardinal.kE);
    if (testGrass(t, neighbours, true,true,false,true)) return getTextureString(kGRASS_EDGE, t.colour, Cardinal.kS);
    if (testGrass(t, neighbours, true,true,true,false)) return getTextureString(kGRASS_EDGE, t.colour, Cardinal.kW);
    // Corners
    if (testGrass(t, neighbours, false,false,true,true)) return getTextureString(kGRASS_CORNER, t.colour, Cardinal.kNE);
    if (testGrass(t, neighbours, true,false,false,true)) return getTextureString(kGRASS_CORNER, t.colour, Cardinal.kSE);
    if (testGrass(t, neighbours, true,true,false,false)) return getTextureString(kGRASS_CORNER, t.colour, Cardinal.kSW);
    if (testGrass(t, neighbours, false,true,true,false)) return getTextureString(kGRASS_CORNER, t.colour, Cardinal.kNW);
    // Inner corners
    if (testGrass(t, neighbours, true,true,true,true)) {
      if (neighbours.get(Cardinal.kNE).colour == kRED && neighbours.get(Cardinal.kSW).colour == kRED)
        return getTextureString(kGRASS_INNER_CORNER_DOUBLE, t.colour, Cardinal.kNE);
      if (neighbours.get(Cardinal.kSE).colour == kRED && neighbours.get(Cardinal.kNW).colour == kRED)
        return getTextureString(kGRASS_INNER_CORNER_DOUBLE, t.colour, Cardinal.kSE);
      if (neighbours.get(Cardinal.kNE).colour == kRED) return getTextureString(kGRASS_INNER_CORNER, t.colour, Cardinal.kNE);
      if (neighbours.get(Cardinal.kSE).colour == kRED) return getTextureString(kGRASS_INNER_CORNER, t.colour, Cardinal.kSE);
      if (neighbours.get(Cardinal.kSW).colour == kRED) return getTextureString(kGRASS_INNER_CORNER, t.colour, Cardinal.kSW);
      if (neighbours.get(Cardinal.kNW).colour == kRED) return getTextureString(kGRASS_INNER_CORNER, t.colour, Cardinal.kNW);
    }

    // Check cliff inner-edge (away from stairs)
    int stepNeighbour = 0;
    for (Cardinal D : Cardinal.NESW) if (neighbours.get(D).type == kSTAIRS) ++stepNeighbour;
    if (stepNeighbour == 0) {
      if (t.level - 1 == neighbours.get(Cardinal.kNE).level
              && t.level - 1 == neighbours.get(Cardinal.kNW).level)
        return getTextureString(kGROUND_CORNER, t.colour, Cardinal.kN);
      else if (t.level - 1 == neighbours.get(Cardinal.kNE).level)
        return getTextureString(kGROUND_CORNER, t.colour, Cardinal.kNE);
      else if (t.level - 1 == neighbours.get(Cardinal.kNW).level)
        return getTextureString(kGROUND_CORNER, t.colour, Cardinal.kNW);
    }

    // Regular ground
    return getTextureString(kGROUND, t.colour);

    // Void
//    if (t.level == 0) return getTextureString(kGROUND, Colour.kBLACK);

//    return "missing";
  }
}
