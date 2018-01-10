package timboe.destructor.enums;

import com.badlogic.gdx.Gdx;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;

/**
 * Created by Tim on 28/12/2017.
 */

public enum BuildingType {
  kHZE,
  kHWM,
  kWEQ,
  kZMQ,
  kMINE,
  kWARP;

  public static final int N_MODES = 3;

  final Pair<Particle, Particle> returnValue = new Pair<Particle,Particle>();

  public boolean accepts(Sprite s) {
    Colour c = (Colour) s.getUserObject();
    Particle p = Particle.getParticleFromColour(c);
    for (int i = 0; i < N_MODES; ++i) {
      if (getInput(i) == p) return true;
    }
    return false;
  }

  public String getString() {
    switch (this) {
      case kHZE: return "H/Z/e";
      case kHWM: return "H/W/μ";
      case kWEQ: return "W/e/q";
      case kZMQ: return "Z/μ/q";
      case kMINE: return "Mine";
      default: return "?";
    }
  }

  public float getCost() {
    return Math.round(getBaseCost() + (getQueueCost() * Param.QUEUE_SIZE));
  }

  // TODO
  public float getBaseCost() {
    switch (this) {
      default: return 10000;
    }
  }

  // TODO
  public float getQueueCost() {
    switch (this) {
      default: return 1000;
    }
  }

  public Particle getInput(int mode) {
    if (mode >= N_MODES) Gdx.app.error("getInput", "Invalid mode:"+mode);
    switch (this) {
      case kHZE:
        if (mode == 0) return Particle.kH;
        if (mode == 1) return Particle.kZ;
        if (mode == 2) return Particle.kE;
      case kHWM:
        if (mode == 0) return Particle.kH;
        if (mode == 1) return Particle.kW;
        if (mode == 2) return Particle.kM;
      case kWEQ:
        if (mode == 0) return Particle.kW;
        if (mode == 1) return Particle.kE;
        if (mode == 2) return Particle.kQ;
      case kZMQ:
        if (mode == 0) return Particle.kZ;
        if (mode == 1) return Particle.kM;
        if (mode == 2) return Particle.kQ;
      case kMINE: default: return null;
    }
  }

  public Pair<Particle,Particle> getOutputs(int mode) {
    if (mode >= N_MODES) Gdx.app.error("getOutputs", "Invalid mode:"+mode);
    switch (this) {
      case kHZE:
        if (mode == 0) return returnValue.set(Particle.kW,Particle.kW);
        if (mode == 1) return returnValue.set(Particle.kM,Particle.kM);
        if (mode == 2) return returnValue.set(null,null);
      case kHWM:
        if (mode == 0) return returnValue.set(Particle.kZ,Particle.kZ);
        if (mode == 1) return returnValue.set(Particle.kE,null);
        if (mode == 2) return returnValue.set(null,null);
      case kWEQ:
        if (mode == 0) return returnValue.set(Particle.kM,null);
        if (mode == 1) return returnValue.set(null,null);
        if (mode == 2) return returnValue.set(null,null);
      case kZMQ:
        if (mode == 0) return returnValue.set(Particle.kE,Particle.kE);
        if (mode == 1) return returnValue.set(null,null);
        if (mode == 2) return returnValue.set(null,null);
      case kMINE: default: return returnValue.set(null,null);
    }
  }

  public int getOutputEnergy(int mode) {
    if (mode >= N_MODES) Gdx.app.error("getOutputs", "Invalid mode:"+mode);
    switch (this) {
      case kHZE:
        if (mode == 0) return 99;
        if (mode == 1) return 88;
        if (mode == 2) return 77;
      case kHWM:
        if (mode == 0) return 66;
        if (mode == 1) return 55;
        if (mode == 2) return 44;
      case kWEQ:
        if (mode == 0) return 33;
        if (mode == 1) return 22;
        if (mode == 2) return 11;
      case kZMQ:
        if (mode == 0) return 1;
        if (mode == 1) return 9;
        if (mode == 2) return 99;
      case kMINE: default: return 50;
    }
  }

}
