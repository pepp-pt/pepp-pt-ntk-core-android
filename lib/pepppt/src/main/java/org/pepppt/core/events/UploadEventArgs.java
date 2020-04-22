package org.pepppt.core.events;

public class UploadEventArgs extends EventArgs {

    public UploadEventArgs(int progress) {
        this.progress = progress;
    }

    public UploadEventArgs(int progress, String message) {
        this.progress = progress;
        this.message = message;
    }

    public UploadEventArgs(String message) {
        this.message = message;
    }

    private int progress;
    private String message;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
