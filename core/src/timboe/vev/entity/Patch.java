package timboe.vev.entity;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.World;

/**
 * Created by Tim on 21/01/2018.
 */

public class Patch extends Entity {
  private Vector<Integer> contained = new Vector<Integer>();
  private int untilNextShardConsumed = 0;

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(false);
    JSONObject jsonContained = new JSONObject();
    int count = 0;
    for (Integer i : contained) {
      jsonContained.put(Integer.toString(count++), i);
    }
    json.put("contained", jsonContained);
    json.put("untilNextShardConsumed", this.untilNextShardConsumed);
    return json;
  }

  public Patch(JSONObject json) throws JSONException {
    super(json);
    this.untilNextShardConsumed = json.getInt("untilNextShardConsumed");
    JSONObject jsonContained = json.getJSONObject("contained");
    Iterator it = jsonContained.keys();
    while (it.hasNext()) {
      contained.add(jsonContained.getInt((String) it.next()));
    }
  }

  public void addContained(Entity e) {
    contained.add(e.id);
  }

  public int remaining() {
    return contained.size();
  }

  public void remove(int energy) {
    this.untilNextShardConsumed += energy;
    while (this.untilNextShardConsumed >= Param.ENERGY_PER_SHARD) {
      this.untilNextShardConsumed -= Param.ENERGY_PER_SHARD;
      removeRandom();
    }
  }

  private int removeRandom() {
    if (contained.size() == 0) {
      return 0;
    }
    int index = Util.R.nextInt(remaining());
    int id = contained.elementAt(index);
    contained.remove(index); // From patch's shards
    Sprite t = World.getInstance().tiberiumShards.remove(id); // From global list of shards
    t.remove(); // From stage
    ++GameState.getInstance().tiberiumMined;
    if (Camera.getInstance().onScreen(t)) {
      Sounds.getInstance().dirt();
    }
    return id;
  }

  public Patch(int x, int y) {
    super(x, y);
  }
}
