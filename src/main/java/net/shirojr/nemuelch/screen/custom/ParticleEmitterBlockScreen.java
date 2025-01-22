package net.shirojr.nemuelch.screen.custom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.particle.ParticleType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.custom.ParticleEmitterBlockEntity;
import net.shirojr.nemuelch.screen.handler.ParticleEmitterBlockScreenHandler;
import net.shirojr.nemuelch.util.logger.LoggerUtil;

import java.util.List;

public class ParticleEmitterBlockScreen extends HandledScreen<ParticleEmitterBlockScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/blank_screen.png");
    private static final int BACKGROUND_WIDTH = 251, BACKGROUND_HEIGHT = 139;

    private final ParticleEmitterBlockEntity.ParticleData particleData;
    private final List<ButtonWidget> buttons = Lists.newArrayList();

    public ParticleEmitterBlockScreen(ParticleEmitterBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // this.selectedParticleCount = handler.getParticleData().getCount();
        this.particleData = handler.getParticleData();
    }


    @Override
    protected void init() {
        super.init();
        if (client == null) return;
        int x = this.client.getWindow().getScaledWidth() / 2 - BACKGROUND_WIDTH / 2;
        int y = this.client.getWindow().getScaledHeight() / 2 - BACKGROUND_HEIGHT / 2;

        this.buttons.add(this.addDrawableChild(new ButtonWidget(x, y, 100, 20,
                new TranslatableText("screen.nemuelch.button.particle_emitter.particle_list"), (button) -> {
            // only client side
            if (this.client != null) {
                this.client.setScreen(new RegistryParticleListScreen(Text.of("Particle List"), this));
            }
        })));

        this.addDrawableChild(new SliderWidget(this.width / 2 - 154, 180, 100, 20, LiteralText.EMPTY, this.particleData.getCount()) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(new TranslatableText("screen.nemuelch.number.particle_count", ParticleEmitterBlockScreen.this.particleData.getCount()));
            }

            @Override
            protected void applyValue() {
                ParticleEmitterBlockScreen.this.particleData.setCount(MathHelper.floor(MathHelper.clampedLerp(0.0, 20.0, this.value)));
                LoggerUtil.devLogger("slider value: " + this.value);
            }

            @Override
            protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
                LoggerUtil.devLogger("dragging slider");
                //FIXME: is not called!
                super.onDrag(mouseX, mouseY, deltaX, deltaY);
            }
        });
    }

    @Override
    public void close() {
        this.handler.sendParticleDataUpdatePacket(this.particleData);
        super.close();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - BACKGROUND_WIDTH) / 2;
        int y = (height - BACKGROUND_HEIGHT) / 2;

        drawTexture(matrices, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        super.renderBackground(matrices);
    }

    public void setSelectedParticleType(ParticleType<?> selectedParticleType) {
        Identifier particleId = Registry.PARTICLE_TYPE.getId(selectedParticleType);
        this.particleData.setParticle(particleId);
        LoggerUtil.devLogger("Set current particleType to: " + this.particleData.getId());
    }

    public void setSelectedParticleCount(int count) {
        this.particleData.setCount(count);
        LoggerUtil.devLogger("Set current particleCount to: " + this.particleData.getCount());
    }
}
