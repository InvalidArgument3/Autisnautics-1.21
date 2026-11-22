# Massive Turbines Server Fix

Create: Massive Turbines 1.0.1 registers casing placement helpers from `MassiveTurbinesPlacement`'s static initializer. That loads `CasingHelper`, whose ghost render reads `Minecraft.player` (`LocalPlayer`). A dedicated server refuses to load `LocalPlayer`, so the mod fails during construction.

`massiveturbinesfix-1.0.0.jar` ships in `.pakku/overrides/mods/` on both sides. Its mixin is in the dedicated-server list only: `CasingHelper.renderAt` becomes empty there. The client does not apply it, so the ghost blocks stay.
