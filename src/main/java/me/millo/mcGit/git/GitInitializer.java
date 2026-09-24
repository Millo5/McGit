package me.millo.mcGit.git;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import me.millo.mcGit.files.FileBank;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.git.diff.BlockModification;
import org.bukkit.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class GitInitializer {

    private final GitCore core;

    public GitInitializer(GitCore core) {
        this.core = core;
    }

    public void run() {
        try {
            setupRoot();
            setupBranches();
            setupInitialCommit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupInitialCommit() {
        CommitHash head = core.getBranchHandler().getBranch().getHeadHash();
        if (head == null) {
            Commit initialCommit = new Commit("Initial Commit", new CommitHash[0], "McGit", new CommitChanges(new Location[0], new BlockModification[0]));
            try {
                initialCommit.save();
                core.getBranchHandler().getBranch().setHead(initialCommit.getHash());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void setupRoot() throws IOException {
        Path path = FileBank.getGitFolder();
        if (Files.exists(path)) {
            System.out.println("Git already exists in " + path);
            return;
        }

        Files.createDirectories(path);
        System.out.println("Git initialized in " + path);
    }

    private void setupBranches() throws IOException {
        File file = FileBank.getBranchesFile();

        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            String currentBranchName = json.get("current").getAsString();

            ArrayList<Branch> branches = new ArrayList<>();
            Branch currentBranch = null;
            for (JsonElement element : json.get("branches").getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();

                String name = obj.get("name").getAsString();
                String hash = obj.get("hash").getAsString();

                Branch branch = new Branch(name, new CommitHash(UUID.fromString(hash)));
                branches.add(branch);

                if (currentBranch == null && currentBranchName.equals(name)) {
                    currentBranch = branch;
                }
            }

            if (currentBranch == null) {
                currentBranch = branches.getFirst();
            }

            core.getBranchHandler().getFoundBranches().clear();
            core.getBranchHandler().getFoundBranches().addAll(branches);
            core.getBranchHandler().setBranch(currentBranch);
        }
    }
}
