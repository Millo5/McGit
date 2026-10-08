package me.millo.mcGit.exceptions;

import me.millo.mcGit.utility.messenger.Messenger;

public class McGitException extends Exception {
    public McGitException(String s) {
        super(s);
    }

    public void broadcast() {
        Messenger.createAll().sendError(getMessage());
    }

    public void send(Messenger messenger) {
        messenger.sendError(getMessage());
    }
}
