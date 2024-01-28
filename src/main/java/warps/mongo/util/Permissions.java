package warps.mongo.util;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import warps.mongo.message.Message;

public enum Permissions {
    WARP_CREATE("warp.create"),
    WARP_REMOVE("warp.remove"),
    WARP_MENU("warp.menu"),
    WARP_RELOAD("warp.reload");

    private final Permission permission;

    Permissions(String permissionName) {
        this.permission = new Permission(permissionName);
    }

    public String getName() {
        return permission.getName();
    }

    /**
     * Comprueba si la entidad tiene el permiso
     * @param target entidad
     * @return <tt>true</tt> si tiene el permiso
     */
    public boolean check(Permissible target) {
        return target.hasPermission(this.permission);
    }

    /**
     * Comprueba si la entidad tiene el permiso y envía un mensaje si no lo tiene
     * @param target entidad
     * @return <tt>true</tt> si no tiene el permiso
     */
    public boolean checkAndSend(CommandSender target) {
        if (!check(target)) {
            Message.COMMAND_PERMISSION_REQUIRED.send(target);
            return true;
        }
        return false;
    }
}
