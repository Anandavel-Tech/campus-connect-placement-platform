package storage;

import java.util.*;
import java.io.*;

public class FileStorageManager {
private static final String DATA_DIR = "data/";

public FileStorageManager() {
    File dir = new File(DATA_DIR);
    if (!dir.exists()) {
        dir.mkdir();
    }
}

public <T> void saveToFile(String filename, List<T> items) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
        for (T item : items) {
            writer.println(item.toString());
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public List<String> loadFromFile(String filename) {
    List<String> lines = new ArrayList<>();
    File file = new File(DATA_DIR + filename);
    if (!file.exists()) {
        return lines;
    }
    
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return lines;
}

public void appendToFile(String filename, String data) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + filename, true))) {
        writer.println(data);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}