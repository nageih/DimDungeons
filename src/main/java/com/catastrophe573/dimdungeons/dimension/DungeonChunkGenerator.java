package com.catastrophe573.dimdungeons.dimension;

import java.util.List;
import java.util.Random;

import com.catastrophe573.dimdungeons.feature.AdvancedDungeonFeature;
import com.catastrophe573.dimdungeons.feature.BasicDungeonFeature;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DungeonChunkGenerator extends ChunkGenerator
{
    // this is unused, I just need it to compile
    public static final Codec<FlatChunkGenerator> dummyCodec = FlatGenerationSettings.field_236932_a_.fieldOf("settings").xmap(FlatChunkGenerator::new, FlatChunkGenerator::func_236073_g_).codec();

    // this however is used
    public long worldSeed;

    public DungeonChunkGenerator(BiomeProvider p_i231903_1_, long p_i231903_2_, DimensionSettings p_i231903_4_)
    {
	this(p_i231903_1_, p_i231903_1_, p_i231903_2_, p_i231903_4_);
    }

    private DungeonChunkGenerator(BiomeProvider p_i231904_1_, BiomeProvider p_i231904_2_, long p_i231904_3_, DimensionSettings p_i231904_5_)
    {
	super(p_i231904_2_, p_i231904_2_, p_i231904_5_.func_236108_a_(), p_i231904_3_);
	worldSeed = p_i231904_3_;
    }

    @Override
    //public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos)
    public List<Biome.SpawnListEntry> func_230353_a_(Biome p_230353_1_, StructureManager p_230353_2_, EntityClassification p_230353_3_, BlockPos p_230353_4_)
    {
	return Lists.newArrayList(); // intentionally prevent creatures from spawning
    }

    @Override
    public int getGroundHeight()
    {
	return 55;
    }

    public void makeBase(IWorld worldIn, IChunk chunkIn)
    {
	// I still want a random seed, like the overworld, for use in structures
	Random randomSeed = worldIn.getRandom();

	int x = chunkIn.getPos().x;
	int z = chunkIn.getPos().z;
	randomSeed.setSeed((worldSeed + (long) (x * x * 4987142) + (long) (x * 5947611) + (long) (z * z) * 4392871L + (long) (z * 389711) ^ worldSeed));

	// first generate a superflat world - sandstone where dungeons can appear, and void otherwise
	if (BasicDungeonFeature.isDungeonChunk(x, z) || AdvancedDungeonFeature.isDungeonChunk(x, z))
	{
	    for (int px = 0; px < 16; px++)
	    {
		for (int py = 1; py < 255; py++)
		{
		    for (int pz = 0; pz < 16; pz++)
		    {
			if (py < 2)
			{
			    chunkIn.setBlockState(new BlockPos(px, py, pz), Blocks.BEDROCK.getDefaultState(), false);
			}
			else if (py < 50)
			{
			    // for debugging mostly but it also kind of looks good when you're in creative mode
			    if (BasicDungeonFeature.isEntranceChunk(x, z) || AdvancedDungeonFeature.isEntranceChunk(x, z))
			    {
				chunkIn.setBlockState(new BlockPos(px, py, pz), Blocks.ANDESITE.getDefaultState(), false);
			    }
			    else
			    {
				chunkIn.setBlockState(new BlockPos(px, py, pz), Blocks.SANDSTONE.getDefaultState(), false);
			    }
			}
		    }
		}
	    }
	}
	else
	{
	    // add barrier blocks to the void, just to be sure (players could escape with ender pearls, use elytra with fireworks, etc)
	    if (x % 16 == 0 || z % 16 == 0)
	    {
		for (int px = 0; px < 16; px++)
		{
		    for (int py = 1; py < 255; py++)
		    {
			for (int pz = 0; pz < 16; pz++)
			{
			    chunkIn.setBlockState(new BlockPos(px, py, pz), Blocks.BARRIER.getDefaultState(), false);
			}
		    }
		}
	    }
	}
    }

    @Override
    public void func_235954_a_(StructureManager p_235954_1_, IChunk p_235954_2_, TemplateManager p_235954_3_, long p_235954_4_)
    {
	// do not place features
    }

    @Override
    public void func_235953_a_(IWorld p_235953_1_, StructureManager p_235953_2_, IChunk p_235953_3_)
    {
	// do not place structure starts
    }

    public void func_230352_b_(IWorld p_230352_1_, StructureManager p_230352_2_, IChunk p_230352_3_)
    {
    }

    @Override
    // getGeneratorCodec()
    protected Codec<? extends ChunkGenerator> func_230347_a_()
    {
	return dummyCodec;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    // getSeededGenerator()
    public ChunkGenerator func_230349_a_(long p_230349_1_)
    {
	return new DungeonChunkGenerator(this.biomeProvider.func_230320_a_(p_230349_1_), p_230349_1_, null);
    }

    @Override
    public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_)
    {
	makeBase(p_225551_1_, p_225551_2_);
    }

    // getHeight() but idk what for
    public int func_222529_a(int p_222529_1_, int p_222529_2_, Type heightmapType)
    {
	return 0; // if anyone asks, the answer is 'void'
    }

    // getBlockReader()
    public IBlockReader func_230348_a_(int p_230348_1_, int p_230348_2_)
    {
	// no idea what this is for but it seems to be used by structures that need blocks replaced, which my dimension doesn't need
	return new Blockreader(new BlockState[0]); // so this is the stubbed out implementation from the DebugChunkGenerator
    }
}