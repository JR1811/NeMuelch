package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public record FleetingWorldNoteData(Vec3d pos, List<Text> lines) {
}
