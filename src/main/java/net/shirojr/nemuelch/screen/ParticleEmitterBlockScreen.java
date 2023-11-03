package net.shirojr.nemuelch.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;

import java.util.List;

public class ParticleEmitterBlockScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/blank_screen.png");
    private static final int BACKGROUND_WIDTH = 251, BACKGROUND_HEIGHT = 139;

    private int particleCount;
    private final List<ButtonWidget> buttons = Lists.newArrayList();


    public ParticleEmitterBlockScreen(Text title, BlockPos particleEmitterBlockPos) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.add(this.addDrawableChild(new ButtonWidget(this.width / 2, this.height / 2, 20, 20,
                new TranslatableText("screen.nemuelch.particle_emitter.button_particle_list"), (button) -> {
            // only client side

            if (this.client != null) {
                this.client.setScreen(new ParticleListScreen(Text.of("Particle List")));
                this.close();
            }
        })));

        this.addDrawableChild(new SliderWidget(this.width / 2 - 154, 180, 100, 20, LiteralText.EMPTY, 0.0) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(new TranslatableText("screen.nemuelch.particle_count", ParticleEmitterBlockScreen.this.particleCount));
            }

            @Override
            protected void applyValue() {
                ParticleEmitterBlockScreen.this.particleCount = MathHelper.floor(MathHelper.clampedLerp(0.0, 20.0, this.value));
            }
        });
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - BACKGROUND_WIDTH) / 2;
        int y = (height - BACKGROUND_HEIGHT) / 2;

        drawTexture(matrices, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        super.renderBackground(matrices);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {


        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        //TODO: send networkpacket of changed values

        super.close();
    }

    private static class ParticleListScreen extends Screen {
        protected ParticleListScreen(Text title) {
            super(title);
        }
    }
}
