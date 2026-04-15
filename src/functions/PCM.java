package functions;

import java.util.ArrayList;
import java.util.List;

public class PCM {
    /**
     * Convert interleaved PCM byte data to separate channel float arrays.
     * Supports 8-bit (unsigned), 16-bit, 24-bit and 32-bit signed PCM.
     * 
     * @param interleavedData raw bytes of interleaved PCM data
     * @param bytesPerSample  bytes per sample (1, 2, 3, or 4)
     * @param channels        number of channels in the audio
     * @param targetSize      desired output length per channel (0 = use actual
     *                        samples)
     * @param bigEndian       whether samples are stored in big-endian byte order
     * @return List of float arrays, one per channel, with samples in approx range
     *         [-1, 1]
     */
    public static List<float[]> separateChannels(byte[] interleavedData, int bytesPerSample,
            int channels, int targetSize, boolean bigEndian) {

        int actualSamples = 0;
        if (bytesPerSample > 0 && channels > 0) {
            actualSamples = interleavedData.length / (bytesPerSample * channels);
        }

        int samplesPerChannel = (targetSize > 0) ? targetSize : actualSamples;

        List<float[]> channelData = new ArrayList<>(channels);
        for (int ch = 0; ch < channels; ch++) {
            channelData.add(new float[samplesPerChannel]);
        }

        int limit = Math.min(actualSamples, samplesPerChannel);

        for (int i = 0; i < limit; i++) {
            for (int ch = 0; ch < channels; ch++) {
                int sampleIndex = (i * channels + ch) * bytesPerSample;
                float sample = decodeSample(interleavedData, sampleIndex, bytesPerSample, bigEndian);
                channelData.get(ch)[i] = sample;
            }
        }

        // Remaining samples (if targetSize > actualSamples) remain as 0.0f

        return channelData;
    }

    /**
     * Decode a single PCM sample from byte array to normalized float.
     * 
     * @param data           byte array containing PCM data
     * @param startIndex     starting index of the sample in the byte array
     * @param bytesPerSample number of bytes per sample (1, 2, 3, or 4)
     * @param bigEndian      whether sample is stored in big-endian byte order
     * @return normalized float sample in range [-1.0, 1.0]
     */
    private static float decodeSample(byte[] data, int startIndex, int bytesPerSample, boolean bigEndian) {
        switch (bytesPerSample) {
            case 1: // 8-bit unsigned
                int unsignedByte = data[startIndex] & 0xFF;
                return (unsignedByte - 128) / 128.0f;

            case 2: // 16-bit signed
                int b0 = data[startIndex] & 0xFF;
                int b1 = data[startIndex + 1] & 0xFF;
                int val16 = bigEndian ? ((b0 << 8) | b1) : ((b1 << 8) | b0);

                if ((val16 & 0x8000) != 0)
                    val16 |= 0xFFFF0000;
                return val16 / 32768.0f;

            case 3: // 24-bit signed
                int c0 = data[startIndex] & 0xFF;
                int c1 = data[startIndex + 1] & 0xFF;
                int c2 = data[startIndex + 2] & 0xFF;
                int val24 = bigEndian ? ((c0 << 16) | (c1 << 8) | c2) : ((c2 << 16) | (c1 << 8) | c0);
                // Sign extension for 24-bit
                if ((val24 & 0x800000) != 0)
                    val24 |= 0xFF000000;
                return val24 / 8388608.0f; // 2^23

            case 4: // 32-bit signed
                int d0 = data[startIndex] & 0xFF;
                int d1 = data[startIndex + 1] & 0xFF;
                int d2 = data[startIndex + 2] & 0xFF;
                int d3 = data[startIndex + 3] & 0xFF;
                int val32 = bigEndian ? ((d0 << 24) | (d1 << 16) | (d2 << 8) | d3)
                        : ((d3 << 24) | (d2 << 16) | (d1 << 8) | d0);
                return val32 / 2147483648.0f; // 2^31

            default:
                throw new IllegalArgumentException("Unsupported bytes per sample: " + bytesPerSample);
        }
    }
}
