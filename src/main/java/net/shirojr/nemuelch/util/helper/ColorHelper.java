package net.shirojr.nemuelch.util.helper;

import org.joml.Vector3f;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class ColorHelper {
    public static String vectorToHex(Vector3f color) {
        int r = Math.round(color.x * 255);
        int g = Math.round(color.y * 255);
        int b = Math.round(color.z * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static String vectorToHexWithAlpha(Vector4f color) {
        int r = Math.round(color.x * 255);
        int g = Math.round(color.y * 255);
        int b = Math.round(color.z * 255);
        int a = Math.round(color.w * 255);
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    public static Vector3f hexToVector(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    public static Vector4f hexToVectorWithAlpha(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        float a = 1.0f;
        if (hex.length() == 8) {
            a = Integer.parseInt(hex.substring(6, 8), 16) / 255.0f;
        }

        return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, a);
    }

    public static String intToHex(int color) {
        return String.format("#%06X", color);
    }

    public static String intToHexWithAlpha(int color) {
        return String.format("#%08X", color);
    }
}
