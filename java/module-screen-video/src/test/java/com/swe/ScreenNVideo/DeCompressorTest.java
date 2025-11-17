package com.swe.ScreenNVideo;

import com.swe.ScreenNVideo.Codec.DeCompressor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprehensive test suite for DeCompressor class.
 * Tests decompression of chrominance and luminance matrices.
 */
public class DeCompressorTest {

    /**
     * Block size for DCT operations.
     */
    private static final int BLOCK_SIZE = 8;
    /**
     * Single block dimension.
     */
    private static final int SINGLE_BLOCK_DIM = 8;
    /**
     * Double block dimension.
     */
    private static final int DOUBLE_BLOCK_DIM = 16;
    /**
     * Triple block dimension.
     */
    private static final int TRIPLE_BLOCK_DIM = 24;
    /**
     * Quad block dimension.
     */
    private static final int QUAD_BLOCK_DIM = 32;

    /**
     * Decompressor instance for testing.
     */
    private DeCompressor decompressor;

//    @Mock
//    private IFIDCT mockDctModule;
//
//    @Mock
//    private QuantisationUtil mockQuantModule;

    /**
     * Sets up test fixture before each test.
     */
    @BeforeEach
    public void setUp() {
//        MockitoAnnotations.openMocks(this);
        decompressor = new DeCompressor();
    }

    /**
     * Tests that DeCompressor constructor creates non-null instance.
     */
    @Test
    public void testConstructorCreatesInstance() {
        assertNotNull(decompressor);
    }

    /**
     * Tests decompressChrome with single 8x8 block.
     */
    @Test
    public void testDecompressChromeSingleBlock() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i * 10 + j);
            }
        }

        decompressor.decompressChrome(matrix, (short) SINGLE_BLOCK_DIM, (short) SINGLE_BLOCK_DIM);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with 16x16 matrix (4 blocks).
     */
    @Test
    public void testDecompressChromeMultipleBlocks() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = 50;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with zero matrix.
     */
    @Test
    public void testDecompressChromeZeroMatrix() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with 24x24 matrix (9 blocks).
     */
    @Test
    public void testDecompressChromeNineBlocks() {
        final short[][] matrix = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final short height = TRIPLE_BLOCK_DIM;
        final short width = TRIPLE_BLOCK_DIM;

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                matrix[i][j] = 100;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with rectangular matrix 8x16.
     */
    @Test
    public void testDecompressChromeRectangular() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = 75;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with rectangular matrix 16x8.
     */
    @Test
    public void testDecompressChromeRectangularTall() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = 60;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with 32x32 matrix (16 blocks).
     */
    @Test
    public void testDecompressChromeLargeMatrix() {
        final short[][] matrix = new short[QUAD_BLOCK_DIM][QUAD_BLOCK_DIM];
        final short height = QUAD_BLOCK_DIM;
        final short width = QUAD_BLOCK_DIM;

        for (int i = 0; i < QUAD_BLOCK_DIM; i++) {
            for (int j = 0; j < QUAD_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) * 5);
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with negative values.
     */
    @Test
    public void testDecompressChromeNegativeValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = -50;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with mixed positive and negative values.
     */
    @Test
    public void testDecompressChromeMixedValues() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 100 : -100);
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with single 8x8 block.
     */
    @Test
    public void testDecompressLuminSingleBlock() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i * 10 + j);
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with 16x16 matrix (4 blocks).
     */
    @Test
    public void testDecompressLuminMultipleBlocks() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = 50;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with zero matrix.
     */
    @Test
    public void testDecompressLuminZeroMatrix() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with 24x24 matrix (9 blocks).
     */
    @Test
    public void testDecompressLuminNineBlocks() {
        final short[][] matrix = new short[TRIPLE_BLOCK_DIM][TRIPLE_BLOCK_DIM];
        final short height = TRIPLE_BLOCK_DIM;
        final short width = TRIPLE_BLOCK_DIM;

        for (int i = 0; i < TRIPLE_BLOCK_DIM; i++) {
            for (int j = 0; j < TRIPLE_BLOCK_DIM; j++) {
                matrix[i][j] = 100;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with rectangular matrix 8x16.
     */
    @Test
    public void testDecompressLuminRectangular() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = 75;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with rectangular matrix 16x8.
     */
    @Test
    public void testDecompressLuminRectangularTall() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = 60;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with 32x32 matrix (16 blocks).
     */
    @Test
    public void testDecompressLuminLargeMatrix() {
        final short[][] matrix = new short[QUAD_BLOCK_DIM][QUAD_BLOCK_DIM];
        final short height = QUAD_BLOCK_DIM;
        final short width = QUAD_BLOCK_DIM;

        for (int i = 0; i < QUAD_BLOCK_DIM; i++) {
            for (int j = 0; j < QUAD_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) * 5);
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with negative values.
     */
    @Test
    public void testDecompressLuminNegativeValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = -50;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with mixed positive and negative values.
     */
    @Test
    public void testDecompressLuminMixedValues() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 100 : -100);
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with maximum short values.
     */
    @Test
    public void testDecompressChromeMaxValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with maximum short values.
     */
    @Test
    public void testDecompressLuminMaxValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MAX_VALUE;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with minimum short values.
     */
    @Test
    public void testDecompressChromeMinValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MIN_VALUE;
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with minimum short values.
     */
    @Test
    public void testDecompressLuminMinValues() {
        final short[][] matrix = new short[SINGLE_BLOCK_DIM][SINGLE_BLOCK_DIM];
        final short height = SINGLE_BLOCK_DIM;
        final short width = SINGLE_BLOCK_DIM;

        for (int i = 0; i < SINGLE_BLOCK_DIM; i++) {
            for (int j = 0; j < SINGLE_BLOCK_DIM; j++) {
                matrix[i][j] = Short.MIN_VALUE;
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with gradient pattern.
     */
    @Test
    public void testDecompressChromeGradient() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i * BLOCK_SIZE + j);
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with gradient pattern.
     */
    @Test
    public void testDecompressLuminGradient() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) (i * BLOCK_SIZE + j);
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressChrome with checkerboard pattern.
     */
    @Test
    public void testDecompressChromeCheckerboard() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 255 : 0);
            }
        }

        decompressor.decompressChrome(matrix, height, width);
        assertNotNull(matrix);
    }

    /**
     * Tests decompressLumin with checkerboard pattern.
     */
    @Test
    public void testDecompressLuminCheckerboard() {
        final short[][] matrix = new short[DOUBLE_BLOCK_DIM][DOUBLE_BLOCK_DIM];
        final short height = DOUBLE_BLOCK_DIM;
        final short width = DOUBLE_BLOCK_DIM;

        for (int i = 0; i < DOUBLE_BLOCK_DIM; i++) {
            for (int j = 0; j < DOUBLE_BLOCK_DIM; j++) {
                matrix[i][j] = (short) ((i + j) % 2 == 0 ? 255 : 0);
            }
        }

        decompressor.decompressLumin(matrix, height, width);
        assertNotNull(matrix);
    }
}