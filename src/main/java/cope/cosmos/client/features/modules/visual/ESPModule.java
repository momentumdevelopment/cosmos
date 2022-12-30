package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IEntityRenderer;
import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.asm.mixins.accessor.IRenderManager;
import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderCrystalEvent;
import cope.cosmos.client.events.render.entity.RenderLivingEntityEvent;
import cope.cosmos.client.events.render.entity.ShaderColorEvent;
import cope.cosmos.client.events.render.entity.tile.RenderTileEntityEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.ColorsModule;
import cope.cosmos.client.features.modules.client.ColorsModule.Rainbow;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.shader.shaders.DotShader;
import cope.cosmos.client.shader.shaders.FillShader;
import cope.cosmos.client.shader.shaders.OutlineShader;
import cope.cosmos.client.shader.shaders.RainbowOutlineShader;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * @author linustouchtips, Surge, aesthetical, oragejuice
 * @since 07/21/2021
 */
public class ESPModule extends Module {
    public static ESPModule INSTANCE;

    public ESPModule() {
        super("ESP", Category.VISUAL, "Allows you to see entities through walls");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SHADER)
            .setDescription("The mode for the render style");

    public static Setting<FragmentShader> shader = new Setting<>("Shader", FragmentShader.OUTLINE)
            .setDescription("The shader to draw on the entity")
            .setVisible(() -> mode.getValue().equals(Mode.SHADER));

    public static Setting<Double> width = new Setting<>("Width", 0.0, 1.25, 5.0, 1)
            .setAlias("LineWidth")
            .setDescription( "Line width for the visual");

    // **************************** entity settings ****************************

    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Highlight players");

    public static Setting<Boolean> passives = new Setting<>("Passives", true)
            .setDescription("Highlight passives");

    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", true)
            .setDescription("Highlight neutrals");

    public static Setting<Boolean> hostiles = new Setting<>("Hostiles", true)
            .setDescription("Highlight hostiles");

    public static Setting<Boolean> items = new Setting<>("Items", true)
            .setDescription("Highlight items");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Highlight crystals");

    public static Setting<Boolean> vehicles = new Setting<>("Vehicles", true)
            .setDescription("Highlight vehicles");

    // **************************** storages ****************************

    public static Setting<Boolean> chests = new Setting<>("Chests", true)
            .setDescription("Highlight chests");

    public static Setting<Boolean> enderChests = new Setting<>("EnderChests", true)
            .setDescription("Highlight chests");

    public static Setting<Boolean> shulkers = new Setting<>("Shulkers", true)
            .setDescription("Highlight shulkers");

    public static Setting<Boolean> hoppers = new Setting<>("Hoppers", true)
            .setDescription("Highlight hoppers");

    public static Setting<Boolean> furnaces = new Setting<>("Furnaces", true)
            .setDescription("Highlight furnaces");

    // **************************** others ****************************

    public static Setting<Boolean> chorus = new Setting<>("Chorus", false)
            .setDescription("Highlights chorus teleports");

    public static Setting<Boolean> lagESP = new Setting<>("Rubberband", false)
            .setDescription("shows your rubberbands");

    public static Setting<Float> fadeSpeed = new Setting<>("FadeSpeed", 1F, 2F, 10F, 1)
            .setDescription("How long the rubber band should show for")
            .setVisible(() -> lagESP.getValue());

    public static Setting<Float> rubberWidth = new Setting<>("LineWidth", 0.1F, 2F, 5F, 1)
            .setDescription("The width of the lines")
            .setVisible(() -> lagESP.getValue());

    public static Setting<Boolean> reverse = new Setting<>("Inverse", false)
            .setDescription("Direction of fade")
            .setVisible(() -> lagESP.getValue());

    // framebuffer
    private Framebuffer framebuffer;
    private int lastScaleFactor;
    private int lastScaleWidth;
    private int lastScaleHeight;

