package contrib.level.generator.perlinNoise;

import java.util.Random;

/**
 * Class generating a perlin noise array.
 *
 * <p>Source: <a href="https://flafla2.github.io/2014/08/09/perlinnoise.html">Blog by Flafla2 (09
 * August 2014)</a>
 */
public class PerlinNoise {
    private final int repetitionWidth;
    private final int repetitionHeight;
    private final int[] octaves;
    private final double[][][] permutation;

    /**
     * PerlinNoise with a random seed.
     *
     * @param repetitionWidth Width of noise.
     * @param repetitionHeight Height of noise.
     * @param octaves Octaves for noise.
     * @param ownPermutationForOctaves Flag if each octave should have own permutation.
     */
    public PerlinNoise(
            final int repetitionWidth,
            final int repetitionHeight,
            final int[] octaves,
            final boolean ownPermutationForOctaves) {
        this(repetitionWidth, repetitionHeight, octaves, ownPermutationForOctaves, new Random());
    }

    /**
     * Creates new PerlinNoise
     *
     * @param repetitionWidth Width of noise.
     * @param repetitionHeight Height of noise.
     * @param octaves Octaves for noise.
     * @param ownPermutationForOctaves Flag if each octave should have own permutation.
     * @param random Random object used for generation.
     */
    public PerlinNoise(
            final int repetitionWidth,
            final int repetitionHeight,
            final int[] octaves,
            final boolean ownPermutationForOctaves,
            final Random random) {
        this.repetitionWidth = repetitionWidth;
        this.repetitionHeight = repetitionHeight;
        this.octaves = octaves;

        // generate permutation
        permutation = new double[octaves.length][repetitionWidth][repetitionHeight];
        for (int x = 0; x < repetitionWidth; x++) {
            for (int y = 0; y < repetitionHeight; y++) {
                double randomNumber = random.nextDouble();
                for (int i = 0; i < octaves.length; i++) {
                    permutation[i][x][y] = randomNumber;
                    if (ownPermutationForOctaves) randomNumber = random.nextDouble();
                }
            }
        }
    }

    /**
     * Get the noise for one point.
     *
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return The noise value.
     */
    public double noise(final int x, final int y) {
        double fNoise = 0;
        double fScaleAcc = 0;
        double fScale = 1;

        for (int o = 0; o < octaves.length; o++) {
            final int nPitchX = repetitionWidth >> octaves[o];
            final int nPitchY = repetitionHeight >> octaves[o];
            final int nSampleX1 = x / nPitchX * nPitchX;
            final int nSampleY1 = y / nPitchY * nPitchY;

            final int nSampleX2 = (nSampleX1 + nPitchX);
            final int nSampleY2 = (nSampleY1 + nPitchY);

            final double fBlendX = (double) (x - nSampleX1) / (double) nPitchX;
            final double fBlendY = (double) (y - nSampleY1) / (double) nPitchY;

            final double fSampleT =
                    interpolate(
                            permutation[o][nSampleX1 % repetitionWidth][
                                    nSampleY1 % repetitionHeight],
                            permutation[o][nSampleX2 % repetitionWidth][
                                    nSampleY1 % repetitionHeight],
                            fBlendX);
            final double fSampleB =
                    interpolate(
                            permutation[o][nSampleX1 % repetitionWidth][
                                    nSampleY2 % repetitionHeight],
                            permutation[o][nSampleX2 % repetitionWidth][
                                    nSampleY2 % repetitionHeight],
                            fBlendX);

            fScaleAcc += fScale;
            fNoise += interpolate(fSampleT, fSampleB, fBlendY) * fScale;
            fScale = fScale / 2;
        }
        // Scale to seed range
        return fNoise / fScaleAcc;
    }

    /**
     * Interpolates the noise for a smooth transition.
     *
     * @param sample1 First sample.
     * @param sample2 Second sample-
     * @param blend Ratio of distance to the sample.
     * @return Interpolated value of noise.
     */
    private double interpolate(final double sample1, final double sample2, final double blend) {
        return (blend * (sample2 - sample1) + sample1);
    }

