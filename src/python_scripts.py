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

DOWNLOAD_FOLDER = r"D:\download_songs"  # Ensure this folder exists and is writable.

def connect_to_database():
    """Connect to the MySQL database."""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("✅ Database connected successfully!")
        return conn
    except Exception as e:
        print(f"❌ Error connecting to the database: {e}")
        return None

def get_all_songs_from_db():
    """Fetch all songs with YouTube links from the database."""
    conn = connect_to_database()
    if conn is None:
        print("❌ Failed to connect to the database!")
        return []
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT id, name, youtube_link FROM songs WHERE file_path IS NULL")
        songs = cursor.fetchall()
        print(f"✅ Found {len(songs)} songs to download.")
        return songs
    except Exception as e:
        print(f"❌ Error retrieving songs from the database: {e}")
        return []
    finally:
        conn.close()

def update_file_path_in_db(song_id, file_path):
    """Update the file path for a song after download."""
    conn = connect_to_database()
    if conn is None:
        print("❌ Database connection failed!")
        return
    try:
        cursor = conn.cursor()
        sql = "UPDATE songs SET file_path = %s WHERE id = %s"
        cursor.execute(sql, (file_path, song_id))
        conn.commit()
        print(f"✅ Database updated with file path for song ID: {song_id}")
    except Exception as e:
        print(f"❌ Error updating database: {e}")
    finally:
        conn.close()

def download_song(youtube_url, song_name):
    """Download a song from YouTube using yt-dlp."""
    try:
        file_path = os.path.join(DOWNLOAD_FOLDER, f"{song_name}.mp3")

        # Check if the song already exists
        if os.path.exists(file_path):
            print(f"⚠️ {song_name} already exists. Skipping download.")
            return file_path

        options = {
            'format': 'bestaudio/best',
            'outtmpl': file_path,
            'postprocessors': [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192'
            }],
            'ffmpeg_location': r"D:\downloads\ffmpeg-7.1-essentials_build\ffmpeg-7.1-essentials_build\bin"
        }

        with yt_dlp.YoutubeDL(options) as ydl:
            ydl.download([youtube_url])
            print(f"✅ {song_name} downloaded successfully!")
        return file_path

    except Exception as e:
        print(f"❌ Error downloading {song_name}: {e}")
        return None

def main():
    songs = get_all_songs_from_db()
    if not songs:
        print("✅ No songs to download. Database is up to date.")
        return

    for song_id, name, youtube_link in songs:
        print(f"\n🎵 Processing Song: {name}")
        file_path = download_song(youtube_link, name)

        if file_path and os.path.exists(file_path):
            print(f"✅ {name} downloaded successfully and saved at: {file_path}")
            update_file_path_in_db(song_id, file_path)
        else:
            print(f"❌ Failed to download {name}. Check your internet connection or link.")

    print("\n🎉 All songs have been processed!")

if __name__ == "__main__":
    main()
