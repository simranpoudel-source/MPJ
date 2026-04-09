import java.util.Scanner;

// Parent class demonstrating inheritance
class HillStation {

    // Methods to be overridden by child classes
    void famousFood() {
        System.out.println("General hill station cuisine");
    }

    void famousFor() {
        System.out.println("Known for scenic beauty");
    }
}

// Subclass 1 → demonstrates method overriding
class Nainital extends HillStation {
    void famousFood() {
        System.out.println("Nainital Famous Food: Bal Mithai");
    }

    void famousFor() {
        System.out.println("Nainital Famous For: Lakes");
    }
}

// Subclass 2 → method overriding
class Kodaikanal extends HillStation {
    void famousFood() {
        System.out.println("Kodaikanal Famous Food: Chocolate");
    }

    void famousFor() {
        System.out.println("Kodaikanal Famous For: Botanical Garden");
    }
}

// Subclass 3 → method overriding
class Gangtok extends HillStation {
    void famousFood() {
        System.out.println("Gangtok Famous Food: Momos");
    }

    void famousFor() {
        System.out.println("Gangtok Famous For: Monasteries");
    }
}

public class Assignment3part2 {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        HillStation dest; // Parent class reference (runtime polymorphism)
        int option;

        // Menu-driven loop
        do {
            System.out.println("\n--- HILL STATION EXPLORER ---");
            System.out.println("1. Nainital");
            System.out.println("2. Kodaikanal");
            System.out.println("3. Gangtok");
            System.out.println("4. Exit");
            System.out.print("Choose a hill station: ");
            option = input.nextInt();

            switch (option) {

                case 1:
                    // Dynamic method dispatch → object decided at runtime
                    dest = new Nainital();
                    dest.famousFood();
                    dest.famousFor();
                    break;

                case 2:
                    dest = new Kodaikanal();
                    dest.famousFood();
                    dest.famousFor();
                    break;

                case 3:
                    dest = new Gangtok();
                    dest.famousFood();
                    dest.famousFor();
                    break;

                case 4:
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid option!");
            }

        } while (option != 4);

        input.close(); // Prevents resource leak
    }
}  