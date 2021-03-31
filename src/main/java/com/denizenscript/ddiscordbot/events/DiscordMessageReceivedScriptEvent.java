package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagContext;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordMessageReceivedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received
    //
    // @Regex ^on discord message received$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord bot receives a message.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.new_message> returns the message received, as a DiscordMessageTag.
    //
    // -->

    public MessageReceivedEvent getEvent() {
        return (MessageReceivedEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord message received");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("channel", getEvent().getChannel().getId())) {
            return false;
        }
        if (getEvent().isFromGuild() && !path.checkSwitch("group", getEvent().getGuild().getId())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "channel":
                return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group":
                if (getEvent().isFromGuild()) {
                    return new DiscordGroupTag(botID, getEvent().getGuild());
                }
                break;
            case "new_message":
                return new DiscordMessageTag(botID, getEvent().getMessage());
            case "message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getContentRaw());
            case "message_id":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getId());
            case "no_mention_message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(DiscordMessageTag.stripMentions(getEvent().getMessage().getContentRaw(), getEvent().getMessage().getMentionedUsers()));
            case "formatted_message":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessage().getContentDisplay());
            case "author":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new DiscordUserTag(botID, getEvent().getMessage().getAuthor());
            case "mentions":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                ListTag list = new ListTag();
                for (User user : getEvent().getMessage().getMentionedUsers()) {
                    list.addObject(new DiscordUserTag(botID, user));
                }
                return list;
            case "is_direct":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getChannel() instanceof PrivateChannel);
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageReceived";
    }
}
