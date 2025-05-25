import java.sql.*;
import java.util.Scanner;

public class HospitalManagement {
    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASS = "12345678";
    private Connection conn;
    private Scanner scanner;

    public HospitalManagement() {
        scanner = new Scanner(System.in);
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL server
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to the database.");

            // Create tables if they do not exist
            createTablesIfNotExist();

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.out.println("Connection failed. Check output console");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        String createPatientsTable = "CREATE TABLE IF NOT EXISTS patients (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "age INT NOT NULL," +
                "gender VARCHAR(10)," +
                "phone VARCHAR(20)" +
                ");";

        String createDoctorsTable = "CREATE TABLE IF NOT EXISTS doctors (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "specialty VARCHAR(100)," +
                "phone VARCHAR(20)" +
                ");";

        Statement stmt = conn.createStatement();
        stmt.execute(createPatientsTable);
        stmt.execute(createDoctorsTable);

        System.out.println("Ensured tables patients and doctors exist.");
    }

    public void run() {
        while (true) {
            System.out.println("\nHospital Management System");
            System.out.println("1. Add Patient");
            System.out.println("2. List Patients");
            System.out.println("3. Add Doctor");
            System.out.println("4. List Doctors");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addPatient();
                    break;
                case 2:
                    listPatients();
                    break;
                case 3:
                    addDoctor();
                    break;
                case 4:
                    listDoctors();
                    break;
                case 5:
                    close();
                    System.exit(0);
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private void addPatient() {
        try {
            System.out.print("Enter patient name: ");
            String name = scanner.nextLine();
            System.out.print("Enter patient age: ");
            int age = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter patient gender: ");
            String gender = scanner.nextLine();
            System.out.print("Enter patient phone: ");
            String phone = scanner.nextLine();

            String sql = "INSERT INTO patients (name, age, gender, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, phone);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Patient added successfully.");
            } else {
                System.out.println("Failed to add patient.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error while adding patient: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age input. Must be a number.");
        }
    }

    private void listPatients() {
        try {
            String sql = "SELECT * FROM patients";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n-- Patients List --");
            System.out.printf("%-5s %-20s %-5s %-10s %-15s%n", "ID", "Name", "Age", "Gender", "Phone");
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-5d %-10s %-15s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error while listing patients: " + e.getMessage());
        }
    }

    private void addDoctor() {
        try {
            System.out.print("Enter doctor name: ");
            String name = scanner.nextLine();
            System.out.print("Enter doctor specialty: ");
            String specialty = scanner.nextLine();
            System.out.print("Enter doctor phone: ");
            String phone = scanner.nextLine();

            String sql = "INSERT INTO doctors (name, specialty, phone) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, specialty);
            pstmt.setString(3, phone);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Doctor added successfully.");
            } else {
                System.out.println("Failed to add doctor.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error while adding doctor: " + e.getMessage());
        }
    }

    private void listDoctors() {
        try {
            String sql = "SELECT * FROM doctors";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n-- Doctors List --");
            System.out.printf("%-5s %-20s %-20s %-15s%n", "ID", "Name", "Specialty", "Phone");
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-20s %-15s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialty"),
                        rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error while listing doctors: " + e.getMessage());
        }
    }

    private void close() {
        try {
            scanner.close();
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            System.out.println("Connection closed. Goodbye!");
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        HospitalManagement hm = new HospitalManagement();
        hm.run();
    }
}

