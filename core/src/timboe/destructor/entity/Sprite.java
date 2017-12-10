package timboe.destructor.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import timboe.destructor.Param;
import timboe.destructor.manager.World;
import timboe.destructor.pathfinding.PathFinding;

import java.util.List;
import java.util.Set;

public class Sprite extends Entity {

  public List<Tile> pathingList;
  private int circleOffset = 0;

  public Sprite(int x, int y) {
    super(x, y, Param.TILE_S * Param.SPRITE_SCALE);
  }

  public void pathTo(Tile target, Set<Tile> solutionKnownFrom, Set<Sprite> doneSet) {
    if (target == null) return;
    pathingList = PathFinding.doAStar(getMyTile(), target, solutionKnownFrom, doneSet);
//    Gdx.app.log("pathTo", "Pathed in " + (pathingList != null ? pathingList.size() : " NULL ") + " steps");
  }

  public Tile getMyTile() {
    return World.getInstance().getTile(Math.round(getX() / scale), Math.round(getY() / scale));
  }

  public void draw(ShapeRenderer sr) {
    if (Param.DEBUG > 0 && pathingList != null && pathingList.size() > 1) {
      for (int i = 1; i < pathingList.size(); ++i) {
        Tile previous = pathingList.get(i-1);
        Tile current = pathingList.get(i);
        sr.line(previous.centreScaleSprite, current.centreScaleSprite);
      }
    }
    if (!selected) return;
    for (float a = 0; a < 360; a += 120) {
      sr.arc( getX() + getWidth()/2, getY() + getWidth()/2, getWidth(), a + 2*Param.FRAME, 30);
      sr.arc( getX() + getWidth()/2, getY() + getWidth()/2, getWidth(), a - 2*Param.FRAME, 30);
    }
  }

}
