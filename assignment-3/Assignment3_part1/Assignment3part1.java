import java.util.Scanner;

class Shapes {
    double length, breadth, radius;

    // Constructor Overloading
    Shapes(double l, double b) {
        length = l;
        breadth = b;
    }

    Shapes(double r) {
        radius = r;
    }

    // Method Overloading
    double area(double l, double b) {
        return l * b; // Rectangle
    }

    double area(double r) {
        return 3.14 * r * r; // Circle
    }
}

public class Assignment3part1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- SHAPE MENU ---");
            System.out.println("1. Area of Rectangle");
            System.out.println("2. Area of Circle");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter length: ");
                    double l = sc.nextDouble();

                    System.out.print("Enter breadth: ");
                    double b = sc.nextDouble();

                    Shapes rect = new Shapes(l, b);
                    System.out.println("Area of Rectangle = " + rect.area(l, b));
                    break;

                case 2:
                    System.out.print("Enter radius: ");
                    double r = sc.nextDouble();

                    Shapes circle = new Shapes(r);
                    System.out.println("Area of Circle = " + circle.area(r));
                    break;

                case 3:
                    System.out.println("Exiting program...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 3);

        sc.close();
    }
}