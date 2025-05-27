// Import necessary Swing and AWT packages for GUI, and SQL package for database handling
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// Main class for the Hospital Management application
public class HospitalManagementUI extends JFrame {

    // Database connection details
    private static final String DB_URL_PREFIX = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "hospital_db";

    private Connection conn; // JDBC connection object
    private CardLayout cardLayout; // Layout manager to switch between panels
    private JPanel mainPanel; // Main panel containing all functional sub-panels

    // Components for login screen
    private JTextField userField; // Input for DB username
    private JPasswordField passField; // Input for DB password
    private JLabel loginStatusLabel; // Label to show login success/failure

    // Components for patient form
    private DefaultTableModel patientTableModel; // Model to hold patient table data
    private JTextField pNameField, pAgeField, pGenderField, pPhoneField; // Patient data fields

    // Components for doctor form
    private DefaultTableModel doctorTableModel; // Model to hold doctor table data
    private JTextField dNameField, dSpecialtyField, dPhoneField; // Doctor data fields

    // Constructor - setup for login screen
    public HospitalManagementUI() {
        setTitle("Hospital Management - Login");
        setSize(400, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initLoginUI(); // Create login UI
    }

    // Initializes login UI layout and event handling
    private void initLoginUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // UI labels and input fields
        JLabel userLabel = new JLabel("Database User:");
        userField = new JTextField(20);
        JLabel passLabel = new JLabel("Database Password:");
        passField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        loginStatusLabel.setForeground(Color.RED); // Red for error messages

        // Layout constraints and component placement
        c.insets = new Insets(5, 10, 5, 10);
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0; c.gridy = 0; panel.add(userLabel, c);
        c.gridy = 1; panel.add(passLabel, c);

        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 1; c.gridy = 0; panel.add(userField, c);
        c.gridy = 1; panel.add(passField, c);

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        panel.add(loginButton, c);
        c.gridy = 3;
        panel.add(loginStatusLabel, c);

        // Add panel to frame
        add(panel);

        // Event listeners
        loginButton.addActionListener(e -> attemptLogin());
        passField.addActionListener(e -> attemptLogin());
    }

    // Handles login process and database connection
    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (user.isEmpty()) {
            loginStatusLabel.setText("Enter a username.");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL driver

            // Create database if not already present
            try (Connection tempConn = DriverManager.getConnection(DB_URL_PREFIX, user, pass)) {
                Statement s = tempConn.createStatement();
                s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            // Establish connection to hospital_db
            conn = DriverManager.getConnection(DB_URL_PREFIX + DB_NAME + "?useSSL=false&serverTimezone=UTC", user, pass);
            createTablesIfNotExist(); // Create tables if needed

            loginStatusLabel.setForeground(Color.GREEN);
            loginStatusLabel.setText("Login successful. Preparing application...");

            SwingUtilities.invokeLater(this::initMainUI); // Launch main UI
        } catch (ClassNotFoundException e) {
            showLoginError("MySQL JDBC Driver not found.");
        } catch (SQLException e) {
            showLoginError("Login failed, please try again.");
        }
    }

    // Show login error and reset password field
    private void showLoginError(String message) {
        loginStatusLabel.setForeground(Color.RED);
        loginStatusLabel.setText(message);
        passField.setText("");
        passField.requestFocus();
    }

