import java.io.*;
import java.util.*;


public class StudentRecordSystem {

    // Abstract Person

    public static abstract class Person {
        protected String name;
        protected String email;

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public abstract void displayInfo();
    }

    // Student class
    
    public static class Student extends Person {
        private int rollNo;
        private String course;
        private double marks;
        private String grade;

        public Student(int rollNo, String name, String email, String course, double marks) {
            super(name, email);
            this.rollNo = rollNo;
            this.course = course;
            setMarks(marks);
        }

        public int getRollNo() { return rollNo; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getCourse() { return course; }
        public double getMarks() { return marks; }
        public String getGrade() { return grade; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setCourse(String course) { this.course = course; }
        public void setMarks(double marks) {
            this.marks = marks;
            calculateGrade();
        }

        public void calculateGrade() {
            if (marks >= 85) grade = "A";
            else if (marks >= 70) grade = "B";
            else if (marks >= 50) grade = "C";
            else grade = "D";
        }

        public void displayDetails() {
            System.out.println("Roll No : " + rollNo);
            System.out.println("Name    : " + name);
            System.out.println("Email   : " + email);
            System.out.println("Course  : " + course);
            System.out.println("Marks   : " + marks);
            System.out.println("Grade   : " + grade);
        }

        @Override
        public void displayInfo() {
            displayDetails();
        }

        /** Return CSV line to save */
        public String toCSV() {
            // roll,name,email,course,marks
            return rollNo + "," + escapeComma(name) + "," + escapeComma(email) + "," + escapeComma(course) + "," + marks;
        }

        private String escapeComma(String s) {
            return s.replace(",", ""); // simple approach: strip commas to avoid CSV issues
        }
    }

    // Interface RecordActions

    public static interface RecordActions {
        void addStudent();
        void deleteStudent();
        void updateStudent();
        void searchStudent();
        void viewAllStudents();
    }

    // Custom Exception
    public static class StudentNotFoundException extends Exception {
        public StudentNotFoundException(String msg) { super(msg); }
    }

    // Loader (multithreading)
    
    public static class Loader implements Runnable {
        private String message;

        public Loader(String message) { this.message = message; }

        @Override
        public void run() {
            System.out.print(message);
            try {
                for (int i = 0; i < 6; i++) {
                    Thread.sleep(300);
                    System.out.print(".");
                }
            } catch (InterruptedException ignored) {}
            System.out.println();
        }
    }

    // StudentManager Implementation

    public static class StudentManager implements RecordActions {
        private final Map<Integer, Student> studentMap = new HashMap<>();
        private final Scanner sc;
        private final String FILE_PATH = "students.txt";

        public StudentManager(Scanner sc) {
            this.sc = sc;
            loadFromFile();
        }

        // Load existing records from file (if present)
        private void loadFromFile() {
            File f = new File(FILE_PATH);
            if (!f.exists()) return;

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    // split into exactly 5 parts: roll,name,email,course,marks
                    String[] parts = line.split(",", 5);
                    if (parts.length < 5) continue;
                    try {
                        int roll = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        String email = parts[2].trim();
                        String course = parts[3].trim();
                        double marks = Double.parseDouble(parts[4].trim());
                        Student s = new Student(roll, name, email, course, marks);
                        studentMap.put(roll, s);
                    } catch (NumberFormatException ignored) {
                        // skip malformed line
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to load saved students: " + e.getMessage());
            }
        }

        // Save to file (use loader thread to simulate saving time)
        public void saveToFile() {
            Thread t = new Thread(new Loader("Saving"));
            t.start();
            try { t.join(); } catch (InterruptedException ignored) {}

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (Student s : studentMap.values()) {
                    bw.write(s.toCSV());
                    bw.newLine();
                }
                System.out.println("Saved " + studentMap.size() + " student(s) to " + FILE_PATH);
            } catch (IOException e) {
                System.out.println("Error saving to file: " + e.getMessage());
            }
        }

        @Override
        public void addStudent() {
            try {
                System.out.print("Enter Roll No: ");
                int roll = readInt();

                if (studentMap.containsKey(roll)) {
                    System.out.println("A student with this roll number already exists.");
                    return;
                }

                System.out.print("Enter Name: ");
                String name = readNonEmptyLine();

                System.out.print("Enter Email: ");
                String email = readNonEmptyLine();

                System.out.print("Enter Course: ");
                String course = readNonEmptyLine();

                System.out.print("Enter Marks (0-100): ");
                double marks = readDoubleInRange(0, 100);

                // simulate loading
                Thread t = new Thread(new Loader("Adding student"));
                t.start();
                t.join();

                Student s = new Student(roll, name, email, course, marks);
                studentMap.put(roll, s);
                System.out.println("Student added successfully.");

            } catch (InputMismatchException ime) {
                System.out.println("Input error: " + ime.getMessage());
                sc.nextLine(); // clear buffer
            } catch (InterruptedException ie) {
                System.out.println("Operation interrupted.");
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }

        @Override
        public void deleteStudent() {
            System.out.print("Delete by (1) Roll No or (2) Name? Enter 1 or 2: ");
            int choice = readInt();
            boolean removed = false;

            if (choice == 1) {
                System.out.print("Enter Roll No to delete: ");
                int roll = readInt();
                if (studentMap.remove(roll) != null) removed = true;
            } else {
                sc.nextLine();
                System.out.print("Enter Name to delete (exact match): ");
                String name = sc.nextLine().trim();
                Iterator<Map.Entry<Integer, Student>> it = studentMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Student> e = it.next();
                    if (e.getValue().getName().equalsIgnoreCase(name)) {
                        it.remove();
                        removed = true;
                        // do not break: remove first match only? assignment deletes by name -> remove first matching
                        break;
                    }
                }
            }

            if (removed) System.out.println("Student record deleted.");
            else System.out.println("Student not found.");
        }

        @Override
        public void updateStudent() {
            System.out.print("Enter Roll No to update: ");
            int roll = readInt();
            Student s = studentMap.get(roll);
            if (s == null) {
                System.out.println("Student not found.");
                return;
            }

            System.out.println("Current details:");
            s.displayDetails();
            sc.nextLine();

            try {
                System.out.print("Enter new Name (leave blank to keep): ");
                String name = sc.nextLine();
                if (!name.trim().isEmpty()) s.setName(name.trim());

                System.out.print("Enter new Email (leave blank to keep): ");
                String email = sc.nextLine();
                if (!email.trim().isEmpty()) s.setEmail(email.trim());

                System.out.print("Enter new Course (leave blank to keep): ");
                String course = sc.nextLine();
                if (!course.trim().isEmpty()) s.setCourse(course.trim());

                System.out.print("Enter new Marks (-1 to keep): ");
                String marksLine = sc.nextLine().trim();
                if (!marksLine.isEmpty()) {
                    double marks = Double.parseDouble(marksLine);
                    if (marks >= 0 && marks <= 100) s.setMarks(marks);
                    else System.out.println("Invalid marks; keeping previous marks.");
                }

                System.out.println("Record updated successfully.");

            } catch (NumberFormatException nfe) {
                System.out.println("Invalid number format. Update aborted.");
            }
        }

        @Override
        public void searchStudent() {
            System.out.print("Search by (1) Roll No or (2) Name? Enter 1 or 2: ");
            int choice = readInt();
            try {
                if (choice == 1) {
                    System.out.print("Enter Roll No: ");
                    int roll = readInt();
                    Student s = studentMap.get(roll);
                    if (s == null) throw new StudentNotFoundException("Student with roll " + roll + " not found.");
                    s.displayDetails();
                } else {
                    sc.nextLine();
                    System.out.print("Enter Name to search (exact or partial): ");
                    String name = sc.nextLine().trim();
                    boolean found = false;
                    for (Student s : studentMap.values()) {
                        if (s.getName().toLowerCase().contains(name.toLowerCase())) {
                            System.out.println("-----");
                            s.displayDetails();
                            found = true;
                        }
                    }
                    if (!found) throw new StudentNotFoundException("No student matched the name '" + name + "'.");
                }
            } catch (StudentNotFoundException snfe) {
                System.out.println(snfe.getMessage());
            }
        }

        @Override
        public void viewAllStudents() {
            if (studentMap.isEmpty()) {
                System.out.println("No students found.");
                return;
            }
            System.out.println("All Students:");
            for (Student s : studentMap.values()) {
                System.out.println("-----");
                s.displayDetails();
            }
        }

        public void sortByMarksDescending() {
            if (studentMap.isEmpty()) {
                System.out.println("No students to sort.");
                return;
            }
            List<Student> list = new ArrayList<>(studentMap.values());
            list.sort((a, b) -> Double.compare(b.getMarks(), a.getMarks())); // descending

            System.out.println("Sorted Student List by Marks (descending):");
            for (Student s : list) {
                System.out.println("-----");
                s.displayDetails();
            }
        }

        // -----------------
        // Helper input methods
        // -----------------
        private int readInt() {
            while (true) {
                try {
                    int v = sc.nextInt();
                    return v;
                } catch (InputMismatchException ime) {
                    System.out.print("Please enter a valid integer: ");
                    sc.nextLine();
                }
            }
        }

        private double readDoubleInRange(double min, double max) {
            while (true) {
                try {
                    double v = sc.nextDouble();
                    if (v < min || v > max) {
                        System.out.print("Enter a value between " + min + " and " + max + ": ");
                        continue;
                    }
                    return v;
                } catch (InputMismatchException ime) {
                    System.out.print("Please enter a valid number: ");
                    sc.nextLine();
                }
            }
        }

        private String readNonEmptyLine() {
            sc.nextLine(); // consume leftover newline
            while (true) {
                String s = sc.nextLine().trim();
                if (!s.isEmpty()) return s;
                System.out.print("Input cannot be empty. Enter again: ");
            }
        }
    }

    // ------------------------------
    // Main (driver)
    // ------------------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StudentManager manager = new StudentManager(sc);

        while (true) {
            try {
                System.out.println();
                System.out.println("===== Capstone Student Menu =====");
                System.out.println("1. Add Student");
                System.out.println("2. View All Students");
                System.out.println("3. Search Student");
                System.out.println("4. Delete Student");
                System.out.println("5. Update Student");
                System.out.println("6. Sort by Marks (descending)");
                System.out.println("7. Save and Exit");
                System.out.print("Enter choice: ");

                int choice = manager.readInt(); // uses manager helper for robust int read
                System.out.println();

                switch (choice) {
                    case 1 -> manager.addStudent();
                    case 2 -> manager.viewAllStudents();
                    case 3 -> manager.searchStudent();
                    case 4 -> manager.deleteStudent();
                    case 5 -> manager.updateStudent();
                    case 6 -> manager.sortByMarksDescending();
                    case 7 -> {
                        manager.saveToFile();
                        System.out.println("Exiting. Goodbye!");
                        sc.close();
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }
}
