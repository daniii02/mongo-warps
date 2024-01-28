package warps.mongo.warp;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import warps.mongo.MongoWarps;

import java.util.Objects;

@Getter
public class Warp {
    private final String name;
    private final Location location;

    private Permission permission;

    public Warp(String name, Location location) {
        this.name = name;
        this.location = location;

        loadPermission(MongoWarps.get().getWarpManager().getWarpPermissionFormat());
    }

    /**
     * Carga el permiso del warp en el formato introducido.
     * Se reemplaza <tt>{warp}</tt> por el nombre del warp.
     * Uso interno
     */
    void loadPermission(String format) {
        this.permission = format == null ? null : new Permission(format.replace("{warp}", name));
    }

    /**
     * Teletransporta a un jugador al warp
     * @param player jugador
     */
    public void teleport(Player player) {
        player.teleport(location);
    }

    /**
     * Comprueba si la entidad tiene permiso para acceder al warp
     * @param permissible entidad
     * @return <tt>true</tt> si tiene permiso
     */
    public boolean hasPermission(Permissible permissible) {
        return permission == null || permissible.hasPermission(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warp warp = (Warp) o;
        return Objects.equals(name, warp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
