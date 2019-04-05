package net.minecraft.server;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MobSpawnerAbstract {

    private static final Logger a = LogManager.getLogger();
    public int spawnDelay = 20;
    private final List<MobSpawnerData> mobs = Lists.newArrayList();
    private MobSpawnerData spawnData = new MobSpawnerData();
    private double e;
    private double f;
    public int minSpawnDelay = 200;
    public int maxSpawnDelay = 800;
    public int spawnCount = 4;
    private Entity j;
    public int maxNearbyEntities = 6;
    public int requiredPlayerRange = 16;
    public int spawnRange = 4;
    private int tickDelay = 0; // Paper

    public MobSpawnerAbstract() {}

    @Nullable
    public MinecraftKey getMobName() {
        String s = this.spawnData.b().getString("id");

        try {
            return UtilColor.b(s) ? null : new MinecraftKey(s);
        } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
            BlockPosition blockposition = this.b();

            MobSpawnerAbstract.a.warn("Invalid entity id '{}' at spawner {}:[{},{},{}]", s, this.a().worldProvider.getDimensionManager(), blockposition.getX(), blockposition.getY(), blockposition.getZ());
            return null;
        }
    }

    public void setMobName(EntityTypes<?> entitytypes) {
        this.spawnData.b().setString("id", IRegistry.ENTITY_TYPE.getKey(entitytypes).toString());
        this.mobs.clear(); // CraftBukkit - SPIGOT-3496, MC-92282
    }

    private boolean h() {
        BlockPosition blockposition = this.b();

        return this.a().b((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, (double) this.requiredPlayerRange);
    }

    public void c() {
        // Paper start - Configurable mob spawner tick rate
        if (spawnDelay > 0 && --tickDelay > 0) return;
        tickDelay = this.a().paperConfig.mobSpawnerTickRate;
        // Paper end
        if (!this.h()) {
            this.f = this.e;
        } else {
            BlockPosition blockposition = this.b();

            if (this.a().isClientSide) {
                // Akarin start - this handle by client
                /*
                double d0 = (double) ((float) blockposition.getX() + this.a().random.nextFloat());
                double d1 = (double) ((float) blockposition.getY() + this.a().random.nextFloat());
                double d2 = (double) ((float) blockposition.getZ() + this.a().random.nextFloat());

                this.a().addParticle(Particles.M, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                this.a().addParticle(Particles.y, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                */
                // Akarin end
                if (this.spawnDelay > 0) {
                    this.spawnDelay -= tickDelay; // Paper
                }

                this.f = this.e;
                this.e = (this.e + (double) (1000.0F / ((float) this.spawnDelay + 200.0F))) % 360.0D;
            } else {
                if (this.spawnDelay < -tickDelay) { // Paper
                    this.i();
                }

                if (this.spawnDelay > 0) {
                    this.spawnDelay -= tickDelay; // Paper
                    return;
                }

                boolean flag = false;

                for (int i = 0; i < this.spawnCount; ++i) {
                    NBTTagCompound nbttagcompound = this.spawnData.b();
                    NBTTagList nbttaglist = nbttagcompound.getList("Pos", 6);
                    World world = this.a();
                    int j = nbttaglist.size();
                    double d3 = j >= 1 ? nbttaglist.k(0) : (double) blockposition.getX() + (world.random.nextDouble() - world.random.nextDouble()) * (double) this.spawnRange + 0.5D;
                    double d4 = j >= 2 ? nbttaglist.k(1) : (double) (blockposition.getY() + world.random.nextInt(3) - 1);
                    double d5 = j >= 3 ? nbttaglist.k(2) : (double) blockposition.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * (double) this.spawnRange + 0.5D;
                    // Paper start
                    if (this.getMobName() == null) {
                        return;
                    }
                    String key = this.getMobName().getKey();
                    org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.fromName(key);
                    if (type != null) {
                        com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent event;
                        event = new com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent(
                                MCUtil.toLocation(world, d3, d4, d5),
                                type,
                                MCUtil.toLocation(world, blockposition)
                        );
                        if (!event.callEvent()) {
                            flag = true;
                            if (event.shouldAbortSpawn()) {
                                break;
                            }
                            continue;
                        }
                    }
                    // Paper end
                    Entity entity = ChunkRegionLoader.spawnEntity(nbttagcompound, world, d3, d4, d5, false, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER); // Paper

                    if (entity == null) {
                        this.i();
                        return;
                    }

                    int k = world.a(entity.getClass(), (new AxisAlignedBB((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), (double) (blockposition.getX() + 1), (double) (blockposition.getY() + 1), (double) (blockposition.getZ() + 1))).g((double) this.spawnRange)).size();

                    if (k >= this.maxNearbyEntities) {
                        this.i();
                        return;
                    }

                    EntityInsentient entityinsentient = entity instanceof EntityInsentient ? (EntityInsentient) entity : null;

                    entity.setPositionRotation(entity.locX, entity.locY, entity.locZ, world.random.nextFloat() * 360.0F, 0.0F);
                    if (entityinsentient == null || entityinsentient.a((GeneratorAccess) world, true) && entityinsentient.canSpawn()) {
                        if (this.spawnData.b().d() == 1 && this.spawnData.b().hasKeyOfType("id", 8) && entity instanceof EntityInsentient) {
                            ((EntityInsentient) entity).prepare(world.getDamageScaler(new BlockPosition(entity)), (GroupDataEntity) null, (NBTTagCompound) null);
                        }
                        entity.spawnedViaMobSpawner = true; // Paper
                        // Spigot Start
                        if ( entity.world.spigotConfig.nerfSpawnerMobs )
                        {
                            entity.fromMobSpawner = true;
                        }

                        flag = true; // Paper

                        if (org.bukkit.craftbukkit.event.CraftEventFactory.callSpawnerSpawnEvent(entity, blockposition).isCancelled()) {
                            Entity vehicle = entity.getVehicle();
                            if (vehicle != null) {
                                vehicle.dead = true;
                            }
                            for (Entity passenger : entity.getAllPassengers()) {
                                passenger.dead = true;
                            }
                            continue;
                        }
                        // Spigot End
                        ChunkRegionLoader.a(entity, (GeneratorAccess) world, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER); // CraftBukkit
                        world.triggerEffect(2004, blockposition, 0);
                        if (entityinsentient != null) {
                            entityinsentient.doSpawnEffect();
                        }

                        /*flag = true;*/ // Paper - moved up above cancellable event
                    }
                }

                if (flag) {
                    this.i();
                }
            }

        }
    }

    private void i() {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            int i = this.maxSpawnDelay - this.minSpawnDelay;

            this.spawnDelay = this.minSpawnDelay + this.a().random.nextInt(i);
        }

        if (!this.mobs.isEmpty()) {
            this.a((MobSpawnerData) WeightedRandom.a(this.a().random, this.mobs));
        }

        this.a(1);
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.spawnDelay = nbttagcompound.getShort("Delay");
        this.mobs.clear();
        if (nbttagcompound.hasKeyOfType("SpawnPotentials", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("SpawnPotentials", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                this.mobs.add(new MobSpawnerData(nbttaglist.getCompound(i)));
            }
        }

        if (nbttagcompound.hasKeyOfType("SpawnData", 10)) {
            this.a(new MobSpawnerData(1, nbttagcompound.getCompound("SpawnData")));
        } else if (!this.mobs.isEmpty()) {
            this.a((MobSpawnerData) WeightedRandom.a(this.a().random, this.mobs));
        }

        if (nbttagcompound.hasKeyOfType("MinSpawnDelay", 99)) {
            this.minSpawnDelay = nbttagcompound.getShort("MinSpawnDelay");
            this.maxSpawnDelay = nbttagcompound.getShort("MaxSpawnDelay");
            this.spawnCount = nbttagcompound.getShort("SpawnCount");
        }

        if (nbttagcompound.hasKeyOfType("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = nbttagcompound.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = nbttagcompound.getShort("RequiredPlayerRange");
        }

        if (nbttagcompound.hasKeyOfType("SpawnRange", 99)) {
            this.spawnRange = nbttagcompound.getShort("SpawnRange");
        }

        if (this.a() != null) {
            this.j = null;
        }

    }

    public NBTTagCompound b(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = this.getMobName();

        if (minecraftkey == null) {
            return nbttagcompound;
        } else {
            nbttagcompound.setShort("Delay", (short) this.spawnDelay);
            nbttagcompound.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
            nbttagcompound.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
            nbttagcompound.setShort("SpawnCount", (short) this.spawnCount);
            nbttagcompound.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
            nbttagcompound.setShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
            nbttagcompound.setShort("SpawnRange", (short) this.spawnRange);
            nbttagcompound.set("SpawnData", this.spawnData.b().clone());
            NBTTagList nbttaglist = new NBTTagList();

            if (this.mobs.isEmpty()) {
                nbttaglist.add((NBTBase) this.spawnData.a());
            } else {
                Iterator iterator = this.mobs.iterator();

                while (iterator.hasNext()) {
                    MobSpawnerData mobspawnerdata = (MobSpawnerData) iterator.next();

                    nbttaglist.add((NBTBase) mobspawnerdata.a());
                }
            }

            nbttagcompound.set("SpawnPotentials", nbttaglist);
            return nbttagcompound;
        }
    }

    public boolean b(int i) {
        if (i == 1 && this.a().isClientSide) {
            this.spawnDelay = this.minSpawnDelay;
            return true;
        } else {
            return false;
        }
    }

    public void a(MobSpawnerData mobspawnerdata) {
        this.spawnData = mobspawnerdata;
    }

    public abstract void a(int i);

    public abstract World a();

    public abstract BlockPosition b();
}
