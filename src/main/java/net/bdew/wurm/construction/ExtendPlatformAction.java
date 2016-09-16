package net.bdew.wurm.construction;

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

public class ExtendPlatformAction implements ModAction, ActionPerformer, BehaviourProvider {
    private final ActionEntry actionEntry;
    private final short actionId;
    private final TerrainHelper.Direction direction;

    public ExtendPlatformAction(TerrainHelper.Direction direction) {
        this.direction = direction;
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, direction.name, "building", new int[]{
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
    public boolean action(Action act, Creature aPerformer, Item item, boolean onSurface, BridgePart aBridgePart, int encodedTile, short action, float counter) {
        try {
            Communicator comm = aPerformer.getCommunicator();

            if (!onSurface) {
                comm.sendNormalServerMessage("You can't build platforms in caves.", (byte) 3);
                return true;
            }

            int sourceX = aBridgePart.getTileX();
            int sourceY = aBridgePart.getTileY();
            int height = aBridgePart.getRealHeight();
            BridgeConstants.BridgeMaterial material = aBridgePart.getMaterial();

            if (TerrainHelper.getTileBorderMinHeight(sourceX, sourceY, direction) * 10 >= height) {
                comm.sendNormalServerMessage("You can't extend the platform in that direction.", (byte) 3);
                return true;
            }

            int tileX = sourceX + direction.xOffs;
            int tileY = sourceY + direction.yOffs;
            VolaTile tile = Zones.getOrCreateTile(tileX, tileY, true);

            comm.sendNormalServerMessage(String.format("Extending %s platform to the %s...", material.getName(), direction.name));

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

            int ne = (int) TerrainHelper.getCornerHeightShort(tileX, tileY, TerrainHelper.Corner.NE);
            int nw = (int) TerrainHelper.getCornerHeightShort(tileX, tileY, TerrainHelper.Corner.NW);
            int se = (int) TerrainHelper.getCornerHeightShort(tileX, tileY, TerrainHelper.Corner.SE);
            int sw = (int) TerrainHelper.getCornerHeightShort(tileX, tileY, TerrainHelper.Corner.SW);

            if (ne > height || nw > height || se > height || sw > height) {
                comm.sendNormalServerMessage("The platform would collide with terrain.", (byte) 3);
                return true;
            }

            Byte dir = aBridgePart.getDir();
            BridgeConstants.BridgeType type = BridgeConstants.BridgeType.FLOATING_CENTER;

            if (material == BridgeConstants.BridgeMaterial.WOOD) {
                if (TerrainHelper.getBridgeDirection(direction) != dir) {
                    comm.sendNormalServerMessage("Wooden platforms can't change direction for now.", (byte) 3);
                    return true;
                }
                if (TerrainHelper.isTileBorderFlat(tileX, tileY, direction) && TerrainHelper.getTileBorderMaxHeight(tileX, tileY, direction) * 10 == height) {
                    type = BridgeConstants.BridgeType.ABUTMENT_NARROW;
                    dir = (byte) ((dir + 4) % 8);
                } else {
                    type = BridgeConstants.BridgeType.CROWN_NARROW;
                }
            } else {
                if (ne == height && nw == height) {
                    dir = TerrainHelper.getBridgeDirection(TerrainHelper.Direction.SOUTH);
                    type = BridgeConstants.BridgeType.BRACING_CENTER;
                } else if (se == height && sw == height) {
                    dir = TerrainHelper.getBridgeDirection(TerrainHelper.Direction.NORTH);
                    type = BridgeConstants.BridgeType.BRACING_CENTER;
                } else if (ne == height && se == height) {
                    dir = TerrainHelper.getBridgeDirection(TerrainHelper.Direction.WEST);
                    type = BridgeConstants.BridgeType.BRACING_CENTER;
                } else if (nw == height && sw == height) {
                    dir = TerrainHelper.getBridgeDirection(TerrainHelper.Direction.EAST);
                    type = BridgeConstants.BridgeType.BRACING_CENTER;
                }
            }

            Structure structure = Structures.getStructure(aBridgePart.getStructureId());
            structure.addBuildTile(tile, false);
            tile.addBridge(structure);

            BridgePart bridgePart = new DbBridgePart(type, tileX, tileY, height, 1.0f, structure.getWurmId(), material, dir, (byte) 0, 1, 1, 1, 1);
            tile.addBridgePart(bridgePart);

            return true;
        } catch (Exception e) {
            ConstructionMod.logException("Start platform error", e);
            return true;
        }
    }
}
