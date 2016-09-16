package net.bdew.wurm.construction;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.shared.constants.BridgeConstants;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlatformBehaviour implements ModAction, BehaviourProvider {
    private final List<ActionEntry> menuStart, menuExtend;

    public PlatformBehaviour() {
        StartPlatformAction actionWood = new StartPlatformAction(BridgeConstants.BridgeMaterial.WOOD);
        StartPlatformAction actionBrick = new StartPlatformAction(BridgeConstants.BridgeMaterial.BRICK);
        StartPlatformAction actionMarble = new StartPlatformAction(BridgeConstants.BridgeMaterial.MARBLE);

        ModActions.registerAction(actionWood);
        ModActions.registerAction(actionBrick);
        ModActions.registerAction(actionMarble);

        menuStart = new LinkedList<>();
        menuStart.add(new ActionEntry((short) -3, "Start platform", ""));
        menuStart.add(actionWood.getActionEntry());
        menuStart.add(actionBrick.getActionEntry());
        menuStart.add(actionMarble.getActionEntry());


        ExtendPlatformAction extendNorth = new ExtendPlatformAction(TerrainHelper.Direction.NORTH);
        ExtendPlatformAction extendEast = new ExtendPlatformAction(TerrainHelper.Direction.EAST);
        ExtendPlatformAction extendSouth = new ExtendPlatformAction(TerrainHelper.Direction.SOUTH);
        ExtendPlatformAction extendWest = new ExtendPlatformAction(TerrainHelper.Direction.WEST);

        ModActions.registerAction(extendNorth);
        ModActions.registerAction(extendEast);
        ModActions.registerAction(extendSouth);
        ModActions.registerAction(extendWest);

        menuExtend = new LinkedList<>();
        menuExtend.add(new ActionEntry((short) -4, "Extend platform", ""));
        menuExtend.add(extendNorth.getActionEntry());
        menuExtend.add(extendEast.getActionEntry());
        menuExtend.add(extendSouth.getActionEntry());
        menuExtend.add(extendWest.getActionEntry());
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return null;
    }


    @Override
    public List<ActionEntry> getBehavioursFor(Creature aPerformer, Item item, boolean aOnSurface, BridgePart aBridgePart) {
        if (item.getTemplateId() == ItemList.hammerMetal)
            return menuExtend;
        else
            return null;
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset) {
        if (onSurface && border && object.getTemplateId() == ItemList.hammerMetal)
            return menuStart;
        else
            return null;
    }
}
