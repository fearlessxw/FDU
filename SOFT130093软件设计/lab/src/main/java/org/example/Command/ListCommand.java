package org.example.Command;

import org.example.Editor.Editor;

public class ListCommand extends DisplayCommand{
    protected Editor textEditor;
    public ListCommand(Editor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void execute() {
        this.textEditor.list();
    }
}
