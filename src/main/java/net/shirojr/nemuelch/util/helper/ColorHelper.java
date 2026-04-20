package net.shirojr.nemuelch.util.helper;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

@SuppressWarnings("unused")
public class ColorHelper {

    /////////////////////// BASIC COLOR UTIL ///////////////////////

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

    public static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    /////////////////////// HSL CONVERSION UTIL ///////////////////////

    public static int hslToRgb(Vector3f hsl) {
        float h = hsl.x;
        float s = hsl.y;
        float l = hsl.z;

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = l - c / 2;

        float r, g, b;

        if (h < 1f/6f) {
            r = c; g = x; b = 0;
        } else if (h < 2f/6f) {
            r = x; g = c; b = 0;
        } else if (h < 3f/6f) {
            r = 0; g = c; b = x;
        } else if (h < 4f/6f) {
            r = 0; g = x; b = c;
        } else if (h < 5f/6f) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        int red = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue = Math.round((b + m) * 255);

        return (red << 16) | (green << 8) | blue;
    }

    public static Vector3f rgbToHsl(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0, s = 0, l = (max + min) / 2;

        if (delta != 0) {
            s = l > 0.5f ? delta / (2 - max - min) : delta / (max + min);

            if (max == r) {
                h = ((g - b) / delta + (g < b ? 6 : 0)) / 6f;
            } else if (max == g) {
                h = ((b - r) / delta + 2) / 6f;
            } else {
                h = ((r - g) / delta + 4) / 6f;
            }
        }

        return new Vector3f(h, s, l);
    }

    public static Vector3f getColorFromDec(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new Vector3f(r / 255f, g / 255f, b / 255f);
    }

    public static int getColorFromVec(Vector3f color) {
        int r = (int) (color.x * 255);
        int g = (int) (color.y * 255);
        int b = (int) (color.z * 255);
        return (r << 16) | (g << 8) | b;
    }

    public static Vector3f mixColorsAverage(List<Vector3f> colors) {
        if (colors.isEmpty()) return new Vector3f(0f, 0f, 0f);
        float r = 0f, g = 0f, b = 0f;
        for (Vector3f color : colors) {
            r += color.x;
            g += color.y;
            b += color.z;
        }
        int count = colors.size();
        return new Vector3f(r / count, g / count, b / count);
    }
}
