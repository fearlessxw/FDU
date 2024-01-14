package impl;
//TODO: your implementation
import api.NameNodePOA;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import utils.FileDesc;
import utils.BlockInfo;
import java.util.Iterator;
import java.time.LocalDateTime;

//import static org.junit.Assert.*;
//import org.jackson.core.com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;

public class NameNodeImpl extends NameNodePOA {
    public static final int MAX_DATA_NODE = 2;
    private Map<String, FileDesc> fileSystemMap;
    private List<String> writeFileList;
    private static final String FsImagePath = "../FsImage.txt";

    public NameNodeImpl() {
        this.fileSystemMap = new HashMap<>();
        this.loadFsImage(FsImagePath);
        this.writeFileList = new ArrayList<>();

    }

    @Override
    public String open(String filepath, int mode) {
        // if mode is 'w' and the filePath is being written by other clients
        if ((mode == 0b11 || mode == 0b10) && this.writeFileList.contains(filepath)) {
            return "";
        }

        // check whether the file exists; if not, create a new file
        FileDesc fileDesc = this.fileSystemMap.get(filepath);
        if (fileDesc == null) {
            fileDesc = new FileDesc();
            fileDesc.setFilepath(filepath);
            // assign a datanode, initialize the blockid as -1
            Random random = new Random();
            int datanode = random.nextInt(MAX_DATA_NODE);
            BlockInfo blockInfo = new BlockInfo(datanode, -1);
            fileDesc.appendBlock(blockInfo);
            this.fileSystemMap.put(filepath, fileDesc);
            this.saveFsImage();
            // System.out.println("create a new fileDesc..." );
        }

        // if the mode is 'w', add the filePath to writeFileList
        if (mode == 0b11 || mode == 0b10) {
            this.writeFileList.add(filepath);
            // System.out.println("add the filepath to writeFileList...");
        }

        return fileDesc.toString();
    }

    @Override
    public void close(String fileInfo) {
        FileDesc newFileDesc = new FileDesc();
        newFileDesc = newFileDesc.fromString(fileInfo);

        String filepath = newFileDesc.getFilepath();
        // remove it from writeList
        Iterator<String> iterator = writeFileList.iterator();
        while(iterator.hasNext()){
            String next = iterator.next();
            if(next.equals(filepath)){
                iterator.remove();
            }
        }

        // update BlockInfoList and time
        FileDesc fileDesc = fileSystemMap.get(filepath);
        if (fileDesc.getLastModifyTime().isBefore(newFileDesc.getLastModifyTime())) {
            fileDesc.setLastModifyTime(newFileDesc.getLastModifyTime());
            fileDesc.setBlockIdList(newFileDesc.getBlockIdList());
        }
        if (fileDesc.getLastVisitTime().isBefore(newFileDesc.getLastVisitTime())) {
            fileDesc.setLastVisitTime(newFileDesc.getLastVisitTime());
        }

        this.fileSystemMap.put(filepath, fileDesc);
        saveFsImage();
    }




    private void loadFsImage(String FsImagePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FsImagePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                FileDesc fileDesc = new FileDesc();
                fileDesc = fileDesc.fromString(line);
                this.fileSystemMap.put(fileDesc.getFilepath(), fileDesc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveFsImage() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FsImagePath))) {
            for (Map.Entry<String, FileDesc> entry : this.fileSystemMap.entrySet()) {
                writer.println(entry.getValue().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
