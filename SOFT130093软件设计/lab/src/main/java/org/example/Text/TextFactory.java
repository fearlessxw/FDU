package org.example.Text;

public class TextFactory {
    public Text createText(String textContent) {
        String[] parts = textContent.split(" ", 2);

        if (parts.length<2) {
            throw new RuntimeException("Invalid Text!");
        }

        String textType = parts[0];
        if (textType.startsWith("#")) {
            int level = textType.split("#", -1).length - 1;
            if (level > 6) {
                throw new RuntimeException("Invalid Text!");
            }
            String content = parts[1];
            return new Header(content, level);
        } else if (textType.matches("\\d*\\.$")) {
            String content = parts[1];
            return new OrderedList(content, Integer.parseInt(textType.replaceFirst("\\.","")));
        } else if (textType.equals("*")||textType.equals("+")||textType.equals("-")) {
            String content = parts[1];
            return new UnorderedList(content, textType.charAt(0));
        } else {
            throw new RuntimeException("Invalid Text!");
        }
    }
}

