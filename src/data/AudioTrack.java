package data;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioTrack {
    public volatile AudioState state = AudioState.STOPPED;
    private SourceDataLine line;
    private Thread playThread;
    private AudioFormat format;
    private String filePath;
    private long totalBytes;
    public volatile long currentByte = 0;
    private volatile long seekBytes = -1;

    private int sampleSize = 512;
    private byte[] dataBuffer = new byte[sampleSize];

    public AudioTrack(String filePath) {
        this.filePath = filePath;
        loadAudio(filePath);
    }

    private void loadAudio(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                throw new IllegalArgumentException("Audio file not found: " + filePath);
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
            format = ais.getFormat();
            long frameLength = ais.getFrameLength();
            if (frameLength != AudioSystem.NOT_SPECIFIED) {
                totalBytes = frameLength * format.getFrameSize();
            }
            ais.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void play() {
        if (state == AudioState.PLAYING)
            return;

        if (state == AudioState.PAUSED) {
            state = AudioState.PLAYING;
            notifyAll();
            return;
        }

        state = AudioState.PLAYING;
        playThread = new Thread(this::playbackLoop);
        playThread.start();
    }

    public synchronized void pause() {
        if (state == AudioState.PLAYING) {
            state = AudioState.PAUSED;
        }
    }

    public synchronized void stop() {
        state = AudioState.STOPPED;
        seekBytes = -1;
        currentByte = 0;
        notifyAll();
    }

    public void seek(double percentage) {
        if (totalBytes > 0) {
            long pos = (long) (totalBytes * percentage);
            int frameSize = format.getFrameSize();
            if (frameSize > 0) {
                pos = (pos / frameSize) * frameSize;
            }
            seekBytes = pos;
        }
    }

    public byte[] getWaveform() {
        return Arrays.copyOf(dataBuffer, dataBuffer.length);
    }

    public AudioFormat getFormat() {
        return format;
    }

    public synchronized long getCurrentByte() {
        return currentByte;
    }

    public synchronized long getTotalBytes() {
        return totalBytes;
    }

    private void playbackLoop() {
        AudioInputStream ais = null;
        try {
            File file = new File(filePath);
            ais = AudioSystem.getAudioInputStream(file);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            byte[] buffer = new byte[sampleSize];
            int bytesRead;

            while (state != AudioState.STOPPED) {
                synchronized (this) {
                    while (state == AudioState.PAUSED) {
                        line.stop();
                        wait();
                        if (state == AudioState.STOPPED)
                            break;
                        line.start();
                    }
                }
                if (state == AudioState.STOPPED)
                    break;

                if (seekBytes != -1) {
                    line.flush();
                    ais.close();
                    ais = AudioSystem.getAudioInputStream(file);
                    long toSkip = seekBytes;
                    long skipped = 0;
                    while (skipped < toSkip) {
                        long s = ais.skip(toSkip - skipped);
                        if (s <= 0)
                            break;
                        skipped += s;
                    }
                    currentByte = skipped;
                    seekBytes = -1;
                }

                bytesRead = ais.read(buffer, 0, buffer.length);
                if (bytesRead == -1)
                    break;

                if (bytesRead < buffer.length) {
                    System.arraycopy(buffer, 0, dataBuffer, 0, bytesRead);
                    for (int i = bytesRead; i < dataBuffer.length; i++)
                        dataBuffer[i] = 0;
                } else {
                    System.arraycopy(buffer, 0, dataBuffer, 0, buffer.length);
                }

                line.write(buffer, 0, bytesRead);
                currentByte += bytesRead;
            }

            line.drain();
            line.stop();
            line.close();
            ais.close();

            if (state == AudioState.PLAYING) {
                state = AudioState.STOPPED;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}