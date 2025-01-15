package net.shirojr.nemuelch.entity.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class DropPotDamageSource extends DamageSource {
    private final Entity attacker;

    public DropPotDamageSource(String name, Entity attacker) {
        super(name);
        this.attacker = attacker;
    }

    @Nullable
    @Override
    public Entity getAttacker() {
        return attacker;
    }

    public static DropPotDamageSource create(Entity attacker) {
        return new DropPotDamageSource("drop_pot_hit", attacker);
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        if (this.attacker != null && this.attacker instanceof LivingEntity) {
            String key = "death.attack.%s.player".formatted(this.name);
            return new TranslatableText(key, entity.getDisplayName(), this.attacker.getDisplayName());
        }
        return new TranslatableText("death.attack." + this.name, entity.getDisplayName());
    }
}
