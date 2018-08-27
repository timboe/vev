package timboe.destructor.enums;

import com.badlogic.gdx.Gdx;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.entity.Sprite;
import timboe.destructor.manager.GameState;

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
    Colour c = ((Particle) s.getUserObject()).getColourFromParticle();
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
    return Math.round(getBaseCost() + (getQueueCost() * GameState.getInstance().queueSize));
  }

  // TODO
  public float getBaseCost() {
    switch (this) {
      case kHZE: return 5000;
      case kHWM: return 5000;
      case kWEQ: return 2500;
      case kZMQ: return 2500;
      case kMINE: return 10000;
      default: return 10000;
    }
  }

  // TODO
  public float getQueueCost() {
    switch (this) {
      case kHZE: return 100;
      case kHWM: return 100;
      case kWEQ: return 150;
      case kZMQ: return 150;
      case kMINE: return 0;
      default: return 100;
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

  public float getDissassembleBonus(Particle p) {
    switch (this) {
      case kHZE:
        if (p == Particle.kH) return 0.75f; // Z, e
      case kHWM:
        if (p == Particle.kW) return 0.8f; // H, m
      case kWEQ:
        if (p == Particle.kQ) return 0.85f; // W, e
      case kZMQ:
        if (p == Particle.kZ) return 0.8f;
      default: return 1f;
    }
  }

  public Pair<Particle,Particle> getOutputs(Particle p) {
    for (int i = 0; i < N_MODES; ++i) {
      if (getInput(i) == p) return getOutputs(i);
    }
    return null;
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

  // How much energy is released in the decay.
  // Rest is still locked up in the decay particles
  public int getOutputEnergy(int mode) {
    final int createEnergy = getInput(mode).getCreateEnergy();
    Pair<Particle,Particle> outputs = getOutputs(mode);
    int decayCreateEnergy = 0;
    if (outputs.getKey() != null) decayCreateEnergy += outputs.getKey().getCreateEnergy();
    if (outputs.getValue() != null) decayCreateEnergy += outputs.getValue().getCreateEnergy();
    return createEnergy - decayCreateEnergy;
  }

    public int getOutputEnergy(Particle p) {
    for (int mode = 0; mode < N_MODES; ++mode) {
      if (getInput(mode) == p) {
        return getOutputEnergy(mode);
      }
    }
    return 0;
  }

}
