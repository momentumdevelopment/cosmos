package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.windowed.window.ScrollableWindow;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;

public class ChangelogWindow extends ScrollableWindow {

    private float offset;

    public ChangelogWindow(String name, Vec2f position) {
        super(name, new ResourceLocation("cosmos", "textures/icons/info.png"), position, 150, 250, true);
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + 3), (int) (getPosition().y + getBar() + 2), (int) (getPosition().x + getWidth() - 15), (int) (getPosition().y - getBar() + getHeight() + 11));
            glEnable(GL_SCISSOR_TEST);
        }

        // changelog background
        RenderUtil.drawBorderRect(getPosition().x + 4, getPosition().y + getBar() + 3, getWidth() - 20, getHeight() - getBar() - 3, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        // changelog list
        offset = 0;
        getCosmos().getChangelogManager().getChangelog().forEach(change -> {
            // change formatted
            StringBuilder changeBuilder = new StringBuilder();

            // bullet point
            if (change.contains("-")) {
                changeBuilder.append("   ");
            }

            changeBuilder.append(change);

            // normal size
            if (change.contains(":  ")) {
                FontUtil.drawStringWithShadow(changeBuilder.toString(), getPosition().x + 7, getPosition().y + getBar() + 5 + offset - getScroll(), -1);
                offset += FontUtil.getFontHeight() + 1;
            }

            // subtext
            else {
                glPushMatrix();
                glScaled(0.8, 0.8, 0.8); {
                    float scaledX = (getPosition().x + 7) * 1.25F;
                    float scaledY = (getPosition().y + getBar() + 5 + offset - getScroll()) * 1.25F;
                    FontUtil.drawStringWithShadow(changeBuilder.toString(), scaledX, scaledY, -1);
                }

                glScaled(1.25, 1.25, 1.25);
                glPopMatrix();

                offset += (FontUtil.getFontHeight() * 0.8F) + 1;
            }
        });

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();

        // lower bound is our last offset
        setLowerBound(offset);
    }
}
