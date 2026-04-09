import java.io.*;
import java.util.Scanner;

public class Assignment4 {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int selection;
        String fileName = "notes.txt"; // File used for all operations

        // Menu-driven loop (runs until user chooses exit)
        do {
            System.out.println("\n--- MY FILE MANAGER ---");
            System.out.println("1. Write to File");
            System.out.println("2. Read from File");
            System.out.println("3. Append to File");
            System.out.println("4. Exit");
            System.out.print("Enter your selection: ");

            selection = scanner.nextInt();
            scanner.nextLine(); // Clear buffer after integer input

            switch (selection) {

                case 1:
                    // Write mode (overwrites existing file content)
                    try {
                        FileWriter writer = new FileWriter(fileName);

                        System.out.print("Enter content to write: ");
                        String content = scanner.nextLine();

                        writer.write(content);
                        writer.close();

                        System.out.println("Content written successfully.");

                    } catch (IOException e) {
                        System.out.println("Error writing to file: " + e.getMessage());
                    } finally {
                        System.out.println("Write operation done.");
                    }
                    break;

                case 2:
                    // Read file line-by-line using BufferedReader
                    try {
                        FileReader reader = new FileReader(fileName);
                        BufferedReader buffer = new BufferedReader(reader);

                        String line;
                        System.out.println("\nFile Contents:");

                        while ((line = buffer.readLine()) != null) {
                            System.out.println(line);
                        }

                        buffer.close();

                    } catch (FileNotFoundException e) {
                        System.out.println("File does not exist!");
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    } finally {
                        System.out.println("Read operation done.");
                    }
                    break;

                case 3:
                    // Append mode (adds data without deleting existing content)
                    try {
                        FileWriter writer = new FileWriter(fileName, true);

                        System.out.print("Enter content to append: ");
                        String content = scanner.nextLine();

                        writer.write("\n" + content);
                        writer.close();

                        System.out.println("Content appended successfully.");

                    } catch (IOException e) {
                        System.out.println("Error appending to file: " + e.getMessage());
                    } finally {
                        System.out.println("Append operation done.");
                    }
                    break;

                case 4:
                    System.out.println("Closing application...");
                    break;

                default:
                    System.out.println("Invalid selection!");
            }

        } while (selection != 4);

        scanner.close(); // Prevents resource leak
    }
}