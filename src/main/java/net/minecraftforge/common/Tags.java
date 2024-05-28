/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class Tags {
    public static void init() {
        Blocks.init();
        EntityTypes.init();
        Items.init();
        Fluids.init();
        Enchantments.init();
        Biomes.init();
        Structures.init();
    }

    public static class Blocks {
        private static void init() {}

        //region `forge` tags for functional behavior provided by Forge
        /**
         * Controls what blocks Endermen cannot place blocks onto.
         * <p></p>
         * This is patched into the following method: {@link EnderMan.EndermanLeaveBlockGoal#canPlaceBlock(Level, BlockPos, BlockState, BlockState, BlockState, BlockPos)}
         */
        public static final TagKey<Block> ENDERMAN_PLACE_ON_BLACKLIST = forgeTag("enderman_place_on_blacklist");
        public static final TagKey<Block> NEEDS_WOOD_TOOL = forgeTag("needs_wood_tool");
        public static final TagKey<Block> NEEDS_GOLD_TOOL = forgeTag("needs_gold_tool");
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = forgeTag("needs_netherite_tool");
        public static final TagKey<Block> STORAGE_BLOCKS_AMETHYST = forgeTag("storage_blocks/amethyst");
        public static final TagKey<Block> STORAGE_BLOCKS_QUARTZ = forgeTag("storage_blocks/quartz");
        //endregion

        //region `c` tags for common conventions
        public static final TagKey<Block> BARRELS = cTag("barrels");
        public static final TagKey<Block> BARRELS_WOODEN = cTag("barrels/wooden");
        public static final TagKey<Block> BOOKSHELVES = cTag("bookshelves");
        /**
         * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks
         */
        public static final TagKey<Block> BUDDING_BLOCKS = cTag("budding_blocks");
        /**
         * For blocks that are similar to amethyst where they have buddings forming from budding blocks
         */
        public static final TagKey<Block> BUDS = cTag("buds");
        public static final TagKey<Block> CHAINS = cTag("chains");
        public static final TagKey<Block> CHESTS = cTag("chests");
        public static final TagKey<Block> CHESTS_ENDER = cTag("chests/ender");
        public static final TagKey<Block> CHESTS_TRAPPED = cTag("chests/trapped");
        public static final TagKey<Block> CHESTS_WOODEN = cTag("chests/wooden");
        /**
         * For blocks that are similar to amethyst where they have clusters forming from budding blocks
         */
        public static final TagKey<Block> CLUSTERS = cTag("clusters");
        public static final TagKey<Block> COBBLESTONES = cTag("cobblestones");
        public static final TagKey<Block> COBBLESTONES_NORMAL = cTag("cobblestones/normal");
        public static final TagKey<Block> COBBLESTONES_INFESTED = cTag("cobblestones/infested");
        public static final TagKey<Block> COBBLESTONES_MOSSY = cTag("cobblestones/mossy");
        public static final TagKey<Block> COBBLESTONES_DEEPSLATE = cTag("cobblestones/deepslate");

        /**
         * Tag that holds all blocks that can be dyed a specific color.
         * (Does not include color blending blocks that would behave similar to leather armor item)
         */
        public static final TagKey<Block> DYED = cTag("dyed");
        public static final TagKey<Block> DYED_BLACK = cTag("dyed/black");
        public static final TagKey<Block> DYED_BLUE = cTag("dyed/blue");
        public static final TagKey<Block> DYED_BROWN = cTag("dyed/brown");
        public static final TagKey<Block> DYED_CYAN = cTag("dyed/cyan");
        public static final TagKey<Block> DYED_GRAY = cTag("dyed/gray");
        public static final TagKey<Block> DYED_GREEN = cTag("dyed/green");
        public static final TagKey<Block> DYED_LIGHT_BLUE = cTag("dyed/light_blue");
        public static final TagKey<Block> DYED_LIGHT_GRAY = cTag("dyed/light_gray");
        public static final TagKey<Block> DYED_LIME = cTag("dyed/lime");
        public static final TagKey<Block> DYED_MAGENTA = cTag("dyed/magenta");
        public static final TagKey<Block> DYED_ORANGE = cTag("dyed/orange");
        public static final TagKey<Block> DYED_PINK = cTag("dyed/pink");
        public static final TagKey<Block> DYED_PURPLE = cTag("dyed/purple");
        public static final TagKey<Block> DYED_RED = cTag("dyed/red");
        public static final TagKey<Block> DYED_WHITE = cTag("dyed/white");
        public static final TagKey<Block> DYED_YELLOW = cTag("dyed/yellow");
        public static final TagKey<Block> END_STONES = cTag("end_stones");
        public static final TagKey<Block> FENCE_GATES = cTag("fence_gates");
        public static final TagKey<Block> FENCE_GATES_WOODEN = cTag("fence_gates/wooden");
        public static final TagKey<Block> FENCES = cTag("fences");
        public static final TagKey<Block> FENCES_NETHER_BRICK = cTag("fences/nether_brick");
        public static final TagKey<Block> FENCES_WOODEN = cTag("fences/wooden");

        public static final TagKey<Block> GLASS_BLOCKS = cTag("glass_blocks");
        public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = cTag("glass_blocks/colorless");
        /**
         * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes
         */
        public static final TagKey<Block> GLASS_BLOCKS_CHEAP = cTag("glass_blocks/cheap");
        public static final TagKey<Block> GLASS_BLOCKS_TINTED = cTag("glass_blocks/tinted");

        public static final TagKey<Block> GLASS_PANES = cTag("glass_panes");
        public static final TagKey<Block> GLASS_PANES_COLORLESS = cTag("glass_panes/colorless");

        public static final TagKey<Block> GRAVELS = cTag("gravels");
        /**
         * Tag that holds all blocks that recipe viewers should not show to users.
         * Recipe viewers may use this to automatically find the corresponding BlockItem to hide.
         */
        public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = cTag("hidden_from_recipe_viewers");
        public static final TagKey<Block> NETHERRACKS = cTag("netherracks");
        public static final TagKey<Block> OBSIDIANS = cTag("obsidians");
        /**
         * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_DEEPSLATE = cTag("ore_bearing_ground/deepslate");
        /**
         * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_NETHERRACK = cTag("ore_bearing_ground/netherrack");
        /**
         * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_STONE = cTag("ore_bearing_ground/stone");
        /**
         * Ores which on average result in more than one resource worth of materials
         */
        public static final TagKey<Block> ORE_RATES_DENSE = cTag("ore_rates/dense");
        /**
         * Ores which on average result in one resource worth of materials
         */
        public static final TagKey<Block> ORE_RATES_SINGULAR = cTag("ore_rates/singular");
        /**
         * Ores which on average result in less than one resource worth of materials
         */
        public static final TagKey<Block> ORE_RATES_SPARSE = cTag("ore_rates/sparse");
        public static final TagKey<Block> ORES = cTag("ores");
        public static final TagKey<Block> ORES_COAL = cTag("ores/coal");
        public static final TagKey<Block> ORES_COPPER = cTag("ores/copper");
        public static final TagKey<Block> ORES_DIAMOND = cTag("ores/diamond");
        public static final TagKey<Block> ORES_EMERALD = cTag("ores/emerald");
        public static final TagKey<Block> ORES_GOLD = cTag("ores/gold");
        public static final TagKey<Block> ORES_IRON = cTag("ores/iron");
        public static final TagKey<Block> ORES_LAPIS = cTag("ores/lapis");
        public static final TagKey<Block> ORES_NETHERITE_SCRAP = cTag("ores/netherite_scrap");
        public static final TagKey<Block> ORES_QUARTZ = cTag("ores/quartz");
        public static final TagKey<Block> ORES_REDSTONE = cTag("ores/redstone");
        /**
         * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output
         */
        public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = cTag("ores_in_ground/deepslate");
        /**
         * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output
         */
        public static final TagKey<Block> ORES_IN_GROUND_NETHERRACK = cTag("ores_in_ground/netherrack");
        /**
         * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output
         */
        public static final TagKey<Block> ORES_IN_GROUND_STONE = cTag("ores_in_ground/stone");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_CRAFTING_TABLES = cTag("player_workstations/crafting_tables");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_FURNACES = cTag("player_workstations/furnaces");
        /**
         * Blocks should be included in this tag if their movement/relocation can cause serious issues such
         * as world corruption upon being moved or for balance reason where the block should not be able to be relocated.
         * Example: Chunk loaders or pipes where other mods that move blocks do not respect
         * {@link BlockBehaviour.BlockStateBase#getPistonPushReaction}.
         */
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = cTag("relocation_not_supported");
        public static final TagKey<Block> ROPES = cTag("ropes");

        public static final TagKey<Block> SANDS = cTag("sands");
        public static final TagKey<Block> SANDS_COLORLESS = cTag("sands/colorless");
        public static final TagKey<Block> SANDS_RED = cTag("sands/red");

        public static final TagKey<Block> SANDSTONE_BLOCKS = cTag("sandstone/blocks");
        public static final TagKey<Block> SANDSTONE_SLABS = cTag("sandstone/slabs");
        public static final TagKey<Block> SANDSTONE_STAIRS = cTag("sandstone/stairs");
        public static final TagKey<Block> SANDSTONE_RED_BLOCKS = cTag("sandstone/red_blocks");
        public static final TagKey<Block> SANDSTONE_RED_SLABS = cTag("sandstone/red_slabs");
        public static final TagKey<Block> SANDSTONE_RED_STAIRS = cTag("sandstone/red_stairs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_BLOCKS = cTag("sandstone/uncolored_blocks");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_SLABS = cTag("sandstone/uncolored_slabs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_STAIRS = cTag("sandstone/uncolored_stairs");
        /**
         * Tag that holds all head based blocks such as Skeleton Skull or Player Head. (Named skulls to match minecraft:skulls item tag)
         */
        public static final TagKey<Block> SKULLS = cTag("skulls");
        /**
         * Natural stone-like blocks that can be used as a base ingredient in recipes that takes stone.
         */
        public static final TagKey<Block> STONES = cTag("stones");
        /**
         * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
         * and has a mirror recipe to reverse the crafting with no loss in resources.
         * <p></p>
         * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
         * and so, it is considered a special case and not given a storage block tag.
         */
        public static final TagKey<Block> STORAGE_BLOCKS = cTag("storage_blocks");
        public static final TagKey<Block> STORAGE_BLOCKS_BONE_MEAL = cTag("storage_blocks/bone_meal");
        public static final TagKey<Block> STORAGE_BLOCKS_COAL = cTag("storage_blocks/coal");
        public static final TagKey<Block> STORAGE_BLOCKS_COPPER = cTag("storage_blocks/copper");
        public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = cTag("storage_blocks/diamond");
        public static final TagKey<Block> STORAGE_BLOCKS_DRIED_KELP = cTag("storage_blocks/dried_kelp");
        public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = cTag("storage_blocks/emerald");
        public static final TagKey<Block> STORAGE_BLOCKS_GOLD = cTag("storage_blocks/gold");
        public static final TagKey<Block> STORAGE_BLOCKS_IRON = cTag("storage_blocks/iron");
        public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = cTag("storage_blocks/lapis");
        public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = cTag("storage_blocks/netherite");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = cTag("storage_blocks/raw_copper");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = cTag("storage_blocks/raw_gold");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = cTag("storage_blocks/raw_iron");
        public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = cTag("storage_blocks/redstone");
        public static final TagKey<Block> STORAGE_BLOCKS_SLIME = cTag("storage_blocks/slime");
        public static final TagKey<Block> STORAGE_BLOCKS_WHEAT = cTag("storage_blocks/wheat");
        public static final TagKey<Block> VILLAGER_JOB_SITES = cTag("villager_job_sites");
        //endregion

        //region Redirect fields for improved backward-compatibility
        // TODO: Remove backwards compat redirect fields in 1.22
        /** @deprecated Use {@link #COBBLESTONES} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> COBBLESTONE = COBBLESTONES;
        /** @deprecated Use {@link #COBBLESTONES_NORMAL} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> COBBLESTONE_NORMAL = COBBLESTONES_NORMAL;
        /** @deprecated Use {@link #COBBLESTONES_INFESTED} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> COBBLESTONE_INFESTED = COBBLESTONES_INFESTED;
        /** @deprecated Use {@link #COBBLESTONES_MOSSY} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> COBBLESTONE_MOSSY = COBBLESTONES_MOSSY;
        /** @deprecated Use {@link #COBBLESTONES_DEEPSLATE} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> COBBLESTONE_DEEPSLATE = COBBLESTONES_DEEPSLATE;

        /** @deprecated Use {@link #GRAVELS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> GRAVEL = GRAVELS;

        /** @deprecated Use {@link #NETHERRACKS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> NETHERRACK = NETHERRACKS;

        /** @deprecated Use {@link #SANDS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> SAND = SANDS;
        /** @deprecated Use {@link #SANDS_COLORLESS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> SAND_COLORLESS = SANDS_COLORLESS;
        /** @deprecated Use {@link #SANDS_RED} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> SAND_RED = SANDS_RED;

        /** @deprecated Use {@link #GLASS_BLOCKS_CHEAP} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Block> GLASS_SILICA = GLASS_BLOCKS_CHEAP;
        //endregion

        private static TagKey<Block> cTag(String name) {
            return BlockTags.create(new ResourceLocation("c", name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class EntityTypes {
        private static void init() {}

        //region `c` tags for common conventions
        public static final TagKey<EntityType<?>> BOSSES = cTag("bosses");
        public static final TagKey<EntityType<?>> MINECARTS = cTag("minecarts");
        public static final TagKey<EntityType<?>> BOATS = cTag("boats");

        /**
         * Entities should be included in this tag if they are not allowed to be picked up by items or grabbed in a way
         * that a player can easily move the entity to anywhere they want. Ideal for special entities that should not
         * be able to be put into a mob jar for example.
         */
        public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = cTag("capturing_not_supported");

        /**
         * Entities should be included in this tag if they are not allowed to be teleported in any way.
         * This is more for mods that allow teleporting entities within the same dimension. Any mod that is
         * teleporting entities to new dimensions should be checking canChangeDimensions method on the entity itself.
         */
        public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = cTag("teleporting_not_supported");
        //endregion

        private static TagKey<EntityType<?>> cTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("c", name));
        }
    }

    public static class Items {
        private static void init() {}

        //region `forge` tags for functional behavior provided by Forge
        /**
         * Controls what items can be consumed for enchanting such as Enchanting Tables.
         * This tag defaults to {@link net.minecraft.world.item.Items#LAPIS_LAZULI} when not present in any datapacks, including forge client on vanilla server
         */
        public static final TagKey<Item> ENCHANTING_FUELS = forgeTag("enchanting_fuels");

        public static final TagKey<Item> STORAGE_BLOCKS_AMETHYST = forgeTag("storage_blocks/amethyst");
        public static final TagKey<Item> STORAGE_BLOCKS_QUARTZ = forgeTag("storage_blocks/quartz");
        //endregion

        //region `c` tags for common conventions
        public static final TagKey<Item> BARRELS = cTag("barrels");
        public static final TagKey<Item> BARRELS_WOODEN = cTag("barrels/wooden");
        public static final TagKey<Item> BONES = cTag("bones");
        public static final TagKey<Item> BOOKSHELVES = cTag("bookshelves");
        public static final TagKey<Item> BRICKS = cTag("bricks");
        public static final TagKey<Item> BRICKS_NORMAL = cTag("bricks/normal");
        public static final TagKey<Item> BRICKS_NETHER = cTag("bricks/nether");
        public static final TagKey<Item> BUCKETS = cTag("buckets");
        public static final TagKey<Item> BUCKETS_EMPTY = cTag("buckets/empty");
        /**
         * Does not include entity water buckets.
         * If checking for the fluid this bucket holds in code, please use {@link FluidBucketWrapper#getFluid} instead.
         */
        public static final TagKey<Item> BUCKETS_WATER = cTag("buckets/water");
        /**
         * If checking for the fluid this bucket holds in code, please use {@link FluidBucketWrapper#getFluid} instead.
         */
        public static final TagKey<Item> BUCKETS_LAVA = cTag("buckets/lava");
        public static final TagKey<Item> BUCKETS_MILK = cTag("buckets/milk");
        public static final TagKey<Item> BUCKETS_POWDER_SNOW = cTag("buckets/powder_snow");
        public static final TagKey<Item> BUCKETS_ENTITY_WATER = cTag("buckets/entity_water");
        /**
         * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks
         */
        public static final TagKey<Item> BUDDING_BLOCKS = cTag("budding_blocks");
        /**
         * For blocks that are similar to amethyst where they have buddings forming from budding blocks
         */
        public static final TagKey<Item> BUDS = cTag("buds");
        public static final TagKey<Item> CHAINS = cTag("chains");
        public static final TagKey<Item> CHESTS = cTag("chests");
        public static final TagKey<Item> CHESTS_ENDER = cTag("chests/ender");
        public static final TagKey<Item> CHESTS_TRAPPED = cTag("chests/trapped");
        public static final TagKey<Item> CHESTS_WOODEN = cTag("chests/wooden");
        public static final TagKey<Item> COBBLESTONES = cTag("cobblestones");
        public static final TagKey<Item> COBBLESTONES_NORMAL = cTag("cobblestones/normal");
        public static final TagKey<Item> COBBLESTONES_INFESTED = cTag("cobblestones/infested");
        public static final TagKey<Item> COBBLESTONES_MOSSY = cTag("cobblestones/mossy");
        public static final TagKey<Item> COBBLESTONES_DEEPSLATE = cTag("cobblestones/deepslate");
        /**
         * For blocks that are similar to amethyst where they have clusters forming from budding blocks
         */
        public static final TagKey<Item> CLUSTERS = cTag("clusters");
        public static final TagKey<Item> CROPS = cTag("crops");
        public static final TagKey<Item> CROPS_BEETROOT = cTag("crops/beetroot");
        public static final TagKey<Item> CROPS_CARROT = cTag("crops/carrot");
        public static final TagKey<Item> CROPS_NETHER_WART = cTag("crops/nether_wart");
        public static final TagKey<Item> CROPS_POTATO = cTag("crops/potato");
        public static final TagKey<Item> CROPS_WHEAT = cTag("crops/wheat");
        public static final TagKey<Item> DUSTS = cTag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = cTag("dusts/redstone");
        public static final TagKey<Item> DUSTS_GLOWSTONE = cTag("dusts/glowstone");

        /**
         * Tag that holds all blocks and items that can be dyed a specific color.
         * (Does not include color blending items like leather armor
         * Use {@link net.minecraft.tags.ItemTags#DYEABLE} tag instead for color blending items)
         */
        public static final TagKey<Item> DYED = cTag("dyed");
        public static final TagKey<Item> DYED_BLACK = cTag("dyed/black");
        public static final TagKey<Item> DYED_BLUE = cTag("dyed/blue");
        public static final TagKey<Item> DYED_BROWN = cTag("dyed/brown");
        public static final TagKey<Item> DYED_CYAN = cTag("dyed/cyan");
        public static final TagKey<Item> DYED_GRAY = cTag("dyed/gray");
        public static final TagKey<Item> DYED_GREEN = cTag("dyed/green");
        public static final TagKey<Item> DYED_LIGHT_BLUE = cTag("dyed/light_blue");
        public static final TagKey<Item> DYED_LIGHT_GRAY = cTag("dyed/light_gray");
        public static final TagKey<Item> DYED_LIME = cTag("dyed/lime");
        public static final TagKey<Item> DYED_MAGENTA = cTag("dyed/magenta");
        public static final TagKey<Item> DYED_ORANGE = cTag("dyed/orange");
        public static final TagKey<Item> DYED_PINK = cTag("dyed/pink");
        public static final TagKey<Item> DYED_PURPLE = cTag("dyed/purple");
        public static final TagKey<Item> DYED_RED = cTag("dyed/red");
        public static final TagKey<Item> DYED_WHITE = cTag("dyed/white");
        public static final TagKey<Item> DYED_YELLOW = cTag("dyed/yellow");

        public static final TagKey<Item> DYES = cTag("dyes");
        public static final TagKey<Item> DYES_BLACK = DyeColor.BLACK.getTag();
        public static final TagKey<Item> DYES_RED = DyeColor.RED.getTag();
        public static final TagKey<Item> DYES_GREEN = DyeColor.GREEN.getTag();
        public static final TagKey<Item> DYES_BROWN = DyeColor.BROWN.getTag();
        public static final TagKey<Item> DYES_BLUE = DyeColor.BLUE.getTag();
        public static final TagKey<Item> DYES_PURPLE = DyeColor.PURPLE.getTag();
        public static final TagKey<Item> DYES_CYAN = DyeColor.CYAN.getTag();
        public static final TagKey<Item> DYES_LIGHT_GRAY = DyeColor.LIGHT_GRAY.getTag();
        public static final TagKey<Item> DYES_GRAY = DyeColor.GRAY.getTag();
        public static final TagKey<Item> DYES_PINK = DyeColor.PINK.getTag();
        public static final TagKey<Item> DYES_LIME = DyeColor.LIME.getTag();
        public static final TagKey<Item> DYES_YELLOW = DyeColor.YELLOW.getTag();
        public static final TagKey<Item> DYES_LIGHT_BLUE = DyeColor.LIGHT_BLUE.getTag();
        public static final TagKey<Item> DYES_MAGENTA = DyeColor.MAGENTA.getTag();
        public static final TagKey<Item> DYES_ORANGE = DyeColor.ORANGE.getTag();
        public static final TagKey<Item> DYES_WHITE = DyeColor.WHITE.getTag();

        public static final TagKey<Item> EGGS = cTag("eggs");
        public static final TagKey<Item> END_STONES = cTag("end_stones");
        public static final TagKey<Item> ENDER_PEARLS = cTag("ender_pearls");
        public static final TagKey<Item> FEATHERS = cTag("feathers");
        public static final TagKey<Item> FENCE_GATES = cTag("fence_gates");
        public static final TagKey<Item> FENCE_GATES_WOODEN = cTag("fence_gates/wooden");
        public static final TagKey<Item> FENCES = cTag("fences");
        public static final TagKey<Item> FENCES_NETHER_BRICK = cTag("fences/nether_brick");
        public static final TagKey<Item> FENCES_WOODEN = cTag("fences/wooden");
        public static final TagKey<Item> FOODS = cTag("foods");
        /**
         * Apples and other foods that are considered fruits in the culinary field belong in this tag.
         * Cherries would go here as they are considered a "stone fruit" within culinary fields.
         */
        public static final TagKey<Item> FOODS_FRUITS = cTag("foods/fruits");
        /**
         * Tomatoes and other foods that are considered vegetables in the culinary field belong in this tag.
         */
        public static final TagKey<Item> FOODS_VEGETABLES = cTag("foods/vegetables");
        /**
         * Strawberries, raspberries, and other berry foods belong in this tag.
         * Cherries would NOT go here as they are considered a "stone fruit" within culinary fields.
         */
        public static final TagKey<Item> FOODS_BERRIES = cTag("foods/berries");
        public static final TagKey<Item> FOODS_BREADS = cTag("foods/breads");
        public static final TagKey<Item> FOODS_COOKIES = cTag("foods/cookies");
        public static final TagKey<Item> FOODS_RAW_MEATS = cTag("foods/raw_meats");
        public static final TagKey<Item> FOODS_COOKED_MEATS = cTag("foods/cooked_meats");
        public static final TagKey<Item> FOODS_RAW_FISHES = cTag("foods/raw_fishes");
        public static final TagKey<Item> FOODS_COOKED_FISHES = cTag("foods/cooked_fishes");
        /**
         * Soups, stews, and other liquid food in bowls belongs in this tag.
         */
        public static final TagKey<Item> FOODS_SOUPS = cTag("foods/soups");
        /**
         * Sweets and candies like lollipops or chocolate belong in this tag.
         */
        public static final TagKey<Item> FOODS_CANDIES = cTag("foods/candies");
        /**
         * Foods like cake that can be eaten when placed in the world belong in this tag.
         */
        public static final TagKey<Item> FOODS_EDIBLE_WHEN_PLACED = cTag("foods/edible_when_placed");
        /**
         * For foods that inflict food poisoning-like effects.
         * Examples are Rotten Flesh's Hunger or Pufferfish's Nausea, or Poisonous Potato's Poison.
         */
        public static final TagKey<Item> FOODS_FOOD_POISONING = cTag("foods/food_poisoning");
        public static final TagKey<Item> GEMS = cTag("gems");
        public static final TagKey<Item> GEMS_DIAMOND = cTag("gems/diamond");
        public static final TagKey<Item> GEMS_EMERALD = cTag("gems/emerald");
        public static final TagKey<Item> GEMS_AMETHYST = cTag("gems/amethyst");
        public static final TagKey<Item> GEMS_LAPIS = cTag("gems/lapis");
        public static final TagKey<Item> GEMS_PRISMARINE = cTag("gems/prismarine");
        public static final TagKey<Item> GEMS_QUARTZ = cTag("gems/quartz");

        public static final TagKey<Item> GLASS_BLOCKS = cTag("glass_blocks");
        public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = cTag("glass_blocks/colorless");
        /**
         * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes
         */
        public static final TagKey<Item> GLASS_BLOCKS_CHEAP = cTag("glass_blocks/cheap");
        public static final TagKey<Item> GLASS_BLOCKS_TINTED = cTag("glass_blocks/tinted");

        public static final TagKey<Item> GLASS_PANES = cTag("glass_panes");
        public static final TagKey<Item> GLASS_PANES_COLORLESS = cTag("glass_panes/colorless");

        public static final TagKey<Item> GRAVELS = cTag("gravels");
        public static final TagKey<Item> GUNPOWDERS = cTag("gunpowders");
        /**
         * Tag that holds all items that recipe viewers should not show to users.
         */
        public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = cTag("hidden_from_recipe_viewers");
        public static final TagKey<Item> INGOTS = cTag("ingots");
        public static final TagKey<Item> INGOTS_COPPER = cTag("ingots/copper");
        public static final TagKey<Item> INGOTS_GOLD = cTag("ingots/gold");
        public static final TagKey<Item> INGOTS_IRON = cTag("ingots/iron");
        public static final TagKey<Item> INGOTS_NETHERITE = cTag("ingots/netherite");
        public static final TagKey<Item> LEATHERS = cTag("leathers");
        public static final TagKey<Item> MUSHROOMS = cTag("mushrooms");
        public static final TagKey<Item> NETHER_STARS = cTag("nether_stars");
        public static final TagKey<Item> NETHERRACKS = cTag("netherracks");
        public static final TagKey<Item> NUGGETS = cTag("nuggets");
        public static final TagKey<Item> NUGGETS_GOLD = cTag("nuggets/gold");
        public static final TagKey<Item> NUGGETS_IRON = cTag("nuggets/iron");
        public static final TagKey<Item> OBSIDIANS = cTag("obsidians");
        /**
         * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_DEEPSLATE = cTag("ore_bearing_ground/deepslate");
        /**
         * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_NETHERRACK = cTag("ore_bearing_ground/netherrack");
        /**
         * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_STONE = cTag("ore_bearing_ground/stone");
        /**
         * Ores which on average result in more than one resource worth of materials
         */
        public static final TagKey<Item> ORE_RATES_DENSE = cTag("ore_rates/dense");
        /**
         * Ores which on average result in one resource worth of materials
         */
        public static final TagKey<Item> ORE_RATES_SINGULAR = cTag("ore_rates/singular");
        /**
         * Ores which on average result in less than one resource worth of materials
         */
        public static final TagKey<Item> ORE_RATES_SPARSE = cTag("ore_rates/sparse");
        public static final TagKey<Item> ORES = cTag("ores");
        public static final TagKey<Item> ORES_COAL = cTag("ores/coal");
        public static final TagKey<Item> ORES_COPPER = cTag("ores/copper");
        public static final TagKey<Item> ORES_DIAMOND = cTag("ores/diamond");
        public static final TagKey<Item> ORES_EMERALD = cTag("ores/emerald");
        public static final TagKey<Item> ORES_GOLD = cTag("ores/gold");
        public static final TagKey<Item> ORES_IRON = cTag("ores/iron");
        public static final TagKey<Item> ORES_LAPIS = cTag("ores/lapis");
        public static final TagKey<Item> ORES_NETHERITE_SCRAP = cTag("ores/netherite_scrap");
        public static final TagKey<Item> ORES_QUARTZ = cTag("ores/quartz");
        public static final TagKey<Item> ORES_REDSTONE = cTag("ores/redstone");
        /**
         * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output
         */
        public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = cTag("ores_in_ground/deepslate");
        /**
         * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output
         */
        public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = cTag("ores_in_ground/netherrack");
        /**
         * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output
         */
        public static final TagKey<Item> ORES_IN_GROUND_STONE = cTag("ores_in_ground/stone");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = cTag("player_workstations/crafting_tables");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = cTag("player_workstations/furnaces");
        public static final TagKey<Item> RAW_BLOCKS = cTag("raw_blocks");
        public static final TagKey<Item> RAW_BLOCKS_COPPER = cTag("raw_blocks/copper");
        public static final TagKey<Item> RAW_BLOCKS_GOLD = cTag("raw_blocks/gold");
        public static final TagKey<Item> RAW_BLOCKS_IRON = cTag("raw_blocks/iron");
        public static final TagKey<Item> RAW_MATERIALS = cTag("raw_materials");
        public static final TagKey<Item> RAW_MATERIALS_COPPER = cTag("raw_materials/copper");
        public static final TagKey<Item> RAW_MATERIALS_GOLD = cTag("raw_materials/gold");
        public static final TagKey<Item> RAW_MATERIALS_IRON = cTag("raw_materials/iron");
        /**
         * For rod-like materials to be used in recipes.
         */
        public static final TagKey<Item> RODS = cTag("rods");
        public static final TagKey<Item> RODS_BLAZE = cTag("rods/blaze");
        public static final TagKey<Item> RODS_BREEZE = cTag("rods/breeze");
        /**
         * For stick-like materials to be used in recipes.
         * One example is a mod adds stick variants such as Spruce Sticks but would like stick recipes to be able to use it.
         */
        public static final TagKey<Item> RODS_WOODEN = cTag("rods/wooden");
        public static final TagKey<Item> ROPES = cTag("ropes");

        public static final TagKey<Item> SANDS = cTag("sands");
        public static final TagKey<Item> SANDS_COLORLESS = cTag("sands/colorless");
        public static final TagKey<Item> SANDS_RED = cTag("sands/red");

        public static final TagKey<Item> SANDSTONE_BLOCKS = cTag("sandstone/blocks");
        public static final TagKey<Item> SANDSTONE_SLABS = cTag("sandstone/slabs");
        public static final TagKey<Item> SANDSTONE_STAIRS = cTag("sandstone/stairs");
        public static final TagKey<Item> SANDSTONE_RED_BLOCKS = cTag("sandstone/red_blocks");
        public static final TagKey<Item> SANDSTONE_RED_SLABS = cTag("sandstone/red_slabs");
        public static final TagKey<Item> SANDSTONE_RED_STAIRS = cTag("sandstone/red_stairs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_BLOCKS = cTag("sandstone/uncolored_blocks");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_SLABS = cTag("sandstone/uncolored_slabs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_STAIRS = cTag("sandstone/uncolored_stairs");

        public static final TagKey<Item> SEEDS = cTag("seeds");
        public static final TagKey<Item> SEEDS_BEETROOT = cTag("seeds/beetroot");
        public static final TagKey<Item> SEEDS_MELON = cTag("seeds/melon");
        public static final TagKey<Item> SEEDS_PUMPKIN = cTag("seeds/pumpkin");
        public static final TagKey<Item> SEEDS_WHEAT = cTag("seeds/wheat");
        public static final TagKey<Item> SLIMEBALLS = cTag("slimeballs");
        /**
         * Natural stone-like blocks that can be used as a base ingredient in recipes that takes stone.
         */
        public static final TagKey<Item> STONES = cTag("stones");
        /**
         * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
         * and has a mirror recipe to reverse the crafting with no loss in resources.
         * <p></p>
         * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
         * and so, it is considered a special case and not given a storage block tag.
         */
        public static final TagKey<Item> STORAGE_BLOCKS = cTag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = cTag("storage_blocks/bone_meal");
        public static final TagKey<Item> STORAGE_BLOCKS_COAL = cTag("storage_blocks/coal");
        public static final TagKey<Item> STORAGE_BLOCKS_COPPER = cTag("storage_blocks/copper");
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = cTag("storage_blocks/diamond");
        public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = cTag("storage_blocks/dried_kelp");
        public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = cTag("storage_blocks/emerald");
        public static final TagKey<Item> STORAGE_BLOCKS_GOLD = cTag("storage_blocks/gold");
        public static final TagKey<Item> STORAGE_BLOCKS_IRON = cTag("storage_blocks/iron");
        public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = cTag("storage_blocks/lapis");
        public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = cTag("storage_blocks/netherite");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = cTag("storage_blocks/raw_copper");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = cTag("storage_blocks/raw_gold");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = cTag("storage_blocks/raw_iron");
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = cTag("storage_blocks/redstone");
        public static final TagKey<Item> STORAGE_BLOCKS_SLIME = cTag("storage_blocks/slime");
        public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = cTag("storage_blocks/wheat");
        public static final TagKey<Item> STRINGS = cTag("strings");
        public static final TagKey<Item> VILLAGER_JOB_SITES = cTag("villager_job_sites");

        // Tools and Armors
        /**
         * A tag containing all existing tools. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS = cTag("tools");
        /**
         * A tag containing all existing shields. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_SHIELDS = cTag("tools/shields");
        /**
         * A tag containing all existing bows. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_BOWS = cTag("tools/bows");
        /**
         * A tag containing all existing crossbows. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_CROSSBOWS = cTag("tools/crossbows");
        /**
         * A tag containing all existing fishing rods. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_FISHING_RODS = cTag("tools/fishing_rods");
        /**
         * A tag containing all existing spears. Other tools such as throwing knives or boomerangs
         * should not be put into this tag and should be put into their own tool tags.
         * Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_SPEARS = cTag("tools/spears");
        /**
         * A tag containing all existing shears. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_SHEARS = cTag("tools/shears");
        /**
         * A tag containing all existing brushes. Do not use this tag for determining a tool's behavior.
         * Please use {@link ToolActions} instead for what action a tool can do.
         *
         * @see ToolAction
         * @see ToolActions
         */
        public static final TagKey<Item> TOOLS_BRUSHES = cTag("tools/brushes");
        /**
         * Collects the 4 vanilla armor tags into one parent collection for ease.
         */
        public static final TagKey<Item> ARMORS = cTag("armors");
        /**
         * Collects the many enchantable tags into one parent collection for ease.
         */
        public static final TagKey<Item> ENCHANTABLES = cTag("enchantables");
        //endregion

        //region Redirect fields for improved backward-compatibility
        // TODO: Remove backwards compat redirect fields in 1.22
        /** @deprecated Use {@link #COBBLESTONES} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> COBBLESTONE = COBBLESTONES;
        /** @deprecated Use {@link #COBBLESTONES_NORMAL} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> COBBLESTONE_NORMAL = COBBLESTONES_NORMAL;
        /** @deprecated Use {@link #COBBLESTONES_INFESTED} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> COBBLESTONE_INFESTED = COBBLESTONES_INFESTED;
        /** @deprecated Use {@link #COBBLESTONES_MOSSY} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> COBBLESTONE_MOSSY = COBBLESTONES_MOSSY;
        /** @deprecated Use {@link #COBBLESTONES_DEEPSLATE} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> COBBLESTONE_DEEPSLATE = COBBLESTONES_DEEPSLATE;
        /** @deprecated Use {@link #GRAVELS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> GRAVEL = GRAVELS;
        /** @deprecated Use {@link #GUNPOWDERS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> GUNPOWDER = GUNPOWDERS;
        /** @deprecated Use {@link #LEATHERS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> LEATHER = LEATHERS;
        /** @deprecated Use {@link #NETHERRACKS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> NETHERRACK = NETHERRACKS;
        /** @deprecated Use {@link #OBSIDIANS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> OBSIDIAN = OBSIDIANS;
        /** @deprecated Use {@link #SANDS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> SAND = SANDS;
        /** @deprecated Use {@link #SANDS_COLORLESS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> SAND_COLORLESS = SANDS_COLORLESS;
        /** @deprecated Use {@link #SANDS_RED} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> SAND_RED = SANDS_RED;
        /** @deprecated Use {@link #SANDSTONE_BLOCKS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> SANDSTONE = SANDSTONE_BLOCKS;
        /** @deprecated Use {@link #STONES} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> STONE = STONES;
        /** @deprecated Use {@link #TOOLS_SHEARS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> SHEARS = TOOLS_SHEARS;
        /** @deprecated Use {@link #TOOLS_SPEARS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> TOOLS_TRIDENTS = TOOLS_SPEARS;
        /** @deprecated Use {@link #STRINGS} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Item> STRING = STRINGS;
        //endregion

        private static TagKey<Item> cTag(String name) {
            return ItemTags.create(new ResourceLocation("c", name));
        }

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Fluids {
        private static void init() {}

        //region `c` tags for common conventions
        /**
         * Holds all fluids related to water.
         * This tag is done to help out multi-loader mods/datapacks where the vanilla water tag has attached behaviors outside Forge.
         */
        public static final TagKey<Fluid> WATER = cTag("water");
        /**
         * Holds all fluids related to lava.
         * This tag is done to help out multi-loader mods/datapacks where the vanilla lava tag has attached behaviors outside Forge.
         */
        public static final TagKey<Fluid> LAVA = cTag("lava");
        /**
         * Holds all fluids related to milk.
         */
        public static final TagKey<Fluid> MILK = cTag("milk");
        /**
         * Holds all fluids that are gaseous at room temperature.
         */
        public static final TagKey<Fluid> GASEOUS = cTag("gaseous");
        /**
         * Holds all fluids related to honey.<br></br>
         * (Standard unit for honey bottle is 250mb per bottle)
         */
        public static final TagKey<Fluid> HONEY = cTag("honey");
        /**
         * Holds all fluids related to potions. The effects of the potion fluid should be read from NBT.
         * The effects and color of the potion fluid should be read from {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}
         * component that people should be attaching to the fluidstack of this fluid.<br></br>
         * (Standard unit for potions is 250mb per bottle)
         */
        public static final TagKey<Fluid> POTION = cTag("potion");
        /**
         * Holds all fluids related to Suspicious Stew.
         * The effects of the suspicious stew fluid should be read from {@link net.minecraft.core.component.DataComponents#SUSPICIOUS_STEW_EFFECTS}
         * component that people should be attaching to the fluidstack of this fluid.<br></br>
         * (Standard unit for suspicious stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> SUSPICIOUS_STEW = cTag("suspicious_stew");
        /**
         * Holds all fluids related to Mushroom Stew.<br></br>
         * (Standard unit for mushroom stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> MUSHROOM_STEW = cTag("mushroom_stew");
        /**
         * Holds all fluids related to Rabbit Stew.<br></br>
         * (Standard unit for rabbit stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> RABBIT_STEW = cTag("rabbit_stew");
        /**
         * Holds all fluids related to Beetroot Soup.<br></br>
         * (Standard unit for beetroot soup is 250mb per bowl)
         */
        public static final TagKey<Fluid> BEETROOT_SOUP = cTag("beetroot_soup");
        /**
         * Tag that holds all fluids that recipe viewers should not show to users.
         */
        public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = cTag("hidden_from_recipe_viewers");
        //endregion

        private static TagKey<Fluid> cTag(String name) {
            return FluidTags.create(new ResourceLocation("c", name));
        }
    }

    public static class Enchantments {
        private static void init() {}

        //region `c` tags for common conventions
        /**
         * A tag containing enchantments that increase the amount or
         * quality of drops from blocks, such as {@link net.minecraft.world.item.enchantment.Enchantments#FORTUNE}.
         */
        public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = cTag("increase_block_drops");
        /**
         * A tag containing enchantments that increase the amount or
         * quality of drops from entities, such as {@link net.minecraft.world.item.enchantment.Enchantments#LOOTING}.
         */
        public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = cTag("increase_entity_drops");
        /**
         * For enchantments that increase the damage dealt by an item.
         */
        public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = cTag("weapon_damage_enhancements");
        /**
         * For enchantments that increase movement speed for entity wearing armor enchanted with it.
         */
        public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = cTag("entity_speed_enhancements");
        /**
         * For enchantments that applies movement-based benefits unrelated to speed for the entity wearing armor enchanted with it.
         * Example: Reducing falling speeds ({@link net.minecraft.world.item.enchantment.Enchantments#FEATHER_FALLING}) or allowing walking on water ({@link net.minecraft.world.item.enchantment.Enchantments#FROST_WALKER})
         */
        public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = cTag("entity_auxiliary_movement_enhancements");
        /**
         * For enchantments that decrease damage taken or otherwise benefit, in regard to damage, the entity wearing armor enchanted with it.
         */
        public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = cTag("entity_defense_enhancements");
        //endregion

        private static TagKey<Enchantment> cTag(String name) {
            return TagKey.create(Registries.ENCHANTMENT, new ResourceLocation("c", name));
        }
    }

    public static class Biomes {
        private static void init() {}

        //region `c` tags for common conventions
        /**
         * For biomes that should not spawn monsters over time the normal way.
         * In other words, their Spawners and Spawn Cost entries have the monster category empty.
         * Example: Mushroom Biomes not having Zombies, Creepers, Skeleton, nor any other normal monsters.
         */
        public static final TagKey<Biome> NO_DEFAULT_MONSTERS = cTag("no_default_monsters");
        /**
         * Biomes that should not be locatable/selectable by modded biome-locating items or abilities.
         */
        public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = cTag("hidden_from_locator_selection");

        public static final TagKey<Biome> IS_VOID = cTag("is_void");

        public static final TagKey<Biome> IS_HOT = cTag("is_hot");
        public static final TagKey<Biome> IS_HOT_OVERWORLD = cTag("is_hot/overworld");
        public static final TagKey<Biome> IS_HOT_NETHER = cTag("is_hot/nether");
        public static final TagKey<Biome> IS_HOT_END = cTag("is_hot/end");

        public static final TagKey<Biome> IS_COLD = cTag("is_cold");
        public static final TagKey<Biome> IS_COLD_OVERWORLD = cTag("is_cold/overworld");
        public static final TagKey<Biome> IS_COLD_NETHER = cTag("is_cold/nether");
        public static final TagKey<Biome> IS_COLD_END = cTag("is_cold/end");

        public static final TagKey<Biome> IS_SPARSE_VEGETATION = cTag("is_sparse_vegetation");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_OVERWORLD = cTag("is_sparse_vegetation/overworld");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_NETHER = cTag("is_sparse_vegetation/nether");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_END = cTag("is_sparse_vegetation/end");
        public static final TagKey<Biome> IS_DENSE_VEGETATION = cTag("is_dense_vegetation");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_OVERWORLD = cTag("is_dense_vegetation/overworld");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_NETHER = cTag("is_dense_vegetation/nether");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_END = cTag("is_dense_vegetation/end");

        public static final TagKey<Biome> IS_WET = cTag("is_wet");
        public static final TagKey<Biome> IS_WET_OVERWORLD = cTag("is_wet/overworld");
        public static final TagKey<Biome> IS_WET_NETHER = cTag("is_wet/nether");
        public static final TagKey<Biome> IS_WET_END = cTag("is_wet/end");
        public static final TagKey<Biome> IS_DRY = cTag("is_dry");
        public static final TagKey<Biome> IS_DRY_OVERWORLD = cTag("is_dry/overworld");
        public static final TagKey<Biome> IS_DRY_NETHER = cTag("is_dry/nether");
        public static final TagKey<Biome> IS_DRY_END = cTag("is_dry/end");

        /**
         * Biomes that spawn in the Overworld.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_OVERWORLD}
         * <p></p>
         * NOTE: If you do not add to the vanilla Overworld tag, be sure to add to
         * {@link net.minecraft.tags.BiomeTags#HAS_STRONGHOLD} so some Strongholds do not go missing.)
         */
        public static final TagKey<Biome> IS_OVERWORLD = cTag("is_overworld");

        public static final TagKey<Biome> IS_CONIFEROUS_TREE = cTag("is_tree/coniferous");
        public static final TagKey<Biome> IS_SAVANNA_TREE = cTag("is_tree/savanna");
        public static final TagKey<Biome> IS_JUNGLE_TREE = cTag("is_tree/jungle");
        public static final TagKey<Biome> IS_DECIDUOUS_TREE = cTag("is_tree/deciduous");

        /**
         * Biomes that spawn as part of giant mountains.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_MOUNTAIN})
         */
        public static final TagKey<Biome> IS_MOUNTAIN = cTag("is_mountain");
        public static final TagKey<Biome> IS_MOUNTAIN_PEAK = cTag("is_mountain/peak");
        public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = cTag("is_mountain/slope");

        /**
         * For temperate or warmer plains-like biomes.
         * For snowy plains-like biomes, see {@link #IS_SNOWY_PLAINS}.
         */
        public static final TagKey<Biome> IS_PLAINS = cTag("is_plains");
        /**
         * For snowy plains-like biomes.
         * For warmer plains-like biomes, see {@link #IS_PLAINS}.
         */
        public static final TagKey<Biome> IS_SNOWY_PLAINS = cTag("is_snowy_plains");
        /**
         * Biomes densely populated with deciduous trees.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_FOREST})
         */
        public static final TagKey<Biome> IS_FOREST = cTag("is_forest");
        public static final TagKey<Biome> IS_BIRCH_FOREST = cTag("is_birch_forest");
        public static final TagKey<Biome> IS_FLOWER_FOREST = cTag("is_flower_forest");
        /**
         * Biomes that spawn as a taiga.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_TAIGA})
         */
        public static final TagKey<Biome> IS_TAIGA = cTag("is_taiga");
        public static final TagKey<Biome> IS_OLD_GROWTH = cTag("is_old_growth");
        /**
         * Biomes that spawn as a hills biome. (Previously was called Extreme Hills biome in past)
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_HILL})
         */
        public static final TagKey<Biome> IS_HILL = cTag("is_hill");
        public static final TagKey<Biome> IS_WINDSWEPT = cTag("is_windswept");
        /**
         * Biomes that spawn as a jungle.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_JUNGLE})
         */
        public static final TagKey<Biome> IS_JUNGLE = cTag("is_jungle");
        /**
         * Biomes that spawn as a savanna.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_SAVANNA})
         */
        public static final TagKey<Biome> IS_SAVANNA = cTag("is_savanna");
        public static final TagKey<Biome> IS_SWAMP = cTag("is_swamp");
        public static final TagKey<Biome> IS_DESERT = cTag("is_desert");
        /**
         * Biomes that spawn as a badlands.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_BADLANDS})
         */
        public static final TagKey<Biome> IS_BADLANDS = cTag("is_badlands");
        /**
         * Biomes that are dedicated to spawning on the shoreline of a body of water.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_BEACH})
         */
        public static final TagKey<Biome> IS_BEACH = cTag("is_beach");
        public static final TagKey<Biome> IS_STONY_SHORES = cTag("is_stony_shores");
        public static final TagKey<Biome> IS_MUSHROOM = cTag("is_mushroom");

        /**
         * Biomes that spawn as a river.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_RIVER})
         */
        public static final TagKey<Biome> IS_RIVER = cTag("is_river");
        /**
         * Biomes that spawn as part of the world's oceans.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_OCEAN})
         */
        public static final TagKey<Biome> IS_OCEAN = cTag("is_ocean");
        /**
         * Biomes that spawn as part of the world's oceans that have low depth.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_DEEP_OCEAN})
         */
        public static final TagKey<Biome> IS_DEEP_OCEAN = cTag("is_deep_ocean");
        public static final TagKey<Biome> IS_SHALLOW_OCEAN = cTag("is_shallow_ocean");

        public static final TagKey<Biome> IS_UNDERGROUND = cTag("is_underground");
        public static final TagKey<Biome> IS_CAVE = cTag("is_cave");

        public static final TagKey<Biome> IS_LUSH = cTag("is_lush");
        public static final TagKey<Biome> IS_MAGICAL = cTag("is_magical");
        public static final TagKey<Biome> IS_RARE = cTag("is_rare");
        public static final TagKey<Biome> IS_PLATEAU = cTag("is_plateau");
        public static final TagKey<Biome> IS_MODIFIED = cTag("is_modified");
        public static final TagKey<Biome> IS_SPOOKY = cTag("is_spooky");
        /**
         * Biomes that lack any natural life or vegetation.
         * (Example, land destroyed and sterilized by nuclear weapons)
         */
        public static final TagKey<Biome> IS_WASTELAND = cTag("is_wasteland");
        /**
         * Biomes whose flora primarily consists of dead or decaying vegetation.
         */
        public static final TagKey<Biome> IS_DEAD = cTag("is_dead");
        /**
         * Biomes with a large amount of flowers.
         */
        public static final TagKey<Biome> IS_FLORAL = cTag("is_floral");
        /**
         * Biomes that are able to spawn sand-based blocks on the surface.
         */
        public static final TagKey<Biome> IS_SANDY = cTag("is_sandy");
        /**
         * For biomes that contains lots of naturally spawned snow.
         * For biomes where lot of ice is present, see {@link #IS_ICY}.
         * Biome with lots of both snow and ice may be in both tags.
         */
        public static final TagKey<Biome> IS_SNOWY = cTag("is_snowy");
        /**
         * For land biomes where ice naturally spawns.
         * For biomes where snow alone spawns, see {@link #IS_SNOWY}.
         */
        public static final TagKey<Biome> IS_ICY = cTag("is_icy");
        /**
         * Biomes consisting primarily of water.
         */
        public static final TagKey<Biome> IS_AQUATIC = cTag("is_aquatic");
        /**
         * For water biomes where ice naturally spawns.
         * For biomes where snow alone spawns, see {@link #IS_SNOWY}.
         */
        public static final TagKey<Biome> IS_AQUATIC_ICY = cTag("is_aquatic_icy");

        /**
         * Biomes that spawn in the Nether.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_NETHER})
         */
        public static final TagKey<Biome> IS_NETHER = cTag("is_nether");
        public static final TagKey<Biome> IS_NETHER_FOREST = cTag("is_nether_forest");

        /**
         * Biomes that spawn in the End.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_END})
         */
        public static final TagKey<Biome> IS_END = cTag("is_end");
        /**
         * Biomes that spawn as part of the large islands outside the center island in The End dimension.
         */
        public static final TagKey<Biome> IS_OUTER_END_ISLAND = cTag("is_outer_end_island");
        //endregion

        //region Redirect fields for improved backward-compatibility
        // TODO: Remove backwards compat redirect fields in 1.22
        /** @deprecated Use {@link #IS_SPARSE_VEGETATION} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_SPARSE = IS_SPARSE_VEGETATION;
        /** @deprecated Use {@link #IS_SPARSE_VEGETATION_OVERWORLD} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_SPARSE_OVERWORLD = IS_SPARSE_VEGETATION_OVERWORLD;
        /** @deprecated Use {@link #IS_SPARSE_VEGETATION_NETHER} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_SPARSE_NETHER = IS_SPARSE_VEGETATION_NETHER;
        /** @deprecated Use {@link #IS_SPARSE_VEGETATION_END} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_SPARSE_END = IS_SPARSE_VEGETATION_END;

        /** @deprecated Use {@link #IS_DENSE_VEGETATION} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_DENSE = IS_DENSE_VEGETATION;
        /** @deprecated Use {@link #IS_DENSE_VEGETATION_OVERWORLD} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_DENSE_OVERWORLD = IS_DENSE_VEGETATION_OVERWORLD;
        /** @deprecated Use {@link #IS_DENSE_VEGETATION_NETHER} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_DENSE_NETHER = IS_DENSE_VEGETATION_NETHER;
        /** @deprecated Use {@link #IS_DENSE_VEGETATION_END} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_DENSE_END = IS_DENSE_VEGETATION_END;

        /** @deprecated Use {@link #IS_AQUATIC} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_WATER = IS_AQUATIC;

        /** @deprecated Use {@link #IS_MOUNTAIN_SLOPE} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_SLOPE = IS_MOUNTAIN_SLOPE;
        /** @deprecated Use {@link #IS_MOUNTAIN_PEAK} instead */
        @Deprecated(forRemoval = true)
        public static final TagKey<Biome> IS_PEAK = IS_MOUNTAIN_PEAK;
        //endregion

        private static TagKey<Biome> cTag(String name) {
            return TagKey.create(Registries.BIOME, new ResourceLocation("c", name));
        }
    }

    public static class Structures {
        private static void init() {}

        //region `c` tags for common conventions
        /**
         * Structures that should not show up on minimaps or world map views from mods/sites.
         * No effect on vanilla map items.
         */
        public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = cTag("hidden_from_displayers");

        /**
         * Structures that should not be locatable/selectable by modded structure-locating items or abilities.
         * No effect on vanilla map items.
         */
        public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = cTag("hidden_from_locator_selection");
        //endregion

        private static TagKey<Structure> cTag(String name) {
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation("c", name));
        }
    }
}
