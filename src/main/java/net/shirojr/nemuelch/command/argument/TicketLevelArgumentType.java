package net.shirojr.nemuelch.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.shirojr.nemuelch.util.constants.TicketMapper;

import java.util.concurrent.CompletableFuture;

public class TicketLevelArgumentType extends EnumArgumentType<TicketMapper> {
    private TicketLevelArgumentType() {
        super(TicketMapper.CODEC, TicketMapper::values);
    }

    public static EnumArgumentType<TicketMapper> level() {
        return new TicketLevelArgumentType();
    }

    public static TicketMapper getLevel(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, TicketMapper.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (TicketMapper entry : TicketMapper.values()) {
            builder.suggest(entry.asString());
        }
        return builder.buildFuture();
    }
}
