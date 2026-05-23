package com.courseplatform.api.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VectorMathUtils {

    private VectorMathUtils() {
    }

    public static double cosineSimilarity(float[] left, float[] right) {
        if (left == null || right == null || left.length == 0 || right.length == 0) {
            return 0.0d;
        }

        int length = Math.min(left.length, right.length);
        double dotProduct = 0.0d;
        double leftMagnitude = 0.0d;
        double rightMagnitude = 0.0d;

        for (int index = 0; index < length; index++) {
            float leftValue = left[index];
            float rightValue = right[index];
            dotProduct += (double) leftValue * rightValue;
            leftMagnitude += (double) leftValue * leftValue;
            rightMagnitude += (double) rightValue * rightValue;
        }

        if (leftMagnitude == 0.0d || rightMagnitude == 0.0d) {
            return 0.0d;
        }

        return dotProduct / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }

    public static byte[] toByteArray(float[] vector) {
        if (vector == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + (Float.BYTES * vector.length)).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(vector.length);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    public static byte[] floatArrayToByteArray(float[] vector) {
        return toByteArray(vector);
    }

    public static float[] fromByteArray(byte[] bytes) {
        if (bytes == null || bytes.length < Integer.BYTES) {
            return new float[0];
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        int length = buffer.getInt();
        if (length <= 0 || buffer.remaining() < (long) length * Float.BYTES) {
            return new float[0];
        }

        float[] vector = new float[length];
        for (int index = 0; index < length; index++) {
            vector[index] = buffer.getFloat();
        }
        return vector;
    }

    public static void normalizeInPlace(float[] vector) {
        if (vector == null || vector.length == 0) {
            return;
        }

        double sumSquares = 0.0d;
        for (float value : vector) {
            sumSquares += (double) value * value;
        }

        if (sumSquares == 0.0d) {
            return;
        }

        double scale = 1.0d / Math.sqrt(sumSquares);
        for (int index = 0; index < vector.length; index++) {
            vector[index] = (float) (vector[index] * scale);
        }
    }
}