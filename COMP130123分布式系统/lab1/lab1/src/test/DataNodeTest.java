package test;

import api.DataNode;
import impl.DataNodeImpl;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DataNodeTest {
    static DataNodeImpl dn;

    @Before
    public void setUp() {
        dn = new DataNodeImpl(0);
    }

    @Test
    public void testRead() {
        int blockId = dn.randomBlockId();
        assertNotNull(dn.read(blockId));
    }

    @Test
    public void testAppend() {
        int blockId = dn.randomBlockId();
        dn.createBlock(blockId); // create a block after get a random block id
        byte[] toWrite = "Hello World".getBytes(StandardCharsets.UTF_8);

        dn.append(blockId, toWrite);
        byte[] read = dn.read(blockId);

        int n = toWrite.length;
        // read return byte[4*1024], use the following code(which is equal to dn.getFilledSize()) to get the valid size of block
        int N = read.length;
        for (int i = 0; i < read.length; i++) {
            if (read[i] == 0) {
                N = i;
                break;
            }
        }

        for (int i = 0; i < n; i++) {
            assertEquals("Block ID: " + blockId + ". Read block bytes and appended bytes differ at the " + i
                    + " byte to the eof.", toWrite[n - 1 - i], read[N - 1 - i]);
        }
    }
}