    // shaders
    private final OutlineShader outlineShader = new OutlineShader();
    private final RainbowOutlineShader rainbowOutlineShader = new RainbowOutlineShader();
    private final DotShader dotShader = new DotShader();
    private final FillShader fillShader = new FillShader();

    // contains chorus fruit teleports
    private final List<Vec3d> chorusTeleports = new ArrayList<>();

    private final LinkedList<RubberBand> list = new LinkedList<>();
    private final Timer lastChorus = new Timer();

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.GLOW)) {

            // set all entities in the world glowing
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity != null && !entity.equals(mc.player) && hasHighlight(entity)) {
                    entity.setGlowing(true);
                }
            });

            // get the shaders
            ShaderGroup outlineShaderGroup = ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader();
            List<Shader> shaders = ((IShaderGroup) outlineShaderGroup).getListShaders();

            // update the shader radius
            shaders.forEach(shader -> {
                ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");

                if (outlineRadius != null) {
                    outlineRadius.set(width.getValue().floatValue());
                }
            });
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove glow effect from all entities
        if (mode.getValue().equals(Mode.GLOW)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity != null && entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }

        // remove all cached teleports
        chorusTeleports.clear();
    }

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting().equals(mode) && !event.getSetting().getValue().equals(Mode.GLOW)) {

            // remove glow effect from all entities
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity != null && entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.PacketReceiveEvent event) {

        // packet for world sound effects
        if (event.getPacket() instanceof SPacketSoundEffect) {

            // current packet
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            // if the sound being sent from the server is a chorus teleport, that means someone has eaten a chorus fruit
            // since this sound plays at the position the player teleports at, that's where they'll be teleported
            if (packet.getSound().equals(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) || packet.getSound().equals(SoundEvents.ENTITY_ENDERMEN_TELEPORT)) {

                // cache their teleport position
                chorusTeleports.add(new Vec3d(packet.getX(), packet.getY(), packet.getZ()));
            }
        }

        // packet for rubberbands
        if (event.getPacket() instanceof SPacketPlayerPosLook) {

            // current packet
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

            // log rubberband
            if (lagESP.getValue()) {

                // if we have eaten a chorus recently, then the teleport is likely that of the chorus
                if (!lastChorus.passedTime(400, Timer.Format.MILLISECONDS)) {
                    return;
                }

                //if the teleport (x and z values only) is telporting you more than 8 blocks
                // then it is likely not a rubberband
                if (mc.player.getPositionVector().distanceTo(new Vec3d(packet.getX(), mc.player.posY, packet.getZ())) > 16) {
                    return;
                }

                /* register that a rubberband happened */
                list.add(new RubberBand(mc.player.getPositionVector(), new Vec3d(packet.getX(), packet.getY(), packet.getZ())));
            }
        }

        // packet for item use
        if (event.getPacket() instanceof CPacketPlayerTryUseItem) {

            // used chorus
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemChorusFruit) {
                if (lagESP.getValue()) {
                    lastChorus.resetTime();
                }
            }
        }
    }

    @Override
    public void onRender3D() {

        // if we can render chorus fruit teleports
        if (chorus.getValue() && !chorusTeleports.isEmpty()) {

            // draw teleports
            chorusTeleports.forEach((pos) -> {

                // i hate milo
                RenderUtil.drawBox(new RenderBuilder()
                        .position(new AxisAlignedBB(pos.x, pos.y, pos.z, pos.x, pos.y + 2, pos.z))
                        .box(Box.BOTH)
                        .width(width.getValue())
                        .color(ColorUtil.getPrimaryAlphaColor(80))
                        .blend()
                        .depth(true)
                        .texture()
                );
            });
        }

        // render rubberband
        if (lagESP.getValue()){

            // Render positions
            list.forEach(r -> {
                glPushMatrix();
                glDisable(GL_TEXTURE_2D);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_BLEND);
                glDisable(GL_DEPTH_TEST);
                glLineWidth(rubberWidth.getValue());

                // disable render lighting
                mc.entityRenderer.disableLightmap();
                glBegin(GL_LINE_STRIP);

                // Set line colour
                //starting position should always be clear
                glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                        ColorUtil.getPrimaryColor().getGreen() / 255F,
                        ColorUtil.getPrimaryColor().getBlue() / 255F,
                        reverse.getValue() ? 1F : 0.1F
                );

                // draw line from starting inital position to intermediary point
                glVertex3d(
                        r.getFrom().x - mc.getRenderManager().viewerPosX,
                        r.getFrom().y - mc.getRenderManager().viewerPosY,
                        r.getFrom().z - mc.getRenderManager().viewerPosZ
                );

                //set the intermadiary point colour
                glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                        ColorUtil.getPrimaryColor().getGreen() / 255F,
                        ColorUtil.getPrimaryColor().getBlue() / 255F,
                        reverse.getValue() ? 1F : 0.1F

                );

                // calculate intermediary point
                r.calculateIntermediary();

                //render to the intermadiary point
                // draw line from starting initial position to intermediary point
                glVertex3d(
                        r.getIntermediary().x - mc.getRenderManager().viewerPosX,
                        r.getIntermediary().y - mc.getRenderManager().viewerPosY,
                        r.getIntermediary().z - mc.getRenderManager().viewerPosZ
                );

                // render to the final position
                glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                        ColorUtil.getPrimaryColor().getGreen() / 255F,
                        ColorUtil.getPrimaryColor().getBlue() / 255F,
                        reverse.getValue() ? 0.1F : 1F

                );

                glVertex3d(
                        r.getTo().x - mc.getRenderManager().viewerPosX,
                        r.getTo().y - mc.getRenderManager().viewerPosY,
                        r.getTo().z - mc.getRenderManager().viewerPosZ
                );

                if (System.currentTimeMillis() - r.getTime() >= fadeSpeed.getValue() * 1000){
                    list.remove(r);
                }

                // Reset colour
                glColor4d(1, 1, 1, 1);

                glEnd();
                glEnable(GL_DEPTH_TEST);
                glDisable(GL_LINE_SMOOTH);
                glDisable(GL_BLEND);
                glEnable(GL_TEXTURE_2D);
                glPopMatrix();
            });
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (nullCheck()) {

            // render over hotbar
            if (event.getType().equals(ElementType.HOTBAR)) {
                if (mode.getValue().equals(Mode.SHADER)) {
                    GlStateManager.enableAlpha();
                    GlStateManager.pushMatrix();
                    GlStateManager.pushAttrib();

                    // delete our old framebuffer, we'll create a new one
                    if (framebuffer != null) {
                        framebuffer.framebufferClear();

                        // resolution info
                        ScaledResolution scaledResolution = new ScaledResolution(mc);

                        if (lastScaleFactor != scaledResolution.getScaleFactor() || lastScaleWidth != scaledResolution.getScaledWidth() || lastScaleHeight != scaledResolution.getScaledHeight()) {
                            framebuffer.deleteFramebuffer();

                            // create a new framebuffer
                            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
                            framebuffer.framebufferClear();
                        }

                        // update scale info
                        lastScaleFactor = scaledResolution.getScaleFactor();
                        lastScaleWidth = scaledResolution.getScaledWidth();
                        lastScaleHeight = scaledResolution.getScaledHeight();
                    }

                    else {
                        // create a new framebuffer
                        framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
                    }

                    // bind our new framebuffer (i.e. set it as the current active buffer)
                    framebuffer.bindFramebuffer(false);

                    // prevent entity shadows from rendering
                    boolean previousShadows = mc.gameSettings.entityShadows;
                    mc.gameSettings.entityShadows = false;

                    // https://hackforums.net/showthread.php?tid=4811280
                    ((IEntityRenderer) mc.entityRenderer).setupCamera(event.getPartialTicks(), 0);

                    // make sure render manager is not null this caused issues when launching game
                    if (mc.getRenderManager() != null) {
                        try {

                            // draw all entities
                            for (Entity entity : mc.world.loadedEntityList) {

                                // render entity if valid
                                if (entity != null && entity != mc.getRenderViewEntity() && hasHighlight(entity)) {
                                    mc.getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), true);
                                }
                            }

                            // draw all storages
                            for (TileEntity tileEntity : mc.world.loadedTileEntityList)

                                // draw tile entity if valid
                                if (tileEntity != null && hasStorageHighlight(tileEntity)) {

                                    // get our render offsets.
                                    double renderX = ((IRenderManager) mc.getRenderManager()).getRenderX();
                                    double renderY = ((IRenderManager) mc.getRenderManager()).getRenderY();
                                    double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderZ();

                                    // render
                                    TileEntityRendererDispatcher.instance.render(tileEntity, tileEntity.getPos().getX() - renderX, tileEntity.getPos().getY() - renderY, tileEntity.getPos().getZ() - renderZ, mc.getRenderPartialTicks());
                                }
                        } catch (Exception exception) {

                            // show error if dev mode
                            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                                exception.printStackTrace();
                            }
                        }
                    }

                    // reset shadows
                    mc.gameSettings.entityShadows = previousShadows;

                    GlStateManager.enableBlend();
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                    // rebind the mc framebuffer
                    framebuffer.unbindFramebuffer();
                    mc.getFramebuffer().bindFramebuffer(true);

                    // remove lighting
                    mc.entityRenderer.disableLightmap();
                    RenderHelper.disableStandardItemLighting();

                    GlStateManager.pushMatrix();

                    // draw the rainbow shader
                    if (!ColorsModule.rainbow.getValue().equals(Rainbow.NONE)) {
                        switch (shader.getValue()) {
                            case DOTTED:
                                dotShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                            case OUTLINE:
                                rainbowOutlineShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                            case OUTLINE_FILL:
                                fillShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                        }
                    }

                    // draw the shader
                    else {
                        switch (shader.getValue()) {
                            case DOTTED:
                                dotShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                            case OUTLINE:
                                outlineShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                            case OUTLINE_FILL:
                                fillShader.startShader(width.getValue().intValue(), ColorUtil.getPrimaryColor());
                                break;
                        }
                    }

                    // prepare overlay render
                    mc.entityRenderer.setupOverlayRendering();

                    ScaledResolution scaledResolution = new ScaledResolution(mc);

                    // draw the framebuffer
                    glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
                    glBegin(GL_QUADS);
                    glTexCoord2d(0, 1);
                    glVertex2d(0, 0);
                    glTexCoord2d(0, 0);
                    glVertex2d(0, scaledResolution.getScaledHeight());
                    glTexCoord2d(1, 0);
                    glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
                    glTexCoord2d(1, 1);
                    glVertex2d(scaledResolution.getScaledWidth(), 0);
                    glEnd();

                    // stop drawing our shader
                    glUseProgram(0);
                    glPopMatrix();

                    // reset lighting
                    mc.entityRenderer.enableLightmap();

                    GlStateManager.popMatrix();
                    GlStateManager.popAttrib();

                    // Let the hotbar render over the shader
                    mc.entityRenderer.setupOverlayRendering();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEntityEvent event) {
        if (mode.getValue().equals(Mode.OUTLINE)) {
            
            // check if entity has highlight
            if (hasHighlight(event.getEntityLivingBase())) {
                
                // setup framebuffer
                if (mc.getFramebuffer().depthBuffer > -1) {

                    // delete old framebuffer extensions
                    EXTFramebufferObject.glDeleteRenderbuffersEXT(mc.getFramebuffer().depthBuffer);

                    // generates a new render buffer ID for the depth and stencil extension
                    int stencilFrameBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();

                    // bind a new render buffer
                    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // add the depth and stencil extension
                    EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);

                    // add the depth and stencil attachment
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // reset depth buffer
                    mc.getFramebuffer().depthBuffer = -1;
                }

                // begin drawing the stencil
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_ALPHA_TEST);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(width.getValue().floatValue());
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_STENCIL_TEST);
                glClear(GL_STENCIL_BUFFER_BIT);
                glClearStencil(0xF);
                glStencilFunc(GL_NEVER, 1, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // render the entity model
                event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

                // fill the entity model
                glStencilFunc(GL_NEVER, 0, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                // render the entity model
                event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

                // outline the entity model
                glStencilFunc(GL_EQUAL, 1, 0xF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // color the stencil and clear the depth
                glColor4d(getColor(event.getEntityLivingBase()).getRed() / 255F, getColor(event.getEntityLivingBase()).getGreen() / 255F, getColor(event.getEntityLivingBase()).getBlue() / 255F, getColor(event.getEntityLivingBase()).getAlpha() / 255F);
                glDepthMask(false);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_POLYGON_OFFSET_LINE);
                glPolygonOffset(3, -2000000);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

                // render the entity model
                event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

                // reset stencil
                glPolygonOffset(-3, 2000000);
                glDisable(GL_POLYGON_OFFSET_LINE);
                glEnable(GL_DEPTH_TEST);
                glDepthMask(true);
                glDisable(GL_STENCIL_TEST);
                glDisable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
                glEnable(GL_BLEND);
                glEnable(GL_LIGHTING);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_ALPHA_TEST);
                glPopAttrib();
            }
        }
    }

    @SubscribeEvent
    public void onRenderCrystal(RenderCrystalEvent.RenderCrystalPostEvent event) {
        if (mode.getValue().equals(Mode.OUTLINE)) {
            if (crystals.getValue()) {

                // calculate model rotations
                float rotation = event.getEntityEnderCrystal().innerRotation + event.getPartialTicks();
                float rotationMoved = MathHelper.sin(rotation * 0.2F) / 2 + 0.5F;
                rotationMoved += StrictMath.pow(rotationMoved, 2);

                glPushMatrix();
                
                // translate module to position
                glTranslated(event.getX(), event.getY(), event.getZ());
                glLineWidth(1 + width.getValue().floatValue());

                // render the entity model
                if (event.getEntityEnderCrystal().shouldShowBottom()) {
                    event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                else {
                    event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                // setup framebuffer
                if (mc.getFramebuffer().depthBuffer > -1) {

                    // delete old framebuffer extensions
                    EXTFramebufferObject.glDeleteRenderbuffersEXT(mc.getFramebuffer().depthBuffer);

                    // generates a new render buffer ID for the depth and stencil extension
                    int stencilFrameBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();

                    // bind a new render buffer
                    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // add the depth and stencil extension
                    EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);

                    // add the depth and stencil attachment
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // reset depth buffer
                    mc.getFramebuffer().depthBuffer = -1;
                }

                // begin drawing the stencil
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_ALPHA_TEST);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(1 + width.getValue().floatValue());
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_STENCIL_TEST);
                glClear(GL_STENCIL_BUFFER_BIT);
                glClearStencil(0xF);
                glStencilFunc(GL_NEVER, 1, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // render the entity model
                if (event.getEntityEnderCrystal().shouldShowBottom()) {
                    event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                else {
                    event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                // fill the entity model
                glStencilFunc(GL_NEVER, 0, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                // render the entity model
                if (event.getEntityEnderCrystal().shouldShowBottom()) {
                    event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                else {
                    event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                // outline the entity model
                glStencilFunc(GL_EQUAL, 1, 0xF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // color the stencil and clear the depth
                glDepthMask(false);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_POLYGON_OFFSET_LINE);
                glPolygonOffset(3, -2000000);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                glColor4d(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);

                // render the entity model
                if (event.getEntityEnderCrystal().shouldShowBottom()) {
                    event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                else {
                    event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                }

                // reset stencil
                glPolygonOffset(-3, 2000000);
                glDisable(GL_POLYGON_OFFSET_LINE);
                glEnable(GL_DEPTH_TEST);
                glDepthMask(true);
                glDisable(GL_STENCIL_TEST);
                glDisable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
                glEnable(GL_BLEND);
                glEnable(GL_LIGHTING);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_ALPHA_TEST);
                glPopAttrib();
                glPopMatrix();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onRenderTileEntity(RenderTileEntityEvent event) {
        if (mode.getValue().equals(Mode.OUTLINE)) {
            if (hasStorageHighlight(event.getTileEntity())) {

                // hotbar render sets all positions to 0
                boolean hotbarRender = event.getX() == 0 && event.getY() == 0 && event.getZ() == 0;

                // check if it's rendering in hotbar
                if (!hotbarRender) {
                    if (TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()) != null) {

                        // cancel rendering
                        event.setCanceled(true);

                        glPushMatrix();

                        // render the tile entity model
                        if (event.getTileEntity().hasFastRenderer()) {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).renderTileEntityFast(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial(), event.getBuffer().getBuffer());
                        }

                        else {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).render(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial());
                        }

                        // setup framebuffer
                        if (mc.getFramebuffer().depthBuffer > -1) {

                            // delete old framebuffer extensions
                            EXTFramebufferObject.glDeleteRenderbuffersEXT(mc.getFramebuffer().depthBuffer);

                            // generates a new render buffer ID for the depth and stencil extension
                            int stencilFrameBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();

                            // bind a new render buffer
                            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                            // add the depth and stencil extension
                            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);

                            // add the depth and stencil attachment
                            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);
                            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                            // reset depth buffer
                            mc.getFramebuffer().depthBuffer = -1;
                        }

                        // begin drawing the stencil
                        glPushAttrib(GL_ALL_ATTRIB_BITS);
                        glDisable(GL_ALPHA_TEST);
                        glDisable(GL_TEXTURE_2D);
                        glDisable(GL_LIGHTING);
                        glEnable(GL_BLEND);
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        glLineWidth(1 + width.getValue().floatValue());
                        glEnable(GL_LINE_SMOOTH);
                        glEnable(GL_STENCIL_TEST);
                        glClear(GL_STENCIL_BUFFER_BIT);
                        glClearStencil(0xF);
                        glStencilFunc(GL_NEVER, 1, 0xF);
                        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                        // render the tile entity model
                        if (event.getTileEntity().hasFastRenderer()) {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).renderTileEntityFast(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial(), event.getBuffer().getBuffer());
                        }

                        else {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).render(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial());
                        }

                        // fill the entity model
                        glStencilFunc(GL_NEVER, 0, 0xF);
                        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                        // render the tile entity model
                        if (event.getTileEntity().hasFastRenderer()) {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).renderTileEntityFast(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial(), event.getBuffer().getBuffer());
                        }

                        else {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).render(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial());
                        }

                        // outline the entity model
                        glStencilFunc(GL_EQUAL, 1, 0xF);
                        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                        glDepthMask(false);
                        glDisable(GL_DEPTH_TEST);
                        glEnable(GL_POLYGON_OFFSET_LINE);
                        glPolygonOffset(3, -2000000);
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

                        // color the stencil and clear the depth
                        glColor4d(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);

                        // render the tile entity model
                        if (event.getTileEntity().hasFastRenderer()) {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).renderTileEntityFast(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial(), event.getBuffer().getBuffer());
                        }

                        else {
                            TileEntityRendererDispatcher.instance.getRenderer(event.getTileEntity()).render(event.getTileEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialTicks(), event.getDestroyStage(), event.getPartial());
                        }

                        // reset stencil
                        glPolygonOffset(-3, 2000000);
                        glDisable(GL_POLYGON_OFFSET_LINE);
                        glEnable(GL_DEPTH_TEST);
                        glDepthMask(true);
                        glDisable(GL_STENCIL_TEST);
                        glDisable(GL_LINE_SMOOTH);
                        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
                        glEnable(GL_BLEND);
                        glEnable(GL_LIGHTING);
                        glEnable(GL_TEXTURE_2D);
                        glEnable(GL_ALPHA_TEST);
                        glPopAttrib();
                        glPopMatrix();
                    }
                }
            }
        }
    }

    /*
    @SubscribeEvent
    public void onRenderEntityItem(RenderEntityItemEvent event) {
        if (mode.getValue().equals(Mode.OUTLINE)) {

            // make sure items need highlight
            if (hasHighlight(event.getEntityItem())) {

                // cancel rendering
                event.setCanceled(true);

                glPushMatrix();

                // item model
                IBakedModel ibakedmodel = event.getItemRenderer().getItemModelWithOverrides(event.getEntityItem().getItem(), event.getEntityItem().world, null);
              
                // render item
                event.getItemRenderer().renderItem(event.getEntityItem().getItem(), ibakedmodel);

                // setup framebuffer
                if (mc.getFramebuffer().depthBuffer > -1) {

                    // delete old framebuffer extensions
                    EXTFramebufferObject.glDeleteRenderbuffersEXT(mc.getFramebuffer().depthBuffer);

                    // generates a new render buffer ID for the depth and stencil extension
                    int stencilFrameBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();

                    // bind a new render buffer
                    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // add the depth and stencil extension
                    EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);

                    // add the depth and stencil attachment
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilFrameBufferID);

                    // reset depth buffer
                    mc.getFramebuffer().depthBuffer = -1;
                }

                // begin drawing the stencil
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_ALPHA_TEST);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(1 + width.getValue().floatValue());
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_STENCIL_TEST);
                glClear(GL_STENCIL_BUFFER_BIT);
                glClearStencil(0xF);
                glStencilFunc(GL_NEVER, 1, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // render item
                event.getItemRenderer().renderItem(event.getEntityItem().getItem(), ibakedmodel);

                // fill the entity model
                glStencilFunc(GL_NEVER, 0, 0xF);
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                // render item
                event.getItemRenderer().renderItem(event.getEntityItem().getItem(), ibakedmodel);

                // outline the entity model
                glStencilFunc(GL_EQUAL, 1, 0xF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                // through walls :DDD
                glDepthMask(false);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_POLYGON_OFFSET_LINE);
                glPolygonOffset(3, -2000000);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

                // color the stencil and clear the depth
                glColor4d(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);

                // render item
                event.getItemRenderer().renderItem(event.getEntityItem().getItem(), ibakedmodel);

                // reset stencil
                glPolygonOffset(-3, 2000000);
                glDisable(GL_POLYGON_OFFSET_LINE);
                glEnable(GL_DEPTH_TEST);
                glDepthMask(true);
                glDisable(GL_STENCIL_TEST);
                glDisable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
                glEnable(GL_BLEND);
                glEnable(GL_LIGHTING);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_ALPHA_TEST);
                glPopAttrib();
                glPopMatrix();
            }
        }
    }
     */

    @SubscribeEvent
    public void onShaderColor(ShaderColorEvent event) {
        if (mode.getValue().equals(Mode.GLOW)) {

            // change the shader color
            event.setColor(getColor(event.getEntity()));

            // remove vanilla team color
            event.setCanceled(true);
        }
    }

    /**
     * Gets the color for a given entity
     * @param in The entity
     * @return The color for the entity
     */
    public Color getColor(Entity in) {
        return getCosmos().getSocialManager().getSocial(in.getName()).equals(Relationship.FRIEND) ? Color.CYAN : ColorUtil.getPrimaryColor();
    }

    /**
     * Checks if the {@link Entity} entity has an ESP highlight
     * @param entity The entity to check
     * @return Whether the entity has an ESP highlight
     */
    public boolean hasHighlight(Entity entity) {
        return players.getValue() && entity instanceof EntityPlayer || passives.getValue() && EntityUtil.isPassiveMob(entity) || neutrals.getValue() && EntityUtil.isNeutralMob(entity) || hostiles.getValue() && EntityUtil.isHostileMob(entity) || vehicles.getValue() && EntityUtil.isVehicleMob(entity) || items.getValue() && (entity instanceof EntityItem || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb) || crystals.getValue() && entity instanceof EntityEnderCrystal;
    }

    /**
     * Checks if the {@link TileEntity} tile entity has and ESP highlight
     * @param tileEntity the tile entity to check
     * @return Whether the tile entity has an ESP highlight
     */
    public boolean hasStorageHighlight(TileEntity tileEntity) {
        return chests.getValue() && tileEntity instanceof TileEntityChest || enderChests.getValue() && tileEntity instanceof TileEntityEnderChest || shulkers.getValue() && tileEntity instanceof TileEntityShulkerBox || hoppers.getValue() && (tileEntity instanceof TileEntityHopper || tileEntity instanceof TileEntityDropper) || furnaces.getValue() && tileEntity instanceof TileEntityFurnace;
    }

    public enum Mode {

        /**
         * Draws the Minecraft Glow shader
         */
        GLOW,

        /**
         * Draws a 2D shader on the GPU over the entity
         */
        SHADER,

        /**
         * Draws an outline over the entity
         */
        OUTLINE
    }

    public enum FragmentShader {

        /**
         * Draws an outline over the entity
         */
        OUTLINE,

        /**
         * Draws a dotted map over the entity
         */
        DOTTED,

        /**
         * Draws an outline with a transparent fill underneath
         */
        OUTLINE_FILL
    }

    /**
     * @author oragejuice
     * @since 09/11/2022
     */
    public static class RubberBand {

        //where the player was initially
        private final Vec3d from;

        //where they got rubberbanded to
        private final Vec3d to;

        // time of rubberband
        private final long time;

        // pos
        private Vec3d intermediary;

        public RubberBand(Vec3d from, Vec3d to) {
            this.from = from;
            this.to = to;
            this.time = System.currentTimeMillis();
            intermediary = from;
        }

        /**
         * Gets the original position
         * @return The original position
         */
        public Vec3d getFrom() {
            return from;
        }

        /**
         * Gets the rubberband position
         * @return The rubberband position
         */
        public Vec3d getTo() {
            return to;
        }

        /**
         * Gets the intermediary position
         * @return The intermediary position
         */
        public Vec3d getIntermediary() {
            return intermediary;
        }

        /**
         * Gets the time of the rubberband
         * @return The time of the rubberband
         */
        public long getTime() {
            return time;
        }

        /**
         * calculate, update then return the intermediary position for rendering
         */
        public void calculateIntermediary() {

            // the difference in time between the creation and now
            long timeDelta = System.currentTimeMillis() - time;
            timeDelta = timeDelta == 0 ? 1 : timeDelta;

            /*
             * so normally i would use a higher order function for calcuting the interp,
             * but im lazy and nobody will ever look at this. so i wont
             */
            if (reverse.getValue()) {

                //calculate the difference
                Vec3d d = from.subtract(to);

                //set the position to be an interpolated value from `from` to `to`
                intermediary = to.addVector(
                        d.x * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                        d.y * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                        d.z * ((timeDelta) / (fadeSpeed.getValue() * 1000))
                );
            }

            else {

                //calculate the difference
                Vec3d d = to.subtract(from);

                //set the position to be an interpolated value from `from` to `to`
                intermediary = from.addVector(
                        d.x * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                        d.y * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                        d.z * ((timeDelta) / (fadeSpeed.getValue() * 1000))
                );
            }
        }
    }
}
