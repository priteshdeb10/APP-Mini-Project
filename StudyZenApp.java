import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class StudyZenApp extends JFrame {

    // --- 1. React State Equivalent ---
    private List<Task> tasks = new ArrayList<>();
    private int userPoints = 0;
    private int streak = 0;
    private final String DATA_FILE = "studyzen_data.ser";

    // --- 2. UI Components (Swing) ---
    private JTabbedPane mainTabs;
    private DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(taskListModel);
    private JLabel pointsLabel = new JLabel("0");
    private JLabel streakLabel = new JLabel("0 days");

    // --- 3. Constructor and Initialization ---
    public StudyZenApp() {
        super("StudyZen - Smart Study Planner (Swing Edition)");
        
        loadState();
        initUI();
        
        // Save state on window close (localStorage useEffect equivalent)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveState();
                dispose();
                System.exit(0);
            }
        });

        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabs
        mainTabs = new JTabbedPane();
        mainTabs.addTab("My Tasks", createTasksPanel());
        mainTabs.addTab("Focus Timer", createPlaceholderPanel("Focus Timer (Pomodoro)"));
        mainTabs.addTab("Progress", createPlaceholderPanel("Progress Charts"));
        mainTabs.addTab("Analytics", createPlaceholderPanel("Subject Analytics"));
        add(mainTabs, BorderLayout.CENTER);

        refreshTaskList();
    }
    
    // --- 4. UI Component Creation Methods ---

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        header.setBackground(Color.decode("#F0F0F0"));
        
        // App Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(new JLabel("<html><div style='padding-right: 50px;'><b>StudyZen</b><br><small>Smart Study Planner</small></div></html>"));
        titlePanel.setOpaque(false);
        header.add(titlePanel, BorderLayout.WEST);

        // Points and Streak
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);
        
        // Points Card
        JPanel points = new JPanel(new GridLayout(2, 1));
        points.add(new JLabel("Points", SwingConstants.RIGHT));
        pointsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        points.add(pointsLabel);
        statsPanel.add(points);

        // Streak Card
        JPanel streakStat = new JPanel(new GridLayout(2, 1));
        streakStat.add(new JLabel("Streak", SwingConstants.RIGHT));
        streakLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        streakStat.add(streakLabel);
        statsPanel.add(streakStat);
        
        header.add(statsPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createTasksPanel() {
        JPanel tasksPanel = new JPanel(new BorderLayout(10, 10));
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header and Add Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("<html><h2>My Study Tasks</h2><p><small>Organize and track your assignments, exams, and study sessions</small></p></html>"), BorderLayout.WEST);
        
        JButton addButton = new JButton("➕ Add Task");
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD, 14f));
        addButton.addActionListener(e -> showAddTaskDialog());
        topPanel.add(addButton, BorderLayout.EAST);
        
        tasksPanel.add(topPanel, BorderLayout.NORTH);
        
        // Task List (TaskList equivalent)
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskListCellRenderer());
        tasksPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Actions (Toggle/Delete)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton completeButton = new JButton("Toggle Complete");
        completeButton.addActionListener(e -> toggleTaskComplete());
        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(e -> deleteTask());

        buttonPanel.add(completeButton);
        buttonPanel.add(deleteButton);
        tasksPanel.add(buttonPanel, BorderLayout.SOUTH);

        return tasksPanel;
    }
    
    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("<html><center><h1>" + title + "</h1><p style='color: gray;'><small>Future functionality here. Focus on the Tasks tab for core features.</small></p></center></html>", SwingConstants.CENTER);
        panel.add(label);
        return panel;
    }
    
    // --- 5. Task List Renderer (Mimics Custom TaskList UI) ---
    private class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Task task = (Task) value;
            
            String colorCode = "#000000"; 
            if (task.getPriority().equalsIgnoreCase("high")) {
                colorCode = "#D9534F"; // Red
            } else if (task.getPriority().equalsIgnoreCase("medium")) {
                colorCode = "#F0AD4E"; // Orange
            } else if (task.getPriority().equalsIgnoreCase("low")) {
                colorCode = "#5CB85C"; // Green
            }

            String check = task.isCompleted() ? "✅" : "☐";
            
            label.setText(String.format("<html>%s <span style='color: %s;'>%s</span>: <b>%s</b> (Sub: %s, Type: %s)</html>", 
                                        check, colorCode, task.getPriority().toUpperCase(), task.getTitle(), task.getSubject(), task.getType()));
            
            if (task.isCompleted()) {
                 label.setForeground(Color.GRAY);
                 label.setFont(label.getFont().deriveFont(Font.ITALIC));
            } else {
                 label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
                 label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
            
            return label;
        }
    }
    
    // --- 6. State Management Logic (Mirrors React Handlers) ---
    
    private void showAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog(this);
        dialog.reset();
        dialog.setVisible(true);
        Task newTask = dialog.getNewTask();
        
        if (newTask != null) {
            tasks.add(newTask);
            refreshTaskList();
            // useToast equivalent
            JOptionPane.showMessageDialog(this, newTask.getTitle() + " has been added to your planner.", "Task Added! ✅", JOptionPane.INFORMATION_MESSAGE);
            saveState();
        }
    }

    private void toggleTaskComplete() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) return;
        
        boolean wasCompleted = selectedTask.isCompleted();
        selectedTask.setCompleted(!wasCompleted);

        if (!wasCompleted) {
            // Task completed, give points and update streak
            int points = 0;
            if (selectedTask.getPriority().equalsIgnoreCase("high")) {
                points = 30;
            } else if (selectedTask.getPriority().equalsIgnoreCase("medium")) {
                points = 20;
            } else {
                points = 10;
            }
            userPoints += points;
            streak++;
            
            updateStatsLabels();
            // useToast equivalent
            JOptionPane.showMessageDialog(this, "Task Completed! 🎉\n+" + points + " points earned! Current streak: " + streak, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Task marked incomplete (no point deduction for simplicity)
            // useToast equivalent
            JOptionPane.showMessageDialog(this, "Task marked incomplete.", "Update", JOptionPane.WARNING_MESSAGE);
        }
        
        refreshTaskList();
        saveState();
    }
    
    private void deleteTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) return;

        int confirmation = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete '" + selectedTask.getTitle() + "'?", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            tasks.remove(selectedTask);
            refreshTaskList();
            // useToast equivalent
            JOptionPane.showMessageDialog(this, "Task has been deleted from your planner.", "Task Removed 🗑️", JOptionPane.INFORMATION_MESSAGE);
            saveState();
        }
    }

    private void refreshTaskList() {
        taskListModel.clear();
        // Sort tasks (e.g., incomplete first, then by priority/deadline in a real app)
        for (Task task : tasks) {
            taskListModel.addElement(task);
        }
        updateStatsLabels();
    }
    
    private void updateStatsLabels() {
        pointsLabel.setText(String.valueOf(userPoints));
        streakLabel.setText(streak + " days");
    }

    // --- 7. Persistence (localStorage Equivalent) ---
    private void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(tasks);
            oos.writeObject(userPoints);
            oos.writeObject(streak);
        } catch (IOException e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }

    private void loadState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            @SuppressWarnings("unchecked")
            List<Task> loadedTasks = (List<Task>) ois.readObject();
            tasks = loadedTasks;
            userPoints = (Integer) ois.readObject();
            streak = (Integer) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No saved state found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading state. Starting fresh. " + e.getMessage());
        }
    }

    // --- 8. Data Model (React Interface/Type Equivalent) ---
    private class Task implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private String title;
        private String subject;
        private String type;
        private Date deadline; 
        private String priority;
        private boolean completed;
        private int timeSpent;
        private String difficulty;

        public Task(String title, String subject, String type, Date deadline, String priority, String difficulty) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.title = title;
            this.subject = subject;
            this.type = type;
            this.deadline = deadline;
            this.priority = priority;
            this.completed = false;
            this.timeSpent = 0;
            this.difficulty = difficulty;
        }

        // Getters and Setters (essential for logic and rendering)
        public String getTitle() { return title; }
        public String getSubject() { return subject; }
        public String getType() { return type; }
        public String getPriority() { return priority; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    // --- 9. Add Task Dialog (React Dialog Component Equivalent) ---
    private class AddTaskDialog extends JDialog {
        private Task newTask = null;
        
        private JTextField titleField = new JTextField(20);
        private JTextField subjectField = new JTextField(20);
        private JComboBox<String> typeBox = new JComboBox<>(new String[]{"study", "assignment", "exam"});
        private JComboBox<String> priorityBox = new JComboBox<>(new String[]{"medium", "high", "low"});
        private JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"medium", "easy", "hard"});
        private JTextField deadlineField = new JTextField("YYYY-MM-DD", 10); 

        public AddTaskDialog(Frame owner) {
            super(owner, "Add New Task", true);
            setLayout(new BorderLayout(10, 10));
            
            JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            formPanel.add(new JLabel("Title:"));
            formPanel.add(titleField);
            formPanel.add(new JLabel("Subject:"));
            formPanel.add(subjectField);
            formPanel.add(new JLabel("Type:"));
            formPanel.add(typeBox);
            formPanel.add(new JLabel("Priority:"));
            formPanel.add(priorityBox);
            formPanel.add(new JLabel("Difficulty:"));
            formPanel.add(difficultyBox);
            formPanel.add(new JLabel("Deadline (Placeholder):"));
            formPanel.add(deadlineField);

            JButton addButton = new JButton("Add Task");
            addButton.addActionListener(e -> createTask());
            
            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPanel.add(addButton);

            add(formPanel, BorderLayout.CENTER);
            add(southPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(owner);
        }

        private void createTask() {
            if (titleField.getText().trim().isEmpty() || subjectField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Subject are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Note: Date parsing is complex in Java. Using current date as a placeholder.
            Date placeholderDeadline = new Date(); 

            newTask = new Task(
                titleField.getText(),
                subjectField.getText(),
                (String) typeBox.getSelectedItem(),
                placeholderDeadline,
                (String) priorityBox.getSelectedItem(),
                (String) difficultyBox.getSelectedItem()
            );
            setVisible(false);
        }

        public Task getNewTask() {
            return newTask;
        }

        public void reset() {
            newTask = null;
            titleField.setText("");
            subjectField.setText("");
            typeBox.setSelectedIndex(0);
            priorityBox.setSelectedIndex(1); // Default to 'high' for better testing
            difficultyBox.setSelectedIndex(0);
            deadlineField.setText("YYYY-MM-DD");
        }
    }

    // --- 10. Main Method ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Default look and feel used if system L&F fails
        }
        new StudyZenApp();
    }
}