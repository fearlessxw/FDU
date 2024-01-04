package org.example.Command;

import org.example.Editor.Editor;

public class AppendTailCommand extends InsertCommand {
    public AppendTailCommand(Editor textEditor, int lineNumber, String content) {
        super(textEditor, lineNumber, content);
    }
}
