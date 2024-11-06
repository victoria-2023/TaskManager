import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

enum TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED
}

enum TaskPriority {
    LOW, MEDIUM, HIGH
}

class Task implements Serializable {
    private static final long serialVersionUID = 2L;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;

    private Task(TaskBuilder builder) {
        this.description = builder.description;
        this.status = builder.status;
        this.priority = builder.priority;
        this.dueDate = builder.dueDate;
    }

    // Getters and setters
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }

    @Override
    public String toString() {
        return String.format("[%s] [%s] [Due: %s] %s", 
            status, priority, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE), description);
    }

    public static class TaskBuilder {
        private String description;
        private TaskStatus status = TaskStatus.PENDING;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private LocalDate dueDate = LocalDate.now().plusDays(7);

        public TaskBuilder(String description) {
            this.description = description;
        }

        public TaskBuilder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}

public class ImprovedTaskManager {
    private static final String FILE_NAME = "improved_tasks.ser";
    private static List<Task> tasks = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(ImprovedTaskManager.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("taskmanager.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error setting up file handler", e);
        }
    }

    public static void main(String[] args) {
        loadTasks();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Improved Task Manager ===");
            System.out.println("1. Add Task");
            System.out.println("2. View Tasks");
            System.out.println("3. Update Task Status");
            System.out.println("4. Remove Task");
            System.out.println("5. Sort Tasks");
            System.out.println("6. Filter Tasks");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> addTask(scanner);
                    case 2 -> viewTasks();
                    case 3 -> updateTaskStatus(scanner);
                    case 4 -> removeTask(scanner);
                    case 5 -> sortTasks(scanner);
                    case 6 -> filterTasks(scanner);
                    case 7 -> {
                        saveTasks();
                        System.out.println("Goodbye!");
                        LOGGER.info("Application closed");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                LOGGER.log(Level.WARNING, "Invalid input entered", e);
            }
        }
    }

    private static void addTask(Scanner scanner) {
        System.out.print("Enter task description: ");
        String description = scanner.nextLine();

        System.out.print("Enter priority (LOW, MEDIUM, HIGH): ");
        TaskPriority priority = TaskPriority.valueOf(scanner.nextLine().toUpperCase());

        System.out.print("Enter due date (YYYY-MM-DD): ");
        LocalDate dueDate = LocalDate.parse(scanner.nextLine());

        Task newTask = new Task.TaskBuilder(description)
                .priority(priority)
                .dueDate(dueDate)
                .build();

        tasks.add(newTask);
        System.out.println("Task added successfully!");
        LOGGER.info("New task added: " + newTask);
    }

    private static void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    private static void updateTaskStatus(Scanner scanner) {
        viewTasks();
        System.out.print("Enter the number of the task to update: ");
        int taskNumber = Integer.parseInt(scanner.nextLine()) - 1;

        if (taskNumber >= 0 && taskNumber < tasks.size()) {
            System.out.print("Enter new status (PENDING, IN_PROGRESS, COMPLETED): ");
            TaskStatus newStatus = TaskStatus.valueOf(scanner.nextLine().toUpperCase());
            Task task = tasks.get(taskNumber);
            task.setStatus(newStatus);
            System.out.println("Task status updated!");
            LOGGER.info("Task status updated: " + task);
        } else {
            System.out.println("Invalid task number.");
        }
    }

    private static void removeTask(Scanner scanner) {
        viewTasks();
        System.out.print("Enter the number of the task to remove: ");
        int taskNumber = Integer.parseInt(scanner.nextLine()) - 1;

        if (taskNumber >= 0 && taskNumber < tasks.size()) {
            Task removedTask = tasks.remove(taskNumber);
            System.out.println("Task removed successfully!");
            LOGGER.info("Task removed: " + removedTask);
        } else {
            System.out.println("Invalid task number.");
        }
    }

    private static void sortTasks(Scanner scanner) {
        System.out.println("Sort by:");
        System.out.println("1. Due Date");
        System.out.println("2. Priority");
        System.out.print("Enter your choice: ");
        int sortChoice = Integer.parseInt(scanner.nextLine());

        switch (sortChoice) {
            case 1 -> tasks.sort(Comparator.comparing(Task::getDueDate));
            case 2 -> tasks.sort(Comparator.comparing(Task::getPriority));
            default -> System.out.println("Invalid choice. No sorting performed.");
        }
        viewTasks();
    }

    private static void filterTasks(Scanner scanner) {
        System.out.println("Filter by:");
        System.out.println("1. Status");
        System.out.println("2. Priority");
        System.out.print("Enter your choice: ");
        int filterChoice = Integer.parseInt(scanner.nextLine());

        switch (filterChoice) {
            case 1 -> {
                System.out.print("Enter status to filter by (PENDING, IN_PROGRESS, COMPLETED): ");
                TaskStatus status = TaskStatus.valueOf(scanner.nextLine().toUpperCase());
                tasks.stream()
                        .filter(task -> task.getStatus() == status)
                        .forEach(System.out::println);
            }
            case 2 -> {
                System.out.print("Enter priority to filter by (LOW, MEDIUM, HIGH): ");
                TaskPriority priority = TaskPriority.valueOf(scanner.nextLine().toUpperCase());
                tasks.stream()
                        .filter(task -> task.getPriority() == priority)
                        .forEach(System.out::println);
            }
            default -> System.out.println("Invalid choice. No filtering performed.");
        }
    }

    private static void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
            System.out.println("Tasks saved successfully!");
            LOGGER.info("Tasks saved to file");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error saving tasks", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadTasks() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
                tasks = (List<Task>) ois.readObject();
                System.out.println("Tasks loaded successfully!");
                LOGGER.info("Tasks loaded from file");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading tasks: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            }
        }
    }
}