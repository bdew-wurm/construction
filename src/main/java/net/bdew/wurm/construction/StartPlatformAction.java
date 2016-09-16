package net.bdew.wurm.construction;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbBridgePart;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class StartPlatformAction implements ModAction, ActionPerformer, BehaviourProvider {
    private final ActionEntry actionEntry;
    private final short actionId;
    private final BridgeConstants.BridgeMaterial material;

    public StartPlatformAction(BridgeConstants.BridgeMaterial material) {
        this.material = material;
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, material.getName(), "building", new int[]{
                6 /* ACTION_TYPE_NOMOVE */,
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return this;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    public ActionEntry getActionEntry() {
        return actionEntry;
    }

    @Override
    public boolean action(Action aAct, Creature aPerformer, Item aSource, int aTilex, int aTiley, boolean onSurface, int aHeightOffset, Tiles.TileBorderDirection aDir, long borderId, short aAction, float aCounter) {
        try {
            Communicator comm = aPerformer.getCommunicator();

            if (!onSurface) {
                comm.sendNormalServerMessage("You can't build platforms in caves.", (byte) 3);
                return true;
            }

            int playerX = aPerformer.getTileX();
            int playerY = aPerformer.getTileY();

            TerrainHelper.Direction direction;

            if (aTilex == playerX && aTiley == playerY && aDir == Tiles.TileBorderDirection.DIR_HORIZ) {
                direction = TerrainHelper.Direction.NORTH;
            } else if (aTilex == playerX && aTiley == playerY && aDir == Tiles.TileBorderDirection.DIR_DOWN) {
                direction = TerrainHelper.Direction.WEST;
            } else if (aTilex == playerX && aTiley == playerY + 1 && aDir == Tiles.TileBorderDirection.DIR_HORIZ) {
                direction = TerrainHelper.Direction.SOUTH;
            } else if (aTilex == playerX + 1 && aTiley == playerY && aDir == Tiles.TileBorderDirection.DIR_DOWN) {
                direction = TerrainHelper.Direction.EAST;
            } else {
                comm.sendNormalServerMessage("You are too far away to start the platform.", (byte) 3);
                return true;
            }

            comm.sendNormalServerMessage(String.format("Starting %s platform to the %s...", material.getName(), direction.name));

            if (!TerrainHelper.isTileBorderFlat(playerX, playerY, direction)) {
                comm.sendNormalServerMessage("The starting tile border must be flat.", (byte) 3);
                return true;
            }

            float height = TerrainHelper.getCornerHeightFloat(playerX, playerY, direction.corner1);
            int tileX = playerX + direction.xOffs;
            int tileY = playerY + direction.yOffs;

            if (TerrainHelper.getTileBorderMaxHeight(tileX, tileY, direction) >= height) {
                comm.sendNormalServerMessage("The platform would collide with terrain.", (byte) 3);
                return true;
            }

            VolaTile tile = Zones.getOrCreateTile(tileX, tileY, true);

            if (tile.getStructure() != null) {
                comm.sendNormalServerMessage("Platform can't overlap other structures.", (byte) 3);
                return true;
            }

            if (tile.getVillage() != null) {
                Village village = tile.getVillage();
                VillageRole role = village.getRoleFor(aPerformer);
                if (role == null || role.mayBuild()) {
                    comm.sendNormalServerMessage("You aren't allowed to build here", (byte) 3);
                    return true;
                }
            }

            Structure structure = Structures.createStructure((byte) 1, "Ramp", WurmId.getNextPlanId(), tileX, tileY, true);
            structure.makeFinal(aPerformer, "Ramp");
            tile.addBridge(structure);

            BridgeConstants.BridgeType type;
            if (material == BridgeConstants.BridgeMaterial.WOOD)
                type = BridgeConstants.BridgeType.ABUTMENT_NARROW;
            else
                type = BridgeConstants.BridgeType.BRACING_CENTER;

            BridgePart bridgePart = new DbBridgePart(type, tileX, tileY, (int) (height * 10), 1.0f, structure.getWurmId(), material, TerrainHelper.getBridgeDirection(direction), (byte) 0, 1, 1, 1, 1);
            tile.addBridgePart(bridgePart);

            return true;
        } catch (Exception e) {
            ConstructionMod.logException("Start platform error", e);
            return true;
        }
    }
}
