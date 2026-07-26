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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.HashSet;
import java.util.Set;

public class SpawnerDropper extends Module {
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

    public SpawnerDropper(Category category) {
        super(category, "spawner-dropper", "Tu dong Vut Het & Re-open Spawner khi Full 1 loai do.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
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
                        String itemName = stack.getItem().getTranslationKey().toLowerCase();
                        
                        // Bỏ qua nút bấm GUI
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

                // Nếu trang CHỈ CÓ ĐÚNG 1 LOẠI ĐỒ duy nhất thuộc danh sách mục tiêu
                if (uniqueItemTypes.size() == 1 && containsTargetItem) {
                    // Click nút VỨT HẾT RA ĐẤT (Slot 53)
                    int dropAllButtonSlot = 53;
                    if (dropAllButtonSlot < totalSlots) {
                        mc.interactionManager.clickSlot(handler.syncId, dropAllButtonSlot, 0, SlotActionType.PICKUP, mc.player);
                    }

                    // Đóng GUI
                    mc.player.closeHandledScreen();

                    // Tự động Phải Chuột mở lại Spawner
                    if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                    }

                    timer = delay.get();
                }
            }
        }
    }
}