    /**
     * Get the noise for all points.
     *
     * @param zoom Zoom determines to skip / repeat some values.
     * @return All noise values.
     */
    public double[][] noiseAll(final double zoom) {
        final double[][] noise =
                new double[(int) (repetitionWidth / zoom)][(int) (repetitionHeight / zoom)];
        double fScaleAcc = 0;
        double fScale = 1;

        // every octave
        for (int o = 0; o < octaves.length; o++) {
            final double[][] octaveNoise = calculateOctaveNoise(o);
            // apply noise to overall noise
            for (int y = 0; y < noise.length; y++) {
                for (int x = 0; x < noise[0].length; x++) {
                    noise[y][x] += octaveNoise[(int) (y * zoom)][(int) (x * zoom)] * fScale;
                }
            }
            // scale of next octave
            fScaleAcc += fScale;
            fScale /= 2;
        }
        // scale back to required interval [0, 1)
        for (int y = 0; y < noise.length; y++) {
            for (int x = 0; x < noise[0].length; x++) {
                noise[y][x] /= fScaleAcc;
            }
        }
        return noise;
    }

    private double[][] calculateOctaveNoise(final int octave) {
        final int nPitchX = repetitionWidth >> octaves[octave];
        final int nPitchY = repetitionHeight >> octaves[octave];
        final double[][] octaveNoise = calculateAreaEdge(nPitchX, nPitchY, octave);

        // middle of each square
        for (int nSampleY1 = 0; nSampleY1 < repetitionHeight; nSampleY1 += nPitchY) {
            final int nSampleY2 = nSampleY1 + nPitchY;
            for (int nSampleX1 = 0; nSampleX1 < repetitionWidth; nSampleX1 += nPitchX) {
                final int nSampleX2 = (nSampleX1 + nPitchX);
                // every pixel y noise
                for (int pixelNumberX = nSampleX1;
                        pixelNumberX < nSampleX2 && pixelNumberX < repetitionWidth;
                        pixelNumberX++) {
                    for (int pixelNumberY = nSampleY1;
                            pixelNumberY < nSampleY2 && pixelNumberY < repetitionHeight;
                            pixelNumberY++) {
                        final double pixelTop =
                                octaveNoise[pixelNumberX % repetitionWidth][
                                        nSampleY1 % repetitionHeight];
                        final double pixelBottom =
                                octaveNoise[pixelNumberX % repetitionWidth][
                                        nSampleY2 % repetitionHeight];
                        final double blendY = ((double) (pixelNumberY - nSampleY1) / nPitchY);
                        octaveNoise[pixelNumberX][pixelNumberY] =
                                interpolate(pixelTop, pixelBottom, blendY);
                    }
                }
            }
        }
        return octaveNoise;
    }

    private double[][] calculateAreaEdge(final int nPitchX, final int nPitchY, final int octave) {
        final double[][] octaveNoise = new double[repetitionWidth][repetitionHeight];
        // edges of each square
        for (int heightY = 0; heightY < repetitionHeight; heightY += nPitchY) {
            for (int nSampleX1 = 0; nSampleX1 < repetitionWidth; nSampleX1 += nPitchX) {
                final int nSampleX2 = (nSampleX1 + nPitchX);
                // X-direction
                for (int pixelNumberX = nSampleX1;
                        pixelNumberX < nSampleX2 && pixelNumberX < repetitionWidth;
                        pixelNumberX++) {
                    final double pixelLeft = permutation[octave][nSampleX1][heightY];
                    final double pixelRight =
                            permutation[octave][nSampleX2 % repetitionWidth][heightY];
                    final double blendX = ((double) (pixelNumberX - nSampleX1) / nPitchX);
                    octaveNoise[pixelNumberX][heightY] = interpolate(pixelLeft, pixelRight, blendX);
                }
            }
        }
        return octaveNoise;
    }
}
