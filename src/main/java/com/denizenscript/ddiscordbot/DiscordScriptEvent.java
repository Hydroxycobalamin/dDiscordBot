package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;

public abstract class DiscordScriptEvent extends BukkitScriptEvent {

    public String botID;

    public Event event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "for", botID)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("bot")) {
            return new DiscordBotTag(botID);
        }
        return super.getContext(name);
    }

    public boolean enabled = false;

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    public static boolean tryChannel(ScriptPath path, MessageChannel channel) {
        String text = path.switches.get("channel");
        if (text == null) {
            return true;
        }
        if (channel == null) {
            return false;
        }
        MatchHelper matcher = createMatcher(text);
        if (matcher.doesMatch(channel.getId())) {
            return true;
        }
        if (matcher.doesMatch(channel.getName())) {
            return true;
        }
        return false;
    }

    public static boolean tryGuild(ScriptPath path, Guild guild) {
        String text = path.switches.get("group");
        if (text == null) {
            return true;
        }
        if (guild == null) {
            return false;
        }
        MatchHelper matcher = createMatcher(text);
        if (matcher.doesMatch(guild.getId())) {
            return true;
        }
        if (matcher.doesMatch(guild.getName())) {
            return true;
        }
        return false;
    }
}
