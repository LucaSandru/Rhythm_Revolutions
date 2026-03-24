# Rhythm Revolutions

A full-featured *Java desktop application* that allows users to manage music, create playlists, and interact with a MySQL database through a modern JavaFX interface.

---

## Overview

 **Rhythm Revolutions** is a *Java-based application* developed for learning and demonstrating:

- Object-Oriented Programming (OOP)
- JavaFX GUI development
- MySQL database integration (JDBC Database)
- File handling and media playback (.txt files)

The application supports both **admin** and **user** functionalities, which can be used as CLI when launching the application.

---

## Features

### User Features
- Register & login system
- Search songs by name or artist
- Create custom playlists
- Add/remove songs from playlists
- Play MP3 songs directly in the app

### Admin Features
- Login system (by using a standard password)
- Add new artists and songs
- Update artist information
- Delete users
- Delete songs
- View all users and artists

### GUI Features
- Contain **User** and **Admin** functionalities, in a modern UI.

---

## Project Structure

```
FinalProject/
│
├── src/
  ├── Main.java                                            
│ ├── GUIApplication.java
│ ├── DatabaseConnection.java
│ ├── User.java
│ ├── Artist.java
│ ├── Playlist.java
│ └── ...
│
├── lib/
│ ├── mysql-connector-j.jar           # Prerequisite for application
│ 
│
├── downloads/                        # MP3 files
├── out/                              # compiled classes
└── README.md
```

---

## Technologies Used

- **Java 21**
- **JavaFX (GUI)**
- **MySQL**
- **JDBC (Database connection)**
- **IntelliJ IDEA**


## Setup Instructions

### 1. Install Requirements

- Java JDK 21
- MySQL Server
- JavaFX SDK
- IntelliJ IDEA (recommended)

---

### 2. Configure Database

Create database:

```sql
CREATE DATABASE music_app;
USE music_app;
```

Create tables (example):

```sql
CREATE TABLE artists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    date DATETIME
);

CREATE TABLE songs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    artist_id INT,
    file_path TEXT,
    date DATETIME
);
```

Update Database Credentials (in ```DatabaseConnection.java```)

```sql
private static final String DB_URL = "jdbc:mysql://localhost:3306/music_app";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
```

Place MP3 files in ```downloads```

Then insert them into MySQL (like this):
```sql
INSERT INTO songs (name, artist_id, file_path, date)
VALUES ('SongName', 1, 'D:\\path\\to\\song.mp3', NOW());
```

Compile application (from terminal):

```sql
javac --module-path "PATH_TO_JAVAFX_LIB" \
--add-modules javafx.controls,javafx.fxml,javafx.media \
-cp "out;lib/mysql-connector-j.jar" Main gui
```

Run application (from terminal or from IDE):

```sql
java --module-path "PATH_TO_JAVAFX_LIB" \
--add-modules javafx.controls,javafx.fxml,javafx.media \
-cp "out;lib/mysql-connector-j.jar" Main gui
```

## How to Use
- Start the application
- Choose:
  - Admin mode
  - User mode
  - GUI mode
- Login or register
- Explore music and create playlists
- Explore music and create playlists

## Media Playback
- Uses JavaFX MediaPlayer
- Supports .mp3 files
- Requires valid file paths in database (songs stored locally)

## Future Improvements
- Automatic MP3 scanning
- REST API backend
- Improved UI/UX

## Author
**Luca-David Șandru**
