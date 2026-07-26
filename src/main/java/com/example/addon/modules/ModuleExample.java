package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.HashSet;
import java.util.Set;

public class ModuleExample extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> active = sgGeneral.add(new BoolSetting.Builder()
        .name("active")
        .description("Kich hoat tu dong xa Spawner KingMC.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> targetItems = sgGeneral.add(new StringSetting.Builder()
        .name("target-items")
        .description("Vat pham xet duyet full trang (vi du: bone, iron_ingot).")
        .defaultValue("bone, iron_ingot, blaze_rod, gunpowder")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Thoi gian hoan giua cac thao tac (tick).")
        .defaultValue(5)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private int timer = 0;

    public ModuleExample() {
        super(Category.MISC, "spawner-dropper", "Tu dong Vut Het & Re-open Spawner khi Full 1 loai do.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!active.get() || mc.player == null || mc.level == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        if (mc.screen != null && mc.player.containerMenu != null) {
            var menu = mc.player.containerMenu;
            int totalSlots = menu.slots.size();

            if (totalSlots > 36) {
                int containerSlots = totalSlots - 36;
                
                Set<String> uniqueItemTypes = new HashSet<>();
                boolean containsTargetItem = false;
                String[] targets = targetItems.get().toLowerCase().split(",");

                for (int i = 0; i < containerSlots; i++) {
                    var stack = menu.getSlot(i).getItem();
                    if (!stack.isEmpty()) {
                        String itemName = stack.getItem().toString().toLowerCase();
                        
                        // Bo qua cac o nut GUI
                        if (itemName.contains("glass") || itemName.contains("dispenser") || itemName.contains("emerald") || itemName.contains("arrow")) {
                            continue;
                        }

                        uniqueItemTypes.add(itemName);

                        for (String target : targets) {
                            String clean = target.trim();
                            if (!clean.isEmpty() && itemName.contains(clean)) {
                                containsTargetItem = true;
                                break;
                            }
                        }
                    }
                }

                // Khi trang chi chua 1 loai vat pham duy nhat va thuoc danh sach target
                if (uniqueItemTypes.size() == 1 && containsTargetItem) {
                    int dropAllButtonSlot = 53;
                    
                    // Click Drop All bang API qua ScreenHandler/ContainerMenu
                    if (dropAllButtonSlot < totalSlots && mc.gameMode != null) {
                        try {
                            // Dung ClickSlot theo kieu Minecraft Official Mappings
                            mc.gameMode.handleInventoryMouseClick(menu.containerId, dropAllButtonSlot, 0, net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
                        } catch (Throwable ignored) {
                            // Backup neu mapping Fabric
                            try {
                                mc.interactionManager.clickSlot(menu.containerId, dropAllButtonSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
                            } catch (Throwable ignored2) {}
                        }
                    }

                    // Dong GUI
                    mc.player.closeContainer();

                    // Re-open Spawner
                    if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, blockHit);
                    } else if (mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult blockHit) {
                        mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND, blockHit);
                    }

                    timer = delay.get();
                }
            }
        }
    }
}
