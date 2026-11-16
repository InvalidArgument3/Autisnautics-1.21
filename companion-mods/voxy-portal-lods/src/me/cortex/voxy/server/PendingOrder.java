package me.cortex.voxy.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Puts a retried terrain frame ahead of that player's other pending frames. */
public final class PendingOrder {
    private PendingOrder() {
    }

    public static void requeueFront(LinkedHashMap<Long, Object> pending, Object frame) {
        if (pending == null || frame == null) {
            return;
        }
        long id = frameId(frame);
        String owner;
        try {
            owner = ownerOf(frame);
        } catch (ReflectiveOperationException e) {
            pending.put(Long.valueOf(id), frame);
            return;
        }
        List<Map.Entry<Long, Object>> entries = new ArrayList<Map.Entry<Long, Object>>(pending.entrySet());
        String[] owners = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            try {
                owners[i] = ownerOf(entries.get(i).getValue());
            } catch (ReflectiveOperationException e) {
                owners[i] = "";
            }
        }
        pending.clear();
        boolean inserted = false;
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Long, Object> entry = entries.get(i);
            if (entry.getKey().longValue() == id) {
                continue;
            }
            if (!inserted && owner.equals(owners[i])) {
                pending.put(Long.valueOf(id), frame);
                inserted = true;
            }
            pending.put(entry.getKey(), entry.getValue());
        }
        if (!inserted) {
            pending.put(Long.valueOf(id), frame);
        }
    }

    private static long frameId(Object frame) {
        try {
            return frame.getClass().getField("id").getLong(frame);
        } catch (ReflectiveOperationException e) {
            return 0L;
        }
    }

    static String ownerOf(Object frame) throws ReflectiveOperationException {
        Field keyField = frame.getClass().getField("key");
        Object key = keyField.get(frame);
        Method owner = key.getClass().getMethod("owner");
        return (String) owner.invoke(key);
    }
}
