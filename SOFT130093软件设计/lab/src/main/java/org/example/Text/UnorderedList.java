package org.example.Text;

import java.util.Objects;

public class UnorderedList implements Text {
    private String content;
    private char type;
    public UnorderedList(String content, char type) {
        this.content = content;
        this.type = type;
    }

    @Override
    public String getContent() {
        return this.content;
    }
    public char getType() { return this.type; }
    @Override
    public String getRawText() { return this.getType() + " " + this.content; }
    @Override
    public void list() {
        System.out.println(this.getRawText());
    }

    @Override
    public boolean isEqual(Text text) {
        if (text instanceof UnorderedList) {
            return ((UnorderedList) text).getType() == this.getType() && Objects.equals(text.getContent(), this.getContent());
        }
        return false;
    }

    @Override
    public void list_tree() {
        System.out.print("·");
        System.out.println(this.getContent());
    }
}