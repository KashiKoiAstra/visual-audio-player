package functions;

// Fast Fourier Transform (FFT) implementation
// Code by LLM
public class FFT {
    public static void fft(float[] x, float[] y) {
        int n = x.length;
        int j = 0;
        float[] cosTable = new float[n / 2];
        float[] sinTable = new float[n / 2];
        for (int i = 0; i < n / 2; i++) {
            cosTable[i] = (float) Math.cos(-2 * Math.PI * i / n);
            sinTable[i] = (float) Math.sin(-2 * Math.PI * i / n);
        }

        for (int i = 1; i < n - 1; i++) {
            int bit = n >> 1;
            while (j >= bit) {
                j -= bit;
                bit >>= 1;
            }
            j += bit;
            if (i < j) {
                float temp = x[i];
                x[i] = x[j];
                x[j] = temp;
                temp = y[i];
                y[i] = y[j];
                y[j] = temp;
            }
        }

        for (int L = 2; L <= n; L <<= 1) {
            int LHalf = L / 2;
            int LStep = n / L;
            for (int i = 0; i < n; i += L) {
                for (int k = 0; k < LHalf; k++) {
                    int evenIndex = i + k;
                    int oddIndex = i + k + LHalf;
                    int tableIndex = k * LStep;
                    float realPart = x[oddIndex] * cosTable[tableIndex] - y[oddIndex] * sinTable[tableIndex];
                    float imagPart = x[oddIndex] * sinTable[tableIndex] + y[oddIndex] * cosTable[tableIndex];
                    x[oddIndex] = x[evenIndex] - realPart;
                    y[oddIndex] = y[evenIndex] - imagPart;
                    x[evenIndex] += realPart;
                    y[evenIndex] += imagPart;
                }
            }
        }
    }

    /**
     * Calculate single-sided spectrum in dBFS with DC removal and Hann window.
     * Returns float array for better performance and memory usage.
     */

    private static float[] hanningWindow(int length) {
        float[] window = new float[length];
        if (length > 1) {
            for (int i = 0; i < length; i++) {
                window[i] = 0.5f * (1 - (float) Math.cos(2 * Math.PI * i / (length - 1)));
            }
        } else if (length == 1) {
            window[0] = 1.0f;
        }
        return window;
    }

    public static int nextPowerOfTwo(int n) {
        if (n <= 0) {
            return 1;
        }
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return power;
    }

    public static float[] magnitudesOptimized(float[] samples, int fftSize) {
        int numSamples = Math.min(samples.length, fftSize);
        if (numSamples <= 0) {
            return new float[Math.max(1, fftSize / 2)];
        }

        float[] x = new float[fftSize];

        float sum = 0.0f;
        for (int i = 0; i < numSamples; i++) {
            sum += samples[i];
        }
        float mean = sum / numSamples;

        float[] window = hanningWindow(numSamples);

        for (int i = 0; i < numSamples; i++) {
            x[i] = window[i] * (samples[i] - mean);
        }

        float[] y = new float[fftSize];

        fft(x, y);

        int half = fftSize / 2;
        float[] result = new float[half];
        float scale = 2.0f / (fftSize * 0.5f);

        for (int i = 0; i < half; i++) {
            float mag = (float) Math.sqrt(x[i] * x[i] + y[i] * y[i]);
            float amp = mag * scale;
            if (i == 0) {
                amp *= 0.5f;
            }

            float dB = 20.0f * (float) Math.log10(Math.max(amp, 1e-10f));
            result[i] = Math.max(-120.0f, Math.min(0.0f, dB));
        }

        return result;
    }
}