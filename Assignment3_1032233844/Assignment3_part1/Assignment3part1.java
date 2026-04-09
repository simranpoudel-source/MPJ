import java.util.Scanner;

// Class demonstrating constructor and method overloading
class ShapeCalculator {
    double sideA, sideB, rad;

    // Constructor Overloading → different constructors for different shapes
    ShapeCalculator(double a, double b) {
        sideA = a;
        sideB = b;
    }

    ShapeCalculator(double r) {
        rad = r;
    }

    // Method Overloading → same method name, different parameters
    double computeArea(double a, double b) {
        return a * b; // Rectangle area
    }

    double computeArea(double r) {
        return 3.14 * r * r; // Circle area
    }
}

public class Assignment3part1 {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        int option;

        // Menu-driven loop (runs until user selects exit)
        do {
            System.out.println("\n--- GEOMETRY CALCULATOR ---");
            System.out.println("1. Area of Rectangle");
            System.out.println("2. Area of Circle");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            option = input.nextInt();

            switch (option) {

                case 1:
                    // Rectangle calculation using overloaded constructor & method
                    System.out.print("Enter length: ");
                    double len = input.nextDouble();

                    System.out.print("Enter width: ");
                    double wid = input.nextDouble();

                    ShapeCalculator rect = new ShapeCalculator(len, wid);
                    System.out.println("Rectangle Area = " + rect.computeArea(len, wid));
                    break;

                case 2:
                    // Circle calculation using overloaded constructor & method
                    System.out.print("Enter radius: ");
                    double radius = input.nextDouble();

                    ShapeCalculator circle = new ShapeCalculator(radius);
                    System.out.println("Circle Area = " + circle.computeArea(radius));
                    break;

                case 3:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option!");
            }

        } while (option != 3);

        input.close(); // Prevents resource leak
    }
}