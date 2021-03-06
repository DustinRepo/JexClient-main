package me.dustin.jex.gui.minecraft;

import me.dustin.jex.addon.Addon;
import me.dustin.jex.feature.command.CommandManager;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.helper.file.files.ClientSettingsFile;
import me.dustin.jex.gui.minecraft.blocklist.SearchSelectScreen;
import me.dustin.jex.gui.minecraft.blocklist.XraySelectScreen;
import me.dustin.jex.gui.waypoints.WaypointScreen;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.update.Update;
import me.dustin.jex.helper.update.UpdateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;

public class JexOptionsScreen extends Screen {

    private TextFieldWidget prefixField;
    private ButtonWidget setPrefixButton;
    private ButtonWidget clickGuiButton;
    private ButtonWidget downloadInstallerButton;
    private ButtonWidget xrayButton;
    private ButtonWidget searchButton;
    private ButtonWidget waypointScreenButton;
    private ButtonWidget reloadAddonsButton;
    private static Timer timer = new Timer();
    private boolean updating = false;
    public JexOptionsScreen() {
        super(new LiteralText("Jex Client"));
    }

    @Override
    protected void init() {
        int centerX = Render2DHelper.INSTANCE.getScaledWidth() / 2;
        int centerY = Render2DHelper.INSTANCE.getScaledHeight() / 2;
        int topY = centerY - 100;
        prefixField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), centerX - 55, topY, 50, 20, new LiteralText(CommandManager.INSTANCE.getPrefix()));
        prefixField.setMaxLength(1);
        prefixField.setText(CommandManager.INSTANCE.getPrefix());
        prefixField.setVisible(true);
        setPrefixButton = new ButtonWidget(centerX + 1, topY, 54, 20, new LiteralText("Set Prefix"), button -> {
            CommandManager.INSTANCE.setPrefix(prefixField.getText());
            ClientSettingsFile.write();
        });
        downloadInstallerButton = new ButtonWidget(centerX - 75, topY + 25, 150, 20, new LiteralText("Update Jex to " + UpdateManager.INSTANCE.getLatestVersion()), button -> {
            Update.INSTANCE.update(UpdateManager.INSTANCE.getLatestVersion());
            updating = true;
        });
        clickGuiButton = new ButtonWidget(centerX - 75, topY + 50, 150, 20, new LiteralText("Open ClickGUI"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(Gui.clickgui);
        });
        xrayButton = new ButtonWidget(centerX - 75, topY + 75, 150, 20, new LiteralText("Xray Block Selection"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new XraySelectScreen());
        });
        searchButton = new ButtonWidget(centerX - 75, topY + 100, 150, 20, new LiteralText("Search Block Selection"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new SearchSelectScreen());
        });
        reloadAddonsButton = new ButtonWidget(centerX - 75, topY + 125, 150, 20, new LiteralText("Reload Capes and Hats"), button -> {
            Addon.clearAddons();
            if (Wrapper.INSTANCE.getWorld() != null) {
                Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                    if (entity instanceof PlayerEntity) {
                        Addon.loadAddons((PlayerEntity)entity);
                    }
                });
            }
            timer.reset();
        });
        waypointScreenButton = new ButtonWidget(centerX - 230, topY + 25, 150, 20, new LiteralText("Waypoint Screen"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new WaypointScreen());
        });
        downloadInstallerButton.active = UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH;

        this.addDrawableChild(setPrefixButton);
        this.addDrawableChild(clickGuiButton);
        this.addDrawableChild(downloadInstallerButton);
        this.addDrawableChild(xrayButton);
        this.addDrawableChild(searchButton);
        this.addDrawableChild(reloadAddonsButton);
        this.addDrawableChild(waypointScreenButton);
        this.addSelectableChild(prefixField);
        super.init();
    }

    @Override
    public void tick() {
        prefixField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        prefixField.render(matrices, mouseX, mouseY, delta);
        setPrefixButton.active = !prefixField.getText().isEmpty();
        if (!timer.hasPassed(30 * 1000)) {
            reloadAddonsButton.setMessage(new LiteralText("Reload Capes and Hats (" + ( 30 - ((timer.getCurrentMS() - timer.getLastMS()) / 1000)) + ")"));
            reloadAddonsButton.active = false;
        } else {
            reloadAddonsButton.setMessage(new LiteralText("Reload Capes and Hats"));
            reloadAddonsButton.active = true;
        }
        if (updating) {
            int topY = (height / 2) - 100;
            FontHelper.INSTANCE.drawCenteredString(matrices, Update.INSTANCE.getProgressText() + " \247f" + ClientMathHelper.INSTANCE.roundToPlace(Update.INSTANCE.getProgress() * 100.0F, 2) + "%", width / 2, topY - 20, ColorHelper.INSTANCE.getClientColor());
            float leftX = (width / 2) - 100;
            float pos = 200 * Update.INSTANCE.getProgress();
            Render2DHelper.INSTANCE.fill(matrices, leftX, topY - 10, leftX + 200, topY - 8, 0xff000000);
            Render2DHelper.INSTANCE.fill(matrices, leftX, topY - 10, leftX + pos, topY - 8, ColorHelper.INSTANCE.getClientColor());
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
