package org.example.Workspace;
import org.example.Text.TextFactory;
import org.example.WorkSpace.WorkSpaceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class WorkSpaceTest {
    private WorkSpaceManager workSpaceManager = new WorkSpaceManager();

    @Test
    @DisplayName("test load command: create a new workspace")
    public void testLoadCommand() {
        // load a new file
        this.workSpaceManager.ParseCommand("load test.md");
        assertEquals(this.workSpaceManager.getWorkSpaceCount(), 1);

        // load an existing file
        this.workSpaceManager.ParseCommand("load test.md");
        assertEquals(this.workSpaceManager.getWorkSpaceCount(), 1);
    }

    @Test
    @DisplayName("Make sure user load a specific file before editing")
    void TestLoadFirst() {
        CheckOutput("save", "Please create a workspace first.\n");
    }

    @Test
    @DisplayName("Test list-workspace Command")
    void ListWorkSpaceTest() {
        this.workSpaceManager.ParseCommand("load test.md");
        this.workSpaceManager.ParseCommand("load test2.md");
        CheckOutput("list-workspace", "->test2\n" + "  test\n");
    }

    @Test
    @DisplayName("Test open command")
    void OpenCommandTest() {
        this.workSpaceManager.ParseCommand("load test.md");

        // open an invalid workspace
        CheckOutput("open test2", "Please load the file first!\n");

        // chack active workspace
        this.workSpaceManager.ParseCommand("load test2.md");
        this.workSpaceManager.ParseCommand("open test");
        assertEquals(this.workSpaceManager.getActiveFileName(), "test");

    }

    @Test
    @DisplayName("Test close command")
    void CloseCommandTest() {
        this.workSpaceManager.ParseCommand("load test.md");
        this.workSpaceManager.ParseCommand("load test2.md");
        this.workSpaceManager.ParseCommand("close");

        CheckOutput("list-workspace", "  test\n");

        // check save inquiry
        this.workSpaceManager.ParseCommand("open test");
        this.workSpaceManager.ParseCommand("append-head # test");

        final InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("N".getBytes()));
        CheckOutput("close", "Do you want to save the current workspace [Y/N]?");
        System.setIn(oldIn);

    }

    private void CheckOutput(String command, String expectedOutput) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        this.workSpaceManager.ParseCommand(command);
        assertEquals(expectedOutput, outputStream.toString());
    }
}
