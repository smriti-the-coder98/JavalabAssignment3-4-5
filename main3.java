// File Name: StudentRecordSystem.java

import java.io.*;
import java.util.*;

// ----------------------- STUDENT CLASS -----------------------
class Student {
    int rollNo;
    String name;
    String email;
    String course;
    double marks;

    Student(int rollNo, String name, String email, String course, double marks) {
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.course = course;
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "Roll No: " + rollNo + "\n"
                + "Name: " + name + "\n"
                + "Email: " + email + "\n"
                + "Course: " + course + "\n"
                + "Marks: " + marks + "\n";
    }
}

// ----------------------- FILE UTIL -----------------------
class FileUtil {

    // Read students from students.txt
    public static ArrayList<Student> readStudents(String filename) {
        ArrayList<Student> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                int roll = Integer.parseInt(p[0]);
                String name = p[1];
                String email = p[2];
                String course = p[3];
                double marks = Double.parseDouble(p[4]);

                list.add(new Student(roll, name, email, course, marks));
            }

        } catch (Exception e) {
            System.out.println("No existing file found. Starting fresh...");
        }

        return list;
    }

    // Save all students to file
    public static void writeStudents(String filename, ArrayList<Student> list) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

            for (Student s : list) {
                bw.write(s.rollNo + "," + s.name + "," + s.email + "," + s.course + "," + s.marks);
                bw.newLine();
            }

            System.out.println("✔ Records saved successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error writing to file!");
        }
    }

    // Random Access File Demo
    public static void randomReadDemo(String filename) {
        try {
            RandomAccessFile raf = new RandomAccessFile(filename, "r");
            System.out.println("\nRandomAccessFile Demo (reading first line):");
            System.out.println(raf.readLine());
            raf.close();
        } catch (Exception e) {
            System.out.println("RAF Error!");
        }
    }
}

// ----------------------- MANAGER CLASS -----------------------
class StudentManager {

    ArrayList<Student> students = new ArrayList<>();

    void load(String file) {
        students = FileUtil.readStudents(file);
        System.out.println("\nLoaded students from file:");
        for (Student s : students) {
            System.out.println(s);
        }
    }

    void addStudent(Student s) {
        students.add(s);
        System.out.println("✔ Student added!");
    }

    void viewAll() {
        if (students.isEmpty()) {
            System.out.println("No students available!");
            return;
        }
        Iterator<Student> it = students.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    void searchByName(String name) {
        for (Student s : students) {
            if (s.name.equalsIgnoreCase(name)) {
                System.out.println("\nFound:\n" + s);
                return;
            }
        }
        System.out.println("❌ Student not found!");
    }

    void deleteByName(String name) {
        Iterator<Student> it = students.iterator();
        while (it.hasNext()) {
            if (it.next().name.equalsIgnoreCase(name)) {
                it.remove();
                System.out.println("✔ Student deleted!");
                return;
            }
        }
        System.out.println("❌ Student not found!");
    }

    void sortByMarks() {
        students.sort(Comparator.comparingDouble(s -> s.marks));
        System.out.println("\nSorted Student List by Marks:");
        viewAll();
    }
}

// ----------------------- MAIN CLASS -----------------------
public class StudentRecordSystem {

    public static void main(String[] args) {

        final String FILE = "students.txt";

        StudentManager sm = new StudentManager();

        // Load existing records
        sm.load(FILE);

        FileUtil.randomReadDemo(FILE);

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n===== Capstone Student Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search by Name");
            System.out.println("4. Delete by Name");
            System.out.println("5. Sort by Marks");
            System.out.println("6. Save and Exit");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1:
                    System.out.print("Enter Roll No: ");
                    int roll = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();
                    System.out.print("Enter Course: ");
                    String course = sc.nextLine();
                    System.out.print("Enter Marks: ");
                    double marks = sc.nextDouble();

                    sm.addStudent(new Student(roll, name, email, course, marks));
                    break;

                case 2:
                    sm.viewAll();
                    break;

                case 3:
                    System.out.print("Enter name to search: ");
                    sm.searchByName(sc.nextLine());
                    break;

                case 4:
                    System.out.print("Enter name to delete: ");
                    sm.deleteByName(sc.nextLine());
                    break;

                case 5:
                    sm.sortByMarks();
                    break;

                case 6:
                    FileUtil.writeStudents(FILE, sm.students);
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
