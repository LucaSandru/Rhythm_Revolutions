import yt_dlp
import mysql.connector
import os

# Database connection details
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'sandruluca2004',
    'database': 'music_app'
}

DOWNLOAD_FOLDER = r"D:\songs"

def connect_to_database():
    """Connect to the MySQL database."""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("✅ Database connected successfully!")
        return conn
    except mysql.connector.Error as e:
        print(f"❌ Database connection error: {e}")
        exit()

def get_all_songs_to_download():
    """Fetch all songs with YouTube links from the database where file_path is missing."""
    conn = connect_to_database()
    cursor = conn.cursor()
    cursor.execute("SELECT id, name, youtube_link FROM songs WHERE youtube_link IS NOT NULL AND file_path IS NULL")
    songs = cursor.fetchall()
    conn.close()
    return songs

def get_all_songs_without_file_path():
    """Fetch all songs without a file path from the database."""
    conn = connect_to_database()
    cursor = conn.cursor()
    cursor.execute("SELECT id, name FROM songs WHERE file_path IS NULL OR file_path = ''")
    songs = cursor.fetchall()
    conn.close()
    return songs

def update_file_path_in_db(song_id, file_path):
    """Update the file path for a song in the database."""
    conn = connect_to_database()
    cursor = conn.cursor()
    try:
        sql = "UPDATE songs SET file_path = %s WHERE id = %s"
        cursor.execute(sql, (file_path, song_id))
        conn.commit()
        print(f"✅ File path updated in the database for song ID: {song_id}")
    except mysql.connector.Error as e:
        print(f"❌ Error updating file path: {e}")
    finally:
        conn.close()

def download_song(youtube_url, song_name):
    """Download a song from YouTube using yt-dlp."""
    options = {
        'format': 'bestaudio/best',
        'outtmpl': os.path.join(DOWNLOAD_FOLDER, f"{song_name}.mp3"),
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'mp3',
            'preferredquality': '192'
        }],
        'ffmpeg_location': r"D:\downloads\ffmpeg-7.1-essentials_build\bin"
    }

    try:
        with yt_dlp.YoutubeDL(options) as ydl:
            ydl.download([youtube_url])
            print(f"🎵 {song_name} downloaded successfully!")
            return True
    except yt_dlp.DownloadError as e:
        print(f"❌ Error downloading {song_name}: {e}")
        return False

def check_and_update_file_paths():
    """Check if the file exists and update the database with the file path."""
    songs = get_all_songs_without_file_path()
    if not songs:
        print("⚠️ No songs without file paths found.")
        return

    for song_id, song_name in songs:
        file_path = os.path.join(DOWNLOAD_FOLDER, f"{song_name}.mp3")

        if os.path.exists(file_path):
            update_file_path_in_db(song_id, file_path)
        else:
            print(f"❌ File not found for: {song_name}")

def download_songs_with_links():
    """Download songs that have YouTube links but no file path."""
    songs = get_all_songs_to_download()
    if not songs:
        print("⚠️ No songs to download.")
        return

    for song_id, name, youtube_link in songs:
        print(f"🎧 Downloading: {name} | URL: {youtube_link}")
        if download_song(youtube_link, name):
            file_path = os.path.join(DOWNLOAD_FOLDER, f"{name}.mp3")
            update_file_path_in_db(song_id, file_path)
        else:
            print(f"❌ Failed to download: {name}")

def main():
    """Main function that performs both tasks."""
    print("✅ Checking existing songs for file paths...")
    check_and_update_file_paths()
    print("\n🎧 Starting song download process...")
    download_songs_with_links()

if __name__ == "__main__":
    main()
