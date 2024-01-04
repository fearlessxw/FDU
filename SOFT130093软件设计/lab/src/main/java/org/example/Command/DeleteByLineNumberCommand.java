package org.example.Command;

import org.example.Editor.Editor;
import org.example.Text.Text;

public class DeleteByLineNumberCommand extends EditCommand{
    protected Editor textEditor;
    private int lineNumber;
    private Text text;

    public DeleteByLineNumberCommand(Editor textEditor, int lineNumber) {
        this.textEditor = textEditor;
        this.lineNumber = lineNumber;
    }
    @Override
    public void execute() {
        this.text = this.textEditor.deleteByLineNumber(this.lineNumber);
    }

    @Override
    public void undo() {
        this.textEditor.insert(this.lineNumber, this.text.getRawText());
    }
}
