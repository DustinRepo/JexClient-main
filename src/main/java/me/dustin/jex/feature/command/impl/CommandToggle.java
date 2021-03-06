package me.dustin.jex.feature.command.impl;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.feature.mod.core.Feature;

@Cmd(name = "Toggle", syntax = ".toggle <mod>", description = "Toggle modules.", alias = {"t"})
public class CommandToggle extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            String moduleName = args[1];
            Feature mod = Feature.get(moduleName);
            if (mod != null) {
                mod.toggleState();
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
                ChatHelper.INSTANCE.addClientMessage(String.format("%s %s", moduleName, mod.getState() ? "\247a\247lON" : "\2474\247lOFF"));
            } else {
                ChatHelper.INSTANCE.addClientMessage("That module does not exist!");
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
