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

import net.minecraft.world.inventory.ClickType;

import java.util.HashSet;
import java.util.Set;

public class ModuleExample extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // 1. Cong tac Bat/Tat
    private final Setting<Boolean> active = sgGeneral.add(new BoolSetting.Builder()
        .name("active")
        .description("Kich hoat tu dong xa Spawner KingMC.")
        .defaultValue(true)
        .build()
    );

    // 2. Danh sach vat pham target
    private final Setting<String> targetItems = sgGeneral.add(new StringSetting.Builder()
        .name("target-items")
        .description("Vat pham xet duyet full trang (vi du: bone, iron_ingot).")
        .defaultValue("bone, iron_ingot, blaze_rod, gunpowder")
        .build()
    );

    // 3. Time Delay (tick)
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
        // MOJMAP: Dung mc.level thay vi mc.world
        if (!active.get() || mc.player == null || mc.level == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        // MOJMAP: mc.screen va mc.player.containerMenu
        if (mc.screen != null && mc.player.containerMenu != null) {
            var menu = mc.player.containerMenu;
            int totalSlots = menu.slots.size();

            if (totalSlots > 36) {
                int containerSlots = totalSlots - 36;
                
                Set<String> uniqueItemTypes = new HashSet<>();
                boolean containsTargetItem = false;
                String[] targets = targetItems.get().toLowerCase().split(",");

                for (int i = 0; i < containerSlots; i++) {
                    // MOJMAP: getItem() thay vi getStack()
                    var stack = menu.getSlot(i).getItem();
                    if (!stack.isEmpty()) {
                        String itemName = stack.getItem().toString().toLowerCase();
                        
                        // Bo qua trang tri GUI
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

                // Chi khi trang chua DUY NHAT 1 loai item thuoc Target
                if (uniqueItemTypes.size() == 1 && containsTargetItem) {
                    int dropAllButtonSlot = 53;
                    
                    // MOJMAP: mc.gameMode thay vi mc.interactionManager
                    if (dropAllButtonSlot < totalSlots && mc.gameMode != null) {
                        // Click nut vut het
                        mc.gameMode.handleInventoryMouseClick(menu.containerId, dropAllButtonSlot, 0, ClickType.PICKUP, mc.player);
                    }

                    // MOJMAP: closeContainer() thay vi closeHandledScreen()
                    mc.player.closeContainer();

                    // MOJMAP: mc.options.keyUse.setDown(true) thay vi mc.options.useKey.setPressed(true)
                    if (mc.options != null && mc.options.keyUse != null) {
                        mc.options.keyUse.setDown(true);
                    }

                    timer = delay.get();
                }
            }
        } else {
            // MOJMAP: keyUse.isDown() va keyUse.setDown(false)
            if (mc.options != null && mc.options.keyUse != null && mc.options.keyUse.isDown()) {
                mc.options.keyUse.setDown(false);
            }
        }
    }
}
