package org.example.WorkSpace;

import org.example.Command.*;
import org.example.Logger.Logger;
import org.example.Memento.CareTaker;
import org.example.Memento.WorkSpaceMemento;
import org.example.Memento.WorkSpacesMemento;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class WorkSpaceManager {
    HashMap<String, CommandManager> WorkSpaceMap;
    String activeFileName;
    CommandManager activeWorkSpace;
    Logger logger;
    String currentDirectory;
    public WorkSpaceManager() {
        this.WorkSpaceMap = new HashMap<String, CommandManager>();
        this.logger = new Logger("session.log", "stats.log");
        this.activeWorkSpace = null;
        this.activeFileName = null;
        this.currentDirectory = "data/";
        // loadFromMemento();
    }
    public String getActiveFileName() { return this.activeFileName; }

    public void ParseCommand(String text) {
        String[] parts = text.split(" ", 2);

        String commandType = parts[0];
        String filePath;
        String content;

        // TODO: assure that you have loaded a specific file before editing

        switch (commandType) {
            case "load" -> {
                if (parts.length == 2 && IsValidFilePath(parts[1])) {
                    filePath = this.currentDirectory +parts[1];
                    this.loadCommand(filePath);
                    this.logger.logCommand(text);
                } else {
                    System.out.println("Invalid load command format.");
                }
            }
            case "stats" -> {
                if (parts.length == 1 || parts[1].equals("current")) {
                    this.logger.statsCurrent(activeFileName + ".md");
                } else if (parts[1].equals("all")) {
                    this.logger.statsAll();
                } else {
                    System.out.println("Invalid stats command format.\"");
                }
            }
            case "history" -> {
                if (parts.length == 1) {
                    this.logger.history(-1);
                }
                else {
                    if (parts[1].matches("\\d+")) {
                        int num = Integer.parseInt(parts[1]);
                        this.logger.history(num);
                    } else {
                        System.out.println("Invalid history command format.\"");
                    }
                }
            }
            case "list-workspace" -> {
                // list all workspace and use * to note the unsaved file
                if (parts.length == 1) {
                    this.listWorkSpaceCommand();
                    this.logger.logCommand(text);
                } else {
                    System.out.println("Invalid list-workspace command format.");
                }
            }
            case "open" -> {
                // set active workspace
                if (parts.length == 2) {
                    String fileName = parts[1];
                    if (openCommand(fileName)) {
                        this.logger.logCommand(text);
                    }
                } else {
                    System.out.println("Invalid open command format.");
                }

            }
            case "close" -> {
                // close a workspace; ask whether save
                if (parts.length == 1) {
                    closeCommand();
                    this.logger.logCommand(text);
                } else {
                    System.out.println("Invalid close command format.\"");
                }
            }
            case "exit" -> {
                // exit; ask whether save all the file
                if (parts.length == 1) {
                    this.exitCommand();
                    this.logger.logCommand(text);
                } else {
                    System.out.println("Invalid exit command format.\"");
                }
            }
            case "ls" -> {
                // list the file system
                if (parts.length == 1) {
                    this.lsCommand();
                }
            }
            default -> {
                if (this.activeWorkSpace == null) {
                    System.out.println("Please create a workspace first.");
                } else {
                    if(this.activeWorkSpace.ParseCommand(text)) {
                        this.logger.logCommand(text);
                    }
                }
            }
        }
    }

    private void loadCommand(String filePath) {
        String fileName = filePath.substring(5, filePath.length()-3);
        // check whether the file has been loaded
        Set<String> keys = this.WorkSpaceMap.keySet();
        for (String key : keys) {
            if (key.equals(fileName)) {
                return;
            }
        }

        // create a new workspace and load the file
        CommandManager newWorkSpace = new CommandManager(filePath);
        this.logger.load(fileName+".md");
        this.WorkSpaceMap.put(fileName, newWorkSpace);

        // set active workspace
        this.activeWorkSpace = this.WorkSpaceMap.get(fileName);
        this.activeFileName = fileName;
        System.out.println(this.activeFileName + ".md");

    }
    private void listWorkSpaceCommand() {
        for (Map.Entry entry: this.WorkSpaceMap.entrySet()) {
            String key = (String) entry.getKey();
            CommandManager workSpace = (CommandManager) entry.getValue();
            if (this.activeWorkSpace == workSpace) {
                System.out.print("->" + key);
            } else {
                System.out.print("  " + key);
            }
            if (workSpace.isSaved()) {
                System.out.println();
            } else {
                System.out.println("*");
            }
        }
    }
    private boolean openCommand(String fileName) {
        CommandManager workSpace = this.WorkSpaceMap.get(fileName);
        if (workSpace == null) {
            System.out.println("Please load the file first!");
            return false;
        } else {
            this.activeWorkSpace = workSpace;
            this.activeFileName = fileName;
            return true;
        }
    }
    private void closeCommand() {
        if (this.activeWorkSpace == null) {
            System.out.println("Please assign the active workspace first!");
            return;
        }
        if (!this.activeWorkSpace.isSaved()) {
            Scanner scanner = new Scanner(System.in);
            String userInput;
            while (true) {
                System.out.print("Do you want to save the current workspace [Y/N]?");
                userInput = scanner.nextLine();
                if (userInput.equals("Y")) {
                    this.activeWorkSpace.ParseCommand("save");
                } else if (!userInput.equals("N")) {
                    System.out.println("Please input Y/N");
                    continue;
                }
                break;
            }
        }
        this.WorkSpaceMap.remove(this.activeFileName);
        this.logger.close(this.activeFileName + ".md");
        this.activeWorkSpace = null;
        this.activeFileName = null;
    }
    private void exitCommand() {
        // is there unsaved file
        boolean isSave = false;
        for (Map.Entry<String, CommandManager> entry : this.WorkSpaceMap.entrySet()) {
            CommandManager workSpace = entry.getValue();
            if (workSpace.isSaved() == false) {
                Scanner scanner = new Scanner(System.in);
                String userInput;
                while (true) {
                    System.out.print("Do you want to save the unsaved workspace [Y/N]?");
                    userInput = scanner.nextLine();
                    if (userInput.equals("Y")) {
                        isSave = true;
                        break;
                    } else if (userInput.equals("N")) {
                        isSave = false;
                        break;
                    } else {
                        System.out.println("Please input Y/N");
                    }
                }
            }
        }

        for (Map.Entry<String, CommandManager> entry : this.WorkSpaceMap.entrySet()) {
            CommandManager workSpace = entry.getValue();
            if ((isSave == true) && (workSpace.isSaved() == false)) {
                workSpace.ParseCommand("exit");
            }
            this.logger.close(entry.getKey() + ".md");
        }

        saveToMemento();

        this.WorkSpaceMap = new HashMap<String, CommandManager>();
        this.activeWorkSpace = null;
        this.activeFileName = null;

    }
    private void lsCommand() {
        lsDirectory(this.currentDirectory, 0, "");
    }
    private void lsDirectory(String filePath, int level, String indent) {
        File file = new File(filePath);
        File[] tempList = file.listFiles();
        Set<String> keySet = this.WorkSpaceMap.keySet();

        for (int i = 0; i < tempList.length; i++) {
            if(tempList[i].getName().equals(".DS_Store")) {
                continue;
            }
            System.out.print(indent);
            if (tempList[i].isFile()) {
                String realFilePath = filePath+tempList[i].getName();
                realFilePath = realFilePath.substring(5, realFilePath.length()-3);

                if (i == tempList.length-1) {
                    System.out.print("└── ");
                } else {
                    System.out.print("├── ");
                }
                if (keySet.contains(realFilePath)) {
                    System.out.println(tempList[i].getName().substring(tempList[i].getName().lastIndexOf("\\")+1) + " *");
                } else {
                    System.out.println(tempList[i].getName().substring(tempList[i].getName().lastIndexOf("\\")+1));
                }
            }
            if (tempList[i].isDirectory()) {
                if (i == tempList.length-1) {
                    System.out.println("└── " + tempList[i].getName());
                    lsDirectory(filePath + "/" +tempList[i].getName(), level+1, indent + "    ");
                } else {
                    System.out.println("├── " + tempList[i].getName());
                    lsDirectory(filePath + "/" +tempList[i].getName(), level+1, indent + "│   ");
                }
            }
        }
    }

    public void saveToMemento() {
        try {
            WorkSpacesMemento workSpacesMemento = createMemento();
            CareTaker careTaker = new CareTaker();
            careTaker.saveMementoToFile(workSpacesMemento);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public WorkSpacesMemento createMemento() {
        WorkSpacesMemento workSpacesMemento = new WorkSpacesMemento();
        workSpacesMemento.saveActiveFileName(this.activeFileName);
        workSpacesMemento.saveCurrentDirectory(this.currentDirectory);

        for (Map.Entry<String, CommandManager> entry : this.WorkSpaceMap.entrySet()) {
            CommandManager commandManager = entry.getValue();
            WorkSpaceMemento workSpaceMemento = commandManager.createMemento();
            workSpacesMemento.saveMemento(entry.getKey(), workSpaceMemento);
        }

        return workSpacesMemento;
    }
    public void loadFromMemento() {
        try {
            CareTaker careTaker = new CareTaker();
            WorkSpacesMemento workSpacesMemento = careTaker.loadMementoFromFile();
            restoreMemento(workSpacesMemento);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void restoreMemento(WorkSpacesMemento workSpacesMemento) {
        this.activeFileName = workSpacesMemento.getActiveFileName();
        this.currentDirectory = workSpacesMemento.getCurrentDirectory();

        Map<String, WorkSpaceMemento> mementos =  workSpacesMemento.getMementos();
        for (Map.Entry<String, WorkSpaceMemento> entry : mementos.entrySet()) {
            WorkSpaceMemento workSpaceMemento = entry.getValue();
            CommandManager commandManager = new CommandManager(workSpaceMemento.getFilePath());
            commandManager.restoreCommandManager(workSpaceMemento);
            this.WorkSpaceMap.put(entry.getKey(), commandManager);
            this.logger.load(entry.getKey()+".md");
        }

        this.activeWorkSpace = this.WorkSpaceMap.get(this.activeFileName);
    }

    public int getWorkSpaceCount() {
        return this.WorkSpaceMap.size();
    }
    private boolean IsValidFilePath(String filePath) {
        String pattern = "^[a-zA-Z0-9_.-]+.md$";
        return Pattern.matches(pattern, filePath);
    }
}
