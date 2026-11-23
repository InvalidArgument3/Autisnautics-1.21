/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.attachment.player;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.entity.player.Player;

public class PlayerAttachmentManager
{
    private static final Map<String, PlayerAttachment.Factory> FACTORIES = Maps.newHashMap();

    protected final Map<String, PlayerAttachment> attachments = Maps.newHashMap();
    protected final Player player;

    public PlayerAttachmentManager(Player player)
    {
        this.player = player;
        FACTORIES.forEach((id, f) ->
        {
            attachments.put(id, f.create(player));
        });
    }

    public void tick()
    {
        attachments.values().forEach(PlayerAttachment::tickAttachment);
    }

    public <T extends PlayerAttachment> T getAttachment(String id)
    {
        // URrrrrrrrrrr, not sure about this.
        return (T) attachments.get(id);
    }

    protected void addAttachment(String id, PlayerAttachment attachment)
    {
        attachments.put(id, attachment);
    }

    public static void registerAttachment(String id, PlayerAttachment.Factory factory)
    {
        FACTORIES.put(id, factory);
    }

    public static void init()
    {
//        ServerPlayConnectionEvents.INIT.register((handler, server) ->
//        {
//            handler.getPlayer().neepmeat$getAttachmentManager().
//        });
    }
}
