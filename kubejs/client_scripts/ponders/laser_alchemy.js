Ponder.tags((event) => {
    /**
     * "kubejs:getting_started" -> the tag name
     * "minecraft:paper"        -> the icon
     * "Getting Started"        -> the title
     * "This is a description"  -> the description
     * [...items]               -> default items
     */
    event.createTag(
        "cabin:ponders",
        "kubejs:gold_machine",
        // Ponder for Kubejs is bugged to not show non kubejs namespace text, edit these in en_us.json instead
        "CABIN",
        "Components which are distinctive to A&B and CABIN",
        ["kubejs:alchemical_laser"]
    );
});

Ponder.registry((event) => {
    event.create("kubejs:alchemical_laser")
        .scene("alchemy_setup", "The Alchemical Laser", "cabin:laser_alchemy", (scene, util) => {
            // scene = new CreateSceneBuilder(scene)

            scene.configureBasePlate(0, 0, 5);
            scene.showBasePlate();
            scene.idle(10)

            let largeCog = util.select.position(5, 0, 2)
            let deployerSingle = util.select.position(4, 1, 3)
            let smallCog = util.select.position(4, 1, 2)
            let lamp = util.select.fromTo(4, 1, 4, 4, 2, 4)
            let deployer = util.select.fromTo(4, 1, 2, 4, 2, 4)
            let machine = util.select.position(2, 1, 3)
            let light = util.select.position(2, 1, 2)

            scene.world.showSection(machine, Facing.down)
            scene.idle(15)

            scene.overlay.showText(50)
                .text("Start with an Invar Machine")
                .pointAt(util.vector.topOf(2, 1, 3))
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
            scene.idle(60)

            scene.world.showSection(light, Facing.east)
            scene.overlay.showText(40)
                .text("Attach an Industrial Lamp of any colour")
                .pointAt(util.vector.centerOf(2, 1, 2))
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
            scene.idle(60)


            // scene.overlay.showText(40)
            //     .text("Toggle it on with a wrench")
            //     .colored(PonderPalette.GREEN)
            //     .pointAt(util.vector.centerOf(2, 1, 2))
            //     .placeNearTarget()
            // scene.idle(20)

            // scene.showControls(50, [2.5, 1, 2], "right")
            //     .rightClick().withItem("create:wrench")
            // scene.idle(10)

            // scene.world.setBlock(util.grid.at(2, 1, 2), util.getDefaultState("kubejs:ponder_laser_lamp_on"), false)
            // scene.idle(15)
            //
            // scene.idle(50)

            scene.world.setKineticSpeed(deployer, 0)
            scene.world.showSection(deployerSingle, Facing.down)
            scene.idle(15)

            scene.overlay.showText(60)
                .text("Place a Deployer targeting the Invar Machine")
                .pointAt(util.vector.topOf(4, 1, 3))
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
            scene.idle(70)
            scene.showControls(50, [4, 1, 3], "right")
                .rightClick().withItem("create:wrench")
            scene.idle(8)
            scene.world.modifyBlockEntityNBT(deployerSingle, (nbt) => {nbt.Mode = { Mode: "PUNCH"}});

            scene.overlay.showText(50)
                .text("Set it to Punch mode using a Wrench")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector.topOf(4, 1, 3))
                .placeNearTarget()
            scene.idle(20)
            scene.world.showSection(largeCog, Facing.up)
            scene.world.showSection(smallCog, Facing.down)
            scene.idle(5)
            scene.world.showSection(lamp, Facing.down)
            scene.idle(5)
            scene.world.setKineticSpeed(deployer, 64)
            scene.idle(60)
            scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Whenever the Deployer activates...")
                .pointAt(util.vector.topOf(4, 1, 3))
                .colored(PonderPalette.WHITE)
                .placeNearTarget()

            scene.idle(30)
            scene.world.toggleRedstonePower(lamp)
            scene.idle(3)
            scene.world.moveDeployer(util.grid.at(4, 1, 3), 1, 25);
            scene.idle(15)
            scene.idle(10)

            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 2), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 1), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 0), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, -1), 0xFF9600, 10)

            scene.idle(3)
            scene.world.moveDeployer(util.grid.at(4, 1, 3), -1, 25);
            scene.idle(10)
            scene.world.toggleRedstonePower(lamp)
            // scene.effects.indicateRedstone(util.grid.at(4, 2, 4))

            scene.overlay.showText(50)
                .text("...the Laser Lamp will emit a High-Energy Beam")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector.centerOf(2, 1, 2))
                .placeNearTarget()
            scene.idle(60)

            scene.world.showSection(util.select.fromTo(1, 1, 0, 3, 1, 0), Facing.west)
            scene.idle(5)
            // let HopperMinecart = java("net.minecraft.entity.item.minecart.HopperMinecartEntity")
            // let cartHandle =
            // scene.special.createCart(util.vector.topOf(2, 0, 0), 0, (w, x, y, z) => new HopperMinecart(w, x, y, z))
            scene.world.createEntity("minecraft:hopper_minecart", [2.5,1,0]);
            scene.idle(20)
            scene.overlay.showText(80)
                .attachKeyFrame()
                .text("Hopper Minecarts caught in the Beam will process their contained Items")
                .pointAt(util.vector.centerOf(2, 1, 0))
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
            scene.idle(70)

            scene.showControls(40, [2.5, 2, 0.5], "down")
                .withItem("thermal:flux_magnet")
            scene.idle(5)
            scene.showControls(35, [2.5, 1.75, 0.5], "up")
                .withItem("minecraft:basalt")
            scene.idle(30)

            scene.world.toggleRedstonePower(lamp)
            scene.idle(3)
            scene.world.moveDeployer(util.grid.at(4, 1, 3), 1, 25);
            scene.idle(15)
            scene.idle(10)

            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 2), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 1), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, 0), 0xFF9600, 10)
            scene.effects.createRedstoneParticles(util.grid.at(2, 1, -1), 0xFF9600, 10)

            scene.idle(3)
            scene.world.moveDeployer(util.grid.at(4, 1, 3), -1, 25);
            scene.idle(10)

            scene.showControls(40, [2.5, 2, 0.5], "down")
                .withItem("thermal:flux_magnet")
            scene.idle(5)
            scene.showControls(35, [2.5, 1.75, 0.5], "up")
                .withItem("thermal:basalz_rod")

            // scene.effects.indicateRedstone(util.grid.at(4, 2, 4))
            scene.world.toggleRedstonePower(lamp)
        })
})
