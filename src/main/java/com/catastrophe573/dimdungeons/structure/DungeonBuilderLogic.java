package com.catastrophe573.dimdungeons.structure;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.catastrophe573.dimdungeons.DimDungeons;

import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

// this class is used by the DungeonChunkGenerator to design dungeons
public class DungeonBuilderLogic
{
    // entrance structures appear once per dungeon as the start room
    protected String[] entrance = { "entrance_1", "entrance_2", "entrance_3", "entrance_4", "entrance_5", "entrance_6" };

    // dead ends contain one door
    protected String end[] = { "deadend_1", "deadend_2", "deadend_3", "deadend_4", "deadend_5", "deadend_6", "deadend_7", "deadend_8", "coffin_1", "advice_room_1", "restroom_1", "shoutout_1", "spawner_1", "redspuzzle_1" };

    // corners contain two doors on adjacent sides
    protected String corner[] = { "corner_1", "corner_2", "corner_3", "corner_4", "corner_5", "corner_6", "corner_7", "corner_8", "redstrap_3" };

    // hallways contain two doors on opposite sides
    protected String hallway[] = { "hallway_1", "hallway_2", "hallway_3", "hallway_4", "hallway_5", "hallway_6", "advice_room_3", "tempt_1", "redstrap_2" };

    // threeways contain three doors and one wall
    protected String threeway[] = { "threeway_1", "threeway_2", "threeway_3", "threeway_4", "threeway_5", "advice_room_2", "redstrap_4" };

    // fourways simply have all four possible doors open
    protected String fourway[] = { "fourway_1", "fourway_2", "fourway_3", "fourway_4", "fourway_5", "fourway_6", "fourway_7", "fourway_8", "fourway_9", "combat_1", "combat_1", "redstrap_1" };

    // an enumeration of the six room types, used internally for randomization and classification
    enum RoomType
    {
	ENTRANCE, END, CORNER, HALLWAY, THREEWAY, FOURWAY, NONE
    };

    // dungeons are a maximum of 8x8 chunks (always much smaller) where each chunk is a structure at a specific rotation
    public class DungeonRoom
    {
	public String structure;
	public Rotation rotation;
	public RoomType type;

	DungeonRoom()
	{
	    structure = "";
	    rotation = Rotation.NONE;
	    type = RoomType.NONE;
	}

	public boolean hasRoom()
	{
	    return type != RoomType.NONE;
	}

	public boolean hasDoorNorth()
	{
	    return type == RoomType.FOURWAY || (type == RoomType.ENTRANCE && rotation != Rotation.CLOCKWISE_180) || (type == RoomType.THREEWAY && rotation != Rotation.NONE) || (type == RoomType.CORNER && rotation == Rotation.NONE)
		    || (type == RoomType.CORNER && rotation == Rotation.COUNTERCLOCKWISE_90) || (type == RoomType.HALLWAY && rotation == Rotation.NONE) || (type == RoomType.HALLWAY && rotation == Rotation.CLOCKWISE_180)
		    || (type == RoomType.END && rotation == Rotation.CLOCKWISE_180);
	}

	public boolean hasDoorSouth()
	{
	    return type == RoomType.FOURWAY || (type == RoomType.ENTRANCE && rotation != Rotation.NONE) || (type == RoomType.THREEWAY && rotation != Rotation.CLOCKWISE_180) || (type == RoomType.CORNER && rotation == Rotation.CLOCKWISE_90)
		    || (type == RoomType.CORNER && rotation == Rotation.CLOCKWISE_180) || (type == RoomType.HALLWAY && rotation == Rotation.NONE) || (type == RoomType.HALLWAY && rotation == Rotation.CLOCKWISE_180)
		    || (type == RoomType.END && rotation == Rotation.NONE);
	}

	public boolean hasDoorWest()
	{
	    return type == RoomType.FOURWAY || (type == RoomType.ENTRANCE && rotation != Rotation.CLOCKWISE_90) || (type == RoomType.THREEWAY && rotation != Rotation.COUNTERCLOCKWISE_90)
		    || (type == RoomType.CORNER && rotation == Rotation.COUNTERCLOCKWISE_90) || (type == RoomType.CORNER && rotation == Rotation.CLOCKWISE_180) || (type == RoomType.HALLWAY && rotation == Rotation.CLOCKWISE_90)
		    || (type == RoomType.HALLWAY && rotation == Rotation.COUNTERCLOCKWISE_90) || (type == RoomType.END && rotation == Rotation.CLOCKWISE_90);
	}

