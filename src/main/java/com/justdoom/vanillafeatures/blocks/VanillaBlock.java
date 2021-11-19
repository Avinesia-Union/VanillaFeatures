package com.justdoom.vanillafeatures.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Set;

public abstract class VanillaBlock extends CustomBlock {

    private final Block baseBlock;
    private final BlockPropertyList properties;
    private final BlockStates blockStates;
    private final BlockState baseBlockState;

    public VanillaBlock(Block baseBlock) {
        super(baseBlock, "vanilla_" + baseBlock.name().toLowerCase());
        this.baseBlock = baseBlock;
        this.properties = createPropertyValues();

        // create block states
        this.blockStates = new BlockStates(properties);
        List<String[]> allVariants = properties.getCartesianProduct();
        if (allVariants.isEmpty()) {
            short id = baseBlock.getBlockId();
            BlockState state = new BlockState(id, blockStates);
            blockStates.add(state);
        } else {
            for (String[] variant : allVariants) {
                short id = baseBlock.withProperties(variant);
                BlockState blockState = new BlockState(id, blockStates, variant);
                blockStates.add(blockState);
            }
        }
        baseBlockState = blockStates.getDefault();
    }

    protected abstract BlockPropertyList createPropertyValues();

    public BlockState getBaseBlockState() {
        return baseBlockState;
    }

    public Block getBaseBlock() {
        return baseBlock;
    }

    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return data;
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    public short getBaseBlockId() {
        return baseBlock.getBlockId();
    }

    @Override
    public short getCustomBlockId() {
        return baseBlock.getBlockId();
    }

    @Override
    public int getBreakDelay(Player player, BlockPosition position, byte stage, Set<Player> breakers) {
        return -1;
    }

    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition blockPosition) {
        return getBaseBlockId();
    }

    public Data readBlockEntity(NBTCompound nbt, Instance instance, BlockPosition position, Data originalData) {
        return originalData;
    }

    public BlockStates getBlockStates() {
        return blockStates;
    }
}
