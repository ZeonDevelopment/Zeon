package dextro.zeon.addon.mixins;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor2 {
    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();

    @Accessor("yaw")
    float getYaw();

    @Accessor("pitch")
    float getPitch();

    @Accessor("onGround")
    boolean getOnGround();

    @Accessor("changePosition")
    boolean getChangePosition();

    @Accessor("changeLook")
    boolean getChangeLook();

    @Mutable
    @Accessor("yaw")
    void setYaw(float yaw);

    @Mutable
    @Accessor("pitch")
    void setPitch(float pitch);

    @Mutable
    @Accessor("changeLook")
    void setChangeLook(boolean changeLook);

    @Mutable
    @Accessor("changePosition")
    void setChangePosition(boolean changePosition);

}

