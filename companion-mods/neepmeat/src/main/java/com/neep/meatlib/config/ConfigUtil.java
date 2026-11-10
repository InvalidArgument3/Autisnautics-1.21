package com.neep.meatlib.config;

import com.neep.meatlib.MeatLib;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

public class ConfigUtil
{
      public static <T extends ReflectiveConfig> T createDefaults(Path configPath, String familyId, String id, Class<T> configCreatorClass)
      {
          T config = ReflectiveConfig.createToml(configPath, familyId, id, configCreatorClass);

          // Do not use defaults if disabled
          if (!MeatLib.CONFIG.updateDefaultValues.getRealValue())
              return config;

          T defaults = ReflectiveConfig.createToml(configPath, familyId, id + ".default", configCreatorClass);

          Iterator<TrackedValue<?>> defaultIt = defaults.values().iterator();
          Iterator<TrackedValue<?>> actualIt = config.values().iterator();

          while (defaultIt.hasNext() && actualIt.hasNext())
          {
              // We can hopefully assume that the types will match since the two config structures should be identical
              var defaultTracked = (TrackedValue<Object>) defaultIt.next();
              var actualTracked = (TrackedValue<Object>) actualIt.next();

              check(defaultTracked, actualTracked);
          }

          return config;
      }

      private static <T> void check(TrackedValue<T> defaultTracked, TrackedValue<T> actualTracked)
      {
          T newDefault = defaultTracked.getDefaultValue();
          T oldDefault = defaultTracked.getRealValue();

          if (!Objects.equals(oldDefault, newDefault))
          {
              // A default value has changed.

              if (Objects.equals(oldDefault, actualTracked.getRealValue()))
              {
                  // We must update the actual value if it matches the old default value
                  actualTracked.setValue(newDefault, true);
              }

              // Update the default
              defaultTracked.setValue(defaultTracked.getDefaultValue(), true);
          }
      }
}
