package me.millo.mcGit.git;

import me.millo.mcGit.git.merge.Merge;
import me.millo.mcGit.utility.Broadcast;

public class GitStateHandler {

    private GitState state = GitState.IDLE;
    private Merge activeMerge;

    public boolean isIdle() {
        return state == GitState.IDLE;
    }

    public boolean isBusyAndNotify() {
        if (isIdle()) return false;

        Broadcast.message("Git State is required to be idle. Current: " + state);

        return true;
    }

    public boolean equals(GitState state) {
        return this.state == state;
    }

    public void setIdle() {
        state = GitState.IDLE;
    }

    public void setDisplay() {
        state = GitState.DISPLAY;
    }

    public void setActiveMerge(Merge merge) {
        this.activeMerge = merge;
        state = GitState.MERGE_CONFLICT;
    }

    public Merge getActiveMerge() {
        return activeMerge;
    }
}
