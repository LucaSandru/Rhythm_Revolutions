import yt_dlp

# YouTube URL for "Let It Be"
url = "https://www.youtube.com/watch?v=QDYfEBY9NM4"

# Define download options
options = {
    'format': 'bestaudio/best',
    'extractaudio': True,         # Extract audio only
    'audioformat': 'mp3',         # Convert to MP3
    'outtmpl': '%(title)s.%(ext)s',  # Save with song title
}

# Initialize downloader
with yt_dlp.YoutubeDL(options) as ydl:
    ydl.download([url])

print("Download complete!")
