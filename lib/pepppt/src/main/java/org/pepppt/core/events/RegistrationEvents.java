package org.pepppt.core.events;

public class RegistrationEvents extends EventArgs {
    private String message;
    private String type;

    public RegistrationEvents(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
