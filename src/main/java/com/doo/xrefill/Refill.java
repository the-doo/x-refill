package com.doo.xrefill;

import com.doo.xrefill.config.Config;
import com.doo.xrefill.config.Option;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 加载mod
 */
@Environment(EnvType.CLIENT)
public class Refill implements ClientModInitializer {

    public static final String ID = "xrefill";

    public static final Logger LOGGER = LogManager.getLogger();

    public static Option option = new Option();

    @Override
    public void onInitializeClient() {
        // 加载设置
        option = Config.read(ID, Option.class, option);
    }
}
