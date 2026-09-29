package me.millo.mcGit.exceptions;

import me.millo.mcGit.utility.Broadcast;

public class McGitException extends Exception {
    public McGitException(String s) {
        super(s);
    }

    public void broadcast() {
        Broadcast.message(getMessage());
    }
}
