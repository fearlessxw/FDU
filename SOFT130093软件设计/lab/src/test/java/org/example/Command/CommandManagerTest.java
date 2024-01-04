package org.example.Command;

import org.example.Editor.Editor;
import org.example.Text.Text;
import org.example.Text.TextFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import org.example.WorkSpace.WorkSpaceManager;

class CommandManagerTest {
    private CommandManager commandManager;
    private List<Text> validFile;
    @BeforeEach
    public void SetUp() {
        commandManager = new CommandManager("test.md");
        TextFactory textFactory = new TextFactory();
        validFile = new ArrayList<>();
        validFile.add(textFactory.createText("# header1"));
        validFile.add(textFactory.createText("+ unorderedlist"));
        validFile.add(textFactory.createText("1. orderedlist"));
    }

    private void CheckOutput(String command, String expectedOutput) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        commandManager.ParseCommand(command);
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    @DisplayName("Update Commands")
    void TestExecute() {
        commandManager.ParseCommand("load test.md");
        // Execute a command
        commandManager.ParseCommand("insert 0 # new header");
        assertEquals(this.commandManager.getCommandsLength(), 1);
        assertEquals(this.commandManager.getCurrentCommandIndex(), 0);
    }

    /*
    @Test
    void TestParseInvalidLoadCommand() {
        List<String> InvalidLoadCommandList = new ArrayList<>();
        InvalidLoadCommandList.add("load dd");
        InvalidLoadCommandList.add("load dd.txt");
        InvalidLoadCommandList.add("load d*d.md");
        commandManager.ParseCommand("load test.md");
        for (String command:InvalidLoadCommandList) {
            CheckOutput(command, "Invalid load command format.\n");
        }
    }*/

    @Test
    @DisplayName("Invalid list command: list XXX")
    void TestParseInvalidListCommand() {
        CheckOutput("list X", "Invalid list command format.\n");
    }

    @Test
    @DisplayName("Invalid save command: save XXX")
    void TestParseInvalidSaveCommand() {
        CheckOutput("save X", "Invalid save command format.\n");
    }

    @Test
    @DisplayName("Invalid insert command: no lineNumber or content")
    void TestParseInsertCommand() {
        String expectedOutput = "Invalid Text!\n";
        CheckOutput("insert line content", expectedOutput);
        CheckOutput("insert 1 ", expectedOutput);
    }

    @Test
    @DisplayName("Invalid append-head command: no content")
    void TestParseAppendHeadCommand() {
        String expectedOutput = "Invalid append-head command format.\n";
        CheckOutput("append-head", expectedOutput);
    }

    @Test
    @DisplayName("Invalid append-tail command: no content")
    void TestParseAppendTailCommand() {
        String expectedOutput = "Invalid append-tail command format.\n";
        CheckOutput("append-tail", expectedOutput);
    }

    @Test
    @DisplayName("Invalid undo command: undo XXX")
    void TestParseUndoCommand() {
        String expectedOutput = "Invalid undo command format.\n";
        CheckOutput("undo X", expectedOutput);
    }
    @Test
    @DisplayName("Invalid undo command: redo XXX")
    void TestParseRedoCommand() {
        String expectedOutput = "Invalid redo command format.\n";
        CheckOutput("redo X", expectedOutput);
    }

    @Test
    @DisplayName("Test undo and redo")
    void TestRedoUndoCommand() {
        // undo
        commandManager.ParseCommand("delete header1");
        commandManager.ParseCommand("delete unorderedlist");
        assertEquals(commandManager.getCurrentCommandIndex(),1);
        commandManager.ParseCommand("undo");
        commandManager.ParseCommand("undo");
        assertEquals(commandManager.getCurrentCommandIndex(),-1);
        commandManager.ParseCommand("save");
        assertEquals(commandManager.getCommandsLength(),0);
        commandManager.ParseCommand("redo");
        assertEquals(commandManager.getCurrentCommandIndex(),-1);

    }
}