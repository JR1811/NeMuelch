package net.shirojr.nemuelch.util.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class TextUtil {
    public static List<OrderedText> wrapLines(MinecraftClient client, Text text) {
        return client.textRenderer.wrapLines(text, 170);
    }
}