	public boolean hasDoorEast()
	{
	    return type == RoomType.FOURWAY || (type == RoomType.ENTRANCE && rotation != Rotation.COUNTERCLOCKWISE_90) || (type == RoomType.THREEWAY && rotation != Rotation.CLOCKWISE_90) || (type == RoomType.CORNER && rotation == Rotation.NONE)
		    || (type == RoomType.CORNER && rotation == Rotation.CLOCKWISE_90) || (type == RoomType.HALLWAY && rotation == Rotation.CLOCKWISE_90) || (type == RoomType.HALLWAY && rotation == Rotation.COUNTERCLOCKWISE_90)
		    || (type == RoomType.END && rotation == Rotation.COUNTERCLOCKWISE_90);
	}
    };

    // this is the final constructed dungeon
    public DungeonRoom finalLayout[][] = new DungeonRoom[8][8];
    public int enemyVariation1 = 0;
    public int enemyVariation2 = 0;

    // after shuffling the list of structures, these are used to ensure no duplicates
    protected int entranceIndex = 0;
    protected int endIndex = 0;
    protected int cornerIndex = 0;
    protected int hallwayIndex = 0;
    protected int threewayIndex = 0;
    protected int fourwayIndex = 0;

    // this is initialized during the constructor with values from the ChunkGenerator, to ensure the dungeons use the world seed
    protected Random rand;

    public DungeonBuilderLogic(long worldSeed, long chunkX, long chunkZ)
    {
	// copied the seed logic from the vanilla decorate function (which may be flawed, but since I only use the +X/+Z quadrant it won't matter)
	long newSeed = (worldSeed + (long) (chunkX * chunkX * 4987142) + (long) (chunkX * 5947611) + (long) (chunkZ * chunkZ) * 4392871L + (long) (chunkZ * 389711) ^ worldSeed);
	rand = new Random(newSeed);
	//DimDungeons.LOGGER.info("DUNGEON SEED: " + newSeed);
	
	shuffleArray(entrance);
	shuffleArray(end);
	shuffleArray(corner);
	shuffleArray(hallway);
	shuffleArray(threeway);
	shuffleArray(fourway);

	for (int i = 0; i < 8; i++)
	{
	    for (int j = 0; j < 8; j++)
	    {
		finalLayout[i][j] = new DungeonRoom();
	    }
	}
	
	enemyVariation1 = rand.nextInt(3);
	enemyVariation2 = rand.nextInt(3);
    }

