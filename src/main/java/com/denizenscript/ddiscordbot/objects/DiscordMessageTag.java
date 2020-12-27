package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class DiscordMessageTag implements ObjectTag {

    // <--[language]
    // @name DiscordMessageTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordMessageTag is an object that represents a message already sent on Discord, either as a generic reference,
    // or as a bot-specific reference.
    // Note that this is not used for messages that *are going to be* sent.
    // Note that this often does not contain data for messages that have been deleted (unless that data is cached).
    //
    // These use the object notation "discordmessage@".
    // The identity format for Discord messages is the bot ID (optional), followed by the channel ID (optional), followed by the message ID (required).
    // For example: 1234
    // Or: 12,1234
    // Or: mybot,12,1234
    //
    // -->

    @Fetchable("discordmessage")
    public static DiscordMessageTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordmessage@")) {
            string = string.substring("discordmessage@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> commaSplit = CoreUtilities.split(string, ',');
        if (commaSplit.size() == 0 || commaSplit.size() > 3) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag input is not valid.");
            }
            return null;
        }
        String msgIdText = commaSplit.get(commaSplit.size() - 1);
        if (!ArgumentHelper.matchesInteger(msgIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag input is not a number.");
            }
            return null;
        }
        long msgId = Long.parseLong(msgIdText);
        if (msgId == 0) {
            return null;
        }
        if (commaSplit.size() == 1) {
            return new DiscordMessageTag(null, 0, msgId);
        }
        String chanIdText = commaSplit.get(commaSplit.size() - 2);
        if (!ArgumentHelper.matchesInteger(chanIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag channel ID input is not a number.");
            }
            return null;
        }
        long chanId = Long.parseLong(chanIdText);
        if (chanId == 0) {
            return null;
        }
        return new DiscordMessageTag(commaSplit.size() == 3 ? commaSplit.get(0) : null, chanId, msgId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordmessage@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.lastIndexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public DiscordMessageTag(String bot, long channel_id, long message_id) {
        this.bot = bot;
        this.channel_id = channel_id;
        this.message_id = message_id;
    }

    public DiscordMessageTag(String bot, Message message) {
        this.bot = bot;
        this.message_id = message.getIdLong();
        this.message = message;
        this.channel = message.getChannel();
        this.channel_id = message.getTextChannel().getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public MessageChannel getChannel() {
        if (channel != null) {
            return channel;
        }
        channel = getBot().client.getTextChannelById(channel_id);
        if (channel == null) {
            channel = getBot().client.getPrivateChannelById(channel_id);
        }
        return channel;
    }

    public Message getMessage() {
        if (message != null) {
            return message;
        }
        message = getChannel().retrieveMessageById(message_id).complete();
        return message;
    }

    public String bot;

    public MessageChannel channel;

    public Message message;

    public long channel_id;

    public long message_id;

    public static String stripMentions(String message, List<User> mentioned) {
        for (User user : mentioned) {
            message = message.replace(user.getAsMention(), "")
                    .replace("<@" +user.getId() + ">", "")
                    .replace("<@!" +user.getId() + ">", "");
        }
        return message;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordMessageTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID of the message.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.message_id);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.channel>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the channel that the message was sent to.
        // -->
        registerTag("channel", (attribute, object) -> {
            return new DiscordChannelTag(object.bot, object.channel_id);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the full text of the message.
        // -->
        registerTag("text", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentRaw());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_stripped>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the stripped text of the message (format codes like bold removed).
        // -->
        registerTag("text_stripped", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentStripped());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_display>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the display text of the message (special codes like pings formatted to how they should look for users).
        // -->
        registerTag("text_display", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentDisplay());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_no_mentions>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the text of the message, with '@' mentions removed.
        // -->
        registerTag("text_no_mentions", (attribute, object) -> {
            return new ElementTag(stripMentions(object.getMessage().getContentRaw(), object.getMessage().getMentionedUsers()));
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.author>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the author of the message.
        // -->
        registerTag("author", (attribute, object) -> {
            return new DiscordUserTag(object.bot, object.getMessage().getAuthor());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.was_edited>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether this message was edited.
        // -->
        registerTag("was_edited", (attribute, object) -> {
            return new ElementTag(object.getMessage().isEdited());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.is_pinned>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether this message is pinned.
        // -->
        registerTag("is_pinned", (attribute, object) -> {
            return new ElementTag(object.getMessage().isPinned());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.mentioned_users>
        // @returns ListTag(DiscordUserTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of users mentioned (pinged) by this message.
        // -->
        registerTag("mentioned_users", (attribute, object) -> {
            ListTag list = new ListTag();
            for (User user : object.getMessage().getMentionedUsers()) {
                list.addObject(new DiscordUserTag(object.bot, user));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.is_direct>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the message was sent in a direct (private) channel, or false if in a public channel.
        // -->
        registerTag("is_direct", (attribute, object) -> {
            return new ElementTag(object.getChannel() instanceof PrivateChannel);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.embed>
        // @returns ListTag(DiscordEmbedTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of "embed" formatted data on this message.
        // -->
        registerTag("embed", (attribute, object) -> {
            ListTag list = new ListTag();
            for (MessageEmbed embed : object.getMessage().getEmbeds()) {
                list.addObject(new DiscordEmbedTag(embed));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.reactions>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of reaction on this message.
        // -->
        registerTag("reactions", (attribute, object) -> {
            ListTag list = new ListTag();
            for (MessageReaction reaction : object.getMessage().getReactions()) {
                list.addObject(new DiscordReactionTag(object.bot, object.getMessage(), reaction));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.previous_messages[<#>]>
        // @returns ListTag(DiscordMessageTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of the last (specified number) messages sent in the channel prior to this message.
        // The list is ordered from most recent to least recent.
        // -->
        registerTag("previous_messages", (attribute, object) -> {
            int limit = attribute.getIntContext(1);
            MessageHistory history = object.getChannel().getHistoryBefore(object.message_id, limit).complete();
            ListTag list = new ListTag();
            for (Message message : history.getRetrievedHistory()) {
                list.addObject(new DiscordMessageTag(object.bot, message));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.next_messages[<#>]>
        // @returns ListTag(DiscordMessageTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of the next (specified number) messages sent in the channel after this message.
        // The list is ordered from most recent to least recent.
        // -->
        registerTag("next_messages", (attribute, object) -> {
            int limit = attribute.getIntContext(1);
            MessageHistory history = object.getChannel().getHistoryAfter(object.message_id, limit).complete();
            ListTag list = new ListTag();
            for (Message message : history.getRetrievedHistory()) {
                list.addObject(new DiscordMessageTag(object.bot, message));
            }
            return list;
        });
    }

    public static ObjectTagProcessor<DiscordMessageTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordMessageTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordmessage";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "DiscordMessage";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordmessage@" + bot + "," + channel_id + "," + message_id;
        }
        if (channel_id != 0) {
            return "discordmessage@" + channel_id + "," + message_id;
        }
        return "discordmessage@" + message_id;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
