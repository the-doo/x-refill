package com.doo.xrefill.menu.screen;

import com.doo.xrefill.Refill;
import com.doo.xrefill.config.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.BooleanOption;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final Option ENABLE = new BooleanOption("x_refill.menu.option.enable",
            o -> Refill.option.enable, (o, v) -> Refill.option.enable = v);

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private Screen pre;

    private ButtonListWidget list;

    private ModMenuScreen() {
        super(new LiteralText(Refill.ID));
    }

    @Override
    protected void init() {
        Option[] options = {ENABLE};
        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        // 显示基础高度
        list.addAll(options);
        this.addChild(list);
        // 返回按钮
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
    }

    public static ModMenuScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    private void close() {
        if (client != null) {
            // 返回上个页面
            client.currentScreen = this.pre;
            // save config
            Config.write(Refill.ID, Refill.option);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // background
        super.renderBackground(matrices);
        // button
        list.render(matrices, mouseX, mouseY, delta);
        // other
        super.render(matrices, mouseX, mouseY, delta);
    }
}
