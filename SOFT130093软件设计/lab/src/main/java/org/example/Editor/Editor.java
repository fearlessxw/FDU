package org.example.Editor;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Collections;

import org.example.Text.Header;
import org.example.Text.Text;
import org.example.Text.TextFactory;

import java.io.IOException;

public class Editor implements Serializable {
    private List<Text> file;
    private String filePath;

    public Editor() {
        this.file = new ArrayList<>();
    }
    public String getFilePath() { return this.filePath; }
    public List<String> getRawFile() {
        List<String> rawFile = new ArrayList<>();
        for(Text text : this.file){
            rawFile.add(text.getRawText());
        }
        return rawFile;
    }
    public int getFileLength() { return this.file.size(); }

    public void list() {
        for(Text text : this.file){
            text.list();
        }
    }

    public void dirtree(String content) {
        Text rootText = null;
        for (Text text:this.file) {
            if (content.equals(text.getContent()) && text instanceof Header) {
                rootText = text;
            }
        }
        if (rootText == null) {
            throw new RuntimeException("No such header");
        }

        int rootLevel = ((Header)rootText).getType();

        // search text
        int startLineNumber = -1;
        int endLineNumber = -1;
        for (int i=0; i<this.file.size(); i++) {
            Text text = this.file.get(i);
            if (startLineNumber == -1) {
                // haven't found rootText
                if (rootText.isEqual(text)) {
                    startLineNumber = i;
                }
            } else {
                // have found rootText
                if (text instanceof Header && ((Header)text).getType()<=rootLevel) {
                    endLineNumber = i-1;
                    break;
                }
            }
        }
        if (startLineNumber == -1) {
            throw new RuntimeException("No such header");
        } else if (endLineNumber == -1) {
            endLineNumber = this.file.size()-1;
        }

        ArrayList<Integer> levelList = assignLevel(startLineNumber, endLineNumber);
        printTree(levelList, startLineNumber, endLineNumber);

    }
    public void listtree() {
        ArrayList<Integer> levelList = assignLevel(0, this.getFileLength()-1);
        printTree(levelList, 0, this.getFileLength()-1);

    }
    private boolean isNextLineSameLevel(ArrayList<Integer> levelList, int lineNumber) {
        if (lineNumber == levelList.size() - 1) {
            return false;
        }
        if (levelList.get(lineNumber) == levelList.get(lineNumber+1)) {
            return true;
        } else {
            return false;
        }
    }
    private boolean hasSameLevelHeader(ArrayList<Integer> levelList, int lineNumber) {
        int level = levelList.get(lineNumber);
        for(int i = lineNumber+1; i<levelList.size();i++) {
            if(levelList.get(i) < level) {
                return false;
            }
            if(levelList.get(i) == level) {
                return true;
            }
        }
        return false;
    }
    private void printTree(ArrayList<Integer> levelList, int startLineNumber, int endLineNumber) {
        ArrayList<Integer> prefixList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            prefixList.add(0);
        }
        for (int i=startLineNumber;i<=endLineNumber;i++) {
            Text text = this.file.get(i);

            // update prefix
            if (text instanceof Header) {
                for(int j = levelList.get(i-startLineNumber); j<6;j++) {
                    prefixList.set(j, 0);
                }
                if (hasSameLevelHeader(levelList, i-startLineNumber)) {
                    prefixList.set(levelList.get(i-startLineNumber)-1, 1);
                } else {
                    prefixList.set(levelList.get(i-startLineNumber)-1, 0);
                }
                // System.out.println(prefixList);
            }

            // print prefix
            for(int j=1;j<levelList.get(i-startLineNumber);j++) {
                if (prefixList.get(j-1)==1) {
                    System.out.print("│");
                } else {
                    System.out.print(" ");
                }
                System.out.print("\t");
            }

            // print text
            if (text instanceof Header) {
                if (hasSameLevelHeader(levelList, i-startLineNumber)) {
                    System.out.print("├── ");
                } else {
                    System.out.print("└── ");
                }
                text.list_tree();
            } else {
                if (isNextLineSameLevel(levelList, i-startLineNumber)) {
                    System.out.print("├── ");
                } else {
                    System.out.print("└── ");
                }
                text.list_tree();
            }
        }
    }
    private ArrayList<Integer> assignLevel(int startLineNumber, int endLineNumber) {
        ArrayList<Integer> levelList = new ArrayList<>();
        // assign level to all texts: 1-7
        int level = 1;
        int minLevel = 7;
        for (int i=startLineNumber;i<=endLineNumber;i++) {
            Text text = this.file.get(i);
            if (text instanceof Header) {
                level = ((Header) text).getType();
                levelList.add(level);
                if (level<minLevel) {
                    minLevel = level;
                }
            } else {
                levelList.add(level + 1);
            }
        }

        // reassign level to all texts: level - minLevel + 1
        for (int i=startLineNumber;i<=endLineNumber;i++) {
            levelList.set(i-startLineNumber, levelList.get(i-startLineNumber)-minLevel+1);
        }

        return levelList;
    }

    public void insert(int lineNumber, String content){
        if (lineNumber == -1) {
            lineNumber = this.file.size();
        }

        // if the lineNumber is out of range
        if (lineNumber < 0 || lineNumber > file.size()) {
            throw new RuntimeException("lineNumber is out of range!");
        }

        TextFactory factory = new TextFactory();
        Text text = factory.createText(content);
        this.file.add(lineNumber, text);
    };

    public void load(String filePath){
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                this.file.clear();
                TextFactory factory = new TextFactory();
                String line;
                while ((line = reader.readLine()) != null) {
                    Text text = factory.createText(line);
                    this.file.add(text);
                }
                this.filePath = filePath;
            } catch (IOException e) {
                System.out.println("IOE");
            } catch (RuntimeException e) {
                System.out.println("Invalid Text!");
            }
        } else {
            try {
                file.createNewFile();
                this.filePath = filePath;
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void save(){
        try {
            FileWriter fileWriter = new FileWriter(this.filePath);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            for (Text text: file) {
                writer.write(text.getRawText() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File creation failed.");
        }
    }

    public Text deleteByLineNumber(int lineNumber) {
        // delete last line
        if (lineNumber == -1) {
            lineNumber = this.file.size()-1;
        }

        // check whether lineNumber is out of range
        if (lineNumber < 0 || lineNumber >= file.size()) {
            throw new RuntimeException("lineNumber is out of range!");
        }
        return this.file.remove(lineNumber);
    }
    public void deleteByContent(String content, ArrayList<Integer> lineNumberList, ArrayList<Text> textList) {
        // iterate from the end of file
        for (int i = this.file.size()-1; i >= 0; i--) {
            Text text = this.file.get(i);
            if (content.equals(text.getContent())) {
                lineNumberList.add(i);
                textList.add(text);
                this.file.remove(i);
            }
        }
    }
}
