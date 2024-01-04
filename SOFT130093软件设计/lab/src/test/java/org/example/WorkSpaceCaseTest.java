package org.example;
import org.example.WorkSpace.WorkSpaceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkSpaceCaseTest {
    WorkSpaceManager workSpaceManager = new WorkSpaceManager();
    @BeforeEach
    void startup() {deleteFile();}

    @Test
    void TestCase0() {
        List<String> commands = Arrays.asList("load lab2test1.md", "append-head # 新的标题1", "save", "load lab2test2.md", "append-head # 新的标题2");
        runCommands(commands);
        testDisplay("list-workspace", "  lab2test1\n->lab2test2*\n");

        commands = Arrays.asList("open lab2test1");
        runCommands(commands);
        testDisplay("list-workspace", "->lab2test1\n  lab2test2*\n");

        commands = Arrays.asList("close");
        runCommands(commands);
        testDisplay("list-workspace", "  lab2test2*\n");
    }

    @Test
    void TestCase1() {
        List<String> commands = Arrays.asList("load test1.md", "append-head # 旅行清单", "append-tail ## 亚洲", "append-tail 1. 中国",
                "append-tail 2. 日本", "load test2.md", "append-head # 书籍推荐", "append-tail ## 编程",
                "append-tail * 《设计模式的艺术》", "append-tail * 《云原生：运用容器、函数计算和数据构建下一代应用》",
                "append-tail * 《深入理解Java虚拟机》");
        runCommands(commands);
        testDisplay("list-tree", "└── 书籍推荐\n" +
                " \t└── 编程\n" +
                " \t \t├── ·《设计模式的艺术》\n" +
                " \t \t├── ·《云原生：运用容器、函数计算和数据构建下一代应用》\n" +
                " \t \t└── ·《深入理解Java虚拟机》\n");
        testDisplay("list-workspace", "->test2*\n" + "  test1*\n");

        commands = Arrays.asList("save");
        runCommands(commands);
        testDisplay("list-workspace", "->test2\n" + "  test1*\n");

        commands = Arrays.asList("open test1");
        runCommands(commands);
        testDisplay("list-workspace", "  test2\n" + "->test1*\n");
    }


    private void testDisplay(String command, String target) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        this.workSpaceManager.ParseCommand(command);
        assertEquals(target, outputStream.toString());
    }
    private void runCommands(List<String> commands) {
        for (String command:commands) {
            this.workSpaceManager.ParseCommand(command);
        }
    }
    private void deleteFile() {
        List<String> filePaths = Arrays.asList("data/lab2test1.md", "data/lab2test2.md", "data/test1.md", "data/test2.md", "data/test3.md", "data/test4.md", "data/test5.md");
        for (String filePath:filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
