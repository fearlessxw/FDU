package org.example.Command;
import org.example.Editor.Editor;

public class InsertCommand extends EditCommand{
    protected Editor textEditor;
    private int lineNumber;
    private String content;

    public InsertCommand(Editor textEditor, int lineNumber, String content) {
        this.textEditor = textEditor;
        this.lineNumber = lineNumber;
        this.content = content;
    }

    @Override
    public void execute() {
        this.textEditor.insert(this.lineNumber, this.content);
    }

    @Override
    public void undo() {
        this.textEditor.deleteByLineNumber(this.lineNumber);
    }
}
