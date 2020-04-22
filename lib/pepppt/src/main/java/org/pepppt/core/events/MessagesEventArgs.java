package org.pepppt.core.events;

import org.pepppt.core.messages.models.Message;

import java.util.List;

public class MessagesEventArgs extends EventArgs {
    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    private List<Message> messages;

    public MessagesEventArgs(List<Message> messages) {
        this.messages = messages;
    }
}
