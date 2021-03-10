package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.file.ModuleFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.ModuleManager;

@Cmd(name = "Bind", syntax = ".bind <add/remove/list> <module> <key>", description = "Modify keybinds with a command. List with bind list")
public class CommandBind extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            String action = args[1];
            if (action.equalsIgnoreCase("list")) {
                ChatHelper.INSTANCE.addClientMessage("Listing keybinds.");
                for (Module module : ModuleManager.INSTANCE.getModules().values()) {
                    if (module.getKey() != 0) {
                        ChatHelper.INSTANCE.addClientMessage("\247b" + module.getName() + "\247f: \2477" + KeyboardHelper.INSTANCE.getKeyName(module.getKey()));
                    }
                }
                return;
            } else {
                String moduleName = args[2];
                Module module = Module.get(moduleName);
                if (module == null) {
                    ChatHelper.INSTANCE.addClientMessage("Module not found.");
                    return;
                }
                if (isAddString(action)) {
                    String keyName = args[3];
                    int key = KeyboardHelper.INSTANCE.getKeyFromName(keyName);
                    if (key == -1) {
                        ChatHelper.INSTANCE.addClientMessage("Key not found.");
                        return;
                    }
                    module.setKey(key);
                    ChatHelper.INSTANCE.addClientMessage("\247b" + module.getName() + " \2477has been bound to \247b" + keyName);
                    ModuleFile.write();
                } else if (isDeleteString(action)) {
                    module.setKey(0);
                    ChatHelper.INSTANCE.addClientMessage("\247b" + module.getName() + " \2477has been unbound");
                    ModuleFile.write();
                } else {
                    giveSyntaxMessage();
                }
            }
        }catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}