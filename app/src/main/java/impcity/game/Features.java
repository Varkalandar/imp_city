package impcity.game;

import impcity.game.map.Map;
import java.util.HashSet;
import java.util.function.IntUnaryOperator;

/**
 *
 * @author Hj. Malthaner
 */
public class Features
{
    public static final int GROUND_SAND = 7;
    public static final int GROUND_IMPASSABLE = 10;
    public static final int GROUND_POLY_TILES_DARK = 13;
    public static final int GROUND_POLY_TILES = 16;
    public static final int GROUND_QUAD_TILES = 64;
    public static final int GROUND_GRASS_DARK = 19;   // farmland

    /**
     * Initially all floors under earth or deposits are GROUND_IMPASSABLE. After
     * taking a mining or excavation job, the floor is changed to GROUND_LIGHT_SOIL
     * to make it suitable for imps to find a path into this place.
     */
    public static final int GROUND_LIGHT_SOIL = 28;
    public static final int GROUND_STAINED_MARBLE = 31;
    public static final int GROUND_WATER = 34;
    public static final int GROUND_TREASURY = 40;
    public static final int GROUND_FORGE = 43;
    public static final int GROUND_LAIR = 46;
    public static final int GROUND_LIBRARY = 49;
    public static final int GROUND_HOSPITAL = 52;
    public static final int GROUND_LABORATORY = 58;
    public static final int GROUND_GHOSTYARD = 79;
    
    public static final int SOCKET = 7;
    public static final int SHADOW_BASE = 15;
    public static final int CURSOR_HAND = 25;
    
    public static final int MINING_MARK = 976;

    public static final int BUBBLE_FOOD = 996;
    public static final int BUBBLE_WORK = 997;
    public static final int BUBBLE_SLEEPING = 998;
    public static final int BUBBLE_GO_SLEEPING = 999;
    public static final int BUBBLE_GO_WATER = 1000;


    public static final int I_MUSHROOM = 68;
    public static final int I_MOSSY_PATCH = 92;
    
    public static final int I_SMALL_MOSSY_PATCH = 108;
    public static final int I_SMALL_DUST_PATCH = 109;
    public static final int I_SMALL_DIRT_PATCH = 110;
    public static final int I_TINY_SHRUBS = 111;
    public static final int I_SMALL_DEPRESSION = 112;
    public static final int I_WET_AREA = 113;
    
    // public static final int I_BRONZE_COINS = 2131;
    public static final int I_COPPER_COINS = 200;
    public static final int I_SILVER_COINS = 192;
    public static final int I_GOLD_COINS = 194;
    
    public static final int I_COPPER_ORE = 1115;
    public static final int I_TIN_ORE = 1116;
    public static final int I_MINERAL = 1117;

    public static final int I_STEAM_CLOUD = 1948;
    public static final int I_STEAM_PUFF = 1949;

    public static final int I_GRAVE = 1464;
    public static final int I_LAB_TABLE = 1465;
    public static final int I_ANVIL = 1468;
    
    public static final int I_TORCH_STAND = 1448;
    public static final int I_BOOKSHELF_HALF_RIGHT = 1469;
    public static final int I_BOOKSHELF_RIGHT = 1470;
    
    public static final int I_HEALING_WELL_2 = 1461;
    public static final int I_HEALING_WELL_1 = 1462;
    public static final int I_WELL = 1476;
    public static final int I_PERM_ROCK = 1482;
    public static final int I_EARTH_BLOCK = 1485;
    public static final int I_TUNNEL_PORTAL = 1479;
    public static final int I_TREASURE_BLOCK = 1488;
    public static final int I_GOLD_MOUND = 1491;
    public static final int I_SMALL_VOLCANO = 1494;
    public static final int I_EARTH_MOUND = 1495;
    public static final int I_COPPER_ORE_MOUND = 1498;
    public static final int I_TIN_ORE_MOUND = 1501;
    public static final int I_STEEP_EARTH_BLOCK = 1504;
    public static final int I_MINERAL_BLOCK = 1508;
    public static final int I_GRAPHITE_BLOCK = 1510;
    public static final int I_IRON_ORE_BLOCK = 1513;

    public static final int I_FRAME_RIGHT = 26;
    public static final int I_FRAME_BOT = 27;
    public static final int I_FRAME_LEFT = 28;
    public static final int I_FRAME_TOP = 29;

    public static final int I_EXPEDITION_BANNER = 141;
    
    public static final int MESSAGE_IDEA_BLUE = 953;
    public static final int MESSAGE_IDEA_GREEN = 954;
    public static final int MESSAGE_IDEA_RED = 955;
    public static final int MESSAGE_ARTIFACT_QUEST = 956;
    public static final int MESSAGE_TROPHY_QUEST = 957;
    public static final int MESSAGE_TROPHY_RESULT = 958;
    
