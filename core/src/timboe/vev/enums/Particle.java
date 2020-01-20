package timboe.vev.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import timboe.vev.Param;
import timboe.vev.manager.Textures;

/**
 * Created by Tim on 01/01/2018.
 */

public enum Particle {
  kH,
  kW,
  kZ,
  kE,
  kM,
  kQ,
  kBlank;

  private static final List<Particle> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size() - 1; // Do not return kBlank
  private static final Random R = new Random();

  public static int size() {
    return size;
  }

  public static Particle random() {
    return values.get(R.nextInt(size));
  }

  public String getString() {
    switch (this) {
      case kH: return "Θ";
      case kW: return "Δ";
      case kZ: return "Γ";
      case kE: return "Ψ";
      case kM: return "Ω";
      case kQ: return "Σ";
      case kBlank:
      default: return "?";
    }
  }

  public static Particle getParticleFromColour(Colour c) {
    switch (c) {
      case kBLACK: return kH;
      case kRED_DARK: return kW;
      case kGREEN_DARK: return kZ;
      case kRED: return kE;
      case kGREEN: return kM;
      case kBLUE: return kQ;
      default: return null;
    }
  }

  public Color getHighlightColour() {
    switch (this) {
      case kBlank: return Param.PARTICLE_Blank;
      default: return Textures.getInstance().particleBaseColours.get(this);
    }
  }

  // Offset of chevrons so that they don't overlap
  public int getStandingOrderOffset() {
    switch (this) {
      case kH: return 0;
      case kW: return 2;
      case kZ: return 4;
      case kE: return 6;
      case kM: return 8;
      case kQ: return 10;
      case kBlank: return 12;
      default: return 0;
    }
  }

  public Colour getColourFromParticle() {
    switch (this) {
      case kH: return Colour.kBLACK;
      case kW: return Colour.kRED_DARK;
      case kZ: return Colour.kGREEN_DARK;
      case kE: return Colour.kRED;
      case kM: return Colour.kGREEN;
      case kQ: return Colour.kBLUE;
      case kBlank:
      default: return null;
    }
  }

  // TODO tweak
  public int getCreateEnergy() {
    switch (this) {
      case kH: return 305;
      case kW: return 105;
      case kZ: return 115;
      case kE: return 10;
      case kM: return 15;
      case kQ: return 5;
      case kBlank:
      default: return 0;
    }
  }

  public float getSpeedMod() {
    switch (this) {
      case kH:
        return 0.8f;
      case kW:
      case kZ:
        return 0.9f;
      case kQ:
        return 1.1f;
      case kBlank:
      case kE:
      case kM:
      default: return 1f;
    }
  }

  public float getCreateChance() {
    switch (this) {
      case kH:
        return 0.05f;
      case kW:
      case kZ:
        return 0.15f;
      case kE:
      case kM:
        return 0.2f;
      case kQ:
        return 0.25f;
      case kBlank:
      default:
        return 0;
    }
  }

  public float getDisassembleTime() {
    switch (this) {
      case kH: return 1.5f;
      case kW: return 5.5f;
      case kZ: return 5.0f;
      case kE: return .7f;
      case kM: return 1f;
      case kQ: return 0.4f;
      case kBlank:
      default: return 0;
    }
  }

}
