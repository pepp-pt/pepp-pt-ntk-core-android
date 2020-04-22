package org.pepppt.core.events;

import org.pepppt.core.tan.Tan;

public class NewTanEventArgs extends EventArgs {
    private Tan tan;

    public Tan getTan() {
        return tan;
    }

    private NewTanEventArgs() {
    }

    public NewTanEventArgs(Tan tan) {
        this.tan = tan;
    }
}
