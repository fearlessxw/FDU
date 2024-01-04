package org.example.Text;

import org.example.Text.Text;

import java.util.Objects;

public class OrderedList implements Text {
    private String content;
    private int level;
    public OrderedList(String content, int level) {
        this.content = content;
        this.level = level;
    }

    @Override
    public String getContent() {
        return this.content;
    }
    public int getLevel() { return this.level; }
    @Override
    public String getRawText() { return this.level + ". " + this.content;}
    @Override
    public void list() {
        System.out.println(this.getRawText());
    }

    @Override
    public boolean isEqual(Text text) {
        if (text instanceof OrderedList) {
            return ((OrderedList) text).getLevel() == this.getLevel() && Objects.equals(text.getContent(), this.getContent());
        }
        return false;
    }

    @Override
    public void list_tree() {
        System.out.println(this.getRawText());
    }
}