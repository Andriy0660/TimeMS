package com.example.timecraft.domain.sync.util;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SyncUtils {
  public static String generateColor(final String input) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    final byte[] hashBytes = digest.digest(input.getBytes());

    int rgb = getRgb(hashBytes);

    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    return String.format("#%02x%02x%02x", red, green, blue);
  }

  private static int getRgb(final byte[] hashBytes) {
    int hueSum = 0;
    int saturationSum = 0;
    int brightnessSum = 0;

    for (int i = 0; i < hashBytes.length; i++) {
      int value = hashBytes[i] & 0xFF;
      hueSum += value * (i + 1);
      if (i % 3 == 0) saturationSum += value;
      if (i % 5 == 0) brightnessSum += value;
    }

    float hue = (hueSum % 360) / 360f;
    float saturation = (saturationSum % 500) / 1000f;
    float brightness = 0.7f + (brightnessSum % 300) / 1000f;

    return Color.HSBtoRGB(hue, saturation, brightness);
  }
}
