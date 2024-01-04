package org.example.Command;

import org.example.Editor.Editor;

public class AppendHeadCommand extends InsertCommand {
    public AppendHeadCommand(Editor textEditor, String content) {
        super(textEditor, 0, content);
    }
}
