package utils;

public class BlockInfo {
    private int dataNode; // The data node where the block is stored
    private int blockId;    // The unique identifier for the block

    public BlockInfo(String blockInfoString) {
        String[] blockInfoParts = blockInfoString.split(":");
        this.dataNode = Integer.parseInt(blockInfoParts[0]);
        this.blockId = Integer.parseInt(blockInfoParts[1]);
    }
    public BlockInfo(int dataNode, int blockId) {
        this.dataNode = dataNode;
        this.blockId = blockId;
    }

    public int getDataNode() {
        return dataNode;
    }
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
    public int getBlockId() {
        return blockId;
    }
    public String toString() { return this.getDataNode() + ":" + this.getBlockId(); }
}

/*
* class BlockInfo {
    private String dataNode;
    private long blockId;

    // 构造函数等其他属性和方法

    @Override
    public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BlockInfo fromString(String input) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(input, BlockInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}*/
