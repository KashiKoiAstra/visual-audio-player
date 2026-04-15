package events;

import components.Frame;
import data.AudioState;
import data.AudioTrack;
import functions.Bucketer;
import functions.FFT;
import functions.PCM;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.swing.JFileChooser;
import javax.swing.Timer;

public class MainController {
    private final Frame frame;
    private AudioTrack audioTrack;
    int bucketCount = 64;

    public MainController(Frame frame) {
        this.frame = frame;
        initializeEvents();
        initializeTimer();
    }

    private void initializeEvents() {
        frame.openButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    if (audioTrack != null) {
                        audioTrack.stop();
                    }
                    audioTrack = new AudioTrack(fileChooser.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.playButton.addActionListener(e -> {
            if (audioTrack != null) {
                audioTrack.play();
            }
        });
        frame.pauseButton.addActionListener(e -> {
            if (audioTrack != null) {
                audioTrack.pause();
            }
        });
        frame.stopButton.addActionListener(e -> {
            if (audioTrack != null) {
                audioTrack.stop();
            }
        });
        frame.testButton.addActionListener(e -> {
            // nothing to do
        });
    }

    private void processHistogram() {
        if (audioTrack == null || audioTrack.state != AudioState.PLAYING) {
            return;
        }
        byte[] pcm = audioTrack.getWaveform();

        AudioFormat format = audioTrack.getFormat();
        int bytesPerSample = format.getFrameSize() / format.getChannels();
        int channelCount = format.getChannels();
        int sampleCount = pcm.length / (bytesPerSample * channelCount);
        int targetSize = FFT.nextPowerOfTwo(sampleCount);

        List<float[]> channels = PCM.separateChannels(pcm, bytesPerSample, channelCount, targetSize,
                format.isBigEndian());
        float[] leftChannel = channels.get(0);
        float[] rightChannel = (channelCount > 1) ? channels.get(1) : leftChannel;

        float[] leftSpectrum = FFT.magnitudesOptimized(leftChannel, targetSize);
        float[] rightSpectrum = FFT.magnitudesOptimized(rightChannel, targetSize);

        float[] leftBuckets = Bucketer.bucketize(leftSpectrum, bucketCount);
        float[] rightBuckets = Bucketer.bucketize(rightSpectrum, bucketCount);

        int histogramHeight = frame.histogram.getHeight();
        if (histogramHeight <= 0 || leftBuckets.length == 0 || rightBuckets.length == 0) {
            return;
        }

        int[] leftHeights = Bucketer.normalizeBuckets(leftBuckets, histogramHeight);
        int[] rightHeights = Bucketer.normalizeBuckets(rightBuckets, histogramHeight);

        int[] bars = new int[bucketCount * 2];
        for (int i = 0; i < bucketCount; i++) {
            bars[i] = leftHeights[bucketCount - 1 - i];
            bars[i + bucketCount] = rightHeights[i];
        }

        frame.histogram.update(bars);
    }

    private void initializeTimer() {
        Timer timer = new Timer(16, e -> {
            processHistogram();
        });
        timer.start();
    }
}
