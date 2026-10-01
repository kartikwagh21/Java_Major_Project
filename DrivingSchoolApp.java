import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

// ============================================================================
// MAIN APPLICATION FRAME (CASE STUDY 90)
// ============================================================================
public class DrivingSchoolApp extends JFrame {

    // --- Core Data Structures ---
    private final List<Student> studentList = new ArrayList<>();             // ArrayList: Enrollments
    private final List<TrainingSession> sessionHistory = new LinkedList<>(); // LinkedList: Session history
    private final Map<String, Course> studentCourseMap = new HashMap<>();    // HashMap: Student ID -> Course
    private final List<Test> testList = new ArrayList<>();                   // List: Driving tests

    // --- Modern Zinc Dark Theme Color Palette ---
    private static final Color BG_APP       = new Color(12, 12, 14);
    private static final Color BG_SIDEBAR   = new Color(18, 18, 21);
    private static final Color BG_CARD      = new Color(24, 24, 27);
    private static final Color BG_INPUT     = new Color(32, 32, 36);
    private static final Color BORDER_CARD  = new Color(45, 45, 52);
    private static final Color TEXT_MAIN    = new Color(250, 250, 250);
    private static final Color TEXT_MUTED   = new Color(161, 161, 170);
    private static final Color COLOR_BLUE   = new Color(37, 99, 235);
    private static final Color COLOR_GREEN  = new Color(34, 197, 94);
    private static final Color COLOR_AMBER  = new Color(245, 158, 11);
    private static final Color COLOR_RED    = new Color(239, 68, 68);

    // Navigation & Views
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton[] navButtons;

    // View 1: Students
    private JTextField txtStudentId, txtStudentName, txtStudentPhone, txtStudentFee;
    private JComboBox<String> cbStudentCourse;
    private DefaultTableModel modelStudents;
    private JTable tableStudents;

    // View 2: Sessions
    private JComboBox<String> cbSessionStudent, cbSessionType, cbSessionStatus;
    private JTextField txtSessionDate;
    private DefaultTableModel modelSessions;
    private JTable tableSessions;

    // View 3: Tests & Eligibility
    private JComboBox<String> cbTestStudent, cbTestOutcome;
    private JTextField txtTestDate;
    private JTextArea txtEligibilityStatus;
    private DefaultTableModel modelTests;
    private JTable tableTests;

    // View 4: Search & Reports
    private JTextField txtSearchQuery;
    private JTextArea txtReportsArea;

    // Sidebar Counters
    private JLabel lblTotalStudents, lblTotalSessions, lblTotalTests;

    public DrivingSchoolApp() {
        initLookAndFeel();
        setTitle("Driving School Management System");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1100, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        root.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_APP);

        mainContentPanel.add(createStudentsView(), "STUDENTS");
        mainContentPanel.add(createSessionsView(), "SESSIONS");
        mainContentPanel.add(createTestsView(), "TESTS");
        mainContentPanel.add(createReportsView(), "REPORTS");

        root.add(mainContentPanel, BorderLayout.CENTER);
        add(root);

