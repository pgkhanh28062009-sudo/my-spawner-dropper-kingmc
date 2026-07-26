package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

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
        // ĐÃ FIX L50: Trả lại AddonTemplate.CATEGORY chuẩn của project bạn
        super(AddonTemplate.CATEGORY, "spawner-dropper", "Tu dong Vut Het & Re-open Spawner khi Full 1 loai do.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Dùng biến mc có sẵn của hệ thống Meteor Module
        if (!active.get() || mc.player == null || mc.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        if (mc.currentScreen != null && mc.player.currentScreenHandler != null) {
            var handler = mc.player.currentScreenHandler;
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
                        // FIX LỖI IMPORT BẰNG REFLECTION THUẦN TÚY
                        try {
                            var interactionManager = mc.interactionManager;
                            if (interactionManager != null) {
                                for (var method : interactionManager.getClass().getDeclaredMethods()) {
                                    if (method.getParameterCount() == 5) {
                                        method.setAccessible(true);
                                        Object clickTypePickup = method.getParameterTypes()[3].getEnumConstants()[0];
                                        method.invoke(interactionManager, handler.syncId, dropAllButtonSlot, 0, clickTypePickup, mc.player);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    // Dong GUI
                    mc.player.closeHandledScreen();

                    // Mo lai Spawner
                    if (mc.options != null && mc.options.useKey != null) {
                        mc.options.useKey.setPressed(true);
                    }

                    timer = delay.get();
                }
            }
        } else {
            if (mc.options != null && mc.options.useKey != null && mc.options.useKey.isPressed()) {
                mc.options.useKey.setPressed(false);
            }
        }
    }
}

