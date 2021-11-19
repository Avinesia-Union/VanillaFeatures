package com.justdoom.vanillafeatures.blocks;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.utils.BlockPosition;

import java.util.*;
import java.util.stream.Collectors;

public class BlockStates {

    private final List<BlockState> states;
    private final Map<String, BlockState> nameLookup = new HashMap<>();
    private final Short2ObjectOpenHashMap<BlockState> idLookup = new Short2ObjectOpenHashMap<>();
    private final BlockPropertyList properties;

    public BlockStates(BlockPropertyList properties) {
        this.properties = properties;
        this.states = new LinkedList<>();
    }

    void add(BlockState blockState) {
        states.add(blockState);
        String lookupKey = properties.computeSortedList().stream()
                .map(property -> property.getKey()+"="+blockState.get(property.getKey()))
                .collect(Collectors.joining(","));
        nameLookup.put(lookupKey, blockState);
        idLookup.put(blockState.getBlockId(), blockState);
    }
    public BlockState getState(Map<String, String> properties) {
        String lookupKey = properties.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey()+"="+entry.getValue())
                .collect(Collectors.joining(","));
        return nameLookup.get(lookupKey);
    }
    public BlockState fromStateID(short id) {
        return idLookup.getOrDefault(id, getDefault());
    }

    public BlockState getStateWithChange(Map<String, String> properties, String key, String value) {
        String lookupKey = properties.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    String prefix = entry.getKey()+"=";
                    if(entry.getKey().equalsIgnoreCase(key)) {
                        return prefix+value;
                    }
                    return prefix+entry.getValue();
                })
                .collect(Collectors.joining(","));
        return nameLookup.get(lookupKey);
    }

    public BlockState getDefault() {
        return states.get(0);
    }

    public BlockState getFromInstance(Instance instance, BlockPosition blockPosition) {
        short id = instance.getBlockStateId(blockPosition);
        BlockAlternative alternative = Block.fromStateId(id).getAlternative(id);
        return getState(alternative.createPropertiesMap());
    }
}