    // Create required tables for application if they do not already exist
    private void createTablesIfNotExist() throws SQLException {
        String patientsTable = "CREATE TABLE IF NOT EXISTS patients (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "age INT NOT NULL," +
                "gender VARCHAR(10)," +
                "phone VARCHAR(20));";

        String doctorsTable = "CREATE TABLE IF NOT EXISTS doctors (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "specialty VARCHAR(100)," +
                "phone VARCHAR(20));";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(patientsTable);
            stmt.execute(doctorsTable);
        }
    }

    // Initializes the main GUI for managing patients and doctors
    private void initMainUI() {
        setTitle("Hospital Management System");
        getContentPane().removeAll();
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Adding panels to mainPanel
        mainPanel.add(createWelcomePanel(), "WELCOME");
        mainPanel.add(createPatientManagementPanel(), "PATIENTS");
        mainPanel.add(createDoctorManagementPanel(), "DOCTORS");

        add(mainPanel);
        cardLayout.show(mainPanel, "WELCOME");

        revalidate();
        repaint();
        setVisible(true);
    }

    // Welcome screen with navigation buttons
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel(
                "<html><h1>Welcome to Hospital Management System</h1>" +
                        "<p>Please select what you want to do:</p></html>",
                SwingConstants.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // Navigation buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 40));
        JButton patientsBtn = new JButton("Manage Patients");
        JButton doctorsBtn = new JButton("Manage Doctors");
        JButton exitBtn = new JButton("Exit");

        buttonsPanel.add(patientsBtn);
        buttonsPanel.add(doctorsBtn);
        buttonsPanel.add(exitBtn);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        // Button actions
        patientsBtn.addActionListener(e -> {
            loadPatients();
            cardLayout.show(mainPanel, "PATIENTS");
        });

        doctorsBtn.addActionListener(e -> {
            loadDoctors();
            cardLayout.show(mainPanel, "DOCTORS");
        });

        exitBtn.addActionListener(e -> {
            closeConnection();
            System.exit(0);
        });

        return panel;
    }

    // Panel to add and view patient records
    private JPanel createPatientManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        // Form to add new patient
        JPanel addPatientPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        pNameField = new JTextField(25);
        pAgeField = new JTextField(25);
        pGenderField = new JTextField(25);
        pPhoneField = new JTextField(25);
        JButton addPatientBtn = new JButton("Add Patient");

        // Adding fields to the panel
        c.gridx = 0; c.gridy = 0; addPatientPanel.add(new JLabel("Name:"), c);
        c.gridx = 1; addPatientPanel.add(pNameField, c);
        c.gridx = 0; c.gridy = 1; addPatientPanel.add(new JLabel("Age:"), c);
        c.gridx = 1; addPatientPanel.add(pAgeField, c);
        c.gridx = 0; c.gridy = 2; addPatientPanel.add(new JLabel("Gender (M/F):"), c);
        c.gridx = 1; addPatientPanel.add(pGenderField, c);
        c.gridx = 0; c.gridy = 3; addPatientPanel.add(new JLabel("Phone (10 digits):"), c);
        c.gridx = 1; addPatientPanel.add(pPhoneField, c);
        c.gridx = 1; c.gridy = 4; addPatientPanel.add(addPatientBtn, c);

        addPatientBtn.addActionListener(e -> addPatient());

        // Panel to list existing patients
        JPanel listPatientPanel = new JPanel(new BorderLayout());
        patientTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Gender", "Phone"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable patientTable = new JTable(patientTableModel);
        JScrollPane scrollPane = new JScrollPane(patientTable);
        listPatientPanel.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Add Patient", addPatientPanel);
        tabbedPane.addTab("List Patients", listPatientPanel);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.add(backBtn);

        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(backPanel, BorderLayout.SOUTH);
        return panel;
    }

    // Panel to add and list doctors
    private JPanel createDoctorManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel addDoctorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        dNameField = new JTextField(25);
        dSpecialtyField = new JTextField(25);
        dPhoneField = new JTextField(25);
        JButton addDoctorBtn = new JButton("Add Doctor");

        c.gridx = 0; c.gridy = 0; addDoctorPanel.add(new JLabel("Name:"), c);
        c.gridx = 1; addDoctorPanel.add(dNameField, c);
        c.gridx = 0; c.gridy = 1; addDoctorPanel.add(new JLabel("Specialty:"), c);
        c.gridx = 1; addDoctorPanel.add(dSpecialtyField, c);
        c.gridx = 0; c.gridy = 2; addDoctorPanel.add(new JLabel("Phone:"), c);
        c.gridx = 1; addDoctorPanel.add(dPhoneField, c);
        c.gridx = 1; c.gridy = 3; addDoctorPanel.add(addDoctorBtn, c);

        addDoctorBtn.addActionListener(e -> addDoctor());

        JPanel listDoctorPanel = new JPanel(new BorderLayout());
        doctorTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Specialty", "Phone"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable doctorTable = new JTable(doctorTableModel);
        JScrollPane scrollPane = new JScrollPane(doctorTable);
        listDoctorPanel.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Add Doctor", addDoctorPanel);
        tabbedPane.addTab("List Doctors", listDoctorPanel);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "WELCOME"));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.add(backBtn);

        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(backPanel, BorderLayout.SOUTH);
        return panel;
    }

    // Load all patients from database into table
    private void loadPatients() {
        patientTableModel.setRowCount(0);
        String sql = "SELECT * FROM patients ORDER BY id ASC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patientTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            showErrorDialog("Failed to load patients:\n" + e.getMessage());
        }
    }

    // Add a patient to the database after validation
    private void addPatient() {
        String name = pNameField.getText().trim();
        String ageStr = pAgeField.getText().trim();
        String gender = pGenderField.getText().trim().toUpperCase();
        String phone = pPhoneField.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || phone.isEmpty()) {
            showErrorDialog("Please fill all patient fields.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showErrorDialog("Age must be a non-negative integer.");
            return;
        }

        if (!gender.equals("M") && !gender.equals("F")) {
            showErrorDialog("Gender must be 'M' or 'F' only.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            showErrorDialog("Phone number must be exactly 10 digits.");
            return;
        }

        String sql = "INSERT INTO patients (name, age, gender, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient added successfully.");
            clearPatientFields();
            loadPatients();
        } catch (SQLException e) {
            showErrorDialog("Failed to add patient:\n" + e.getMessage());
        }
    }

    // Clear patient input fields
    private void clearPatientFields() {
        pNameField.setText("");
        pAgeField.setText("");
        pGenderField.setText("");
        pPhoneField.setText("");
    }

    // Load all doctors from database into table
    private void loadDoctors() {
        doctorTableModel.setRowCount(0);
        String sql = "SELECT * FROM doctors ORDER BY id ASC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctorTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialty"),
                        rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            showErrorDialog("Failed to load doctors:\n" + e.getMessage());
        }
    }

    // Add doctor after validating fields
    private void addDoctor() {
        String name = dNameField.getText().trim();
        String specialty = dSpecialtyField.getText().trim();
        String phone = dPhoneField.getText().trim();

        if (name.isEmpty() || specialty.isEmpty() || phone.isEmpty()) {
            showErrorDialog("Please fill all doctor fields.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            showErrorDialog("Phone number must be exactly 10 digits.");
            return;
        }

        String sql = "INSERT INTO doctors (name, specialty, phone) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, specialty);
            ps.setString(3, phone);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor added successfully.");
            dNameField.setText("");
            dSpecialtyField.setText("");
            dPhoneField.setText("");
            loadDoctors();
        } catch (SQLException e) {
            showErrorDialog("Failed to add doctor:\n" + e.getMessage());
        }
    }

    // Show error dialog with message
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Close the DB connection when exiting
    private void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ignored) {}
    }

    // Main method to start the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalManagementUI().setVisible(true));
    }
}
