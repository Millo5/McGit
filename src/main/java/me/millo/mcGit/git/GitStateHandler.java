package me.millo.mcGit.git;

import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.merge.Merge;
import me.millo.mcGit.utility.messenger.Messages;
import me.millo.mcGit.utility.messenger.Messenger;

import java.util.ArrayList;

public class GitStateHandler {

    private GitState state = GitState.IDLE;
    private Merge activeMerge;
    private ArrayList<Commit> viewingCommits;

    public boolean isIdle() {
        return state == GitState.IDLE;
    }

    public boolean isBusyAndNotify() {
        return isBusyAndNotify(Messenger.createAll());
    }

    public boolean isBusyAndNotify(Messenger messenger) {
        if (isIdle()) return false;

        messenger.send(Messages.GIT_STATE_BUSY, state);

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

    public void setView(ArrayList<Commit> commits) {
        viewingCommits = commits;
        state = GitState.VIEW;
    }

    public ArrayList<Commit> getViewingCommits() {
        return viewingCommits;
    }
}
