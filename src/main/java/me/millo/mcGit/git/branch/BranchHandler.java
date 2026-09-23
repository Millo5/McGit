package me.millo.mcGit.git.branch;

import me.millo.mcGit.utility.Broadcast;

import java.util.ArrayList;
import java.util.Optional;

public class BranchHandler {

    private Branch branch;
    private final ArrayList<Branch> foundBranches = new ArrayList<>();

    public BranchHandler() {
        branch = new Branch("master", null);
        foundBranches.add(branch);
    }

    public Branch getBranch() {
        return branch;
    }

    public ArrayList<Branch> getFoundBranches() {
        return foundBranches;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;

        // TODO: resolve commits

        Broadcast.message("Now on branch: " + branch.getName(),
                "Head: " + branch.getHeadHash());
    }

    public Optional<Branch> getBranchByName(String name) {
        for (Branch branch : foundBranches) {
            if (branch.getName().equals(name)) return Optional.of(branch);
        }
        return Optional.empty();
    }

    public void split(String name) {
        Branch newBranch = new Branch(name, branch.getHeadHash());
        foundBranches.add(newBranch);

        setBranch(newBranch);
    }
}
