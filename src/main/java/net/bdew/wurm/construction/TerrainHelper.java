package net.bdew.wurm.construction;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;

public class TerrainHelper {
    public static boolean isTileBorderFlat(int posX, int posY, Direction direction) {
        return getCornerHeightShort(posX, posY, direction.corner1) == getCornerHeightShort(posX, posY, direction.corner2);
    }

    public static float getTileBorderMaxHeight(int posX, int posY, Direction direction) {
        return Math.max(getCornerHeightFloat(posX, posY, direction.corner1), getCornerHeightFloat(posX, posY, direction.corner2));
    }

    public static float getTileBorderMinHeight(int posX, int posY, Direction direction) {
        return Math.min(getCornerHeightFloat(posX, posY, direction.corner1), getCornerHeightFloat(posX, posY, direction.corner2));
    }

    public static float getCornerHeightFloat(int posX, int posY, Corner corner) {
        return Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(posX + corner.xOffs, posY + corner.yOffs));
    }

    public static float getCornerHeightShort(int posX, int posY, Corner corner) {
        return Tiles.decodeHeight(Server.surfaceMesh.getTile(posX + corner.xOffs, posY + corner.yOffs));
    }

    public static byte getBridgeDirection(Direction direction) {
        switch (direction) {
            case NORTH: return 0;
            case WEST: return 2;
            case SOUTH: return 4;
            case EAST: return 6;
            default: return -1;
        }
    }


    public static enum Direction {
        NORTH(0, -1, "North", Corner.NE, Corner.NW),
        EAST(+1, 0, "East", Corner.NE, Corner.SE),
        SOUTH(0, +1, "South", Corner.SE, Corner.SW),
        WEST(-1, 0, "West", Corner.NW, Corner.SW);

        public final int xOffs, yOffs;
        public final String name;
        public final Corner corner1, corner2;

        Direction(int xOffs, int yOffs, String name, Corner corner1, Corner corner2) {
            this.xOffs = xOffs;
            this.yOffs = yOffs;
            this.name = name;
            this.corner1 = corner1;
            this.corner2 = corner2;
        }
    }

    public static enum Corner {
        NW(0, 0, "north west"),
        NE(1, 0, "north east"),
        SE(1, 1, "south east"),
        SW(0, 1, "south west");

        public final int xOffs, yOffs;
        public final String name;

        Corner(int xOffs, int yOffs, String name) {
            this.xOffs = xOffs;
            this.yOffs = yOffs;
            this.name = name;
        }
    }

}
