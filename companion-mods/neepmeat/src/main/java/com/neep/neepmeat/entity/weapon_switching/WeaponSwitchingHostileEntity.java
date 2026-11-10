package com.neep.neepmeat.entity.weapon_switching;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatweapons.MWItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class WeaponSwitchingHostileEntity extends Monster implements RangedAttackMob, AccuracyAware
{
    protected static final EntityDataAccessor<Float> AIM_PROGRESS = SynchedEntityData.defineId(WeaponSwitchingHostileEntity.class, EntityDataSerializers.FLOAT);

    public float smoothAimProgress = 0;
    private boolean updateAttack = true;

    public final MovingAverage accuracy = new MovingAverage(5);

    protected ImplementedInventory internalInventory = ImplementedInventory.ofSize(2);

    protected WeaponSwitchingHostileEntity(EntityType<? extends Monster> entityType, Level world)
    {
        super(entityType, world);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingMultiplier, boolean allowDrops)
    {
        super.dropCustomDeathLoot(source, lootingMultiplier, allowDrops);
    }

    protected Item preferredRangedWeapon()
    {
        return MWItems.ALMOST_PISTOL;
    }

    protected Item preferredMeleeWeapon()
    {
        return MWItems.KNIFE;
//        return Items.IRON_SWORD;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty)
    {
        // Super implementation does some strange things

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData();
        entityData.define(AIM_PROGRESS, 0f);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (updateAttack)
        {
            updateAttack = false;
            updateAttackType();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.put("internal_inventory", internalInventory.writeNbt(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.internalInventory.readNbt(nbt.getCompound("internal_inventory"));
    }

//    protected boolean tryEquipItem(Item item)
//    {
//        for (int i = 0; i < internalInventory.size(); i++)
//        {
//
//        }
//    }

    protected boolean storeStack(ItemStack stack)
    {
        if (stack.isEmpty())
            return true;

        for (int i = 0; i < internalInventory.getContainerSize(); i++)
        {
            var otherStack = internalInventory.getItem(i);
            if (otherStack.isEmpty() || ItemStack.isSameItemSameTags(otherStack, stack))
            {
                internalInventory.setItem(i, stack.copyWithCount(otherStack.getCount() + stack.getCount()));
                return true;
            }
        }

        return false;
    }

    protected int findStoredStack(Item item)
    {
        for (int i = 0; i < internalInventory.getContainerSize(); i++)
        {
            if (internalInventory.getItem(i).is(item))
            {
                return i;
            }
        }

        return -1;
    }

    protected boolean equipPreferredStack(EquipmentSlot slot, Item item)
    {
        int i = findStoredStack(item);
        if (i >= 0)
        {
            ItemStack stack = internalInventory.getItem(i);
            if (!stack.isEmpty())
            {
                setItemSlot(slot, stack);
                internalInventory.setItem(i, ItemStack.EMPTY);
                return true;
            }
        }

        return false;
    }

    public boolean hasRangedStored()
    {
        return findStoredStack(preferredRangedWeapon()) != -1;
    }

    public boolean hasMeleeStored()
    {
        return findStoredStack(preferredMeleeWeapon()) != -1;
    }

    public void switchWeapon(WeaponType type)
    {
        switch (type)
        {
            case NONE ->
            {
                if (storeStack(getMainHandItem()))
                {
                    setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    setAimProgress(0);
                }
            }
            case MELEE ->
            {
                if (storeStack(getMainHandItem()))
                {
                    if (equipPreferredStack(EquipmentSlot.MAINHAND, preferredMeleeWeapon()))
                        setAimProgress(0);
                }
            }
            case RANGED ->
            {
                if (storeStack(getMainHandItem()))
                {
                    if (equipPreferredStack(EquipmentSlot.MAINHAND, preferredRangedWeapon()))
                    {

                    }
                }
            }
        }
        updateAttack = true;
    }

    public void setAimProgress(float value)
    {
        entityData.set(AIM_PROGRESS, value);
    }

    public float getAimProgress()
    {
        return entityData.get(AIM_PROGRESS);
    }

    public WeaponType getWeaponMode()
    {
        return isHolding(preferredMeleeWeapon()) ? WeaponType.MELEE
                : (isHolding(preferredRangedWeapon()) ? WeaponType.RANGED : WeaponType.NONE);
    }

    public abstract void updateAttackType();

    @Override
    public void addShot(boolean hit)
    {
        accuracy.add(hit);
    }

    public enum WeaponType
    {
        NONE, MELEE, RANGED;
    }
}
