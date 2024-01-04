package org.example.Command;
import org.example.Editor.Editor;
import org.example.Logger.Logger;
import org.example.Memento.WorkSpaceMemento;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private List<Command> Commands;
    private int currentCommandIndex;
    private Editor textEditor;
    private boolean isSaved;
    public CommandManager(String filePath){
        this.currentCommandIndex = -1;
        this.Commands = new ArrayList<>();
        this.isSaved = true;

        this.textEditor = new Editor();
        Command loadCommand = new LoadCommand(this.textEditor, filePath);
        this.execute(loadCommand);
    }
    public List<Command> getCommands() { return this.Commands; }
    public int getCommandsLength() { return this.Commands.size(); }
    public int getCurrentCommandIndex() { return this.currentCommandIndex; }
    public boolean isSaved() {
        return this.isSaved;
    }

    public boolean ParseCommand(String text) {
        String[] parts = text.split(" ", 2);

        String commandType = parts[0];
        String filePath;
        String content;

        // assure that you have loaded a specific file before editing
        if (!commandType.equals("load") && !commandType.equals("history") && !commandType.equals("stats") && textEditor == null) {
            System.out.println("Please load a file first.");
            return false;
        }

        switch (commandType) {
            case "save" -> {
                if (parts.length == 1) {
                    Command saveCommand = new SaveCommand(this.textEditor);
                    this.execute(saveCommand);

                    // clear the history commands
                    // this.Commands = new ArrayList<>();
                    // this.currentCommandIndex = -1;

                    return true;
                } else {
                    System.out.println("Invalid save command format.");
                }
            }
            case "exit" -> {
                if (parts.length == 1) {
                    Command saveCommand = new SaveCommand(this.textEditor);
                    this.execute(saveCommand);

                    return false;
                } else {
                    System.out.println("Invalid exit command format.");
                }
            }
            case "insert" -> {
                if (parts.length == 2) {
                    String[] contentParts = parts[1].split(" ",2);
                    int lineNumber = 0;
                    if (contentParts[0].matches("\\d+")) {
                        lineNumber = Integer.parseInt(contentParts[0]);
                        content = contentParts[1];
                    } else {
                        content = parts[1];
                    }
                    try {
                        Command insertCommand = new InsertCommand(this.textEditor, lineNumber-1, content);
                        this.execute(insertCommand);
                        return true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid insert command format.");
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Invalid insert command format.");
                }
            }
            case "append-head" -> {
                if (parts.length == 2) {
                    try {
                        Command appendHeadCommand = new AppendHeadCommand(this.textEditor, parts[1]);
                        this.execute(appendHeadCommand);
                        return true;
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Invalid append-head command format.");
                }
            }
            case "append-tail" -> {
                if (parts.length == 2) {
                    try {
                        Command appendTailCommand = new AppendTailCommand(this.textEditor, this.textEditor.getFileLength(), parts[1]);
                        this.execute(appendTailCommand);
                        return true;
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Invalid append-tail command format.");
                }
            }
            case "delete" -> {
                if (parts.length == 2) {
                    // delete by lineNumber
                    if (parts[1].matches("\\d+")){
                        try {
                            int lineNumber = Integer.parseInt(parts[1]);
                            Command deleteByLineNumberCommand = new DeleteByLineNumberCommand(this.textEditor, lineNumber-1);
                            this.execute(deleteByLineNumberCommand);
                            return true;
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    // delete by content
                    else {
                        try {
                            Command deleteByContentCommand = new DeleteByContentCommand(this.textEditor, parts[1]);
                            this.execute(deleteByContentCommand);
                            return true;
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Invalid delete command format.");
                }
            }
            case "undo" -> {
                if (parts.length == 1) {
                    if (this.currentCommandIndex >= 0) {
                        this.undo();
                        return true;
                    }
                } else {
                    System.out.println("Invalid undo command format.");
                }
            }
            case "redo" -> {
                if (parts.length == 1) {
                    if (this.currentCommandIndex < this.getCommandsLength()-1) {
                        this.redo();
                        return true;
                    }
                } else {
                    System.out.println("Invalid redo command format.");
                }
            }
            case "list" -> {
                if (parts.length == 1) {
                    Command listCommand = new ListCommand(this.textEditor);
                    this.execute(listCommand);
                    return true;
                } else {
                    System.out.println("Invalid list command format.");
                }
            }
            case "list-tree" -> {
                if (parts.length == 1) {
                    Command listTreeCommand = new ListTreeCommand(this.textEditor);
                    this.execute(listTreeCommand);
                    return true;
                } else {
                    System.out.println("Invalid list-tree command format.");
                }
            }
            case "dir-tree" -> {
                try {
                    if (parts.length == 1) {
                        Command listTreeCommand = new ListTreeCommand(this.textEditor);
                        this.execute(listTreeCommand);
                    } else {
                        Command dirTreeCommand = new DirTreeCommand(this.textEditor, parts[1]);
                        this.execute(dirTreeCommand);
                    }
                    return true;
                } catch (RuntimeException e) {
                    // invalid content/no such content
                    System.out.println(e.getMessage());
                }
            }

            default -> {
                System.out.println("Unknown command: " + commandType);
            }
        }
        return false;
    }

    private void execute(Command command){
        if (command instanceof EditCommand) {
            this.Commands.add(this.currentCommandIndex + 1, command);
            this.Commands.subList(this.currentCommandIndex + 2, this.getCommandsLength()).clear();
            command.execute();
            this.currentCommandIndex = this.currentCommandIndex+1;
        } else {
            command.execute();
        }
    }

    private void redo(){
        this.currentCommandIndex = this.currentCommandIndex+1;
        Command commandToRedo = this.Commands.get(this.currentCommandIndex);
        commandToRedo.execute();
    }

    private void undo(){
        Command commandToUndo = this.Commands.get(this.currentCommandIndex);
        this.currentCommandIndex = this.currentCommandIndex - 1;
        commandToUndo.undo();
    }

    public WorkSpaceMemento createMemento() {
        String filePath = this.textEditor.getFilePath();
        return new WorkSpaceMemento(filePath, this.Commands, this.currentCommandIndex, this.isSaved, this.textEditor);
    }
    public void restoreCommandManager(WorkSpaceMemento workSpaceMemento) {
        this.currentCommandIndex = workSpaceMemento.getCurrentCommandIndex();
        this.Commands = workSpaceMemento.getCommands();
        this.textEditor = workSpaceMemento.getTextEditor();
        this.isSaved = workSpaceMemento.isSaved();
    }
}
