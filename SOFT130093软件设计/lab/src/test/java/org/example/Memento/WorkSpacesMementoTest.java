package org.example.Memento;
import org.example.Command.Command;
import org.example.Command.CommandManager;
import org.example.Command.InsertCommand;
import org.example.WorkSpace.WorkSpaceManager;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkSpacesMementoTest {
    WorkSpaceManager workSpaceManager = new WorkSpaceManager();
    String filePath = "WorkSpacesMementoTest.md";
    String filePath2 = "WorkSpacesMementoTest2.md";
    String MEMENTO_FILE = "workspace_memento_test.ser";
    @BeforeEach
    void startup() {
        deleteFile(this.filePath);
        deleteFile(this.filePath2);
        deleteFile(this.MEMENTO_FILE);
    }

    @Test
    void serializeTest() {
        workSpaceManager.ParseCommand("load " + filePath);
        workSpaceManager.ParseCommand("load " + filePath2);
        workSpaceManager.ParseCommand("insert # new header");
        workSpaceManager.ParseCommand("append-tail ## last line");

        WorkSpacesMemento workSpacesMemento = workSpaceManager.createMemento();
        try {
            byte[] data = workSpacesMemento.serialize();
            try (FileOutputStream fileOut = new FileOutputStream(MEMENTO_FILE);
                 ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(data);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        WorkSpacesMemento newWorkSpaceMemento = new WorkSpacesMemento();
        File file = new File(MEMENTO_FILE);
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fileIn = new FileInputStream(MEMENTO_FILE);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                byte[] data = (byte[]) in.readObject();
                newWorkSpaceMemento = WorkSpacesMemento.deserialize(data);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        assertEquals(workSpacesMemento.getActiveFileName(), newWorkSpaceMemento.getActiveFileName());
        assertEquals(workSpacesMemento.getCurrentDirectory(), newWorkSpaceMemento.getCurrentDirectory());
        assertEquals(workSpacesMemento.getMementos().size(), newWorkSpaceMemento.getMementos().size());

    }

    private void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                System.err.println("Failed to delete the file: " + e.getMessage());
            }
        }
    }
}
