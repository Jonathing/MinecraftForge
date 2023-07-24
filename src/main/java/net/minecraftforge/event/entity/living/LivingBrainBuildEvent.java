package net.minecraftforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * LivingBrainBuildEvent is fired whenever a new {@link net.minecraft.world.entity.ai.Brain.Provider} is about to be created through {@link LivingEntity#brainProvider()}.
 * <br>
 * This event holds onto a collection of memory types, a collection of sensor types, and a bifunction dictating how the brain provider should be made.
 */
public class LivingBrainBuildEvent extends LivingEvent
{
    private BiFunction<Collection<? extends MemoryModuleType<?>>, Collection<? extends SensorType<? extends Sensor<? super LivingEntity>>>, Brain.Provider<? extends LivingEntity>> providerType = Brain::provider;

    private Collection<? extends MemoryModuleType<?>> memories;
    private Collection<? extends SensorType<? extends Sensor<?>>> sensors;

    public LivingBrainBuildEvent(Collection<? extends MemoryModuleType<?>> memories, Collection<? extends SensorType<? extends Sensor<?>>> sensors, LivingEntity entity)
    {
        super(entity);
        this.memories = memories;
        this.sensors = sensors;
    }

    public BiFunction<Collection<? extends MemoryModuleType<?>>, Collection<? extends SensorType<? extends Sensor<? super LivingEntity>>>, Brain.Provider<? extends LivingEntity>> getProviderType()
    {
        return providerType;
    }

    public void setProviderType(BiFunction<Collection<? extends MemoryModuleType<?>>, Collection<? extends SensorType<? extends Sensor<? super LivingEntity>>>, Brain.Provider<? extends LivingEntity>> providerType)
    {
        this.providerType = providerType;
    }

    public Collection<? extends MemoryModuleType<?>> getMemories()
    {
        return memories;
    }

    public void setMemories(Collection<? extends MemoryModuleType<?>> memories)
    {
        this.memories = memories;
    }

    public Collection<? extends SensorType<? extends Sensor<?>>> getSensors()
    {
        return sensors;
    }

    public void setSensors(Collection<? extends SensorType<? extends Sensor<LivingEntity>>> sensors)
    {
        this.sensors = sensors;
    }
}