    // when this function is done you may read the dungeon layout from the public variable finalLayout
    public void calculateDungeonShape(int maxNumRooms)
    {
	// step 1: place a constant entrance at the center of the bottom row
	placeRoomShape(4, 7, entrance[entranceIndex], RoomType.ENTRANCE, Rotation.NONE);
	entranceIndex++;
	int numRoomsPlaced = 1;

	// step 2: create three openings off of the entrance room
	ArrayList<ImmutablePair<Integer, Integer>> openings = new ArrayList<ImmutablePair<Integer, Integer>>();
	openings.add(new ImmutablePair<Integer, Integer>(3, 7));
	openings.add(new ImmutablePair<Integer, Integer>(5, 7));
	openings.add(new ImmutablePair<Integer, Integer>(4, 6));
	shuffleArray(openings);

	// remaining rooms: for each opening, place a room that fits, and update openings, until no openings are left
	while (openings.size() > 0)
	{
	    ImmutablePair<Integer, Integer> roomPos = openings.remove(0);
	    RoomType nextType = RoomType.END;
	    Rotation nextRot = Rotation.NONE;

	    boolean mustPickEndings = false;
	    boolean noEndingsYet = false;
	    //DimDungeons.LOGGER.info("Processing opening " + roomPos.left + ", " + roomPos.right);
	    
	    // it can happen that an "opening" is in the list twice due to loops, in which case skip this loop
	    if (finalLayout[roomPos.left][roomPos.right].hasRoom())
	    {
		continue;
	    }

	    // if this room has neighbors already then it must connect to them
	    boolean mustConnectNorth = hasOpenDoor(roomPos.left, roomPos.right, Direction.NORTH);
	    boolean mustConnectSouth = hasOpenDoor(roomPos.left, roomPos.right, Direction.SOUTH);
	    boolean mustConnectWest = hasOpenDoor(roomPos.left, roomPos.right, Direction.WEST);
	    boolean mustConnectEast = hasOpenDoor(roomPos.left, roomPos.right, Direction.EAST);

	    // likewise some walls must be acknowledged as mutual too
	    boolean cantConnectNorth = hasSolidWall(roomPos.left, roomPos.right, Direction.NORTH);
	    boolean cantConnectSouth = hasSolidWall(roomPos.left, roomPos.right, Direction.SOUTH);
	    boolean cantConnectWest = hasSolidWall(roomPos.left, roomPos.right, Direction.WEST);
	    boolean cantConnectEast = hasSolidWall(roomPos.left, roomPos.right, Direction.EAST);

	    // this puts the dungeon generation into "end it now" mode, where doors will no longer create more doors
	    if (numRoomsPlaced + openings.size() >= maxNumRooms)
	    {
		mustPickEndings = true;
	    }
	    // this keeps the randomizer from putting dead ends too early in a dungeon path
	    else if (numRoomsPlaced + openings.size() < maxNumRooms / 2)
	    {
		noEndingsYet = true;
	    }

	    // when mustPickEndings is false this is used to hold the list of valid possibilities
	    ArrayList<ImmutablePair<RoomType, Rotation>> roomPossibilities = new ArrayList<ImmutablePair<RoomType, Rotation>>(0);

	    // look at the neighboring doors and pick the room and rotation that connects them without making new openings
	    // TODO: eliminate the redundant half of this IF statement by using mustPickEndings to abort the roomPossibilities[] check at the end
	    if (mustPickEndings)
	    {
		// this case should be impossible
		if (mustConnectNorth && mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.FOURWAY;
		}

		// forced threeway
		if (!mustConnectNorth && mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		}
		if (mustConnectNorth && !mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.CLOCKWISE_180;
		}
		if (mustConnectNorth && mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;
		}
		if (mustConnectNorth && mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.CLOCKWISE_90;
		}

		// forced corner
		if (mustConnectNorth && !mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		}
		if (!mustConnectNorth && mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.CLOCKWISE_90;
		}
		if (!mustConnectNorth && mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.CLOCKWISE_180;
		}
		if (mustConnectNorth && !mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;
		}

		// forced hallway is really rare but whatever lets code it for consistency
		if (mustConnectNorth && mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.HALLWAY;
		}
		if (!mustConnectNorth && !mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.HALLWAY;
		    nextRot = Rotation.CLOCKWISE_90;
		}

		// forced dead end is most common
		if (mustConnectNorth && !mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.CLOCKWISE_180;
		}
		if (!mustConnectNorth && mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		}
		if (!mustConnectNorth && !mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.CLOCKWISE_90;
		}
		if (!mustConnectNorth && !mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;
		}
	    }
	    else
	    {
		if (mustConnectNorth && mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.FOURWAY;
		}

		// three doors are required, so place either a threeway or a fourway
		if (!mustConnectNorth && mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    if (noEndingsYet && !cantConnectNorth)
		    {
			nextType = RoomType.FOURWAY;
		    }
		}
		if (mustConnectNorth && !mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.CLOCKWISE_180;
		    if (noEndingsYet && !cantConnectSouth)
		    {
			nextType = RoomType.FOURWAY;
		    }
		}
		if (mustConnectNorth && mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;
		    if (noEndingsYet && !cantConnectWest)
		    {
			nextType = RoomType.FOURWAY;
		    }
		}
		if (mustConnectNorth && mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.THREEWAY;
		    nextRot = Rotation.CLOCKWISE_90;
		    if (noEndingsYet && !cantConnectEast)
		    {
			nextType = RoomType.FOURWAY;
		    }
		}

		// forced corner, other two walls may or may not be open
		if (mustConnectNorth && !mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.CORNER;

		    // can we make this a 3way or 4way?
		    if (!cantConnectSouth && !cantConnectWest)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectSouth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if (!cantConnectWest)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		}
		if (!mustConnectNorth && mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.CLOCKWISE_90;

		    // can we make this a 3way or 4way?
		    if (!cantConnectNorth && !cantConnectWest)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectNorth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if (!cantConnectWest)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		}
		if (!mustConnectNorth && mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.CLOCKWISE_180;

		    // can we make this a 3way or 4way?
		    if (!cantConnectNorth && !cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectNorth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if (!cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		}
		if (mustConnectNorth && !mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.CORNER;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;

		    // can we make this a 3way or 4way?
		    if (!cantConnectSouth && !cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectSouth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if (!cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		}

		// forced hallway, but can we make it a 3way or 4way?
		if (mustConnectNorth && mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.HALLWAY;

		    if (!cantConnectWest && !cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectWest)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if (!cantConnectEast)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		}
		if (!mustConnectNorth && !mustConnectSouth && mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.HALLWAY;
		    nextRot = Rotation.CLOCKWISE_90;

		    if (!cantConnectNorth && !cantConnectSouth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if (!cantConnectNorth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		    if (!cantConnectSouth)
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		}

		// one doorway is required, but can we add 2-4 more doorways here?
		if (mustConnectNorth && !mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.CLOCKWISE_180;
		    
		    // try for 4ways, then 3ways
		    if ( !cantConnectSouth && !cantConnectWest && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if ( !cantConnectSouth && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if ( !cantConnectSouth && !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectWest && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		    
		    // try for 2ways too
		    if ( !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.NONE));
		    }
		    if ( !cantConnectSouth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.HALLWAY, Rotation.NONE));
		    }
		    if ( !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.COUNTERCLOCKWISE_90));
		    }
		}
		if (!mustConnectNorth && mustConnectSouth && !mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		    
		    // try for 4ways, then 3ways
		    if ( !cantConnectNorth && !cantConnectWest && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if ( !cantConnectNorth && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if ( !cantConnectNorth && !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectWest && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		    
		    // try for 2ways too
		    if ( !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectNorth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.HALLWAY, Rotation.NONE));
		    }
		    if ( !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.CLOCKWISE_180));
		    }		    
		}
		if (!mustConnectNorth && !mustConnectSouth && mustConnectWest && !mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.CLOCKWISE_90;
		    
		    // try for 4ways, then 3ways
		    if ( !cantConnectNorth && !cantConnectSouth && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if ( !cantConnectNorth && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		    if ( !cantConnectNorth && !cantConnectSouth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectSouth && !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		    
		    // try for 2ways too
		    if ( !cantConnectEast )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.HALLWAY, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectNorth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if ( !cantConnectSouth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.CLOCKWISE_180));
		    }		    
		}
		if (!mustConnectNorth && !mustConnectSouth && !mustConnectWest && mustConnectEast)
		{
		    nextType = RoomType.END;
		    nextRot = Rotation.COUNTERCLOCKWISE_90;
		    
		    // try for 4ways, then 3ways
		    if ( !cantConnectNorth && !cantConnectSouth && !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.FOURWAY, Rotation.NONE));
		    }
		    if ( !cantConnectNorth && !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.CLOCKWISE_180));
		    }
		    if ( !cantConnectNorth && !cantConnectSouth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.COUNTERCLOCKWISE_90));
		    }
		    if ( !cantConnectSouth && !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.THREEWAY, Rotation.NONE));
		    }
		    
		    // try for 2ways too
		    if ( !cantConnectWest )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.HALLWAY, Rotation.CLOCKWISE_90));
		    }
		    if ( !cantConnectNorth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.NONE));
		    }
		    if ( !cantConnectSouth )
		    {
			roomPossibilities.add(new ImmutablePair<RoomType, Rotation>(RoomType.CORNER, Rotation.CLOCKWISE_90));
		    }
		}
	    }
	    
	    // if the previous block of code populated the roomPossibilities array then make a selection from it and use it instead
	    // this code path represents more doorways being added and branching paths
	    if ( roomPossibilities.size() > 0 && !mustPickEndings )
	    {
		shuffleRoomPossibilities(roomPossibilities);
		nextType = roomPossibilities.get(0).left;
		nextRot = roomPossibilities.get(0).right;
	    }

	    // get the next random structure of this type and put it in there
	    String nextRoom = "";
	    if (nextType == RoomType.FOURWAY)
	    {
		nextRoom = fourway[fourwayIndex];
		if (nextRoom == "combat_1")
		{
		    // the combat room appears at most twice per dungeon, and there are 5 variations of it
		    int variation = rand.nextInt(5) + 1;
		    nextRoom = nextRoom.replace("1", "" + variation);
		}
		fourwayIndex = fourwayIndex == fourway.length - 1 ? 0 : fourwayIndex + 1;
	    }
	    if (nextType == RoomType.THREEWAY)
	    {
		nextRoom = threeway[threewayIndex];
		threewayIndex = threewayIndex == threeway.length - 1 ? 0 : threewayIndex + 1;
	    }
	    if (nextType == RoomType.HALLWAY)
	    {
		nextRoom = hallway[hallwayIndex];
		if (nextRoom == "tempt_1")
		{
		    // the chest chance room appears at most once per dungeon, and there are 4 variations of it
		    int variation = rand.nextInt(4) + 1;
		    nextRoom = nextRoom.replace("1", "" + variation);
		}		
		hallwayIndex = hallwayIndex == hallway.length - 1 ? 0 : hallwayIndex + 1;
	    }
	    if (nextType == RoomType.CORNER)
	    {
		nextRoom = corner[cornerIndex];
		cornerIndex = cornerIndex == corner.length - 1 ? 0 : cornerIndex + 1;
	    }
	    if (nextType == RoomType.END)
	    {
		nextRoom = end[endIndex];
		if (nextRoom == "coffin_1")
		{
		    // the coffin room appears at most once per dungeon, and there are 5 variations of it
		    int variation = rand.nextInt(5) + 1;
		    variation = 2; // TODO: q
		    nextRoom = nextRoom.replace("1", "" + variation);
		}
		if (nextRoom == "restroom_1")
		{
		    // the break room appears at most once per dungeon, and there are 5 variations of it
		    int variation = rand.nextInt(5) + 1;
		    variation = 1; // TODO: q
		    nextRoom = nextRoom.replace("1", "" + variation);
		}
		if (nextRoom == "shoutout_1")
		{
		    // the reference to other mods room appears at most once per dungeon, and there are 2 variations of it
		    int variation = rand.nextInt(2) + 1;
		    nextRoom = nextRoom.replace("1", "" + variation);
		}
		if (nextRoom == "redspuzzle_1")
		{
		    // the puzzle/reward room appears at most once per dungeon, and there are 5 variations of it (4 and 5 are intentional duplicates)
		    int variation = rand.nextInt(5) + 1;
		    nextRoom = nextRoom.replace("1", "" + variation);
		}		
		if (nextRoom == "spawner_1")
		{
		    // the spawner room appears at most once per dungeon, and there are 6 weighted variations of it
		    int variation = rand.nextInt(8) + 1;
		    if ( variation < 6 )
		    {
			nextRoom = nextRoom.replace("1", "" + variation);			
		    }
		    else
		    {
			nextRoom = nextRoom.replace("1", "6");			
		    }
		}
		endIndex = endIndex == end.length - 1 ? 0 : endIndex + 1;
	    }
	    
	    // commit the room to the blueprint and open any potential new doors for the next loop to work with
	    int roomX = roomPos.left;
	    int roomZ = roomPos.right;
	    placeRoomShape(roomX, roomZ, nextRoom, nextType, nextRot);
	    numRoomsPlaced++;
	    if ( hasOpenDoor(roomX-1, roomZ, Direction.EAST) && !finalLayout[roomX-1][roomZ].hasRoom() )
	    {
		openings.add(new ImmutablePair<Integer, Integer>(roomX-1, roomZ));
		//DimDungeons.LOGGER.info("Adding opening " + (roomX-1) + ", " + roomZ);
	    }
	    if ( hasOpenDoor(roomX+1, roomZ, Direction.WEST) && !finalLayout[roomX+1][roomZ].hasRoom() )
	    {
		openings.add(new ImmutablePair<Integer, Integer>(roomX+1, roomZ));
		//DimDungeons.LOGGER.info("Adding opening " + (roomX+1) + ", " + roomZ);
	    }
	    if ( hasOpenDoor(roomX, roomZ-1, Direction.SOUTH) && !finalLayout[roomX][roomZ-1].hasRoom())
	    {
		openings.add(new ImmutablePair<Integer, Integer>(roomX, roomZ-1));
		//DimDungeons.LOGGER.info("Adding opening " + roomX + ", " + (roomZ-1));
	    }
	    if ( hasOpenDoor(roomX, roomZ+1, Direction.NORTH) && !finalLayout[roomX][roomZ+1].hasRoom() )
	    {
		openings.add(new ImmutablePair<Integer, Integer>(roomX, roomZ+1));
		//DimDungeons.LOGGER.info("Adding opening " + roomX + ", " + (roomZ+1));
	    }

	    // next loop - reshuffle the openings because we may have added some
	    shuffleArray(openings);
	    //DimDungeons.LOGGER.info("Num openings: " + openings.size());
	}
    }

    // returns true if another chunk has a door leading into this chunk from the specified direction
    // safe to call with x or y that are out of bounds
    private boolean hasOpenDoor(int x, int z, Direction direction)
    {
	if (x < 0 || z < 0 || x > 7 || z > 7)
	{
	    return false;
	}
	if (direction == Direction.NORTH && z > 0)
	{
	    return finalLayout[x][z - 1].hasDoorSouth();
	}
	if (direction == Direction.SOUTH && z < 7)
	{
	    return finalLayout[x][z + 1].hasDoorNorth();
	}
	if (direction == Direction.WEST && x > 0)
	{
	    return finalLayout[x - 1][z].hasDoorEast();
	}
	if (direction == Direction.EAST && x < 7)
	{
	    return finalLayout[x + 1][z].hasDoorWest();
	}
	return false;
    }

    // returns true if the neighbor in this direction has placed a solid wall, and false if there is a door or no neighbor yet at all
    // safe to call with x or y that are out of bounds
    private boolean hasSolidWall(int x, int z, Direction direction)
    {
	if (x < 0 || z < 0 || x > 7 || z > 7)
	{
	    return true;
	}
	if (direction == Direction.NORTH && z > 0)
	{
	    return !finalLayout[x][z - 1].hasDoorSouth() && finalLayout[x][z - 1].hasRoom();
	}
	if (direction == Direction.SOUTH && z < 7)
	{
	    return !finalLayout[x][z + 1].hasDoorNorth() && finalLayout[x][z + 1].hasRoom();
	}
	if (direction == Direction.WEST && x > 0)
	{
	    return !finalLayout[x - 1][z].hasDoorEast() && finalLayout[x - 1][z].hasRoom();
	}
	if (direction == Direction.EAST && x < 7)
	{
	    return !finalLayout[x + 1][z].hasDoorWest() && finalLayout[x + 1][z].hasRoom();
	}
	return true;
    }

    private void placeRoomShape(int x, int z, String room, RoomType type, Rotation rot)
    {
	finalLayout[x][z].structure = room;
	finalLayout[x][z].type = type;
	finalLayout[x][z].rotation = rot;
	//System.out.println("Put a " + type.toString() + " at (" + x + ", " + z + ") with rotation " + rot.toString() + ".");
	//DimDungeons.LOGGER.info("Put a " + room + " at (" + x + ", " + z + ") with rotation " + rot.toString() + ".");
    }

    // Implementing Fisher�Yates shuffle
    private void shuffleArray(String[] array)
    {
	for (int i = array.length - 1; i > 0; i--)
	{
	    int index = rand.nextInt(i + 1);

	    // Simple swap
	    String temp = array[index];
	    array[index] = array[i];
	    array[i] = temp;
	}
    }
    
    // TODO: MAJOR TODO MAKE SURE THIS WORKS AS INTENDED
    private void shuffleArray(ArrayList<ImmutablePair<Integer, Integer>> array)
    {
	/*
	for (int i = array.size() - 1; i > 0; i--)
	{
	    int index = rand.nextInt(i + 1);

	    ImmutablePair<Integer, Integer> temp = array.get(index);
	    array.set(index, array.get(i));
	    array.set(i, temp);
	}
	*/
    }       
    
    private void shuffleRoomPossibilities(ArrayList<ImmutablePair<RoomType, Rotation>> array)
    {
	/*
	for (int i = array.size() - 1; i > 0; i--)
	{
	    int index = rand.nextInt(i + 1);

	    // Simple swap
	    ImmutablePair<RoomType, Rotation> temp = array.get(index);
	    array.set(index, array.get(i));
	    array.set(i, temp);
	}
	*/
    }           
}
