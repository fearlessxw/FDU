package org.example.Memento;

import java.io.*;

public class CareTaker {
    private static final String MEMENTO_FILE = "workspace_memento.ser";

    public void saveMementoToFile(WorkSpacesMemento memento) throws IOException {
        byte[] data = memento.serialize();
        try (FileOutputStream fileOut = new FileOutputStream(MEMENTO_FILE);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(data);
        }
    }

    public WorkSpacesMemento loadMementoFromFile() throws IOException, ClassNotFoundException {
        File file = new File(MEMENTO_FILE);
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fileIn = new FileInputStream(MEMENTO_FILE);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                byte[] data = (byte[]) in.readObject();
                return WorkSpacesMemento.deserialize(data);
            }
        }
        return new WorkSpacesMemento();
    }
}
