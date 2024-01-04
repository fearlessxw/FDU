package org.example.Logger;
import org.example.Command.CommandManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Logger {
    private FileWriter logFileWriter;
    private FileWriter statsFileWriter;
    String logFileName;

    HashMap<String, Instant> statsMap; // working workspace

    List<String> statsFile; // closed workSpace

    public Logger(String logFileName, String statsFileName) {
        try {
            logFileWriter = new FileWriter(logFileName, true);
            statsFileWriter = new FileWriter(statsFileName, true);
            this.logFileName = logFileName;
            this.statsFile = new ArrayList<>();
            this.statsMap = new HashMap<String, Instant>();

            this.logSessionStart();
        } catch (IOException e) {
            System.err.println("Warning: Could not open the log file for writing.");
        }
    }
    public int getStatsMapSize() {
        return this.statsMap.size();
    }

    private void logSessionStart() {
        log(this.statsFileWriter,"session start at " + getCurrentTimestamp());
        log(this.logFileWriter,"session start at " + getCurrentTimestamp());
        this.statsFile.add("session start at " + getCurrentTimestamp());
    }
    public void logSessionEnd() {
        Set<String> keys = this.statsMap.keySet();
        for (String key : keys) {
            close(key);
        }
        try {
            this.logFileWriter.close();
            this.statsFileWriter.close();
        } catch (IOException e) {
            System.err.println("Warning: Could not close the log file.");
        }
    }

    public void load(String filePath) {
        Instant currentTime = Instant.now();
        this.statsMap.put(filePath, currentTime);
    }
    public void close(String filePath) {
        String logContent = getStatsTime(filePath);
        log(this.statsFileWriter, logContent);
        this.statsFile.add(logContent);

        this.statsMap.remove(filePath);
    }

    public void statsAll() {
        for (String stats:statsFile){
            System.out.println(stats);
        }
    }
    public void statsCurrent(String filePath) {
        if (filePath == null) {
            System.out.println("Please create a workspace first...");
            return;
        }
        System.out.println(getStatsTime(filePath));
    }

    public void logCommand(String command) {
        log(this.logFileWriter, getCurrentTimestamp() + " " + command);
    }
    public void history(int num) {
        List<String> logFile = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.logFileName));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("session")) {
                    logFile.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (num == -1) {
            for (int i=logFile.size()-1;i>=0;i--) {
                System.out.println(logFile.get(i));
            }
        } else {
            for (int i=logFile.size()-1;i>=0&&num>0;i--,num--) {
                System.out.println(logFile.get(i));
            }
        }

    }

    // write String to a specific file
    private void log(FileWriter fileWriter, String logEntry) {
        try {
            fileWriter.write(logEntry + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            System.err.println("Warning: Could not write to the log file.");
        }
    }
    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date now = new Date();
        return dateFormat.format(now);
    }
    private String getStatsTime(String filePath) {
        Instant currentTime = Instant.now();
        Instant startTime = this.statsMap.get(filePath);
        Duration duration = Duration.between(startTime, currentTime);

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        String logContent = null;
        if (filePath.startsWith("./")) {
            logContent = filePath + " ";
        } else {
            logContent = "./" + filePath + " ";
        }

        if (days!=0) {
            logContent += days + " 天 ";
        }
        if (hours!=0) {
            logContent += hours + " 小时 ";
        }
        if (minutes!=0) {
            logContent += minutes + " 分钟 ";
        }
        if (days==0&&hours==0&&minutes==0) {
            logContent += " 0 分钟 ";
        }

        return logContent;
    }
}
