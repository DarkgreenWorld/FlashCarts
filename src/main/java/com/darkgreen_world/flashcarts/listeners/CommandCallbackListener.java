package com.darkgreen_world.flashcarts.listeners;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.darkgreen_world.flashcarts.config.ConfigDialog;
import org.jspecify.annotations.NonNull;

public class CommandCallbackListener implements CommandRegistrationCallback, Command<CommandSourceStack> {

    @Override
    public void register(@NonNull CommandDispatcher<CommandSourceStack> dispatcher, @NonNull CommandBuildContext context, Commands.@NonNull CommandSelection selection) {
        dispatcher.register(Commands.literal("flashcarts")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("config").executes(this)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ConfigDialog.open(context.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

}
