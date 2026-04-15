package functions;

public class Bucketer {
    public static float[] bucketize(float[] magnitudes, int bucketCount) {
        if (magnitudes == null || magnitudes.length == 0 || bucketCount <= 0)
            return new float[0];

        float[] buckets = new float[bucketCount];
        int magnitudesPerBucket = magnitudes.length / bucketCount;
        if (magnitudesPerBucket == 0) {
            System.arraycopy(magnitudes, 0, buckets, 0, magnitudes.length);
            return buckets;
        }

        for (int i = 0; i < bucketCount; i++) {
            float sum = 0f;
            int startIdx = i * magnitudesPerBucket;
            int endIdx = (i == bucketCount - 1) ? magnitudes.length : startIdx + magnitudesPerBucket;
            for (int j = startIdx; j < endIdx; j++) {
                sum += magnitudes[j];
            }
            buckets[i] = sum / (endIdx - startIdx);
        }
        return buckets;
    }

    public static int[] normalizeBuckets(float[] buckets, int maxHeight) {
        int[] normalized = new int[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            float amplitude = (float) Math.pow(10f, buckets[i] / 20f);
            int value = Math.round(amplitude * maxHeight);
            if (value < 0) {
                value = 0;
            } else if (value > maxHeight) {
                value = maxHeight;
            }
            normalized[i] = value;
        }
        return normalized;
    }

    public static int[] normalizeBuckets(float[] buckets, int maxHeight, float minDB, float maxDB) {
        int[] normalized = new int[buckets.length];
        float range = maxDB - minDB;
        for (int i = 0; i < buckets.length; i++) {
            float db = buckets[i];
            float ratio = (db - minDB) / range;
            ratio = Math.max(0, Math.min(1, ratio));
            int value = Math.round(ratio * maxHeight);
            normalized[i] = value;
        }
        return normalized;
    }

}
