package timboe.destructor.enums;

import timboe.destructor.Param;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Edge {
  kFLAT,
  kIN,
  kOUT,
  kSTAIRS_IN,
  kSTAIRS_OUT,
  kSTAIRS;

  private static final List<Edge> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size();
  private static final Random R = new Random();

  public static Edge random(boolean includeStairs) { // Returns FLAT, IN, OUR or (if bool is true) has a small chance of STRAIGHT_IN (leads to stairs)
    if (includeStairs && R.nextFloat() < Param.STAIRS_PROB) return kSTAIRS_IN;
    return values.get(R.nextInt(size - 3)); // Minus 3 avoids the three stairs related ones
  }
}
