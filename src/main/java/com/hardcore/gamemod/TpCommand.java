package com.hardcore.gamemod;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.Commands;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.jmx.Server;
import net.minecraft.world.level.block.Blocks;

import java.util.List;


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

        dispatcher.register(
                Commands.literal("getpos")
                        .executes(TpCommand::getPlayerPositionCommand)
        );

        dispatcher.register(
                Commands.literal("killCircle")
                        .executes(TpCommand::drawcircle)
        );

        dispatcher.register(
                Commands.literal("drawCircle")
                        .executes(TpCommand::drawcircleBlock)
        );


        dispatcher.register(
                Commands.literal("tparea")
                        .then(
                                Commands.argument("x", IntegerArgumentType.integer())
                                        .then(
                                                Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(TpCommand::teleportCircle)
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

    private static void GetPlayerPosition(ServerPlayer player){

        Vec3 position = player.getPosition(1);
        player.sendSystemMessage(Component.literal("Tu posicion es ."+ position));
    }

    private static int getPlayerPositionCommand(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        GetPlayerPosition(player);

        return 1;
    }


    private static int drawcircle(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Vec3 center= player.getPosition(1);
        double radius = 10;

        AABB area = new AABB(

                center.x - radius,
                center.y - radius,
                center.z -radius,

                center.x + radius,
                center.y + radius,
                center.z + radius
        );


        ServerLevel level = player.serverLevel();

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);


        for(Entity entity: entities){
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;

            if(dx * dx + dz * dz <= radius * radius) {

                if (entity == player)
                    continue;
                entity.kill();
            }


        }

        return 1;
    }
    private static int teleportCircle(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        int x = IntegerArgumentType.getInteger(ctx, "x");
        int z = IntegerArgumentType.getInteger(ctx, "z");

        Vec3 center= player.getPosition(1);
        double radius = 10;

        AABB area = new AABB(

                center.x - radius,
                center.y - radius,
                center.z -radius,

                center.x + radius,
                center.y + radius,
                center.z + radius
        );


        ServerLevel level = player.serverLevel();

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);


        for(Entity entity: entities){
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;

            if(dx * dx + dz * dz <= radius * radius) {

                if (entity == player)
                    continue;
                entity.teleportTo(x , center.y, z);
            }


        }
        
        player.teleportTo(x , center.y, z);

        return 1;
    }

    private static int drawcircleBlock(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Vec3 center= player.getPosition(1);
        int radius = 10;
        ServerLevel level = player.serverLevel();

        int centerX = (int)Math.floor(center.x);
        int centerY = (int)Math.floor(center.y);
        int centerZ = (int)Math.floor(center.z);


        for(int x = centerX - radius; x<= centerX+radius; x++){
            for(int z = centerZ - radius; z <=centerZ+radius; z++){


                double dx = x-centerX;
                double dZ = z-centerZ;

                if(dx * dx + dZ * dZ <=radius * radius){

                    BlockPos pos = new BlockPos(x, centerY , z);

                    level.setBlock(pos, Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                }
            }
        }
        return 1;
    }

}
