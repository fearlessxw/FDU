package org.example.Command;

import org.example.Editor.Editor;

public class LoadCommand extends FileCommand{
    protected Editor textEditor;
    private String filePath;
    public LoadCommand(Editor textEditor, String filePath) {
        this.textEditor = textEditor;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        this.textEditor.load(this.filePath);
    }
}
