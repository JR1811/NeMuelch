package net.shirojr.nemuelch.util.duck;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public interface Leashable {
    ActionResult neMuelch$interact(PlayerEntity player, Hand hand);
    Entity neMuelch$getLeashHolder();
}
