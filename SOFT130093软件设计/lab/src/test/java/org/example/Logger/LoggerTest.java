package org.example.Logger;

import org.example.Logger.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class LoggerTest {
    private Logger commandLogger;
    private String logFileName = "log.log";
    private String statsFileName = "statslog.log";

    @BeforeEach
    public void setUp() {
        this.commandLogger = new Logger(this.logFileName, this.statsFileName);
    }
    @AfterEach
    public void tearDown() {
        this.commandLogger.logSessionEnd();
        File fileToDelete = new File(this.logFileName);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    @Test
    @DisplayName("test 'session start' line at log&session file")
    public void testSessionStart() {
        //  check if session start has been logged correctly at logFile
        String line = readFileLine(0, logFileName);
        assertTrue(line.startsWith("session start at "));

        // Check if the timestamp format is correct, e.g., "YYYYMMDD HH:mm:SS"
        String timestamp = line.substring("session start at ".length()).trim();

        checkTimeStamp(timestamp);

        //  check if session start has been logged correctly at statsFile
        line = readFileLine(-1, statsFileName);
        assertTrue(line.startsWith("session start at "));
        timestamp = line.substring("session start at ".length()).trim();
        checkTimeStamp(timestamp);
    }

    @Test
    @DisplayName("Test log session")
    public void testLogFile() {
        String command = "load books.md";
        checkLog(command);
        command = "insert 1 # books";
        checkLog(command);
    }

    @Test
    @DisplayName("Test statsMap")
    public void testStatsMap() {
        this.commandLogger.load("test1.md");
        assertEquals(this.commandLogger.getStatsMapSize(), 1);
        this.commandLogger.load("test2.md");
        assertEquals(this.commandLogger.getStatsMapSize(), 2);
        this.commandLogger.close("test1.md");
        assertEquals(this.commandLogger.getStatsMapSize(), 1);
        this.commandLogger.close("test2.md");
        assertEquals(this.commandLogger.getStatsMapSize(), 0);
    }

    @Test
    @DisplayName("Test statsLog")
    public void testStatsFile() {
        this.commandLogger.load("test1.md");
        this.commandLogger.close("test1.md");

        String line = readFileLine(-1, statsFileName);
        assertEquals(line.substring(0,10), "./test1.md");
        assertEquals(line.substring(11), " 0 分钟 ");
    }

    private void checkLog(String command) {
        commandLogger.logCommand(command);

        // Add assertions to check if the command has been logged correctly
        String lastLine = readFileLine(-1, logFileName);

        // Add assertions to check if the last line contains the correct command.
        assertTrue(lastLine.endsWith(command));

        // Add assertions to check if the timeStamp is valid
        String timestamp = lastLine.substring(0, 17);
        checkTimeStamp(timestamp);
    }
    private void checkTimeStamp(String timestamp) {
        // Check if the timestamp format is correct, e.g., "YYYYMMDD HH:mm:SS"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        try {
            dateFormat.parse(timestamp);

            Date parsedDate = dateFormat.parse(timestamp);
            // Add assertions to check if the parsed date is not null, indicating a valid timestamp format
            assertNotNull(parsedDate);

            // Check if the time is within a reasonable range (e.g., current time +/- a few seconds)
            Date now = new Date();
            long timeDifference = Math.abs(now.getTime() - parsedDate.getTime());
            assertTrue(timeDifference < 5000);
        } catch (ParseException e) {
            System.out.println("Timestamp is incorrect");
        }
    }
    private String readFileLine(int lineNumber, String filePath) {
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            int i = 0;
            while ((currentLine = br.readLine()) != null) {
                line = currentLine;
                if (lineNumber == i) {
                    break;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }
}