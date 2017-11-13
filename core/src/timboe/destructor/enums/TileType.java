package timboe.destructor.enums;

import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;

import java.util.Map;
import java.util.Random;

public enum TileType {
  kGROUND,
  kCLIFF_EDGE,
  kCLIFF_EDGE_3,
  kCLIFF,
  kCLIFF_3,
  kSTAIRS;

  private static Random R = new Random();

  public static String getTextureString(TileType tt, Colour c) {
    return getTextureString(tt, c, Cardinal.kN, Colour.kBLACK);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D) {
    return getTextureString(tt, c, D, Colour.kBLACK);
  }

  public static String getTextureString(TileType tt, Colour c, Cardinal D, Colour c2) {
    switch (tt) {
      case kGROUND:
        if (c == Colour.kBLACK) return "b";
        else return "floor_" + c.getString() + "_" + (R.nextFloat() < .9f ? "2" : Util.rndInt(Param.N_GRASS_TILES).toString());
        // 90% chance of having "plain" ground
      case kCLIFF_EDGE:
        return "h_" + c.getString() + "_" + D.getString();
      case kCLIFF_EDGE_3:
        return "h3_" + c.getString() + "_" + D.getString();
      case kCLIFF:
        return "h_" + c.getString() + "_" + D.getString() + "_" + c2.getString();
      case kCLIFF_3:
        return "h3_" + c.getString() + "_" + D.getString() + "_" + c2.getString();
    }
    return "missing";
  }


  private static boolean testCliffEdge(final Tile t, final Map<Cardinal, Tile> neighbours, boolean N, boolean E, boolean S, boolean W) {
    return (neighbours.get(Cardinal.kN).type == kGROUND && t.level - (N ? 1 : 0) == neighbours.get(Cardinal.kN).level
            && neighbours.get(Cardinal.kE).type == kGROUND && t.level - (E ? 1 : 0) == neighbours.get(Cardinal.kE).level
            && neighbours.get(Cardinal.kS).type == kGROUND && t.level - (S ? 1 : 0) == neighbours.get(Cardinal.kS).level
            && neighbours.get(Cardinal.kW).type == kGROUND && t.level - (W ? 1 : 0) == neighbours.get(Cardinal.kW).level);
  }

  private static boolean testCliff(final Tile t, final Map<Cardinal, Tile> neighbours, boolean E, boolean N, boolean W) {
    return (t.level + (E ? 1 : 0)  == neighbours.get(Cardinal.kNE).level
            && t.level + (N ? 1 : 0)  == neighbours.get(Cardinal.kN).level
            && t.level + (W ? 1 : 0)  == neighbours.get(Cardinal.kNW).level);
  }


  // Large test case for all possible tiles
  public static String getTextureString(final Tile t, final Map<Cardinal, Tile> neighbours) {

    // Grass
    if (t.type == kGROUND
            && neighbours.get(Cardinal.kN).type == kGROUND && t.colour == neighbours.get(Cardinal.kN).colour
            && neighbours.get(Cardinal.kE).type == kGROUND && t.colour == neighbours.get(Cardinal.kE).colour
            && neighbours.get(Cardinal.kS).type == kGROUND && t.colour == neighbours.get(Cardinal.kS).colour
            && neighbours.get(Cardinal.kW).type == kGROUND && t.colour == neighbours.get(Cardinal.kW).colour) {
      return getTextureString(kGROUND, t.colour);
    }

    // Cliff Edges
    if (testCliffEdge(t, neighbours, true,false,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kN);
    if (testCliffEdge(t, neighbours, true,true,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kNE);
    if (testCliffEdge(t, neighbours, false,true,false,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kE);
    if (testCliffEdge(t, neighbours, false,true,true,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kSE);
    if (testCliffEdge(t, neighbours, false,false,true,false)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kS);
    if (testCliffEdge(t, neighbours, false,false,true,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kSW);
    if (testCliffEdge(t, neighbours, false,false,false,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kW);
    if (testCliffEdge(t, neighbours, true,false,false,true)) return getTextureString(kCLIFF_EDGE, t.colour, Cardinal.kNW);
    // Three-sides
    if (testCliffEdge(t, neighbours, false,true,true,true)) return getTextureString(kCLIFF_EDGE_3, t.colour, Cardinal.kS);


    // Bottom of cliff. This colour block keeps the correct void fade around tight corners
    Colour lowerC = neighbours.get(Cardinal.kS).colour;
    if      (neighbours.get(Cardinal.kSE).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kSE).colour;
    else if (neighbours.get(Cardinal.kSW).level < neighbours.get(Cardinal.kS).level) lowerC = neighbours.get(Cardinal.kSW).colour;
    if (testCliff(t, neighbours,true, true, true)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kS, lowerC);
    if (testCliff(t, neighbours,false, true, true)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kSE, lowerC);
    if (testCliff(t, neighbours,true, true, false)) return getTextureString(kCLIFF, neighbours.get(Cardinal.kN).colour, Cardinal.kSW, lowerC);
    // Three sides
    if (testCliff(t, neighbours,false, true, false)) return getTextureString(kCLIFF_3, neighbours.get(Cardinal.kN).colour, Cardinal.kS, lowerC);


    // Void
    if (t.level == 0) return getTextureString(kGROUND, Colour.kBLACK);

    return "missing";
  }
}
