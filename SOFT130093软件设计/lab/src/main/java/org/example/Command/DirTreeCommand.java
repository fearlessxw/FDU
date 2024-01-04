package org.example.Command;

import org.example.Editor.Editor;

import java.util.concurrent.ThreadPoolExecutor;

public class DirTreeCommand extends DisplayCommand {
    protected Editor textEditor;
    private String content;
    public DirTreeCommand(Editor textEditor, String content) {
        this.textEditor = textEditor;
        this.content = content;
    }

    @Override
    public void execute() {
        this.textEditor.dirtree(this.content);
    }
}
