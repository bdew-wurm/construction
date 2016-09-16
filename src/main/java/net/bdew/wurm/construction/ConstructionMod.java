package net.bdew.wurm.construction;

import javassist.ClassPool;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConstructionMod implements WurmServerMod, Initable, PreInitable, Configurable, ServerStartedListener {
    private static final Logger logger = Logger.getLogger("HitchLimitsMod");

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    @Override
    public void configure(Properties properties) {

    }

    @Override
    public void init() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();


        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preInit() {
        ModActions.init();
        logInfo("Init called");
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new PlatformBehaviour());
    }
}
