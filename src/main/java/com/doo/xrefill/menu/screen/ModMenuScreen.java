package com.doo.xrefill.menu.screen;

import com.doo.xrefill.Refill;
import com.doo.xrefill.config.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final Option ENABLE =
            CyclingOption.create("x_refill.menu.option.enable",
                    o -> Refill.option.enable, (g, o, v) -> Refill.option.enable = v);
//            new BooleanOption("x_refill.menu.option.enable",
//                    o -> Refill.option.enable, (g, v) -> Refill.option.enable = v);

    private static final Option DELAY = new DoubleOption("x_refill.menu.option.delay", 0, 1000, 1,
            g -> (double) Refill.option.delay, (g, v) -> Refill.option.delay = v.longValue(),
            (g, o) -> new TranslatableText("x_refill.menu.option.delay", Refill.option.delay));

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private Screen pre;

    private ButtonListWidget list;

    private ModMenuScreen() {
        super(new LiteralText(Refill.ID));
    }

    @Override
    protected void init() {
        Option[] options = {ENABLE, DELAY};
        list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

        list.addAll(options);

//        this.addChild(list);
//        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
//                ScreenTexts.BACK, b -> INSTANCE.close()));
        this.addSelectableChild(list);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
    }

    public static ModMenuScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    public void close() {
        if (client != null) {
            client.currentScreen = this.pre;
            Config.write(Refill.ID, Refill.option);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
