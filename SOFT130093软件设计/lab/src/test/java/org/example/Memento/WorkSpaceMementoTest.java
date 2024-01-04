package org.example.Memento;
import org.example.Command.Command;
import org.example.Command.CommandManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class WorkSpaceMementoTest {
    String filePath = "WorkSpaceMementoTest.md";
    CommandManager commandManager;

    @BeforeEach
    void startup() {
        Path path = Paths.get(this.filePath);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                System.err.println("Failed to delete the file: " + e.getMessage());
            }
        }
        this.commandManager = new CommandManager(filePath);
    }

    @Test
    @DisplayName("create workspacememento from commandmanager")
    void CreateTest() {
        this.commandManager.ParseCommand("append-head # new header");
        // check createMemento()
        WorkSpaceMemento workSpaceMemento = this.commandManager.createMemento();
        assertEquals(workSpaceMemento.getFilePath(), this.filePath);
        assertEquals(workSpaceMemento.getCommands(), this.commandManager.getCommands());
        assertEquals(workSpaceMemento.getCurrentCommandIndex(), this.commandManager.getCurrentCommandIndex());

    }
}
