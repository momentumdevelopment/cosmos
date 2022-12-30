package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.WaypointManager.Format;
import cope.cosmos.client.manager.managers.WaypointManager.Waypoint;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.world.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 08/25/2022
 */
public class WaypointsModule extends Module {
    public static WaypointsModule INSTANCE;

    public WaypointsModule() {
        super("Waypoints", new String[] {"LogoutSpots", "DeathSpots"}, Category.VISUAL,"Shows all the client waypoints");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Boolean> logouts = new Setting<>("Logouts", true)
            .setDescription("Adds waypoints to logout spots");

    public static Setting<Boolean> deaths = new Setting<>("Deaths", true)
            .setDescription("Adds waypoints to death spots");

    public static Setting<Boolean> infinite = new Setting<>("Infinite", false)
            .setDescription("Makes added waypoints last forever");

    public static Setting<Float> lifespan = new Setting<>("Lifespan", 1F, 30F, 120F, 1)
            .setAlias("Delay")
            .setDescription("The lifespan of the waypoints in seconds")
            .setVisible(() -> !infinite.getValue());

    // **************************** render ****************************

    public static Setting<Float> lineWidth = new Setting<>("Width", 0.1F, 3.0F, 4.0F, 1)
            .setAlias("LineWidth")
            .setDescription("The width of the outline");

    // added waypoints
    private final Map<String, Long> waypoints = new ConcurrentHashMap<>();

    @Override
    public void onUpdate() {

        // time check
        if (!infinite.getValue()) {

            // remove any waypoints that have exceeded their life span
            waypoints.forEach((name, time) -> {
                if (System.currentTimeMillis() - time >= lifespan.getValue() * 1000) {

                    // remove
                    waypoints.remove(name);
                    getCosmos().getWaypointManager().removeWaypoint(name);
                }
            });
        }
    }

    @Override
    public void onRender3D() {

        // map of all waypoints
        Map<String, Waypoint> waypoints = getCosmos().getWaypointManager().getWaypoints();

        // check if waypoints exist
        if (waypoints != null && !waypoints.isEmpty()) {

            // render all waypoints
            waypoints.forEach((name, waypoint) -> {

                // waypoint's coords
                Vec3d coordinates = waypoint.getCoordinates();

                // check format
                if (waypoint.getFormat().equals(Format.COORDINATE) || deaths.getValue() && waypoint.getFormat().equals(Format.DEATH) || logouts.getValue() && waypoint.getFormat().equals(Format.LOGOUT)) {

                    // draw box highlight
                    RenderUtil.drawBox(new RenderBuilder()
                            .position(new AxisAlignedBB(coordinates.x - 0.3, coordinates.y, coordinates.z - 0.3, coordinates.x + 0.3, coordinates.y + 2.2, coordinates.z + 0.3))
                            .color(ColorUtil.getPrimaryColor())
                            .box(Box.OUTLINE)
                            .setup()
                            .line(lineWidth.getValue())
                            .depth(true)
                            .blend()
                            .texture()
                    );

                    // draw info
                    RenderUtil.drawNametag(coordinates.addVector(0, 2.6, 0), name + " [X: " + coordinates.x + ", Y: " + coordinates.y + ", Z: " + coordinates.z + "]");
                }
            });
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // packet for player list changes
            if (event.getPacket() instanceof SPacketPlayerListItem) {

                // add waypoints to logout spots
                if (logouts.getValue()) {

                    // player data
                    for (SPacketPlayerListItem.AddPlayerData data : ((SPacketPlayerListItem) event.getPacket()).getEntries()) {

                        // check that the player exists
                        if (data.getProfile().getName() != null && !data.getProfile().getName().isEmpty() || data.getProfile().getId() != null) {

                            // player
                            EntityPlayer player = mc.world.getPlayerEntityByUUID(data.getProfile().getId());

                            // check if the player exists
                            if (player != null) {

                                // player join
                                if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.ADD_PLAYER)) {

                                    // track
                                    waypoints.remove(player.getName() + "'s Logout");

                                    // remove logout spot when the player logs back in
                                    getCosmos().getWaypointManager().removeWaypoint(data.getProfile().getName() + "'s Logout");
                                }

                                // player disconnect
                                else if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER)) {

                                    // track
                                    waypoints.put(player.getName() + "'s Logout", System.currentTimeMillis());

                                    // remove logout spot when the player logs back in
                                    getCosmos().getWaypointManager().addWaypoint(player.getName() + "'s Logout", new Waypoint(new Vec3d(MathUtil.roundDouble(player.posX, 1), MathUtil.roundDouble(player.posY, 1), MathUtil.roundDouble(player.posZ, 1)), WorldUtil.getWorldName(), Format.LOGOUT));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRemoveEntity(EntityWorldEvent.EntityRemoveEvent event) {

        // player death
        if (event.getEntity().equals(mc.player)) {

            // add waypoint to death spot
            if (deaths.getValue()) {

                // track
                waypoints.put("Last Death", System.currentTimeMillis());

                // add waypoints
                getCosmos().getWaypointManager().removeWaypoint("Last Death");
                getCosmos().getWaypointManager().addWaypoint("Last Death", new Waypoint(new Vec3d(MathUtil.roundDouble(((IEntityPlayerSP) mc.player).getLastReportedPosX(), 1), MathUtil.roundDouble(((IEntityPlayerSP) mc.player).getLastReportedPosY(), 1), MathUtil.roundDouble(((IEntityPlayerSP) mc.player).getLastReportedPosZ(), 1)), WorldUtil.getWorldName(), Format.DEATH));
            }
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {

        // clear on login
        waypoints.clear();
    }
}
