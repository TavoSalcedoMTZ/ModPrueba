package com.hardcore.gamemod;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.Commands;


public class TpCommand {

    private static final int XP_COST=10; //Representa el costo de Experiencia total que costara el tp


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tpcost")
                        .then(
                                Commands.argument("x", IntegerArgumentType.integer())
                                        .then(
                                                Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(TpCommand::teleportCoords)
                                        )
                        )
        );
    }

    //Metodo para verificar la experiencia del jugador funcionara para poder comprobar si se puede hacer el TP
    private static boolean hasEnoughtXP(Player player){
        if(player!=null) return player.experienceLevel >= XP_COST;
        return  false;
    }


    //Cobramos la experiencia del jugador (en teoria no deberiamos clampear ya que es un int)
    private static void chargeXP(ServerPlayer player){
       //int currentXP =  player.experienceLevel ;
       //currentXP-=XP_COST;
        player.giveExperienceLevels(-XP_COST);
    }


    private static int teleportCoords(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!hasEnoughtXP(player)) {
            player.sendSystemMessage(Component.literal("No tienes suficiente experiencia."));
            return 0;
        }

        chargeXP(player);

        int x = IntegerArgumentType.getInteger(ctx, "x");
        int z = IntegerArgumentType.getInteger(ctx, "z");

        player.teleportTo(x, 64, z);

        return 1;
    }


}
