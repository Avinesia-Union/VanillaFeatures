package com.justdoom.vanillafeatures.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;

import java.io.FileNotFoundException;
import java.util.List;

public enum VanillaBlocks {

    SAND(() -> new GravityBlock(Block.SAND)),
    RED_SAND(() -> new GravityBlock(Block.RED_SAND)),
    GRAVEL(() -> new GravityBlock(Block.GRAVEL));

    private final VanillaBlockSupplier blockSupplier;
    private final BlockPlacementRule placementRule;
    private boolean registered;
    private VanillaBlock instance;

    private VanillaBlocks(VanillaBlockSupplier blockSupplier) {
        this(blockSupplier, null);
    }

    private VanillaBlocks(VanillaBlockSupplier blockSupplier, BlockPlacementRule placementRule) {
        this.blockSupplier = blockSupplier;
        this.placementRule = placementRule;
    }
    public void register(short customBlockID, ConnectionManager connectionManager, BlockManager blockManager) {
        VanillaBlock block = this.blockSupplier.create();
        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
                if (event.getBlockStateId() == block.getBaseBlockId()) {
                    short blockID = block.getVisualBlockForPlacement(event.getPlayer(), event.getHand(), event.getBlockPosition());
                    event.setBlockStateId(blockID);
                    event.setCustomBlockId(block.getCustomBlockId());
                }
            });
        });
        blockManager.registerCustomBlock(block);
        if (placementRule != null) {
            blockManager.registerBlockPlacementRule(placementRule);
        }
        instance = block;
        registered = true;
    }

    public boolean isRegistered() {
        return registered;
    }

    public VanillaBlock getInstance() {
        return instance;
    }

    public static void registerAll(ConnectionManager connectionManager, BlockManager blockManager) {
        for (VanillaBlocks vanillaBlock : values()) {
            vanillaBlock.register((short) vanillaBlock.ordinal(), connectionManager, blockManager);
        }
    }

    @FunctionalInterface
    private interface VanillaBlockSupplier {

        VanillaBlock create();
    }

    public static void dropOnBreak(Instance instance, BlockPosition position) {
        LootTable table = null;
        LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
        CustomBlock customBlock = instance.getCustomBlock(position);
        if (customBlock != null) {
            table = customBlock.getLootTable(lootTableManager);
        }
        Block block = Block.fromStateId(instance.getBlockStateId(position));
        Data lootTableArguments = new DataImpl();
        // TODO: tool used, silk touch, etc.
        try {
            if (table == null) {
                table = lootTableManager.load(NamespaceID.from("blocks/" + block.name().toLowerCase()));
            }
            List<ItemStack> stacks = table.generate(lootTableArguments);
            for (ItemStack item : stacks) {
                Position spawnPosition = new Position((float) (position.getX() + 0.2f + Math.random() * 0.6f), (float) (position.getY() + 0.5f), (float) (position.getZ() + 0.2f + Math.random() * 0.6f));
                ItemEntity itemEntity = new ItemEntity(item, spawnPosition);

                itemEntity.getVelocity().setX((float) (Math.random() * 2f - 1f));
                itemEntity.getVelocity().setY((float) (Math.random() * 2f));
                itemEntity.getVelocity().setZ((float) (Math.random() * 2f - 1f));

                itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                itemEntity.setInstance(instance);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
