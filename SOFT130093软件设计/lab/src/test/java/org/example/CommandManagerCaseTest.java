package org.example;

import org.example.Command.CommandManager;
import org.example.Text.Text;
import org.example.WorkSpace.WorkSpaceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandManagerCaseTest {
    CommandManager commandManager;
    WorkSpaceManager workSpaceManager;

    @BeforeEach
    void SetUp() {
        this.workSpaceManager = new WorkSpaceManager();
        // this.commandManager = new CommandManager();
        deleteFile();
    }

    @Test
    public void TestCases() {
        TestCase1();
        TestCase2();
        TestCase3();
        TestCase4();
    }

    private void TestCase1() {
        List<String> commands = Arrays.asList("load test1.md", "insert ## 程序设计", "append-head # 我的资源", "append-tail ### 软件设计",
                "append-tail #### 设计模式", "append-tail 1. 观察者模式", "append-tail 3. 单例模式", "insert 6 2. 策略模式",
                "delete 单例模式", "append-tail 3. 组合模式");
        runCommands(commands);

        String target = "└── 我的资源\n \t└── 程序设计\n \t \t└── 软件设计\n \t \t \t└── 设计模式\n \t \t \t \t├── 1. 观察者模式\n \t \t \t \t├── 2. 策略模式\n \t \t \t \t└── 3. 组合模式\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("append-tail ## ⼯具箱", "append-tail ### Adobe");
        runCommands(commands);

        target = "└── 我的资源\n \t├── 程序设计\n \t│\t└── 软件设计\n \t│\t \t└── 设计模式\n \t│\t \t \t├── 1. 观察者模式\n \t│\t \t \t├── 2. 策略模式\n \t│\t \t \t└── 3. 组合模式\n \t└── ⼯具箱\n \t \t└── Adobe\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("save");
        runCommands(commands);
        target = "# 我的资源\n" +
                "## 程序设计\n" +
                "### 软件设计\n" +
                "#### 设计模式\n" +
                "1. 观察者模式\n" +
                "2. 策略模式\n" +
                "3. 组合模式\n" +
                "## ⼯具箱\n" +
                "### Adobe\n";
        testFile("data/test1.md", target);

    }
    private void TestCase2() {
        List<String> commands = Arrays.asList("load test2.md", "append-head # 旅⾏清单", "append-tail ## 亚洲", "append-tail 1. 中国",
                "append-tail 2. ⽇本", "delete 亚洲", "undo", "redo");
        runCommands(commands);
        String target = "└── 旅⾏清单\n \t├── 1. 中国\n \t└── 2. ⽇本\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("save");
        runCommands(commands);
        target = "# 旅⾏清单\n" +
                "1. 中国\n" +
                "2. ⽇本\n";
        testFile("data/test2.md", target);
    }
    private void TestCase3() {
        List<String> commands = Arrays.asList("load test3.md", "append-head # 书籍推荐", "append-tail * 《深入理解计算机系统》", "undo",
                "append-tail ## 编程", "append-tail * 《设计模式的艺术》", "redo");
        runCommands(commands);
        String target = "└── 书籍推荐\n \t└── 编程\n \t \t└── ·《设计模式的艺术》\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("append-tail * 《云原⽣：运⽤容器、函数计算和数据构建下⼀代应⽤》", "append-tail * 《深入理解Java虚拟机》",
                "undo", "redo");
        runCommands(commands);
        target = "└── 书籍推荐\n \t└── 编程\n \t \t├── ·《设计模式的艺术》\n \t \t├── ·《云原⽣：运⽤容器、函数计算和数据构建下⼀代应⽤》\n \t \t└── ·《深入理解Java虚拟机》\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("save");
        runCommands(commands);
        target = "# 书籍推荐\n" +
                "## 编程\n" +
                "* 《设计模式的艺术》\n" +
                "* 《云原⽣：运⽤容器、函数计算和数据构建下⼀代应⽤》\n" +
                "* 《深入理解Java虚拟机》\n";
        testFile("data/test3.md", target);
    }
    private void TestCase4() {
        List<String> commands = Arrays.asList("load test4.md", "append-head # 旅⾏清单", "append-tail ## 欧洲", "insert 2 ## 亚洲",
                "insert 3 1. 中国", "insert 4 2. ⽇本", "save", "undo");
        runCommands(commands);
        String target = "└── 旅⾏清单\n \t├── 亚洲\n \t│\t├── 1. 中国\n \t│\t└── 2. ⽇本\n \t└── 欧洲\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("delete 亚洲");
        runCommands(commands);
        target = "└── 旅⾏清单\n \t├── 1. 中国\n \t├── 2. ⽇本\n \t└── 欧洲\n";
        testDisplay("list-tree", target);

        List<String> targetCommands = Arrays.asList("list-tree", "delete 亚洲");
        testHistory(targetCommands, "history 2");

        commands = Arrays.asList("undo");
        runCommands(commands);
        target = "└── 旅⾏清单\n \t├── 亚洲\n \t│\t├── 1. 中国\n \t│\t└── 2. ⽇本\n \t└── 欧洲\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("redo");
        runCommands(commands);
        target = "└── 旅⾏清单\n \t├── 1. 中国\n \t├── 2. ⽇本\n \t└── 欧洲\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("redo");
        runCommands(commands);
        target = "└── 旅⾏清单\n \t├── 1. 中国\n \t├── 2. ⽇本\n \t└── 欧洲\n";
        testDisplay("list-tree", target);

        commands = Arrays.asList("save");
        runCommands(commands);
        target = "# 旅⾏清单\n" +
                "1. 中国\n" +
                "2. ⽇本\n" +
                "## 欧洲\n";
        testFile("data/test4.md", target);
    }

    private void runCommands(List<String> commands) {
        for (String command:commands) {
            this.workSpaceManager.ParseCommand(command);
        }
    }
    private void testDisplay(String command, String target) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        this.workSpaceManager.ParseCommand(command);
        assertEquals(target, outputStream.toString());
    }
    private void testFile(String filePath, String rawFile) {
        String fileContent=null;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            fileContent = content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(fileContent, rawFile);
    }
    private void deleteFile() {
        List<String> filePaths = Arrays.asList("data/test1.md", "data/test2.md", "data/test3.md", "data/test4.md", "data/test5.md");
        for (String filePath:filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    private void testHistory(List<String> commands, String historyCommand) {
        String pattern = "";
        for (String command:commands) {
            pattern += "\\d{8} \\d{2}:\\d{2}:\\d{2} " + command + '\n';
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        runCommands(Collections.singletonList(historyCommand));
        assertEquals(outputStream.toString().matches(pattern), true);
    }
}
