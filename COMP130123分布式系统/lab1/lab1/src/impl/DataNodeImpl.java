package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

public class DataNodeImpl extends DataNodePOA {
    private static int id;
    private int nextBlockId;
    private List<Integer> blockIdList;
    public DataNodeImpl(int id) {
        this.id = id;
        this.blockIdList = getBlockIdList();
    }
    private List<Integer> getBlockIdList() {
        String directoryPath = "../DataNodes/DataNode" + id;
        File directory = new File(directoryPath);

        List<Integer> blockIdList = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            int maxNumber = Integer.MIN_VALUE;
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (fileName.matches("\\d+.dat")) {
                            int number = Integer.parseInt(fileName.substring(0, fileName.length() - 4));
                            blockIdList.add(number);
                        }
                    }
                }
            }
            System.out.println(blockIdList);
        } else {
            boolean created = directory.mkdir();
            System.out.println("create new directory!!!");
        }
        return blockIdList;
    }
    @Override
    public byte[] read(int block_id) {
        System.out.println("read:" + block_id);
        if (block_id == -1) {
            return null;
        }
        String filePath = "../DataNodes/DataNode" + id + "/" + block_id + ".txt";

        try (FileInputStream fis = new FileInputStream(filePath)) {
            // int size = fis.available();
            byte[] bytes = new byte[4*1024];
            fis.read(bytes);
            System.out.println(bytes);
            String readString = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(readString);
            return bytes;
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
            return new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        String filePath = "../DataNodes/DataNode" + id + "/" + block_id + ".txt";

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            int filledBytes = this.getFilledSize(bytes);
            byteArrayOutputStream.write(bytes, 0, filledBytes);

            try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
                byteArrayOutputStream.writeTo(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getFilledSize(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                return i;
            }
        }
        return bytes.length;
    }

    @Override
    public int randomBlockId() {
        Random random = new Random();
        int randomNumber = 0;
        while (true) {
            randomNumber = random.nextInt(1000);
            if (!blockIdList.contains(randomNumber)) {
                break;
            }
        }
        return randomNumber;
    }
    public void createBlock(int block_id) {
        String filePath = "../DataNodes/DataNode" + id + "/" + block_id + ".txt";

        // create a new file
        try {
            File file = new File(filePath);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add to blockIdList
        this.blockIdList.add(block_id);
    }

    public int getBlockSize(int block_id) {
        if (block_id == -1) {
            return 0;
        }
        String filePath = "../DataNodes/DataNode" + id + '/' + block_id + ".txt";

        File file = new File(filePath);
        if (file.exists()) {
            int fileSize = (int)file.length(); // 获取文件大小
            return fileSize;
        }
        return -1;
    }
}
