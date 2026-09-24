package me.millo.mcGit.git.branch;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.millo.mcGit.files.FileBank;
import me.millo.mcGit.utility.Broadcast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        save();

        setBranch(newBranch);
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("current", branch.getName());

        JsonArray branches = new JsonArray();

        for (Branch branch : foundBranches) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", branch.getName());
            obj.addProperty("hash", branch.getHeadHash().toString());

            branches.add(obj);
        }

        root.add("branches", branches);

        try {
            File file = FileBank.getBranchesFile();

            Files.writeString(
                    file.toPath(),
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(root)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to save branches", e);
        }
    }
}
