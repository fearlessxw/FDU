package org.example.Command;

import org.example.Editor.Editor;
import org.example.Text.Text;
import org.example.Text.TextFactory;

import java.util.ArrayList;

public class DeleteByContentCommand extends EditCommand{
    protected Editor textEditor;
    private ArrayList<Integer> lineNumberList;
    private ArrayList<Text> textList;
    private String content;
    public DeleteByContentCommand(Editor textEditor, String content) {
        this.textEditor = textEditor;
        this.content = content;
        this.lineNumberList = new ArrayList<>();
        this.textList = new ArrayList<>();
    }
    @Override
    public void execute() {
        this.textEditor.deleteByContent(this.content, this.lineNumberList, this.textList);
    }

    @Override
    public void undo() {
        for (int i = this.lineNumberList.size() - 1; i >= 0; i--) {
            int lineNumber = this.lineNumberList.get(i);
            Text text = this.textList.get(i);
            this.textEditor.insert(lineNumber, text.getRawText());
        }
    }
}
