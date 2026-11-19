package dev.wyseman.undergardenfloor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FloorBandTest {
    @Test
    void bandIsTheBedrockFloorGradient() {
        int min = -64;
        assertTrue(FloorBand.inFloorBand(-64, min));
        assertTrue(FloorBand.inFloorBand(-60, min));
        assertFalse(FloorBand.inFloorBand(-59, min));
        assertFalse(FloorBand.inFloorBand(0, min));
        assertFalse(FloorBand.inFloorBand(127, min));
    }

    @Test
    void onlyBedrockAndDepthrock() {
        assertTrue(FloorBand.isFloorBlock("minecraft:bedrock"));
        assertTrue(FloorBand.isFloorBlock("undergarden:depthrock"));
        assertFalse(FloorBand.isFloorBlock("minecraft:obsidian"));
        assertFalse(FloorBand.isFloorBlock("undergarden:dreadrock"));
        assertFalse(FloorBand.isFloorBlock("minecraft:deepslate"));
    }
}
