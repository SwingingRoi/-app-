from mutagen.mp3 import MP3
import sys

def getLength(filename):
    audio = MP3(filename)
    print(audio.info.length)

if __name__ == '__main__':
    getLength(sys.argv[1])