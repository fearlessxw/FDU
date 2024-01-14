package utils;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
public class FileDesc {
    /* the id should be assigned uniquely during the lifetime of NameNode,
     * so that NameNode can know which client's open has over at close
     * e.g., on nameNode1
     * client1 opened file "Hello.txt" with mode 'w' , and retrieved a FileDesc with 0x889
     * client2 tries opening the same file "Hello.txt" with mode 'w' , and since the 0x889 is not closed yet, the return
     * value of open() is null.
     * after a while client1 call close() with the FileDesc of id 0x889.
     * client2 tries again and get a new FileDesc with a new id 0x88a
     */
    //final long id;
    private String filepath;
    private LocalDateTime createTime;
    private LocalDateTime lastModifyTime;
    private LocalDateTime lastVisitTime;
    private List<BlockInfo> blockIdList;

    public FileDesc() {
        this.createTime = LocalDateTime.now();
        this.lastModifyTime = this.createTime;
        this.lastVisitTime = this.lastModifyTime;
        this.blockIdList = new ArrayList<>();
    }
    public FileDesc(String filepath, LocalDateTime createTime, LocalDateTime lastModifyTime, LocalDateTime lastVisitTime, List<BlockInfo> blockIdList) {
        this.filepath = filepath;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
        this.lastVisitTime = lastVisitTime;
        this.blockIdList = blockIdList;
    }

    public String getFilepath() {
        return filepath;
    }
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public LocalDateTime getLastModifyTime() {
        return lastModifyTime;
    }
    public void setLastModifyTime() {
        this.lastModifyTime = LocalDateTime.now();
    }
    public void setLastModifyTime(LocalDateTime lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
    public LocalDateTime getLastVisitTime() {
        return lastVisitTime;
    }
    public void setLastVisitTime(LocalDateTime lastVisitTime) { this.lastVisitTime = lastVisitTime; }
    public void setBlockIdList(List<BlockInfo> blockIdList) {
        this.blockIdList = blockIdList;
    }
    public List<BlockInfo> getBlockIdList() {
        return blockIdList;
    }


    /* The following method is for conversion, so we can have interface that return string, which is easy to write in idl */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getFilepath()).append(",");
        sb.append(this.getCreateTime()).append(",");
        sb.append(this.getLastModifyTime()).append(",");
        sb.append(this.getLastVisitTime()).append(",");

        List<BlockInfo> blockIdList = this.getBlockIdList();
        for (BlockInfo blockInfo : blockIdList) {
            sb.append(blockInfo.toString()).append(";");
        }

        return sb.toString();
    }

    public static FileDesc fromString(String str){
        if (str.equals("")) {
            return null;
        }
        String[] parts = str.split(",");

        String[] blockInfoParts = parts[4].split(";");
        List<BlockInfo> blockIdList = new ArrayList<>();
        for (String blockInfoPart : blockInfoParts) {
            BlockInfo blockInfo = new BlockInfo(blockInfoPart);
            blockIdList.add(blockInfo);
        }

        FileDesc fileDesc = new FileDesc(parts[0], LocalDateTime.parse(parts[1]), LocalDateTime.parse(parts[2]), LocalDateTime.parse(parts[3]), blockIdList);

        return fileDesc;
    }

    public void printInfo() {
        System.out.println("filepath: " + getFilepath());
        System.out.println("create: " + getCreateTime());
        System.out.println("visit: " + getLastVisitTime());
        System.out.println("modify: "+ getLastModifyTime());
        System.out.println("block: " + getBlockIdList());
    }
    public void setBlockId(int index, int block_id) {
        int datanodeindex = this.blockIdList.get(index).getDataNode();
        BlockInfo blockInfo = new BlockInfo(datanodeindex, block_id);
        this.blockIdList.set(index, blockInfo);
    }
    public void appendBlock(BlockInfo blockInfo) {
        this.blockIdList.add(blockInfo);
    }
}
