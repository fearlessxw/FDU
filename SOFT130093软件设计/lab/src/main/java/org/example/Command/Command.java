package org.example.Command;

import java.io.Serializable;

public interface Command extends Serializable {
    void execute();
    void undo();
}
