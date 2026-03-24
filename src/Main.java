//javac --module-path "D:\JafaFX\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp ".;C:\Users\Latex\mysql-connector-j-9.1.0.jar" Main.java GUIApplication.java

import java.io.*;
import java.util.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main {
    private static final String USERS_FILE = "users.txt";
    private static final String ARTISTS_FILE = "artists.txt";
    private static final String ADMIN_PASSWORD = "sandruluca2004";
    private static User loggedInUser = null;

    public static void main(String[] args) {
        System.out.println("WELCOME TO RHYTHM REVOLUTIONS!!!");

        List<User> users = new ArrayList<>();
        List<Artist> artists = new ArrayList<>();

        System.out.println("\n=== Sequential Loading ===");
        long startTime = System.currentTimeMillis();
        boolean loadData = shouldLoadData(args);
        if (loadData) {
            loadDataSequential(users, artists);
        } else {
            System.out.println("Starting with a fresh application state.");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential Load Time: " + (endTime - startTime) + " ms");

        users.clear();
        artists.clear();

        System.out.println("\n=== Parallel Loading ===");
        startTime = System.currentTimeMillis();
        if (loadData) {
            loadData(users, artists);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Parallel Load Time: " + (endTime - startTime) + " ms");

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "admin":
                    handleAdminFunctionality(users, artists);
                    break;
                case "user":
                    displayInitialHelp();
                    handleUserInitialActions(users, artists);
                    break;
                case "gui":
                    System.out.println("Starting GUI mode...");
                    GUIApplication.launch(GUIApplication.class, args); // Launch the GUI
                    return;
                default:
                    displayProgramUsage();
            }
        } else {
            displayProgramUsage();
        }

        saveData(users, artists);
    }



    private static void displayProgramUsage() {
        System.out.println("Usage:");
        System.out.println("--------------------------------------------------");

        System.out.println("\njavac --module-path \"<PATH_TO_JAVAFX_LIB>\" --add-modules javafx.controls,javafx.fxml,javafx.media -cp \"lib\\mysql-connector-j-9.6.0.jar;src\\json-20240303.jar\" -d out src\\*.java");
        System.out.println("  Compile the application.");

        System.out.println("\njava --module-path \"<PATH_TO_JAVAFX_LIB>\" --add-modules javafx.controls,javafx.fxml,javafx.media -cp \"out;lib\\mysql-connector-j-9.x.x.jar;src\\json-20240303.jar\" Main admin");
        System.out.println("  Start the program in admin mode.");

        System.out.println("\njava --module-path \"<PATH_TO_JAVAFX_LIB>\" --add-modules javafx.controls,javafx.fxml,javafx.media -cp \"out;lib\\mysql-connector-j-9.x.x.jar;src\\json-20240303.jar\" Main user");
        System.out.println("  Start the program in user mode.");

        System.out.println("\njava --module-path \"<PATH_TO_JAVAFX_LIB>\" --add-modules javafx.controls,javafx.fxml,javafx.media -cp \"out;lib\\mysql-connector-j-9.x.x.jar;src\\json-20240303.jar\" Main gui");
        System.out.println("  Start the program in graphical user interface mode.");

        System.out.println("--------------------------------------------------");
    }

    private static void handleAdminFunctionality(List<User> users, List<Artist> artists) {
        Scanner scanner = new Scanner(System.in);
        String password;

        if (System.console() != null) {
            char[] passwordArray = System.console().readPassword("Enter admin password: ");
            if (passwordArray == null) {
                System.out.println("Error: Input was not provided. Please restart and provide input.");
                return;
            }
            password = new String(passwordArray);
        } else {
            System.out.print("Enter admin password: ");
            try {
                password = scanner.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("Error: Input was not provided. Please restart and provide input.");
                return;
            }
        }

        if (password.equals(ADMIN_PASSWORD)) {
            System.out.println("Admin login successful.");

            displayAdminHelp();

            while (true) {
                System.out.print("Enter command (type 'admin_help' for options): ");
                String input = scanner.nextLine().trim();
                String[] inputArgs = input.split(" ");

                switch (inputArgs[0]) {
                    case "admin_help":
                        displayAdminHelp();
                        break;
                    case "delete_user":
                        if (inputArgs.length >= 2) {
                            String userName = String.join(" ", Arrays.copyOfRange(inputArgs, 1, inputArgs.length));
                            try {
                                deleteUser(users, userName);
                                saveData(users, artists);
                            } catch (NotFoundException e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            System.out.println("Error: Please specify a username.");
                        }
                        break;
                    case "add_artist":
                        addArtist(artists);
                        saveData(users, artists);
                        break;
                    case "delete_artist":
                        if (inputArgs.length >= 2) {
                            String artistName = String.join(" ", Arrays.copyOfRange(inputArgs, 1, inputArgs.length));
                            try {
                                deleteArtist(artists, artistName);
                                saveData(users, artists);
                            } catch (NotFoundException e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            System.out.println("Error: Please specify an artist name.");
                        }
                        break;
                    case "update_artist":
                        if (inputArgs.length >= 2) {
                            updateArtist(artists, inputArgs[1]);
                            saveData(users, artists);
                        } else {
                            System.out.println("Error: Please specify the artist name to update.");
                        }
                        break;
                    case "see_users":
                        displayAllUsers(users);
                        break;
                    case "see_artists":
                        displayArtists(artists);
                        break;
                    case "quit":
                        System.out.println("Exiting admin functionality.");
                        return;
                    default:
                        System.out.println("Unknown command. Type 'admin_help' to see available admin commands.");
                }
            }
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }



    private static void handleUserInitialActions(List<User> users, List<Artist> artists) {
        Scanner scanner = new Scanner(System.in);

        while (loggedInUser == null) {
            System.out.print("Enter command (type 'user_help' for options): ");
            String input = scanner.nextLine().trim();
            String[] inputArgs = input.split(" ");

            switch (inputArgs[0]) {
                case "user_help":
                    displayInitialHelp();
                    break;
                case "create_account":
                    if (inputArgs.length >= 4) {
                        String username = inputArgs[1];
                        String password = inputArgs[2];
                        String email = inputArgs[3];

                        try {
                            createUser(users, username, password, email);
                            saveData(users, artists);
                        } catch (AlreadyExistsException e) {
                            System.out.println(e.getMessage());
                            System.out.println("Please try a different username.");
                        }
                    } else {
                        System.out.println("Error: Please specify username, password, and email.");
                    }
                    break;
                case "login":
                    if (inputArgs.length >= 3) {
                        String email = inputArgs[1];
                        String password = inputArgs[2];
                        loggedInUser = login(users, email, password);
                        if (loggedInUser != null) {
                            System.out.println("Login successful for user: " + loggedInUser.getName());
                            handleUserCommands(users, artists);
                        } else {
                            System.out.println("Error: Invalid email or password.");
                        }
                    } else {
                        System.out.println("Error: Please specify email and password.");
                    }
                    break;
                case "quit":
                    System.out.println("Exiting user functionality.");
                    return;
                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }
    }


    private static void createUser(List<User> users, String username, String password, String email) throws AlreadyExistsException {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(username)) {
                throw new AlreadyExistsException("A user with the name '" + username + "' already exists. Please try a different username.");
            }
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new AlreadyExistsException("A user with the email '" + email + "' already exists. Please try a different email.");
            }
        }
        if (!EmailVerification.isEmailFormatValid(email)) {
            System.out.println("Error: The email address '" + email + "' is not in a valid format. Please enter a correct email.");
            return;
        }
        User newUser = new User(username, password, email);
        users.add(newUser);
        System.out.println("Account created for user: " + username);
    }




    private static void handleUserCommands(List<User> users, List<Artist> artists) {
        Scanner scanner = new Scanner(System.in);
        displayUserHelp();
        while (true) {
            System.out.print("User command (type 'help' for options): ");
            String input = scanner.nextLine().trim();
            String[] inputArgs = input.split(" ");

            switch (inputArgs[0]) {
                case "help":
                    displayUserHelp();
                    break;
                case "user_info":
                    loggedInUser.displayUserInfo();
                    break;
                case "update_account":
                    updateUserAccount(loggedInUser);
                    saveData(users, artists);
                    break;
                case "see_artists":
                    displayArtists(artists);
                    break;
                case "quit":
                    System.out.println("Logging out. Goodbye, " + loggedInUser.getName());
                    loggedInUser = null;
                    return;
                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }
    }

    private static void deleteArtist(List<Artist> artists, String artistName) throws NotFoundException {
        boolean artistFound = false;
        String normalizedArtistName = artistName.trim().toLowerCase();

        Iterator<Artist> iterator = artists.iterator();
        while (iterator.hasNext()) {
            Artist artist = iterator.next();
            if (artist.getName().trim().toLowerCase().equals(normalizedArtistName)) {
                iterator.remove();
                artistFound = true;
                System.out.println("Artist '" + artist.getName() + "' deleted successfully.");
                break;
            }
        }

        if (!artistFound) {
            throw new NotFoundException("Error: Artist '" + artistName + "' not found.");
        }
    }




    private static void deleteUser(List<User> users, String username) throws NotFoundException {
        boolean userFound = false;
        String normalizedUsername = username.trim().toLowerCase();

        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getName().trim().toLowerCase().equals(normalizedUsername)) {
                iterator.remove();
                userFound = true;
                System.out.println("User '" + user.getName() + "' deleted successfully.");
                break;
            }
        }

        if (!userFound) {
            throw new NotFoundException("Error: User '" + username + "' not found.");
        }
    }



    private static void addArtist(List<Artist> artists) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the artist's name: ");
        String name = scanner.nextLine().trim();

        List<Song> songs = new ArrayList<>();
        System.out.print("Enter songs (separate titles with commas): ");
        String[] songTitles = scanner.nextLine().trim().split(",");
        for (String title : songTitles) {
            songs.add(new Song(title.trim()));
        }

        Artist newArtist = new Artist(name, songs);
        artists.add(newArtist);
        System.out.println("Artist '" + name + "' added successfully.");
    }

    private static void updateArtist(List<Artist> artists, String artistName) {
        Artist artist = findArtist(artists, artistName);
        if (artist == null) {
            System.out.println("Error: Artist not found.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter new name for artist (or leave blank to keep '" + artist.getName() + "'): ");
        String newName = scanner.nextLine().trim();
        if (!newName.isEmpty()) {
            artist.setName(newName);
        }

        System.out.print("Do you want to add or remove songs? (type 'add' or 'remove' or 'none'): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (choice.equals("add")) {
            System.out.print("Enter song title to add: ");
            String newSongTitle = scanner.nextLine().trim();
            artist.addSong(new Song(newSongTitle));
        } else if (choice.equals("remove")) {
            System.out.print("Enter song title to remove: ");
            String songTitle = scanner.nextLine().trim();
            artist.removeSong(new Song(songTitle));
        }

        System.out.println("Artist updated successfully.");
    }

    private static void updateUserAccount(User user) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter new username (leave blank to keep current): ");
        String newName = scanner.nextLine().trim();
        if (!newName.isEmpty()) {
            user.edit(newName);
        }

        System.out.print("Enter new email (leave blank to keep current): ");
        String newEmail = scanner.nextLine().trim();
        if (!newEmail.isEmpty()) {
            user.updateEmail(newEmail);
        }

        System.out.print("Enter new password (leave blank to keep current): ");
        String newPassword = scanner.nextLine().trim();
        if (!newPassword.isEmpty()) {
            user.changePassword(newPassword);
        }

        System.out.println("Account updated successfully.");
    }

    private static void displayAllUsers(List<User> users) {
        System.out.println("All users in the system:");
        int userCount = 1;
        for (User user : users) {
            System.out.println("User " + userCount + ":");
            System.out.println("  Name: " + user.getName());
            System.out.println("  Email: " + user.getEmail());
            System.out.println("  Followed Artists: " + user.getFollowedArtists());
            System.out.println("  Account Created: " + user.getFormattedAccountCreated());
            System.out.println("  Genres interacted by user " + user.getName() + ": " + user.getInteractedGenres());
            System.out.println(); // Add an extra line for spacing between users
            userCount++;
        }
    }


    private static void displayArtists(List<Artist> artists) {
        System.out.println("Artists in the system:");
        int artistCount = 1;
        for (Artist artist : artists) {
            System.out.println("Artist " + artistCount + ":");
            System.out.println("  Name: " + artist.getName());
            System.out.print("  Songs: ");
            for (Song song : artist.getSongs()) {
                System.out.print(song.getTitle() + " | ");
            }
            System.out.println("\n");
            artistCount++;
        }
    }


    private static void displayInitialHelp() {
        System.out.println("Commands:");
        System.out.println("--------------------------------------------------");
        System.out.println("help");
        System.out.println("  Display this help menu.");
        System.out.println("\ncreate_account <username> <password> <email>");
        System.out.println("  Create a new account with username, password, and email.");
        System.out.println("\nlogin <email> <password>");
        System.out.println("  Login to the account using email and password.");
        System.out.println("\nquit");
        System.out.println("  Exit user functionality.");
        System.out.println("--------------------------------------------------");
    }

    private static void displayUserHelp() {
        System.out.println("User Commands:");
        System.out.println("--------------------------------------------------");
        System.out.println("user_info");
        System.out.println("  Display the logged-in user's details.");
        System.out.println("\nupdate_account");
        System.out.println("  Update the logged-in user's account details.");
        System.out.println("\nsee_artists");
        System.out.println("  Display all artists and their details.");
        System.out.println("\nquit");
        System.out.println("  Logout and exit user functionality.");
        System.out.println("--------------------------------------------------");
    }

    private static void displayAdminHelp() {
        System.out.println("Admin Commands:");
        System.out.println("--------------------------------------------------");
        System.out.println("delete_user <username>");
        System.out.println("  Delete a specified user.");
        System.out.println("\nadd_artist");
        System.out.println("  Add a new artist with songs.");
        System.out.println("\ndelete_artist <artist_name>");
        System.out.println("  Delete a specified artist.");
        System.out.println("\nupdate_artist <artist_name>");
        System.out.println("  Update the specified artist's details.");
        System.out.println("\nsee_users");
        System.out.println("  Display all users' details.");
        System.out.println("\nsee_artists");
        System.out.println("  Display all artists and their details.");
        System.out.println("\nquit");
        System.out.println("  Exit admin functionality.");
        System.out.println("--------------------------------------------------");
    }

    private static void loadData(List<User> users, List<Artist> artists) {
        Thread loadUsersThread = new Thread(() -> {
            try (BufferedReader userReader = new BufferedReader(new FileReader(USERS_FILE))) {
                String line;
                while ((line = userReader.readLine()) != null) {
                    User user = User.fromFileString(line);
                    if (user != null) {
                        synchronized (users) {
                            users.add(user);
                        }
                    }
                }
                System.out.println("Users loaded successfully (Parallel).");
            } catch (FileNotFoundException e) {
                System.out.println("User data file not found, starting with empty user list.");
            } catch (IOException e) {
                System.out.println("Error loading user data: " + e.getMessage());
            }
        });

        Thread loadArtistsThread = new Thread(() -> {
            try (BufferedReader artistReader = new BufferedReader(new FileReader(ARTISTS_FILE))) {
                String line;
                while ((line = artistReader.readLine()) != null) {
                    Artist artist = Artist.fromFileString(line);
                    if (artist != null) {
                        synchronized (artists) {
                            artists.add(artist);
                        }
                    }
                }
                System.out.println("Artists loaded successfully (Parallel).");
            } catch (FileNotFoundException e) {
                System.out.println("Artist data file not found, starting with empty artist list.");
            } catch (IOException e) {
                System.out.println("Error loading artist data: " + e.getMessage());
            }
        });

        // Start threads
        loadUsersThread.start();
        loadArtistsThread.start();

        try {
            loadUsersThread.join();
            loadArtistsThread.join();
        } catch (InterruptedException e) {
            System.out.println("Data loading interrupted.");
        }
    }


    private static boolean shouldLoadData(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Do you want to load existing data (yes/no)? ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("yes")) {
                return true;
            } else if (response.equals("no")) {
                deleteDataFiles();
                return false;
            } else {
                System.out.println("Invalid input. Please type 'yes' or 'no'.");
            }
        }
    }


    private static void deleteDataFiles() {
        File usersFile = new File(USERS_FILE);
        File artistsFile = new File(ARTISTS_FILE);

        if (usersFile.exists() && usersFile.delete()) {
            System.out.println("Existing users data deleted.");
        }

        if (artistsFile.exists() && artistsFile.delete()) {
            System.out.println("Existing artists data deleted.");
        }
    }

    private static void saveData(List<User> users, List<Artist> artists) {
        Thread saveArtistsThread = new Thread(() -> {
            try (PrintWriter artistWriter = new PrintWriter(new FileWriter(ARTISTS_FILE))) {
                for (Artist artist : artists) {
                    artistWriter.println(artist.toFileString());
                }
                System.out.println("Artists saved successfully.");
            } catch (IOException e) {
                System.out.println("Error saving artist data: " + e.getMessage());
            }
        });

        saveArtistsThread.start();

        try {
            saveArtistsThread.join();
        } catch (InterruptedException e) {
            System.out.println("Data saving interrupted.");
        }
    }


    private static void loadDataSequential(List<User> users, List<Artist> artists) {
        try (BufferedReader userReader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = userReader.readLine()) != null) {
                User user = User.fromFileString(line);
                if (user != null) {
                    users.add(user);
                }
            }
            System.out.println("Users loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("User data file not found, starting with empty user list.");
        } catch (IOException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }

        try (BufferedReader artistReader = new BufferedReader(new FileReader(ARTISTS_FILE))) {
            String line;
            while ((line = artistReader.readLine()) != null) {
                try {
                    Artist artist = Artist.fromFileString(line);
                    artists.add(artist);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping malformed artist line: " + line);
                }
            }
            System.out.println("Artists loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("Artist data file not found, starting with empty artist list.");
        } catch (IOException e) {
            System.out.println("Error loading artist data: " + e.getMessage());
        }
    }



        private static User login(List<User> users, String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private static User findUser(List<User> users, String username) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    private static Artist findArtist(List<Artist> artists, String artistName) {
        for (Artist artist : artists) {
            if (artist.getName().equalsIgnoreCase(artistName)) {
                return artist;
            }
        }
        return null;
    }
}
