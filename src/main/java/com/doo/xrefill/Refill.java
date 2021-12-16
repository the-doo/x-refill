package com.doo.xrefill;

import com.doo.xrefill.config.Config;
import com.doo.xrefill.config.Option;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 加载mod
 */
public class Refill implements ModInitializer {

    public static final String ID = "xrefill";

    public static final Logger LOGGER = LogManager.getLogger();

    public static Option option = new Option();

    @Override
    public void onInitialize() {
        // 加载设置
        option = Config.read(ID, Option.class, option);
    }
}
