package org.example.Memento;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WorkSpacesMemento implements Serializable {
    private Map<String, WorkSpaceMemento> mementos = new HashMap<>();
    private String activeFileName = null;
    private String currentDirectory = "data/";

    public void saveMemento(String fileName, WorkSpaceMemento memento) {
        mementos.put(fileName, memento);
    }
    public Map<String, WorkSpaceMemento> getMementos() {
        return mementos;
    }
    public void saveActiveFileName(String fileName) {
        this.activeFileName = fileName;
    }
    public String getActiveFileName() {
        return activeFileName;
    }
    public String getCurrentDirectory() {
        return currentDirectory;
    }
    public void saveCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            return bos.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
            bos.close();
        }
    }
    public static WorkSpacesMemento deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (WorkSpacesMemento) in.readObject();
        } finally {
            bis.close();
            if (in != null) {
                in.close();
            }
        }
    }
}
