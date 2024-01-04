package org.example.Text;

import java.io.Serializable;

public interface Text extends Serializable {
    public String getContent();
    public String getRawText();
    public void list();
    public void list_tree();
    public boolean isEqual(Text text);
}
