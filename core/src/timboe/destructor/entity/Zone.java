package timboe.destructor.entity;

import javafx.util.Pair;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;
import timboe.destructor.pathfinding.IVector2;

import java.util.Vector;

public class Zone extends Entity {
  public boolean merged;
  public boolean hillWithinHill;
  public IVector2 location = new IVector2();
  public IVector2 lowerLeft = new IVector2();
  public IVector2 upperRight = new IVector2();
  public int w;
  public int h;

  public Zone(int x, int y) {
    merged = false;
    colour = Colour.kRED;
    mask = false;
    hillWithinHill = false;
    level = 1;
    location.set(x, y);
    w = (int) Math.round(Param.TILES_X / (double)Param.ZONES_X);
    h = (int) Math.round(Param.TILES_Y / (double)Param.ZONES_Y);
    int xLeft  = x     * w;
    int xRight = (x+1) * w;
    int yBottom  = y     * h;
    int yTop     = (y+1) * h;
    lowerLeft.set(xLeft, yBottom);
    upperRight.set(xRight, yTop);
    setBounds(xLeft * Param.TILE_S, yBottom * Param.TILE_S, w * Param.TILE_S, h * Param.TILE_S);
  }

  public void addEdgePairs(Vector<Pair<IVector2,IVector2>> edgePairs) {
    // Bottom left to top left, TL to TR, TR to BR, BR to BL
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX(),getLowerY()), new IVector2(getLowerX(),getLowerY() + h)));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX(),getLowerY() + h), new IVector2(getUperX(),getUperY())));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getUperX(),getUperY()), new IVector2(getLowerX() + w, getLowerY())));
    edgePairs.add(new Pair<IVector2, IVector2>(new IVector2(getLowerX() + w, getLowerY()), new IVector2(getLowerX(),getLowerY())));
  }

  public int getZoneX() { return location.x; }

  public int getZoneY() { return location.y; }

  public int getLowerX() { return lowerLeft.x; }

  public int getUperX() { return upperRight.x; }

  public int getLowerY() { return lowerLeft.y; }

  public int getUperY() { return upperRight.y; }

}
