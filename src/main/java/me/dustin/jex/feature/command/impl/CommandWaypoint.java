package me.dustin.jex.feature.command.impl;

import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.file.files.WaypointFile;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.impl.world.Waypoints;

@Cmd(name = "Waypoint", description = "Add or remove waypoints", syntax = {".waypoint add <name> <x/y/z/here>", ".waypoint del <name>"})
public class CommandWaypoint extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            String server = WorldHelper.INSTANCE.getCurrentServerName();
            if (isAddString(args[1])) {
                String name = args[2].replace("_", " ");
                float x;
                float y;
                float z;
                int color = ColorHelper.INSTANCE.getColorViaHue((int) (Math.random() * 270)).getRGB();
                if (args[3].equalsIgnoreCase("here")) {
                    x = (float) Wrapper.INSTANCE.getLocalPlayer().getX();
                    y = (float) Wrapper.INSTANCE.getLocalPlayer().getY();
                    z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ();
                } else {
                    x = Float.parseFloat(args[3]);
                    y = Float.parseFloat(args[4]);
                    z = Float.parseFloat(args[5]);
                }
                Waypoints.waypoints.add(new Waypoints.Waypoint(name, server, x, y, z, WorldHelper.INSTANCE.getDimensionID().toString(), color));
                WaypointFile.write();
                ChatHelper.INSTANCE.addClientMessage("Added waypoint " + name + ".");
            } else if (isDeleteString(args[1])) {
                Waypoints.Waypoint waypoint = Waypoints.get(args[2].replace("_", " "), server);
                if (waypoint != null) {
                    Waypoints.waypoints.remove(waypoint);
                    WaypointFile.write();
                    ChatHelper.INSTANCE.addClientMessage("Removed waypoint " + args[2].replace("_", " ") + ".");
                } else {
                    ChatHelper.INSTANCE.addClientMessage("That waypoint does not exist on this server!");
                }
            } else {
                giveSyntaxMessage();
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
