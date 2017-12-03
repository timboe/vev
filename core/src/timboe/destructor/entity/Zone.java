package timboe.destructor.entity;

import javafx.util.Pair;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;
import timboe.destructor.pathfinding.IVector2;

import java.util.Vector;

public class Zone extends Entity {
  public boolean merged = false;
  public boolean hillWithinHill = false;
  public boolean hasTiberium = false;
  public boolean hasWarp = false;
  public IVector2 lowerLeft = new IVector2();
  public IVector2 upperRight = new IVector2();
  public int w, h;

  public Zone(int x, int y) {
    colour = Colour.kRED;
    mask = false;
    level = 1;
    w = (Param.TILES_X / Param.ZONES_X);
    h = (Param.TILES_Y / Param.ZONES_Y);
    final int xLeft  = x     * w;
    final int xRight = (x+1) * w;
    final int yBottom  = y     * h;
    final int yTop     = (y+1) * h;
    lowerLeft.set(xLeft, yBottom);
    upperRight.set(xRight, yTop);
    setBounds(xLeft * Param.TILE_S, yBottom * Param.TILE_S,
            w * Param.TILE_S,
            h * Param.TILE_S);
  }

  public void addEdgePairs(Vector<Pair<IVector2,IVector2>> edgePairs) {
    // Bottom left to top left, TL to TR, TR to BR, BR to BL
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX(),getLowerY()), new IVector2(getLowerX(),getLowerY() + h)));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX(),getLowerY() + w), new IVector2(getUperX(),getUperY())));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getUperX(),getUperY()), new IVector2(getLowerX() + w, getLowerY())));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX() + w, getLowerY()), new IVector2(getLowerX(),getLowerY())));
  }

  public boolean inZone(int x, int y) {
    return (x >= getLowerX() && x < getUperX() && y >= getLowerY() && y < getUperY());
  }

  public int getLowerX() { return lowerLeft.x; }

  private int getUperX() { return upperRight.x; }

  public int getLowerY() { return lowerLeft.y; }

  private int getUperY() { return upperRight.y; }

}
