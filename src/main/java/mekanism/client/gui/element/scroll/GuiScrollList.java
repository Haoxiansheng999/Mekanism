package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiScalableElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public abstract class GuiScrollList extends GuiScrollableElement {

    protected static final ResourceLocation SCROLL_LIST = MekanismUtils.getResource(ResourceType.GUI, "scroll_list.png");
    protected static final int TEXTURE_WIDTH = 6;
    protected static final int TEXTURE_HEIGHT = 6;

    private final GuiScalableElement background;
    protected final int elementHeight;

    protected GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, GuiScalableElement background) {
        super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.elementHeight = elementHeight;
        this.background = background;
    }

    @Override
    protected int getFocusedElements() {
        return (field_230689_k_ - 2) / elementHeight;
    }

    public abstract boolean hasSelection();

    protected abstract void setSelected(int index);

    public abstract void clearSelection();

    protected abstract void renderElements(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw the background
        background.func_230430_a_(matrix, mouseX, mouseY, partialTicks);
        background.drawBackground(matrix, mouseX, mouseY, partialTicks);
        GuiElement.minecraft.textureManager.bindTexture(getResource());
        //Draw Scroll
        //Top border
        func_238463_a_(matrix, barX - 1, barY - 1, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle border
        func_238466_a_(matrix, barX - 1, barY, 6, maxBarHeight, 0, 1, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Bottom border
        func_238463_a_(matrix, barX - 1, field_230691_m_ + maxBarHeight + 2, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Scroll bar
        func_238463_a_(matrix, barX, barY + getScroll(), 0, 2, barWidth, barHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Draw the elements
        renderElements(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        super.func_230982_a_(mouseX, mouseY);
        if (mouseX >= field_230690_l_ + 1 && mouseX < barX - 1 && mouseY >= field_230691_m_ + 1 && mouseY < field_230691_m_ + field_230689_k_ - 1) {
            int index = getCurrentSelection();
            clearSelection();
            for (int i = 0; i < getFocusedElements(); i++) {
                if (index + i < getMaxElements()) {
                    int shiftedY = field_230691_m_ + 1 + elementHeight * i;
                    if (mouseY >= shiftedY && mouseY <= shiftedY + elementHeight) {
                        setSelected(index + i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean func_231043_a_(double mouseX, double mouseY, double delta) {
        return func_231047_b_(mouseX, mouseY) && adjustScroll(delta);
    }
}