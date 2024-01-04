package org.example.Editor;

import org.example.Text.Text;
import org.example.Text.TextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EditorTest {
    private Editor textEditor;
    private List<String> contentList;
    private String filePath;

    @BeforeEach
    void SetUp() {
        this.filePath = "testLoadCommand.md";

        this.contentList = new ArrayList<>();
        this.contentList.add("# header1");
        this.contentList.add("+ unorderedlist");
        this.contentList.add("1. orderedlist");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String content:this.contentList) {
                writer.write(content+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.textEditor = new Editor();
        this.textEditor.load(this.filePath);
    }
    @AfterEach
    void CleanUp() {
        File file = new File(this.filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Test list command")
    void TestList() {
        StringBuilder contentString = new StringBuilder();
        for (String line : contentList) {
            contentString.append(line).append("\n");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        this.textEditor.list();
        assertEquals(contentString.toString(), outputStream.toString());
    }

    @Test
    void TestLoadValidText() {
        // test filePath
        assertEquals(this.filePath, this.textEditor.getFilePath());

        // test texts in the file
        List<String> fileContent = this.textEditor.getRawFile();
        assertEquals(this.contentList, fileContent);
    }
    @Test
    void TestLoadInvalidText() {
        this.contentList.add("Invalid text");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String content:this.contentList) {
                writer.write(content+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        this.textEditor = new Editor();
        this.textEditor.load(this.filePath);
        String expectedOutput = "Invalid Text!\n";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void TestSave() {
        this.textEditor.save();

        // loading the saved file
        List<String> savedContent = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(textEditor.getFilePath()));
            String line;
            while ((line = reader.readLine()) != null) {
                savedContent.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check whether the contents are the same
        assertEquals(this.contentList, savedContent);
    }

    @Test
    @DisplayName("insert invalid text: invalid lineNumber/content")
    void TestInsertInvalidText() {
        // if lineNumber is out of range
        assertThrows(RuntimeException.class, () -> {
            this.textEditor.insert(-1, "content");
        });
        assertThrows(RuntimeException.class, () -> {
            this.textEditor.insert(10, "content");
        });

        // if the content is invalid
        assertThrows(RuntimeException.class, () -> {
            this.textEditor.insert(0, "#invalidtext");
        });
    }
    @Test
    @DisplayName("insert valid text: header/unorderedList/orderedList")
    void TestInsertValidText() {
        insertTextAndAssert(3, "+ unordederList");
        insertTextAndAssert(0, "# new header");
        insertTextAndAssert(4, "1. orderedList");
    }
    private void insertTextAndAssert(int lineNumber, String textToInsert) {
        textEditor.insert(lineNumber, textToInsert);
        List<String> rawFile = textEditor.getRawFile();
        assertEquals(textToInsert, rawFile.get(lineNumber));
    }

    @Test
    @DisplayName("invalid lineNumber: out of range")
    void TestInvalidDeleteLineNumber() {
        assertThrows(RuntimeException.class, () -> {
            this.textEditor.deleteByLineNumber(-1);
        });
        assertThrows(RuntimeException.class, () -> {
            this.textEditor.deleteByLineNumber(3);
        });
    }
    @Test
    @DisplayName("delete by lineNumber")
    void TestDeleteByLineNumber() {
        this.textEditor.deleteByLineNumber(0);
        this.contentList.remove(0);

        // test texts in the file
        List<String> fileContent = this.textEditor.getRawFile();
        assertEquals(this.contentList, fileContent);
    }
    @Test
    @DisplayName("delete by content")
    void TestDeleteByContent() {
        TextFactory textFactory = new TextFactory();
        // no matching content: remain the same
        Text text = textFactory.createText("# no matching content");
        ArrayList<Integer> lineNumberList = new ArrayList<>();
        ArrayList<Text> textList = new ArrayList<>();
        this.textEditor.deleteByContent(text.getContent(), lineNumberList, textList);
        ArrayList<Integer> myList = new ArrayList<>();
        List<String> fileContent = this.textEditor.getRawFile();
        assertEquals(this.contentList, fileContent);
        assertEquals(lineNumberList, myList);

        // several content matches
        this.textEditor.insert(0, "# header1");
        text = textFactory.createText("# header1");
        this.textEditor.deleteByContent(text.getContent(), lineNumberList, textList);
        fileContent = this.textEditor.getRawFile();
        this.contentList.remove(0);
        myList.add(1);  // 添加整数1
        myList.add(0);
        assertEquals(this.contentList, fileContent);
        assertEquals(lineNumberList, myList);
        assertEquals(lineNumberList.size(), textList.size());
    }

}