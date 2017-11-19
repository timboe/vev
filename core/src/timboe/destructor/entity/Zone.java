package timboe.destructor.entity;

import timboe.destructor.Param;
import timboe.destructor.enums.Colour;
import timboe.destructor.pathfinding.IVector2;

public class Zone {

  public Colour colour = Colour.kRED;
  public boolean hill = false;
  public IVector2 location = new IVector2();
  public IVector2 lowerLeft = new IVector2();
  public IVector2 upperRight = new IVector2();
  public int w;
  public int h;

  public Zone(int x, int y) {
    location.set(x, y);
    w = (int) Math.round(Param.TILES_X / (double)Param.ZONES_X);
    h = (int) Math.round(Param.TILES_Y / (double)Param.ZONES_Y);
    int xLeft  = x     * w;
    int xRight = (x+1) * w;
    int yBottom  = y     * h;
    int yTop     = (y+1) * h;
    lowerLeft.set(xLeft, yBottom);
    upperRight.set(xRight, yTop);
  }

  public int getZoneX() { return location.x; }

  public int getZoneY() { return location.y; }

  public int getLowerX() { return lowerLeft.x; }

  public int getUperX() { return upperRight.x; }

  public int getLowerY() { return lowerLeft.y; }

  public int getUperY() { return upperRight.y; }

}
