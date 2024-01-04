package org.example;

import org.example.Command.CommandManager;
import org.example.Editor.Editor;
import org.example.WorkSpace.WorkSpaceManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        WorkSpaceManager workSpaceManager = new WorkSpaceManager();
        workSpaceManager.loadFromMemento();

        Scanner scanner = new Scanner(System.in);
        String userInput;

        // TODO: 直接关闭
        /*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("end");
            commandManager.endSession();
            scanner.close();
        }));*/

        while (true) {
            System.out.print(">>> ");
            userInput = scanner.nextLine();

            workSpaceManager.ParseCommand(userInput);
            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }
        }

        // TODO: workspace end 
        // commandManager.endSession();
        scanner.close();
    }

}