    public static final int P_FROST_SPRITE_1 = 1941;
    
    public static final int P_SPELL_CLOUD_1 = 1944;
    public static final int P_SPELL_CLOUD_2 = 1945;
    public static final int P_SPELL_SPARK_1 = 1946;
    
    public static final int P_SPLASH_EFFECT_1 = 1959;
    
    public static final int P_SILVER_SPARK_1 = 1964;
    public static final int P_BLUE_SPARK_1 = 1965;
    public static final int P_BROWN_SHARD_1 = 1994;
    public static final int P_ORANGE_SPARK_1 = 1998;
    
    
    public static final int PLANTS_FIRST = 116;
    public static final int PLANTS_FIRST_OLD = 152;
    public static final int PLANTS_LAST = 158;
    public static final int PLANTS_STRIDE = 9;  // Beware, only 7 plant types, 2 unused!

    public static final int ARTIFACTS_FIRST = 1208;

    public static final int ARTIFACT_DRIED_FROG = 1209;
    public static final int ARTIFACT_CARVED_PUMPKIN = 1210;
    public static final int ARTIFACT_MUG = 1211;
    public static final int ARTIFACT_CAT_MUMMY = 1212;
    public static final int ARTIFACT_URN = 1213;
    public static final int ARTIFACT_PRESERVED_TOE = 1214;
    public static final int ARTIFACT_GIANT_EGG = 1215;
    public static final int ARTIFACT_RABBIT_PAW = 1216;
    public static final int ARTIFACT_SHOES = 1217;
    public static final int ARTIFACT_GOAT_SKIN = 1218;
    public static final int ARTIFACT_PETRIFIED_BONES= 1219;
    public static final int ARTIFACT_LINEN_CLOTH = 1220;

    public static final int ARTIFACTS_LAST = 1220;


    public static final int GLYPHS_FIRST = 1975;
    public static final int GLYPHS_LAST = 1990;
    public static final int GLYPHS_COUNT = GLYPHS_LAST - GLYPHS_FIRST + 1;
    
    public static final int [] DUSTS = new int [] 
    {
        I_SMALL_MOSSY_PATCH, I_SMALL_DUST_PATCH, I_SMALL_DIRT_PATCH, 
        I_TINY_SHRUBS, I_SMALL_DEPRESSION, I_WET_AREA
    };
    
    public static final HashSet<Integer> DUST_SET;
    
    
    /** 
     * This filter keeps coins, resources and artifacts
     */
    public static final IntUnaryOperator keepTreasureFilter =
            (int item) -> (isCoins(item) || isResource(item) || (item & Map.F_ITEM) != 0) ? item : 0;
    
    
    static
    {
        DUST_SET = new HashSet<>();

        for(int dust : DUSTS)
        {
            DUST_SET.add(dust);
        }
    }
    
    
    public static boolean isCoins(int item)
    {
        return 
            item == Features.I_GOLD_COINS ||
            item == Features.I_SILVER_COINS ||
            item == Features.I_COPPER_COINS;
    }

    
    public static boolean isResource(int item)
    {
        return 
            item == Features.I_COPPER_ORE ||
            item == Features.I_TIN_ORE ||
            item == Features.I_MINERAL;
    }

    
    public static boolean isImpassable(int ground) 
    {
        return ground >= GROUND_IMPASSABLE && ground < GROUND_IMPASSABLE + 3;
    }

    
    public static boolean isEarth(int item) 
    {
        return item >= I_EARTH_BLOCK && item < I_EARTH_BLOCK + 3;
    }

    
    public static boolean canBeMined(int ground, int block)
    {
        return ((block >=  Features.I_GOLD_MOUND && block < Features.I_GOLD_MOUND + 3) ||
                (block ==  Features.I_MINERAL_BLOCK) ||
                (block >=  Features.I_COPPER_ORE_MOUND && block < Features.I_COPPER_ORE_MOUND + 3) ||
                (block >=  Features.I_TIN_ORE_MOUND && block < Features.I_TIN_ORE_MOUND + 3))
                &&
               ((ground >= Features.GROUND_LIGHT_SOIL && ground <= Features.GROUND_LIGHT_SOIL+3) ||
                (ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE + 3));
    }

    
    public static boolean canBeDug(int ground, int block) {
        return (block >= Features.I_STEEP_EARTH_BLOCK && block < Features.I_STEEP_EARTH_BLOCK + 3) &&
               ((ground >= Features.GROUND_LIGHT_SOIL && ground <= Features.GROUND_LIGHT_SOIL+3) ||
                (ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE + 3));
    }
}
