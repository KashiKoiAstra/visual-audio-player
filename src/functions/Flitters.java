package functions;

public class Flitters {
    // pre-emphasis
    public static float[] preEmphasis(float[] samples, float alpha) {
        if (samples == null || samples.length == 0)
            return samples;
        float[] output = new float[samples.length];
        output[0] = samples[0];
        for (int i = 1; i < samples.length; i++) {
            output[i] = samples[i] - alpha * samples[i - 1];
        }
        return output;
    }

    // band-pass
    public static float[] bandPass(float[] samples, float sampleRate, float lowCutoff, float highCutoff) {
        if (samples == null || samples.length == 0)
            return samples;
        if (lowCutoff <= 0 || highCutoff <= 0 || highCutoff <= lowCutoff)
            return samples;

        float hpAlpha = (float) (1.0 - Math.exp(-2.0 * Math.PI * lowCutoff / sampleRate));
        float lpAlpha = (float) (1.0 - Math.exp(-2.0 * Math.PI * highCutoff / sampleRate));

        float[] highPass = iirHighPassFilter(samples, hpAlpha);
        float[] band = iirLowPassFilter(highPass, lpAlpha);
        return band;
    }

    // IIR（y[n] = alpha * x[n] + (1 - alpha) * y[n-1]）
    public static float[] iirLowPassFilter(float[] samples, float alpha) {
        if (samples == null || samples.length == 0)
            return samples;
        float[] output = new float[samples.length];
        output[0] = samples[0];
        for (int i = 1; i < samples.length; i++) {
            output[i] = alpha * samples[i] + (1 - alpha) * output[i - 1];
        }
        return output;
    }

    // IIR（y[n] = alpha * (y[n-1] + x[n] - x[n-1])）
    public static float[] iirHighPassFilter(float[] samples, float alpha) {
        if (samples == null || samples.length == 0)
            return samples;
        float[] output = new float[samples.length];
        output[0] = 0f;
        for (int i = 1; i < samples.length; i++) {
            output[i] = alpha * (output[i - 1] + samples[i] - samples[i - 1]);
        }
        return output;
    }
}
