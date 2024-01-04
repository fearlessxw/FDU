package org.example.Command;

import org.example.Editor.Editor;

public class SaveCommand extends FileCommand{
    protected Editor textEditor;
    public SaveCommand(Editor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void execute() {
        this.textEditor.save();
    }
}
