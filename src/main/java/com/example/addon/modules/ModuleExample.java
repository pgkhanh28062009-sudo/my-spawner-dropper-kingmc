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

import net.minecraft.client.MinecraftClient;

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
        MinecraftClient client = MinecraftClient.getInstance();

        if (!active.get() || client.player == null || client.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        if (client.currentScreen != null && client.player.currentScreenHandler != null) {
            var handler = client.player.currentScreenHandler;
            int totalSlots = handler.slots.size();

            if (totalSlots > 36) {
                int containerSlots = totalSlots - 36;
                
                Set<String> uniqueItemTypes = new HashSet<>();
                boolean containsTargetItem = false;
                String[] targets = targetItems.get().toLowerCase().split(",");

                for (int i = 0; i < containerSlots; i++) {
                    var stack = handler.getSlot(i).getStack();
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
                    
                    if (dropAllButtonSlot < totalSlots) {
                        try {
                            var interactionManager = client.interactionManager;
                            if (interactionManager != null) {
                                for (var method : interactionManager.getClass().getDeclaredMethods()) {
                                    if (method.getParameterCount() == 5) {
                                        method.setAccessible(true);
                                        Object slotActionTypePickup = method.getParameterTypes()[3].getEnumConstants()[0];
                                        method.invoke(interactionManager, handler.syncId, dropAllButtonSlot, 0, slotActionTypePickup, client.player);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    // Dong GUI
                    client.player.closeHandledScreen();

                    // Mo lai Spawner
                    if (client.options != null && client.options.useKey != null) {
                        client.options.useKey.setPressed(true);
                    }

                    timer = delay.get();
                }
            }
        } else {
            if (client.options != null && client.options.useKey != null && client.options.useKey.isPressed()) {
                client.options.useKey.setPressed(false);
            }
        }
    }
}
