package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityVindicator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vindicator;

public class CraftVindicator extends CraftIllager implements Vindicator {

    public CraftVindicator(CraftServer server, EntityVindicator entity) {
        super(server, entity);
    }

    @Override
    public EntityVindicator getHandle() {
        return (EntityVindicator) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftVindicator";
    }

    @Override
    public EntityType getType() {
        return EntityType.VINDICATOR;
    }

    // Paper start
    public boolean isJohnny() {
        return getHandle().isJohnny();
    }

    public void setJohnny(boolean johnny) {
        getHandle().setJohnny(johnny);
    }
    // Paper end
}
