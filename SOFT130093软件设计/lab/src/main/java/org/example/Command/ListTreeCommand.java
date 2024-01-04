package org.example.Command;

import org.example.Editor.Editor;

public class ListTreeCommand extends DisplayCommand{
    protected Editor textEditor;
    public ListTreeCommand(Editor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void execute() {
        this.textEditor.listtree();
    }
}
