package impl;
//TODO: your implementation
import api.*;
import impl.*;
import utils.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.lang.NullPointerException;
import java.lang.SecurityException;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

public class ClientImpl implements Client{
    private NameNode nameNode;
    public static final int MAX_DATA_NODE = 2;
    private DataNode[] dataNodes = new DataNode[MAX_DATA_NODE];
    private Map<Integer, Pair<String, FileDesc, Integer>> fdToFileMap;
    private int nextFileDescriptor;

    public ClientImpl() {
        // Arrays.fill(ids, -1);
        try {
            String[] args = {};
            Properties properties = new Properties();
            properties.put("org.omg.COBRA.ORBInitialHost", "127.0.0.1");
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");
            // new ORB object
            ORB orb = ORB.init(args, properties);

            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef) ;

            // obtain a remote object
            nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("NameNode is obtained");


            for(int dataNodeId = 0; dataNodeId < MAX_DATA_NODE; dataNodeId++) {
                dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
                System.out.println("DataNode" + dataNodeId + " is obtained.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.fdToFileMap = new HashMap<>();
        this.nextFileDescriptor = 0;
    }

    @Override
    public int open(String filepath, int mode) {
        // System.out.println("ClientImp" + filepath + ", " + mode);
        // mode: 10 write, 01 read, 11 rw
        String metadata = this.nameNode.open(filepath, mode);

        // check whether the return value is null
        if (metadata.equals("")) {
            return -1;
        } else {
            // transfer into fileDesc
            FileDesc fileDesc = new FileDesc();
            fileDesc = fileDesc.fromString(metadata);

            Pair<String, FileDesc, Integer> pair = new Pair<>(filepath, fileDesc, mode);
            this.nextFileDescriptor++;
            this.fdToFileMap.put(this.nextFileDescriptor, pair);
            return this.nextFileDescriptor;
        }
    }

    @Override
    public void append(int fd, byte[] bytes) {
        Pair<String, FileDesc, Integer> pair = this.fdToFileMap.get(fd);

        // check whether fd is valid
        if (pair == null) {
            throw new NullPointerException();
        }
        // check whether open with 'w' or 'rw'
        if (pair.getThird() == 0b01) {
            throw new SecurityException();
        }

        // append to the last block first...
        List<BlockInfo> blockIdList = pair.getSecond().getBlockIdList();
        BlockInfo blockInfo = blockIdList.get(blockIdList.size() - 1);

        int chunkSize = 4 * 1024;
        DataNode dataNode = dataNodes[blockInfo.getDataNode()];
        int blockSize = dataNode.getBlockSize(blockInfo.getBlockId());
        int remainSize = chunkSize - blockSize;
        int length = Math.min(remainSize, bytes.length);
        byte[] firstChunk = new byte[chunkSize];
        System.arraycopy(bytes, 0, firstChunk, 0, length);
        // if block_id == -1
        if (blockInfo.getBlockId() == -1) {
            int newBlockId = dataNode.randomBlockId();

            // update DataNode
            dataNode.createBlock(newBlockId);

            // update blockIdList at client(update fileDesc at client later)
            blockInfo.setBlockId(newBlockId);
            blockIdList.set(blockIdList.size()-1, blockInfo);
        }
        dataNode.append(blockInfo.getBlockId(), firstChunk);

        // if remainSize < bytes.length
        if (remainSize < bytes.length) {
            int remainLength = bytes.length - remainSize;
            byte[] remainingBytes = new byte[remainLength];
            System.arraycopy(bytes, firstChunk.length, remainingBytes, 0, remainLength);

            for (int i=0;i<remainingBytes.length;i+=chunkSize) {
                int minlength = Math.min(chunkSize, remainingBytes.length - i);
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(remainingBytes, i, chunk, 0, minlength);

                int newBlockId = dataNode.randomBlockId();
                // update DataNode
                dataNode.createBlock(newBlockId);
                // update blockIdList at client(update fileDesc at client later)
                BlockInfo newBlockInfo = new BlockInfo(blockInfo.getDataNode(), newBlockId);
                blockIdList.add(newBlockInfo);

                dataNode.append(newBlockId, chunk);
            }
        }

        // update fileDesc
        LocalDateTime currentTime = LocalDateTime.now();
        FileDesc fileDesc = pair.getSecond();
        fileDesc.setLastModifyTime(currentTime);
        fileDesc.setBlockIdList(blockIdList);
        pair.setSecond(fileDesc);
        this.fdToFileMap.put(fd, pair);

        System.out.println("INFO: write done");
    }

    @Override
    public byte[] read(int fd) {
        Pair<String, FileDesc, Integer> pair = this.fdToFileMap.get(fd);

        if (pair == null) {
            throw new NullPointerException();
        }
        if (pair.getThird() == 0b10) {
            throw new SecurityException();
        }

        List<BlockInfo> blockIdList = pair.getSecond().getBlockIdList();
        int blockCount = blockIdList.size();
        int chunkSize = 4 * 1024;
        byte[] bytes = new byte[blockCount * chunkSize];
        for(int i = 0; i<blockCount; i++) {
            BlockInfo blockInfo = blockIdList.get(i);
            DataNode dataNode = dataNodes[blockInfo.getDataNode()];
            int block_id = blockInfo.getBlockId();
            if (block_id == -1) {
                break;
            }
            byte[] readBytes = dataNode.read(block_id);
            System.arraycopy(readBytes, 0, bytes, i*chunkSize, chunkSize);
        }

        // modify lastVisitTime
        LocalDateTime currentTime = LocalDateTime.now();
        FileDesc fileDesc = pair.getSecond();
        fileDesc.setLastVisitTime(currentTime);
        pair.setSecond(fileDesc);
        this.fdToFileMap.put(fd, pair);

        int length = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                length = i;
                break;
            }
        }
        byte[] newArray = Arrays.copyOf(bytes, length);

        return newArray;
    }

    @Override
    public void close(int fd) {
        Pair<String, FileDesc, Integer> pair = this.fdToFileMap.get(fd);
        if (pair == null) {
            throw new NullPointerException();
        }

        nameNode.close(pair.getSecond().toString());

        // remove from Map
        removeByKey(fd);
    }
    public void exit() {
        for (Map.Entry<Integer, Pair<String, FileDesc, Integer>> entry : this.fdToFileMap.entrySet()) {
            close(entry.getKey());
        }
    }

    private class Pair<A, B, C> {
        private A first;
        private B second;
        private C third;

        public Pair(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public A getFirst() {
            return first;
        }
        public B getSecond() {
            return second;
        }
        public C getThird() {
            return third;
        }
        public void setSecond(B second) { this.second = second; }
    }
    private void removeByKey(int fd) {
        Iterator iter = fdToFileMap.keySet().iterator();
        while (iter.hasNext()) {
            int key = (int) iter.next();
            if (key == fd) {
                iter.remove();
            }
        }
    }
}
