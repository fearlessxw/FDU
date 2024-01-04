package org.example.Text;

import java.util.Objects;

public class Header implements Text {
    private int level;
    private String content;

    public Header(String content, int level) {
        this.content = content;
        this.level = level;
    }

    public int getType() {
        return this.level;
    }

    @Override
    public String getContent() {
        return this.content;
    }
    @Override
    public String getRawText() {
        return "#".repeat(level) + " " + this.content;
    }

    @Override
    public void list() {
        System.out.println(this.getRawText());
    }
    @Override
    public void list_tree() {
        System.out.println(this.getContent());
    }

    @Override
    public boolean isEqual(Text text) {
        if (text instanceof Header) {
            return ((Header) text).getType() == this.getType() && Objects.equals(text.getContent(), this.getContent());
        }
        return false;
    }

}