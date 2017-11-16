package timboe.destructor.entity;

import com.badlogic.gdx.math.Vector2;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;

public class Zone {

  public Colour colour = Colour.kBLACK;
  public boolean hill = false;
  public Vector2 location = new Vector2();
  public Vector2 lowerLeft = new Vector2();
  public Vector2 upperRight = new Vector2();

  public Zone(int x, int y) {
    location.set(x, y);
    long xLeft  = x     * Math.round(Param.TILES_X / (double)Param.ZONES_X);
    long xRight = (x+1) * Math.round(Param.TILES_X / (double)Param.ZONES_X);
    long yBottom  = y     * Math.round(Param.TILES_Y / (double)Param.ZONES_Y);
    long yTop     = (y+1) * Math.round(Param.TILES_Y / (double)Param.ZONES_Y);
    lowerLeft.set(xLeft, yBottom);
    upperRight.set(xRight, yTop);
  }

  public int getZoneX() { return (int) location.x; }

  public int getZoneY() { return (int) location.y; }

  public int getLowerX() { return (int) lowerLeft.x; }

  public int getUperX() { return (int) upperRight.x; }

  public int getLowerY() { return (int) lowerLeft.y; }

  public int getUperY() { return (int) upperRight.y; }

}
