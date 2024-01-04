package org.example.Memento;

import org.example.Command.Command;
import org.example.Editor.Editor;
import org.example.WorkSpace.WorkSpaceManager;

import java.io.Serializable;
import java.util.List;

public class WorkSpaceMemento implements Serializable {
    private List<Command> Commands;
    private int currentCommandIndex;
    private String filePath;
    private Editor textEditor;

    private boolean isSaved;

    public WorkSpaceMemento(String filePath, List<Command> Commands, int currentCommandIndex, boolean isSaved, Editor editor) {
        this.filePath = filePath;
        this.Commands = Commands;
        this.currentCommandIndex = currentCommandIndex;
        this.textEditor = editor;
        this.isSaved = isSaved;
    }

    public List<Command> getCommands() {
        return Commands;
    }
    public void setCommands(List<Command> commands) {
        Commands = commands;
    }
    public int getCurrentCommandIndex() {
        return currentCommandIndex;
    }
    public void setCurrentCommandIndex(int currentCommandIndex) {
        this.currentCommandIndex = currentCommandIndex;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public Editor getTextEditor() {
        return this.textEditor;
    }
    public boolean isSaved() {
        return isSaved;
    }
    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
