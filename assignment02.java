class Employee {

    protected double salary;

    Employee(double salary) {
        this.salary = salary;
    }

    public void displaySalary(double newSalary) {
        System.out.println("Salary before hike: " + salary);
        System.out.println("Salary after hike: " + newSalary);
    }
}

class FullTimeEmployee extends Employee {

    FullTimeEmployee(double salary) {
        super(salary);
    }

    public void calculateSalary() {
        double newSalary = salary + (salary * 0.50);
        displaySalary(newSalary);
    }
}

class InternEmployee extends Employee {

    InternEmployee(double salary) {
        super(salary);
    }

    public void calculateSalary() {
        double newSalary = salary + (salary * 0.25);
        displaySalary(newSalary);
    }
}

public class TheEmployee {

    public static void main(String[] args) {

        FullTimeEmployee fullTime = new FullTimeEmployee(40000);
        System.out.println("Full Time Employee");
        fullTime.calculateSalary();

        System.out.println();

        InternEmployee intern = new InternEmployee(20000);
        System.out.println("Intern Employee");
        intern.calculateSalary();
    }
}
