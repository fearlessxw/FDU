import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.lang.NullPointerException;
import java.lang.SecurityException;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import impl.ClientImpl;
import api.*;

public class ClientLauncher {
    public static void main(String[] args) {
        ClientImpl client = new ClientImpl();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(">>> ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                client.exit();
                System.out.println("INFO: bye");
                break;
            }

            String[] parts = userInput.split("\\s+",3);

            // open
            if (parts.length == 3 && parts[0].equals("open")) {
                String fileName = parts[1];
                // mode: 01 write, 10 read, 11 rw
                int mode;
                if (parts[2].equals("w")) {
                    mode = 0b10;
                } else if (parts[2].equals("r")) {
                    mode = 0b01;
                } else if (parts[2].equals("rw")) {
                    mode = 0b11;
                } else {
                    System.out.println("Invalid Command!");
                    continue;
                }

                int fd = client.open(fileName, mode);
                if (fd == -1) {
                    System.out.println("INFO: WRITE not allowed");
                } else {
                    System.out.println("INFO: fd="+ fd);
                }

            } else if (parts.length==3 && parts[0].equals("append")) {
                try {
                    client.append(Integer.parseInt(parts[1]), parts[2].getBytes(StandardCharsets.UTF_8));
                } catch (NullPointerException e) {
                    System.out.println("INFO: No such file...");
                } catch (SecurityException e) {
                    System.out.println("INFO: WRITE not allowed");
                } catch (NumberFormatException e) {
                    System.out.println("INFO: Invalid Command!");
                }
            } else if (parts.length == 2 && parts[0].equals("read")) {
                try {
                    byte[] bytes = client.read(Integer.parseInt(parts[1]));
                    String readString = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println(readString);
                } catch (NullPointerException e) {
                    System.out.println("INFO: No such file...");
                } catch (SecurityException e) {
                    System.out.println("INFO: READ not allowed");
                } catch (NumberFormatException e) {
                    System.out.println("INFO: Invalid Command!");
                }
            } else if (parts.length == 2 && parts[0].equals("close")) {
                try {
                    client.close(Integer.parseInt(parts[1]));
                    System.out.println("INFO: fd " + parts[1] + " closed");
                } catch (NullPointerException e) {
                    System.out.println("INFO: No such file...");
                } catch (NumberFormatException e) {
                    System.out.println("INFO: Invalid Command!");
                }
            } else {System.out.println("INFO: Invalid Command!");}

        }

        scanner.close();
    }
}
