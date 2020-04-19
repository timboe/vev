package timboe.vev.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import timboe.vev.Pair;
import timboe.vev.entity.Sprite;

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

  final Pair<Particle, Particle> returnValue = new Pair<Particle, Particle>();

  public boolean accepts(Sprite s) {
    Particle p = s.getParticle();
    for (int i = 0; i < N_MODES; ++i) {
      if (getInput(i) == p) return true;
    }
    return false;
  }

  public int getShortcut() {
    switch (this) {
      case kHZE:
        return Input.Keys.NUM_1;
      case kHWM:
        return Input.Keys.NUM_2;
      case kWEQ:
        return Input.Keys.NUM_4;
      case kZMQ:
        return Input.Keys.NUM_5;
      case kMINE:
        return Input.Keys.NUM_6;
      case kWARP:
        return Input.Keys.NUM_7;
      default:
        return Input.Keys.NUM_0;
    }
  }

  public String getString() {
    switch (this) {
      case kHZE:
        return "Disassembler Alpha";
      case kHWM:
        return "Disassembler Beta";
      case kWEQ:
        return "Disassembler Gamma";
      case kZMQ:
        return "Disassembler Omega";
      case kMINE:
        return "Refinery";
      case kWARP:
        return "Warp";
      default:
        return "?";
    }
  }

  public int getBaseCost() {
    switch (this) {
      case kMINE:
        return 3000;
      case kHZE:
      case kHWM:
      case kWEQ:
      case kZMQ:
      default:
        return 10000;
    }
  }

  public int getQueueBaseCost() {
    switch (this) {
      case kHZE:
      case kWEQ:
      case kHWM:
      case kZMQ:
        return 100;
      case kMINE:
      default:
        return 0;
    }
  }

  public float getCostIncrease() {
    switch (this) {
      case kHWM:
      case kHZE:
      case kWEQ:
      case kZMQ:
      case kMINE:
      default:
        return 1.4f;
    }
  }


  public float getUpgradeBaseCost() {
    switch (this) {
      case kMINE:
        return 2500;
      case kHZE:
      case kHWM:
      case kWEQ:
      case kZMQ:
      default:
        return 7500;
    }
  }

  public float getUpgradeBaseTime() {
    return 5f;
  }

  public Particle getInput(int mode) {
    if (mode >= N_MODES || this == kMINE)
      Gdx.app.error("getInput", "Invalid mode:" + mode + " or getInput called on a kMINE");
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
      case kMINE:
      default:
        return Particle.kBlank;
    }
  }

  public float getDisassembleMod(Particle p) {
    switch (this) {
      case kHZE:
        if (p == Particle.kH) return 2f;
        if (p == Particle.kZ) return .9f;
      case kHWM:
        if (p == Particle.kH) return .6f;
        if (p == Particle.kM) return .9f;
      case kWEQ:
        if (p == Particle.kW) return .9f;
        if (p == Particle.kE) return .9f;
      case kZMQ:
        if (p == Particle.kQ) return .9f;
      default:
        return 1f;
    }
  }

  public Pair<Particle, Particle> getOutputs(Particle p) {
    for (int i = 0; i < N_MODES; ++i) {
      if (getInput(i) == p) return getOutputs(i);
    }
    return null;
  }

  public Pair<Particle, Particle> getOutputs(int mode) {
    if (mode >= N_MODES) Gdx.app.error("getOutputs", "Invalid mode:" + mode);
    switch (this) {
      case kHZE:
        if (mode == 0) return returnValue.set(Particle.kW, Particle.kW);
        if (mode == 1) return returnValue.set(Particle.kM, Particle.kM);
        if (mode == 2) return returnValue.set(null, null);
      case kHWM:
        if (mode == 0) return returnValue.set(Particle.kZ, Particle.kZ);
        if (mode == 1) return returnValue.set(Particle.kE, null);
        if (mode == 2) return returnValue.set(null, null);
      case kWEQ:
        if (mode == 0) return returnValue.set(Particle.kM, null);
        if (mode == 1) return returnValue.set(null, null);
        if (mode == 2) return returnValue.set(null, null);
      case kZMQ:
        if (mode == 0) return returnValue.set(Particle.kE, Particle.kE);
        if (mode == 1) return returnValue.set(null, null);
        if (mode == 2) return returnValue.set(null, null);
      case kMINE:
      default:
        return returnValue.set(null, null);
    }
  }

  // How much energy is released in the decay.
  // Rest is still locked up in the decay particles
  public int getOutputEnergy(int mode) {
    final int createEnergy = getInput(mode).getCreateEnergy();
    Pair<Particle, Particle> outputs = getOutputs(mode);
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
