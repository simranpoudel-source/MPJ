import java.io.*;
import java.util.Scanner;

public class Assignment4 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;
        String filename = "sample.txt";

        do {
            System.out.println("\n--- FILE MENU ---");
            System.out.println("1. Write to File");
            System.out.println("2. Read from File");
            System.out.println("3. Append to File");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (choice) {

                case 1:
                    try {
                        FileWriter fw = new FileWriter(filename);
                        System.out.print("Enter text to write: ");
                        String data = sc.nextLine();
                        fw.write(data);
                        fw.close();
                        System.out.println("Data written successfully.");
                    } catch (IOException e) {
                        System.out.println("Error writing file: " + e.getMessage());
                    } finally {
                        System.out.println("Write operation completed.");
                    }
                    break;

                case 2:
                    try {
                        FileReader fr = new FileReader(filename);
                        BufferedReader br = new BufferedReader(fr);

                        String line;
                        System.out.println("\nFile Content:");
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }

                        br.close();
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found!");
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    } finally {
                        System.out.println("Read operation completed.");
                    }
                    break;

                case 3:
                    try {
                        FileWriter fw = new FileWriter(filename, true); // append mode
                        System.out.print("Enter text to append: ");
                        String data = sc.nextLine();
                        fw.write("\n" + data);
                        fw.close();
                        System.out.println("Data appended successfully.");
                    } catch (IOException e) {
                        System.out.println("Error appending file: " + e.getMessage());
                    } finally {
                        System.out.println("Append operation completed.");
                    }
                    break;

                case 4:
                    System.out.println("Exiting program...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 4);

        sc.close();
    }
}