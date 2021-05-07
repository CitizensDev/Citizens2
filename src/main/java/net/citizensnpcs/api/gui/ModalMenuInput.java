package net.citizensnpcs.api.gui;

import java.util.function.Consumer;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;

public class ModalMenuInput {
    private ModalMenuInput() {
    }

    public static void captureInput(Player player, InventoryMenu menu, Consumer<String> input) {
        menu.close(player);
        player.beginConversation(
                new ConversationFactory(CitizensAPI.getPlugin()).addConversationAbandonedListener((evt) -> {
                    menu.present(player);
                }).withLocalEcho(false).withEscapeSequence("exit").withModality(false).withTimeout(60)
                        .withFirstPrompt(new StringPrompt() {
                            @Override
                            public Prompt acceptInput(ConversationContext ctx, String text) {
                                input.accept(text);
                                menu.present(player);
                                return null;
                            }

                            @Override
                            public String getPromptText(ConversationContext ctx) {
                                return "";
                            }
                        }).buildConversation(player));
    }
}
