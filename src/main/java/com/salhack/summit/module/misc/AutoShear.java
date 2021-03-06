package com.salhack.summit.module.misc;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemShears;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoShear extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for sheep", 4, 0, 10, 1);
    
    public AutoShear()
    {
        super("AutoShear", new String[] {""}, "Shears sheep in range", "NONE", -1, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemShears))
            return;

        EntitySheep l_Sheep = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidSheep(p_Entity))
                .map(p_Entity -> (EntitySheep) p_Entity)
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);
        
        if (l_Sheep != null)
        {
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_Sheep.posX,
                    l_Sheep.posY,
                    l_Sheep.posZ,
                    mc.player);
            
            p_Event.setPitch(l_Pos[1]);
            p_Event.setYaw(l_Pos[0]);
            
            mc.getConnection().sendPacket(new CPacketUseEntity(l_Sheep, EnumHand.MAIN_HAND));
        }
    });
    
    private boolean IsValidSheep(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntitySheep))
            return false;
        
        if (p_Entity.getDistance(mc.player) > Radius.getValue())
            return false;
        
        EntitySheep l_Sheep = (EntitySheep)p_Entity;

        if (l_Sheep.isShearable(mc.player.getHeldItemMainhand(), mc.world, p_Entity.getPosition()))
            return true;
        
        return false;
    }
}
