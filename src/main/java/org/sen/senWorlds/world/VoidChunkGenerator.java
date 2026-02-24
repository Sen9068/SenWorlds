package org.sen.senWorlds.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;
import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {

    private boolean bedrockPlaced = false;

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        // Only place bedrock in chunk (0,0)
        if (!bedrockPlaced && chunkX == 0 && chunkZ == 0) {
            chunk.setBlock(0, 0, 0, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
            bedrockPlaced = true;
        }

        return chunk;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of();
    }
}