        loadSampleData();
        switchView(0);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.foreground", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Panel.background", BG_APP);
        UIManager.put("Label.foreground", TEXT_MAIN);
        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("TableHeader.background", BG_SIDEBAR);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("Table.selectionBackground", new Color(40, 50, 75));
        UIManager.put("Table.selectionForeground", TEXT_MAIN);
        UIManager.put("Table.gridColor", BORDER_CARD);
        UIManager.put("Viewport.background", BG_CARD);
        UIManager.put("ScrollPane.background", BG_APP);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER_CARD, 1));
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_MAIN);
        UIManager.put("ComboBox.selectionBackground", COLOR_BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    }

    // =========================================================================
    // SIDEBAR NAVIGATION
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_CARD));

        JPanel brandPanel = new JPanel(new BorderLayout(5, 5));
        brandPanel.setBackground(BG_SIDEBAR);
        brandPanel.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel lblBrand = new JLabel("DRIVING SCHOOL");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblBrand.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel("Management Portal");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        brandPanel.add(lblBrand, BorderLayout.NORTH);
        brandPanel.add(lblSub, BorderLayout.SOUTH);

        JPanel navList = new JPanel(new GridLayout(4, 1, 0, 8));
        navList.setBackground(BG_SIDEBAR);
        navList.setBorder(new EmptyBorder(10, 14, 10, 14));

        String[] navLabels = {"Student Enrollment", "Training Sessions", "Test & Eligibility", "Search & Analytics"};
        navButtons = new JButton[navLabels.length];
        for (int i = 0; i < navLabels.length; i++) {
            final int index = i;
            JButton btn = createNavButton(navLabels[i]);
            btn.addActionListener(e -> switchView(index));
            navButtons[i] = btn;
            navList.add(btn);
        }

        JPanel centerNav = new JPanel(new BorderLayout());
        centerNav.setBackground(BG_SIDEBAR);
        centerNav.add(navList, BorderLayout.NORTH);

        JPanel footer = new JPanel(new GridLayout(3, 1, 4, 4));
        footer.setBackground(new Color(15, 15, 17));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD),
            new EmptyBorder(14, 18, 14, 18)
        ));

        lblTotalStudents = new JLabel("Students: 0");
        lblTotalSessions = new JLabel("Sessions: 0");
        lblTotalTests = new JLabel("Tests: 0");
        for (JLabel l : new JLabel[]{lblTotalStudents, lblTotalSessions, lblTotalTests}) {
            l.setFont(new Font("SansSerif", Font.PLAIN, 11));
            l.setForeground(TEXT_MUTED);
            footer.add(l);
        }

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(centerNav, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getForeground().equals(TEXT_MAIN)) btn.setBackground(new Color(28, 28, 32));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getForeground().equals(TEXT_MAIN)) btn.setBackground(BG_SIDEBAR);
            }
        });
        return btn;
    }

    private void switchView(int index) {
        String[] cards = {"STUDENTS", "SESSIONS", "TESTS", "REPORTS"};
        for (int i = 0; i < navButtons.length; i++) {
            if (i == index) {
                navButtons[i].setBackground(new Color(36, 36, 42));
                navButtons[i].setForeground(TEXT_MAIN);
                navButtons[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            } else {
                navButtons[i].setBackground(BG_SIDEBAR);
                navButtons[i].setForeground(TEXT_MUTED);
                navButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            }
        }
        cardLayout.show(mainContentPanel, cards[index]);
    }

    private void updateSidebarStats() {
        lblTotalStudents.setText("Students: " + studentList.size());
        lblTotalSessions.setText("Sessions Logged: " + sessionHistory.size());
        lblTotalTests.setText("Tests Scheduled: " + testList.size());
    }

    // =========================================================================
    // VIEW 1: STUDENTS (CRUD ENROLLMENT)
    // =========================================================================
    private JPanel createStudentsView() {
        JPanel view = new JPanel(new BorderLayout(16, 16));
        view.setBackground(BG_APP);
        view.setBorder(new EmptyBorder(18, 20, 18, 20));
        view.add(createViewHeader("Student Enrollment", "Manage student registrations and course track assignments (CRUD)"), BorderLayout.NORTH);

        JPanel splitContainer = new JPanel(new BorderLayout(16, 16));
        splitContainer.setBackground(BG_APP);

        JPanel formCard = createCardPanel();
        formCard.setPreferredSize(new Dimension(360, 0));

        JPanel formFields = new JPanel(new GridLayout(5, 1, 0, 10));
        formFields.setOpaque(false);

        txtStudentId = createInput();
        txtStudentName = createInput();
        txtStudentPhone = createInput();
        txtStudentFee = createInput();
        cbStudentCourse = createComboBox(new String[]{"Two-Wheeler", "Four-Wheeler"});

        formFields.add(createLabeledField("Student ID", txtStudentId));
        formFields.add(createLabeledField("Full Name", txtStudentName));
        formFields.add(createLabeledField("Phone Number", txtStudentPhone));
        formFields.add(createLabeledField("Course Track", cbStudentCourse));
        formFields.add(createLabeledField("Fee Paid (₹)", txtStudentFee));

        JButton btnAdd = createActionButton("Add Student", COLOR_BLUE);
        JButton btnUpdate = createActionButton("Update", new Color(63, 63, 70));
        JButton btnDelete = createActionButton("Delete", COLOR_RED);
        JButton btnClear = createActionButton("Clear", new Color(39, 39, 42));

        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 8, 8)); btnRow1.setOpaque(false); btnRow1.add(btnAdd); btnRow1.add(btnUpdate);
        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 8, 8)); btnRow2.setOpaque(false); btnRow2.add(btnDelete); btnRow2.add(btnClear);

        JPanel btnBox = new JPanel(new GridLayout(2, 1, 0, 8)); btnBox.setOpaque(false); btnBox.add(btnRow1); btnBox.add(btnRow2);

        JPanel formCenter = new JPanel(new BorderLayout());
        formCenter.setOpaque(false);
        formCenter.add(formFields, BorderLayout.NORTH);

        formCard.add(formCenter, BorderLayout.CENTER);
        formCard.add(btnBox, BorderLayout.SOUTH);

        JPanel tableCard = createCardPanel();
        modelStudents = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Track", "Fee Paid", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableStudents = createCustomTable(modelStudents);
        tableStudents.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        tableStudents.getSelectionModel().addListSelectionListener(e -> {
            int row = tableStudents.getSelectedRow();
            if (row >= 0) {
                txtStudentId.setText(modelStudents.getValueAt(row, 0).toString());
                txtStudentName.setText(modelStudents.getValueAt(row, 1).toString());
                txtStudentPhone.setText(modelStudents.getValueAt(row, 2).toString());
                cbStudentCourse.setSelectedItem(modelStudents.getValueAt(row, 3).toString());
                txtStudentFee.setText(modelStudents.getValueAt(row, 4).toString().replace("₹", ""));
            }
        });

        tableCard.add(new JScrollPane(tableStudents), BorderLayout.CENTER);
        splitContainer.add(formCard, BorderLayout.WEST);
        splitContainer.add(tableCard, BorderLayout.CENTER);
        view.add(splitContainer, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearStudentForm());

        return view;
    }

    private void addStudent() {
        try {
            String id = txtStudentId.getText().trim();
            String name = txtStudentName.getText().trim();
            String phone = txtStudentPhone.getText().trim();
            String feeStr = txtStudentFee.getText().trim();

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
                throw new InvalidDataException("All enrollment fields are required.");
            }
            if (findStudent(id) != null) {
                throw new InvalidDataException("Student ID '" + id + "' is already enrolled.");
            }

            Course course = cbStudentCourse.getSelectedItem().equals("Two-Wheeler") ? new TwoWheelerCourse() : new FourWheelerCourse();
            double feePaid = parseAndValidateFee(feeStr, course);

            Student student = new Student(id, name, phone, feePaid);
            studentList.add(student);
            studentCourseMap.put(id, course);

            refreshStudentTable();
            refreshDropdowns();
            updateSidebarStats();
            clearStudentForm();
            showDarkDialog("Student enrolled successfully.", "Enrolled", JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidDataException ex) {
            showDarkDialog(ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showDarkDialog("Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        try {
            String id = txtStudentId.getText().trim();
            Student s = findStudent(id);
            if (s == null) throw new InvalidDataException("Select a student to update.");

            String name = txtStudentName.getText().trim();
            String phone = txtStudentPhone.getText().trim();
            String feeStr = txtStudentFee.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) throw new InvalidDataException("Name and phone cannot be empty.");

            Course updatedCourse = cbStudentCourse.getSelectedItem().equals("Two-Wheeler") ? new TwoWheelerCourse() : new FourWheelerCourse();
            double feePaid = parseAndValidateFee(feeStr, updatedCourse);

            s.setName(name);
            s.setPhone(phone);
            s.setFeePaid(feePaid);
            studentCourseMap.put(id, updatedCourse);

            refreshStudentTable();
            refreshDropdowns();
            updateSidebarStats();
            showDarkDialog("Student details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidDataException ex) {
            showDarkDialog(ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showDarkDialog(ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseAndValidateFee(String feeStr, Course course) throws InvalidDataException {
        if (feeStr == null || feeStr.trim().isEmpty()) throw new InvalidDataException("Fee amount cannot be empty.");
        double fee;
        try {
            fee = Double.parseDouble(feeStr.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Invalid fee format: '" + feeStr + "'.\nPlease enter a valid numeric amount (e.g. " + (int)course.getFee() + ").");
        }
        if (fee < 0) throw new InvalidDataException("Fee amount cannot be negative.");
        if (fee > 30000.0) throw new InvalidDataException("Fee amount exceeds maximum limit (₹30,000.00).");
        if (fee > course.getFee()) {
            throw new InvalidDataException("Fee amount ₹" + String.format("%.2f", fee) + " exceeds course fee (₹" + String.format("%.2f", course.getFee()) + ").");
        }
        return fee;
    }

    private void deleteStudent() {
        String id = txtStudentId.getText().trim();
        Student s = findStudent(id);
        if (s != null && showDarkConfirm("Remove Student " + id + " and all associated session/test records?", "Confirm Deletion")) {
            studentList.remove(s);
            studentCourseMap.remove(id);
            sessionHistory.removeIf(ts -> ts.getStudentId().equalsIgnoreCase(id));
            testList.removeIf(t -> t.getStudentId().equalsIgnoreCase(id));
            refreshStudentTable();
            refreshSessionTable();
            refreshTestTable();
            refreshDropdowns();
            updateSidebarStats();
            clearStudentForm();
        }
    }

    private void clearStudentForm() {
        txtStudentId.setText("");
        txtStudentName.setText("");
        txtStudentPhone.setText("");
        txtStudentFee.setText("");
    }

    // =========================================================================
    // VIEW 2: TRAINING SESSIONS & ATTENDANCE (LINKEDLIST)
    // =========================================================================
    private JPanel createSessionsView() {
        JPanel view = new JPanel(new BorderLayout(16, 16));
        view.setBackground(BG_APP);
        view.setBorder(new EmptyBorder(18, 20, 18, 20));
        view.add(createViewHeader("Training Sessions & Attendance", "Log theory and practical sessions into LinkedList history log"), BorderLayout.NORTH);

        JPanel splitContainer = new JPanel(new BorderLayout(16, 16));
        splitContainer.setBackground(BG_APP);

        JPanel formCard = createCardPanel();
        formCard.setPreferredSize(new Dimension(360, 0));

        JPanel formFields = new JPanel(new GridLayout(4, 1, 0, 10));
        formFields.setOpaque(false);

        cbSessionStudent = createComboBox(new String[]{});
        cbSessionType = createComboBox(new String[]{"THEORY", "PRACTICAL"});
        cbSessionStatus = createComboBox(new String[]{"COMPLETED", "SCHEDULED", "ABSENT"});
        txtSessionDate = createInput();
        txtSessionDate.setText("2026-03-30");

        formFields.add(createLabeledField("Select Student", cbSessionStudent));
        formFields.add(createLabeledField("Session Type", cbSessionType));
        formFields.add(createLabeledField("Attendance Status", cbSessionStatus));
        formFields.add(createLabeledField("Session Date (YYYY-MM-DD)", txtSessionDate));

        JButton btnLog = createActionButton("Log Session", COLOR_GREEN);
        JButton btnUpdateSession = createActionButton("Update", new Color(63, 63, 70));
        JButton btnDeleteSession = createActionButton("Delete", COLOR_RED);
        JButton btnClearSession = createActionButton("Clear", new Color(39, 39, 42));

        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 8, 8)); btnRow1.setOpaque(false); btnRow1.add(btnLog); btnRow1.add(btnUpdateSession);
        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 8, 8)); btnRow2.setOpaque(false); btnRow2.add(btnDeleteSession); btnRow2.add(btnClearSession);

        JPanel btnBox = new JPanel(new GridLayout(2, 1, 0, 8)); btnBox.setOpaque(false); btnBox.add(btnRow1); btnBox.add(btnRow2);

        JPanel formCenter = new JPanel(new BorderLayout());
        formCenter.setOpaque(false);
        formCenter.add(formFields, BorderLayout.NORTH);

        formCard.add(formCenter, BorderLayout.CENTER);
        formCard.add(btnBox, BorderLayout.SOUTH);

        JPanel tableCard = createCardPanel();
        modelSessions = new DefaultTableModel(new String[]{"Session ID", "Student ID", "Type", "Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableSessions = createCustomTable(modelSessions);
        tableSessions.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        tableSessions.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tableSessions.getSelectedRow();
            if (row >= 0 && row < modelSessions.getRowCount()) {
                String studentId = modelSessions.getValueAt(row, 1).toString();
                String type = modelSessions.getValueAt(row, 2).toString();
                String date = modelSessions.getValueAt(row, 3).toString();
                String status = modelSessions.getValueAt(row, 4).toString();

                for (int i = 0; i < cbSessionStudent.getItemCount(); i++) {
                    if (cbSessionStudent.getItemAt(i).startsWith(studentId)) {
                        cbSessionStudent.setSelectedIndex(i);
                        break;
                    }
                }
                cbSessionType.setSelectedItem(type);
                cbSessionStatus.setSelectedItem(status);
                txtSessionDate.setText(date);
            }
        });

        tableCard.add(new JScrollPane(tableSessions), BorderLayout.CENTER);
        splitContainer.add(formCard, BorderLayout.WEST);
        splitContainer.add(tableCard, BorderLayout.CENTER);
        view.add(splitContainer, BorderLayout.CENTER);

        btnLog.addActionListener(e -> {
            try {
                if (cbSessionStudent.getSelectedItem() == null) throw new InvalidDataException("Select an enrolled student.");
                String date = parseAndValidateDate(txtSessionDate.getText());
                String studentId = cbSessionStudent.getSelectedItem().toString().split(" - ")[0];
                String type = cbSessionType.getSelectedItem().toString();
                String status = cbSessionStatus.getSelectedItem().toString();

                sessionHistory.add(new TrainingSession("SESS-" + (sessionHistory.size() + 101), studentId, type, date, status));
                refreshSessionTable();
                previewEligibility();
                updateSidebarStats();
                showDarkDialog("Session logged successfully.", "Session Logged", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidDataException ex) {
                showDarkDialog(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdateSession.addActionListener(e -> {
            try {
                int row = tableSessions.getSelectedRow();
                if (row < 0 || row >= modelSessions.getRowCount()) throw new InvalidDataException("Select a session record to update.");
                String sessId = modelSessions.getValueAt(row, 0).toString();
                String date = parseAndValidateDate(txtSessionDate.getText());
                String type = cbSessionType.getSelectedItem().toString();
                String status = cbSessionStatus.getSelectedItem().toString();

                for (TrainingSession ts : sessionHistory) {
                    if (ts.getSessionId().equals(sessId)) {
                        ts.setType(type); ts.setDate(date); ts.setStatus(status);
                        break;
                    }
                }
                refreshSessionTable();
                previewEligibility();
                updateSidebarStats();
                showDarkDialog("Session record " + sessId + " updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidDataException ex) {
                showDarkDialog(ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteSession.addActionListener(e -> {
            int row = tableSessions.getSelectedRow();
            if (row < 0 || row >= modelSessions.getRowCount()) {
                showDarkDialog("Select a session record from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sessId = modelSessions.getValueAt(row, 0).toString();
            if (showDarkConfirm("Delete session record " + sessId + "?", "Confirm Delete")) {
                sessionHistory.removeIf(ts -> ts.getSessionId().equals(sessId));
                refreshSessionTable();
                previewEligibility();
                updateSidebarStats();
                showDarkDialog("Session record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnClearSession.addActionListener(e -> {
            txtSessionDate.setText("2026-03-30");
            tableSessions.clearSelection();
        });

        return view;
    }

    // =========================================================================
    // VIEW 3: TEST ELIGIBILITY & SCHEDULING
    // =========================================================================
    private JPanel createTestsView() {
        JPanel view = new JPanel(new BorderLayout(16, 16));
        view.setBackground(BG_APP);
        view.setBorder(new EmptyBorder(18, 20, 18, 20));
        view.add(createViewHeader("Test Eligibility & Scheduling", "Verify session/fee requirements and schedule final driving tests"), BorderLayout.NORTH);

        JPanel splitContainer = new JPanel(new BorderLayout(16, 16));
        splitContainer.setBackground(BG_APP);

        JPanel formCard = createCardPanel();
        formCard.setPreferredSize(new Dimension(380, 0));

        JPanel formFields = new JPanel(new GridLayout(3, 1, 0, 10));
        formFields.setOpaque(false);

        cbTestStudent = createComboBox(new String[]{});
        txtTestDate = createInput();
        txtTestDate.setText("2026-04-15");
        cbTestOutcome = createComboBox(new String[]{"SCHEDULED", "PASSED", "FAILED"});

        formFields.add(createLabeledField("Candidate Student", cbTestStudent));
        formFields.add(createLabeledField("Test Date (YYYY-MM-DD)", txtTestDate));
        formFields.add(createLabeledField("Test Outcome", cbTestOutcome));

        txtEligibilityStatus = new JTextArea(6, 20);
        txtEligibilityStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtEligibilityStatus.setBackground(new Color(18, 18, 21));
        txtEligibilityStatus.setForeground(new Color(228, 228, 231));
        txtEligibilityStatus.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtEligibilityStatus.setEditable(false);

        JPanel statusWrapper = new JPanel(new BorderLayout(4, 4));
        statusWrapper.setOpaque(false);
        JLabel lblCheck = new JLabel("Eligibility Status Preview:");
        lblCheck.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblCheck.setForeground(TEXT_MUTED);
        statusWrapper.add(lblCheck, BorderLayout.NORTH);
        statusWrapper.add(new JScrollPane(txtEligibilityStatus), BorderLayout.CENTER);

        cbTestStudent.addActionListener(e -> previewEligibility());

        JButton btnSchedule = createActionButton("Schedule Test", COLOR_BLUE);
        JButton btnUpdateOutcome = createActionButton("Update Outcome", COLOR_AMBER);
        JButton btnDeleteTest = createActionButton("Cancel / Delete", COLOR_RED);

        JPanel btnGrid = new JPanel(new GridLayout(1, 3, 6, 6));
        btnGrid.setOpaque(false);
        btnGrid.add(btnSchedule); btnGrid.add(btnUpdateOutcome); btnGrid.add(btnDeleteTest);

        JPanel leftCenter = new JPanel(new BorderLayout(0, 10));
        leftCenter.setOpaque(false);
        leftCenter.add(formFields, BorderLayout.NORTH);
        leftCenter.add(statusWrapper, BorderLayout.CENTER);

        formCard.add(leftCenter, BorderLayout.CENTER);
        formCard.add(btnGrid, BorderLayout.SOUTH);

        JPanel tableCard = createCardPanel();
        modelTests = new DefaultTableModel(new String[]{"Test ID", "Student ID", "Track", "Test Date", "Outcome"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTests = createCustomTable(modelTests);
        tableTests.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        tableTests.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tableTests.getSelectedRow();
            if (row >= 0 && row < modelTests.getRowCount()) {
                String studentId = modelTests.getValueAt(row, 1).toString();
                String testDate = modelTests.getValueAt(row, 3).toString();
                String outcome = modelTests.getValueAt(row, 4).toString();

                for (int i = 0; i < cbTestStudent.getItemCount(); i++) {
                    if (cbTestStudent.getItemAt(i).startsWith(studentId)) {
                        cbTestStudent.setSelectedIndex(i);
                        break;
                    }
                }
                txtTestDate.setText(testDate);
                cbTestOutcome.setSelectedItem(outcome);
                previewEligibility();
            }
        });

        tableCard.add(new JScrollPane(tableTests), BorderLayout.CENTER);
        splitContainer.add(formCard, BorderLayout.WEST);
        splitContainer.add(tableCard, BorderLayout.CENTER);
        view.add(splitContainer, BorderLayout.CENTER);

        btnSchedule.addActionListener(e -> scheduleTest());
        btnUpdateOutcome.addActionListener(e -> {
            int row = tableTests.getSelectedRow();
            String outcome = (cbTestOutcome.getSelectedItem() != null) ? cbTestOutcome.getSelectedItem().toString() : "SCHEDULED";

            if (row >= 0 && row < modelTests.getRowCount()) {
                String testId = modelTests.getValueAt(row, 0).toString();
                for (Test t : testList) {
                    if (t.getTestId().equals(testId)) { t.setResult(outcome); break; }
                }
                refreshTestTable();
                for (int i = 0; i < modelTests.getRowCount(); i++) {
                    if (modelTests.getValueAt(i, 0).toString().equals(testId)) { tableTests.setRowSelectionInterval(i, i); break; }
                }
                updateSidebarStats();
                showDarkDialog("Test " + testId + " outcome updated to '" + outcome + "'.", "Outcome Updated", JOptionPane.INFORMATION_MESSAGE);
            } else if (cbTestStudent.getSelectedItem() != null) {
                String studentId = cbTestStudent.getSelectedItem().toString().split(" - ")[0];
                Test targetTest = null;
                for (int i = testList.size() - 1; i >= 0; i--) {
                    if (testList.get(i).getStudentId().equals(studentId)) { targetTest = testList.get(i); break; }
                }
                if (targetTest != null) {
                    targetTest.setResult(outcome);
                    refreshTestTable();
                    for (int i = 0; i < modelTests.getRowCount(); i++) {
                        if (modelTests.getValueAt(i, 0).toString().equals(targetTest.getTestId())) { tableTests.setRowSelectionInterval(i, i); break; }
                    }
                    updateSidebarStats();
                    showDarkDialog("Test " + targetTest.getTestId() + " outcome updated to '" + outcome + "'.", "Outcome Updated", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showDarkDialog("No existing test record found for candidate.\nPlease click 'Schedule Test' first.", "No Test Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnDeleteTest.addActionListener(e -> {
            int row = tableTests.getSelectedRow();
            if (row >= 0 && row < modelTests.getRowCount()) {
                String testId = modelTests.getValueAt(row, 0).toString();
                if (showDarkConfirm("Cancel / Delete test record " + testId + "?", "Confirm Deletion")) {
                    testList.removeIf(t -> t.getTestId().equals(testId));
                    refreshTestTable();
                    updateSidebarStats();
                    showDarkDialog("Test record " + testId + " removed.", "Test Cancelled", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                showDarkDialog("Please select a test record from the table to cancel / delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        return view;
    }

    private void previewEligibility() {
        if (cbTestStudent.getSelectedItem() == null) return;
        String studentId = cbTestStudent.getSelectedItem().toString().split(" - ")[0];
        StringBuilder sb = new StringBuilder();
        boolean eligible = checkEligibility(studentId, sb);
        sb.append("\nFinal Decision: ").append(eligible ? "ELIGIBLE FOR TEST" : "NOT ELIGIBLE");
        txtEligibilityStatus.setText(sb.toString());
    }

    private boolean checkEligibility(String studentId, StringBuilder log) {
        Student s = findStudent(studentId);
        Course c = studentCourseMap.get(studentId);
        if (s == null || c == null) return false;

        boolean pass = true;
        if (s.getFeePaid() < c.getFee()) {
            log.append("• Fee: Pending (Paid ₹").append(s.getFeePaid()).append(" of ₹").append(c.getFee()).append(")\n");
            pass = false;
        } else {
            log.append("• Fee: Paid in full (₹").append(s.getFeePaid()).append(")\n");
        }

        int theoryCount = 0, practicalCount = 0;
        for (TrainingSession ts : sessionHistory) {
            if (ts.getStudentId().equals(studentId) && "COMPLETED".equalsIgnoreCase(ts.getStatus())) {
                if ("THEORY".equalsIgnoreCase(ts.getType())) theoryCount++;
                if ("PRACTICAL".equalsIgnoreCase(ts.getType())) practicalCount++;
            }
        }

        if (theoryCount < c.getRequiredTheory()) {
            log.append("• Theory: ").append(theoryCount).append("/").append(c.getRequiredTheory()).append(" (Shortage)\n");
            pass = false;
        } else {
            log.append("• Theory: ").append(theoryCount).append("/").append(c.getRequiredTheory()).append(" (Satisfied)\n");
        }

        if (practicalCount < c.getRequiredPractical()) {
            log.append("• Practical: ").append(practicalCount).append("/").append(c.getRequiredPractical()).append(" (Shortage)\n");
            pass = false;
        } else {
            log.append("• Practical: ").append(practicalCount).append("/").append(c.getRequiredPractical()).append(" (Satisfied)\n");
        }

        return pass;
    }

    private void scheduleTest() {
        try {
            if (cbTestStudent.getSelectedItem() == null) throw new InvalidDataException("Select a student.");
            String date = parseAndValidateDate(txtTestDate.getText());
            String id = cbTestStudent.getSelectedItem().toString().split(" - ")[0];
            StringBuilder log = new StringBuilder();

            if (!checkEligibility(id, log)) {
                throw new IneligibleException("Student is not eligible for test:\n" + log.toString());
            }

            Course c = studentCourseMap.get(id);
            String testId = "TEST-" + (testList.size() + 501);
            testList.add(new Test(testId, id, date, c.getType(), "SCHEDULED"));
            refreshTestTable();
            for (int i = 0; i < modelTests.getRowCount(); i++) {
                if (modelTests.getValueAt(i, 0).toString().equals(testId)) { tableTests.setRowSelectionInterval(i, i); break; }
            }
            updateSidebarStats();
            showDarkDialog("Driving Test Scheduled (" + testId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IneligibleException | InvalidDataException ex) {
            showDarkDialog(ex.getMessage(), "Scheduling Blocked", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String parseAndValidateDate(String rawDate) throws InvalidDataException {
        if (rawDate == null || rawDate.trim().isEmpty()) throw new InvalidDataException("Date cannot be empty.");
        String trimmed = rawDate.trim();
        if (!trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new InvalidDataException("Invalid date format: '" + trimmed + "'. Use YYYY-MM-DD.");
        }
        try {
            DateTimeFormatter strictFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
            LocalDate date = LocalDate.parse(trimmed, strictFormatter);
            if (date.getYear() < 2000 || date.getYear() > 2100) {
                throw new InvalidDataException("Year out of valid range (2000 - 2100).");
            }
            return trimmed;
        } catch (DateTimeParseException ex) {
            throw new InvalidDataException("Invalid calendar date: '" + trimmed + "'.");
        }
    }

    // =========================================================================
    // VIEW 4: SEARCH, SORTING (TREEMAP) & SUMMARY REPORTS
    // =========================================================================
    private JPanel createReportsView() {
        JPanel view = new JPanel(new BorderLayout(16, 16));
        view.setBackground(BG_APP);
        view.setBorder(new EmptyBorder(18, 20, 18, 20));
        view.add(createViewHeader("Search & Analytics", "Search records, sort by sessions via TreeMap, and generate school audit reports"), BorderLayout.NORTH);

        JPanel mainCard = createCardPanel();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setOpaque(false);

        txtSearchQuery = createInput();
        txtSearchQuery.setPreferredSize(new Dimension(150, 34));

        JButton btnSearch = createActionButton("Search", COLOR_BLUE);
        JButton btnSortTreeMap = createActionButton("Sort by Sessions (TreeMap)", new Color(63, 63, 70));
        JButton btnSortDates = createActionButton("Sort Tests by Date", new Color(63, 63, 70));
        JButton btnFullReport = createActionButton("Full School Report", COLOR_GREEN);

        toolbar.add(new JLabel("Filter:"));
        toolbar.add(txtSearchQuery);
        toolbar.add(btnSearch);
        toolbar.add(btnSortTreeMap);
        toolbar.add(btnSortDates);
        toolbar.add(btnFullReport);

        txtReportsArea = new JTextArea();
        txtReportsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReportsArea.setBackground(new Color(15, 15, 18));
        txtReportsArea.setForeground(TEXT_MAIN);
        txtReportsArea.setCaretColor(Color.WHITE);
        txtReportsArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        txtReportsArea.setEditable(false);

        mainCard.add(toolbar, BorderLayout.NORTH);
        mainCard.add(new JScrollPane(txtReportsArea), BorderLayout.CENTER);
        view.add(mainCard, BorderLayout.CENTER);

        // Search Action
        btnSearch.addActionListener(e -> {
            String q = txtSearchQuery.getText().trim().toLowerCase();
            StringBuilder sb = new StringBuilder("SEARCH RESULTS (Student ID, Name, or Course Track):\n\n");
            sb.append(String.format("%-12s %-20s %-14s %-16s %-10s\n", "ID", "NAME", "PHONE", "COURSE", "FEE PAID"));
            sb.append("----------------------------------------------------------------------\n");

            int count = 0;
            for (Student s : studentList) {
                Course c = studentCourseMap.get(s.getId());
                String track = (c != null) ? c.getType() : "N/A";
                if (s.getId().toLowerCase().contains(q) || s.getName().toLowerCase().contains(q) || track.toLowerCase().contains(q)) {
                    sb.append(String.format("%-12s %-20s %-14s %-16s ₹%-10.2f\n", s.getId(), s.getName(), s.getPhone(), track, s.getFeePaid()));
                    count++;
                }
            }
            sb.append("----------------------------------------------------------------------\n");
            sb.append("Total Matches: ").append(count).append("\n");
            txtReportsArea.setText(sb.toString());
        });

        // Sort using TreeMap (Case Study Requirement)
        btnSortTreeMap.addActionListener(e -> {
            TreeMap<Integer, List<Student>> treeMap = new TreeMap<>(Collections.reverseOrder());
            for (Student s : studentList) {
                int count = 0;
                for (TrainingSession ts : sessionHistory) {
                    if (ts.getStudentId().equals(s.getId()) && "COMPLETED".equalsIgnoreCase(ts.getStatus())) count++;
                }
                treeMap.computeIfAbsent(count, k -> new ArrayList<>()).add(s);
            }

            StringBuilder sb = new StringBuilder("STUDENTS RANKED BY COMPLETED SESSIONS (TreeMap Descending):\n\n");
            for (Map.Entry<Integer, List<Student>> entry : treeMap.entrySet()) {
                sb.append("Completed Sessions: ").append(entry.getKey()).append("\n");
                for (Student s : entry.getValue()) {
                    Course c = studentCourseMap.get(s.getId());
                    String track = (c != null) ? c.getType() : "N/A";
                    sb.append("   • [").append(s.getId()).append("] ").append(s.getName())
                      .append(" | Track: ").append(track).append(" | Paid: ₹").append(s.getFeePaid()).append("\n");
                }
                sb.append("\n");
            }
            txtReportsArea.setText(sb.toString());
        });

        // Sort Tests by Date
        btnSortDates.addActionListener(e -> {
            List<Test> sorted = new ArrayList<>(testList);
            sorted.sort(Comparator.comparing(Test::getDate));

            StringBuilder sb = new StringBuilder("DRIVING TESTS (Chronological Order by Date):\n\n");
            sb.append(String.format("%-12s %-12s %-12s %-16s %-10s\n", "DATE", "TEST ID", "STUDENT ID", "TRACK", "RESULT"));
            sb.append("----------------------------------------------------------------------\n");
            for (Test t : sorted) {
                sb.append(String.format("%-12s %-12s %-12s %-16s %-10s\n", t.getDate(), t.getTestId(), t.getStudentId(), t.getTrack(), t.getResult()));
            }
            txtReportsArea.setText(sb.toString());
        });

        // Full Audit Report
        btnFullReport.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("DRIVING SCHOOL SUMMARY & ELIGIBILITY REPORT\n");
            sb.append("======================================================================\n\n");
            sb.append("Total Enrolled Students: ").append(studentList.size()).append("\n");
            sb.append("Total Sessions Logged:   ").append(sessionHistory.size()).append("\n");
            sb.append("Total Tests Scheduled:   ").append(testList.size()).append("\n\n");
            sb.append("INDIVIDUAL STATUS AUDIT:\n");
            sb.append("----------------------------------------------------------------------\n");

            for (Student s : studentList) {
                StringBuilder log = new StringBuilder();
                boolean ok = checkEligibility(s.getId(), log);
                Course c = studentCourseMap.get(s.getId());
                String track = (c != null) ? c.getType() : "N/A";

                sb.append("[").append(s.getId()).append("] ").append(s.getName()).append(" (").append(track).append(")\n");
                sb.append(log.toString());
                sb.append("Status: ").append(ok ? "READY FOR FINAL TEST" : "INELIGIBLE").append("\n\n");
            }
            txtReportsArea.setText(sb.toString());
        });

        return view;
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================
    private void showDarkDialog(String message, String title, int type) {
        JLabel label = new JLabel("<html><div style='color: #FFFFFF; font-family: sans-serif; font-size: 13px; padding: 4px;'>" 
                + message.replace("\n", "<br>") + "</div></html>");
        JOptionPane.showMessageDialog(this, label, title, type);
    }

    private boolean showDarkConfirm(String message, String title) {
        JLabel label = new JLabel("<html><div style='color: #FFFFFF; font-family: sans-serif; font-size: 13px; padding: 4px;'>" 
                + message.replace("\n", "<br>") + "</div></html>");
        return JOptionPane.showConfirmDialog(this, label, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private JPanel createViewHeader(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
        p.setBackground(BG_APP);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        p.add(lblTitle); p.add(lblSub);
        return p;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        field.setPreferredSize(new Dimension(0, 62));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);

        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JTextField createInput() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_CARD, 1));
        return cb;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTable createCustomTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_CARD);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : new Color(28, 28, 33));
                    c.setForeground(TEXT_MAIN);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        return table;
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String val = (value != null) ? value.toString() : "";
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) label.setBackground(row % 2 == 0 ? BG_CARD : new Color(28, 28, 33));

            if (val.contains("PAID") || "COMPLETED".equals(val) || "PASSED".equals(val)) {
                label.setForeground(COLOR_GREEN);
            } else if (val.contains("DUE") || "SCHEDULED".equals(val)) {
                label.setForeground(COLOR_AMBER);
            } else if ("ABSENT".equals(val) || "FAILED".equals(val)) {
                label.setForeground(COLOR_RED);
            } else {
                label.setForeground(TEXT_MAIN);
            }
            return label;
        }
    }

    private Student findStudent(String id) {
        for (Student s : studentList) {
            if (s.getId().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private void refreshStudentTable() {
        modelStudents.setRowCount(0);
        for (Student s : studentList) {
            Course c = studentCourseMap.get(s.getId());
            String track = (c != null) ? c.getType() : "N/A";
            double feeReq = (c != null) ? c.getFee() : 0.0;
            String status = s.getFeePaid() >= feeReq ? "PAID" : "DUE (₹" + (feeReq - s.getFeePaid()) + ")";
            modelStudents.addRow(new Object[]{s.getId(), s.getName(), s.getPhone(), track, "₹" + s.getFeePaid(), status});
        }
    }

    private void refreshSessionTable() {
        modelSessions.setRowCount(0);
        for (TrainingSession ts : sessionHistory) {
            modelSessions.addRow(new Object[]{ts.getSessionId(), ts.getStudentId(), ts.getType(), ts.getDate(), ts.getStatus()});
        }
    }

    private void refreshTestTable() {
        modelTests.setRowCount(0);
        for (Test t : testList) {
            modelTests.addRow(new Object[]{t.getTestId(), t.getStudentId(), t.getTrack(), t.getDate(), t.getResult()});
        }
    }

    private void refreshDropdowns() {
        cbSessionStudent.removeAllItems();
        cbTestStudent.removeAllItems();
        for (Student s : studentList) {
            String item = s.getId() + " - " + s.getName();
            cbSessionStudent.addItem(item);
            cbTestStudent.addItem(item);
        }
        previewEligibility();
    }

    // =========================================================================
    // SAMPLE DATA INITIALIZER
    // =========================================================================
    private void loadSampleData() {
        // Students
        studentList.addAll(Arrays.asList(
            new Student("STU-101", "Aarav Sharma", "9876543210", 3500.0),
            new Student("STU-102", "Ananya Verma", "9123456789", 4000.0),
            new Student("STU-103", "Rohan Mehta", "9988776655", 7500.0),
            new Student("STU-104", "Priya Patel", "9811223344", 3500.0),
            new Student("STU-105", "Vikram Singh", "9765432109", 7500.0),
            new Student("STU-106", "Sneha Kulkarni", "9654321870", 3500.0),
            new Student("STU-107", "Kabir Deshmukh", "9543216789", 7500.0),
            new Student("STU-108", "Neha Joshi", "9432165870", 3500.0)
        ));

        studentCourseMap.put("STU-101", new TwoWheelerCourse());
        studentCourseMap.put("STU-102", new FourWheelerCourse());
        studentCourseMap.put("STU-103", new FourWheelerCourse());
        studentCourseMap.put("STU-104", new TwoWheelerCourse());
        studentCourseMap.put("STU-105", new FourWheelerCourse());
        studentCourseMap.put("STU-106", new TwoWheelerCourse());
        studentCourseMap.put("STU-107", new FourWheelerCourse());
        studentCourseMap.put("STU-108", new TwoWheelerCourse());

        // Sessions (LinkedList)
        sessionHistory.add(new TrainingSession("SESS-101", "STU-101", "THEORY", "2026-03-01", "COMPLETED"));
        sessionHistory.add(new TrainingSession("SESS-102", "STU-101", "THEORY", "2026-03-02", "COMPLETED"));
        sessionHistory.add(new TrainingSession("SESS-103", "STU-101", "THEORY", "2026-03-03", "ABSENT"));
        sessionHistory.add(new TrainingSession("SESS-104", "STU-101", "THEORY", "2026-03-04", "COMPLETED"));
        sessionHistory.add(new TrainingSession("SESS-105", "STU-101", "THEORY", "2026-03-05", "COMPLETED"));
        sessionHistory.add(new TrainingSession("SESS-106", "STU-101", "THEORY", "2026-03-06", "COMPLETED"));
        for (int i = 11; i <= 20; i++) sessionHistory.add(new TrainingSession("SESS-1" + i, "STU-101", "PRACTICAL", "2026-03-" + i, "COMPLETED"));

        for (int i = 1; i <= 8; i++) sessionHistory.add(new TrainingSession("SESS-" + (130 + i), "STU-102", "THEORY", "2026-03-0" + i, "COMPLETED"));
        for (int i = 1; i <= 15; i++) sessionHistory.add(new TrainingSession("SESS-" + (140 + i), "STU-102", "PRACTICAL", "2026-03-" + (10 + (i % 10)), "COMPLETED"));

        sessionHistory.add(new TrainingSession("SESS-160", "STU-103", "THEORY", "2026-03-15", "COMPLETED"));
        sessionHistory.add(new TrainingSession("SESS-161", "STU-103", "THEORY", "2026-03-16", "ABSENT"));
        sessionHistory.add(new TrainingSession("SESS-162", "STU-103", "THEORY", "2026-03-17", "COMPLETED"));

        for (int i = 1; i <= 5; i++) sessionHistory.add(new TrainingSession("SESS-" + (170 + i), "STU-104", "THEORY", "2026-03-0" + i, "COMPLETED"));
        for (int i = 1; i <= 10; i++) sessionHistory.add(new TrainingSession("SESS-" + (180 + i), "STU-104", "PRACTICAL", "2026-03-" + (10 + i), "COMPLETED"));

        for (int i = 1; i <= 8; i++) sessionHistory.add(new TrainingSession("SESS-" + (200 + i), "STU-105", "THEORY", "2026-03-0" + i, "COMPLETED"));
        for (int i = 1; i <= 15; i++) sessionHistory.add(new TrainingSession("SESS-" + (210 + i), "STU-105", "PRACTICAL", "2026-03-" + (10 + (i % 10)), "COMPLETED"));

        // Tests
        testList.add(new Test("TEST-501", "STU-101", "2026-04-10", "Two-Wheeler", "SCHEDULED"));
        testList.add(new Test("TEST-502", "STU-104", "2026-04-12", "Two-Wheeler", "SCHEDULED"));
        testList.add(new Test("TEST-503", "STU-105", "2026-04-05", "Four-Wheeler", "PASSED"));
        testList.add(new Test("TEST-504", "STU-108", "2026-03-28", "Two-Wheeler", "FAILED"));

        refreshStudentTable();
        refreshSessionTable();
        refreshTestTable();
        refreshDropdowns();
        updateSidebarStats();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrivingSchoolApp().setVisible(true));
    }
}

// ============================================================================
// DOMAIN MODELS & ENTITIES
// ============================================================================
class Student {
    private String id, name, phone;
    private double feePaid;

    public Student(String id, String name, String phone, double feePaid) {
        this.id = id; this.name = name; this.phone = phone; this.feePaid = feePaid;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getFeePaid() { return feePaid; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFeePaid(double feePaid) { this.feePaid = feePaid; }
}

abstract class Course {
    private String name;
    private double fee;
    private int requiredTheory, requiredPractical;

    public Course(String name, double fee, int requiredTheory, int requiredPractical) {
        this.name = name; this.fee = fee;
        this.requiredTheory = requiredTheory; this.requiredPractical = requiredPractical;
    }

    public String getName() { return name; }
    public double getFee() { return fee; }
    public int getRequiredTheory() { return requiredTheory; }
    public int getRequiredPractical() { return requiredPractical; }

    public abstract String getType();
}

class TwoWheelerCourse extends Course {
    public TwoWheelerCourse() { super("Two-Wheeler Course", 3500.0, 5, 10); }
    @Override public String getType() { return "Two-Wheeler"; }
}

class FourWheelerCourse extends Course {
    public FourWheelerCourse() { super("Four-Wheeler Course", 7500.0, 8, 15); }
    @Override public String getType() { return "Four-Wheeler"; }
}

class TrainingSession {
    private String sessionId, studentId, type, date, status;

    public TrainingSession(String sessionId, String studentId, String type, String date, String status) {
        this.sessionId = sessionId; this.studentId = studentId;
        this.type = type; this.date = date; this.status = status;
    }

    public String getSessionId() { return sessionId; }
    public String getStudentId() { return studentId; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getStatus() { return status; }

    public void setType(String type) { this.type = type; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
}

class Test {
    private String testId, studentId, date, track, result;

    public Test(String testId, String studentId, String date, String track, String result) {
        this.testId = testId; this.studentId = studentId;
        this.date = date; this.track = track; this.result = result;
    }

    public String getTestId() { return testId; }
    public String getStudentId() { return studentId; }
    public String getDate() { return date; }
    public String getTrack() { return track; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

// ============================================================================
// CUSTOM CHECKED EXCEPTIONS
// ============================================================================
class InvalidDataException extends Exception {
    public InvalidDataException(String message) { super(message); }
}

class IneligibleException extends Exception {
    public IneligibleException(String message) { super(message); }
}