package com.swe.ScreenNVideo.Codec;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Provides functionality for encoding and decoding images in the JPEG format.
 *
 * <p>
 * This class implements the {@link Codec} interface, offering methods to convert 
 * raw image data into compressed JPEG byte streams and to reconstruct images 
 * from JPEG-encoded data.
 * </p>
 *
 * @see Codec
 *
 */
public class JpegCodec implements Codec {
    /**
     * Image that needs to be encoded.
     */
    private int[][] screenshot;

    /** Offset for alpha component in the AARRGGBB value.*/
    private static final int A_OFFSET = 24;

    /** Offset for red color component in the AARRGGBB value.*/
    private static final int R_OFFSET = 16;

    /** Offset for green color component in the AARRGGBB value.*/
    private static final int G_OFFSET = 8;

    /** Mask for extracting color component. */
    private static final int MASK = 0xFF;

    /** Coefficient for red contribution in luminance(Y) calculation.*/
    private static final double Y_R_COEFF  = 0.299;

    /** Coefficient for green contribution in luminance(Y) calculation.*/
    private static final double Y_G_COEFF  = 0.587;

    /** Coefficient for blue contribution in luminance(Y) calculation.*/
    private static final double Y_B_COEFF  = 0.114;

    /** Coefficient for red contribution in chroma blue-difference (Cb) calculation. */
    private static final double CB_R_COEFF = -0.168736;

    /** Coefficient for green contribution in chroma blue-difference (Cb) calculation.*/
    private static final double CB_G_COEFF = -0.331264;

    /** Coefficient for blue contribution in chroma blue-difference (Cb) calculation. */
    private static final double CB_B_COEFF = 0.5;

    /** Coefficient for red contribution in chroma red-difference (Cr) calculation. */
    private static final double CR_R_COEFF = 0.5;

    /** Coefficient for green contribution in chroma red-difference (Cr) calculation. */
    private static final double CR_G_COEFF = -0.418688;

    /** Coefficient for blue contribution in chroma red-difference (Cr) calculation.*/
    private static final double CR_B_COEFF = -0.081312;

    /** Offset applied to Cb and Cr channels to center them around 128.*/
    private static final int CHROMA_OFFSET = 128;

    /** Coefficient for Cr contribution to red channel in YCbCr->RGB conversion.*/
    private static final double CR_TO_R = 1.402;

    /** Coefficient for Cb contribution to green channel in YCbCr->RGB conversion.*/
    private static final double CB_TO_G = 0.344136;

    /** Coefficient for Cr contribution to green channel in YCbCr->RGB conversion.*/
    private static final double CR_TO_G = 0.714136;

    /** Coefficient for Cb contribution to blue channel in YCbCr->RGB conversion.*/
    private static final double CB_TO_B = 1.772;

    /** Maximum value for a color channel (8-bit).*/
    private static final int COLOR_MAX = 255;

    /** Number of pixels in a 2*2 block for 4:2:0 chroma subsampling.*/
    private static final int SUBSAMPLE_BLOCK_SIZE = 4;

    /**
     * Creates a JpegCodec instance with screenshot.
     *
     * @param image screenshot that needs to be encoded
     */
    public JpegCodec(final int[][] image) {
        this.screenshot = image;
        int maxLen = (int) ((128*128 * 1.5 * 4 ) + (4 * 3) + 0.5);
        resRLEBuffer = ByteBuffer.allocate(maxLen);
    }

    public long ZigZagtime = 0;
    public long dctTime = 0;
    public long quantTime = 0;

    private Compressor compressor = new Compressor();
    private IDeCompressor decompressor = new DeCompressor();
    private IRLE enDeRLE = encodeDecodeRLE.getInstance();
    private QuantisationUtil quantUtil = QuantisationUtil.getInstance();

    final ByteBuffer resRLEBuffer;
    /**
     * Creates a JpegCode instance.
     *
     */
    public JpegCodec() {
        int maxLen = (int) ((128*128 * 1.5 * 4 ) + (4 * 3) + 0.5);
        resRLEBuffer = ByteBuffer.allocate(maxLen);
    }

    /**
     * Sets the screenshot image for this object.
     *
     * @param image a 2D integer array representing the image,
     *              where each entry encodes a pixel color value.
     */
    public void setScreenshot(final int[][] image) {
        this.screenshot = image;
    }

    @Override
    public void setCompressionFactor(final short qfactor) {
//        quantUtil.setCompressonResulation(qfactor);
    }

    /**
     * <p>
     * The encoding process includes:
     * <ul>
     *    <li>Converting the RGB pixel data of the screenshot into YCbCr color space.</li>
     *    <li>Applying 4:2:0 chroma sampling to reduce color resolution.</li>
     * </ul>
     * </p>
     */
    @Override
    public byte[] encode(final int topLeftX, final int topLeftY, final int height, final int width) {

        // System.out.println("topLeftX topLeftY W H = " + topLeftX + " " + topLeftY + ":" + width + " " + height);

        if (screenshot == null) {
            throw new RuntimeException("Cannot encode: screenshot is null");
        }

        if (height <= 0 || width <= 0) {
            throw new RuntimeException("Invalid Matrix for encoding: height and width must be positive");
        }

        if (height % 2 == 1 || width % 2 == 1) {
            throw new RuntimeException("Invalid Matrix for encoding: height and width must be even");
        }

        if (topLeftY + height > screenshot.length || topLeftX + width > screenshot[0].length) {
            throw new RuntimeException("Invalid Matrix for encoding: coordinates exceed screenshot bounds");
        }

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        final int CbHeight = (int)Math.ceil(halfHeight / 8.0) * 8;
        final int CbWidth = (int)Math.ceil(halfWidth / 8.0) * 8;
        final short[][] yMatrix =  new short[height][width];
        final short[][] cbMatrix = new short[CbHeight][CbWidth];
        final short[][] crMatrix = new short[CbHeight][CbWidth];

        final int bottomLeftX = topLeftX + width;
        final int bottomLeftY = topLeftY + height;
        for (int i = topLeftY; i < bottomLeftY; i += 2) {
            for (int j = topLeftX; j < bottomLeftX; j += 2) {
                double cbPixel = 0;
                double crPixel = 0;
                for (int ii = i; ii < i + 2; ++ii) {
                    for (int jj = j; jj < j + 2; ++jj) {
                        final int pixel = screenshot[ii][jj];

                        final int r = (pixel >> R_OFFSET) & MASK;
                        final int g = (pixel >> G_OFFSET) & MASK;
                        final int b = pixel & MASK;

                        // conversion to YCbCr
                        int y  = (int) (Y_R_COEFF * r + Y_G_COEFF * g + Y_B_COEFF * b);
                        final double cb = CHROMA_OFFSET + CB_R_COEFF * r + CB_G_COEFF * g + CB_B_COEFF * b;
                        final double cr = CHROMA_OFFSET + CR_R_COEFF * r + CR_G_COEFF * g + CR_B_COEFF * b;

                        cbPixel += cb;
                        crPixel += cr;

                        // Clamp to 0-255
                        y = Math.min(COLOR_MAX, Math.max(0, y));
                        yMatrix[ii - topLeftY][jj - topLeftX] = (short) (y - 128);
                    }
                }

                final int posY = i - topLeftY;
                final int posX = j - topLeftX;
                cbMatrix[posY / 2][posX / 2] = (short) (Math.min(COLOR_MAX, Math.max(0, (int) (cbPixel / SUBSAMPLE_BLOCK_SIZE))) - 128);
                crMatrix[posY / 2][posX / 2] = (short) (Math.min(COLOR_MAX, Math.max(0, (int) (crPixel / SUBSAMPLE_BLOCK_SIZE))) - 128);

            }
        }

        resRLEBuffer.clear();
//        compressor.zigZagTime = 0;
//        compressor.quantTime = 0;
//        compressor.dctTime = 0;

        // YMatrix;
        compressor.compressLumin(yMatrix, (short) height, (short) width, resRLEBuffer);
//        System.out.println("Compression Y : " + resRLEBuffer.position());

        // CbMatrix;
        compressor.compressChrome(cbMatrix,(short)CbHeight,(short)CbWidth,resRLEBuffer);
//        System.out.println("Compression Cb : " + resRLEBuffer.position());

        // CyMatrix
        compressor.compressChrome(crMatrix,(short)CbHeight,(short)CbWidth,resRLEBuffer);
//        System.out.println("Compression Cr : " + resRLEBuffer.position());


//        ZigZagtime += compressor.zigZagTime;
//        dctTime += compressor.dctTime;
//        quantTime += compressor.quantTime;
        byte[] res = new byte[resRLEBuffer.position()];
        resRLEBuffer.rewind();
        resRLEBuffer.get(res);
        return res;
    }

    /**
     * Decodes a encoded string back into the screenshot matrix.
     *
     * <p>
     * The decoding process includes:
     * <ul>
     *    <li>Reconstructing the coefficient matrix from the zigzag scanned string.</li>
     *    <li>Reversing 4:2:0 chroma subsampling to approximate the original chroma values.</li>
     *    <li>Converting the data from YCbCr color space back to RGB pixel values.</li>
     * </ul>
     * </p>
     */
    @Override
    public int[][] decode(final byte[] encodedImage) {
        if (encodedImage == null || encodedImage.length == 0) {
            throw new RuntimeException("Cannot decode: encoded image data is null or empty");
        }

        final ByteBuffer resRLEBuffer = ByteBuffer.wrap(encodedImage);

        // Decode Y matrix
        if (resRLEBuffer.remaining() < 4) {
            throw new RuntimeException("Cannot decode: insufficient data for Y matrix dimensions");
        }
        short[][] yMatrix = enDeRLE.revZigZagRLE(resRLEBuffer);

        // Decode Cb matrix
        if (resRLEBuffer.remaining() < 4) {
            throw new RuntimeException("Cannot decode: insufficient data for Cb matrix dimensions");
        }
        short[][] cbMatrix = enDeRLE.revZigZagRLE(resRLEBuffer);

        // Decode Cr matrix
        if (resRLEBuffer.remaining() < 4) {
            throw new RuntimeException("Cannot decode: insufficient data for Cr matrix dimensions");
        }
        short[][] crMatrix = enDeRLE.revZigZagRLE(resRLEBuffer);

        // Validate matrix dimensions before decompression
        if (yMatrix.length == 0 || yMatrix[0].length == 0) {
            throw new RuntimeException("Cannot decode: invalid Y matrix dimensions");
        }
        if (cbMatrix.length == 0 || cbMatrix[0].length == 0) {
            throw new RuntimeException("Cannot decode: invalid Cb matrix dimensions");
        }
        if (crMatrix.length == 0 || crMatrix[0].length == 0) {
            throw new RuntimeException("Cannot decode: invalid Cr matrix dimensions");
        }

        decompressor.decompressLumin(yMatrix,(short)yMatrix.length,(short)yMatrix[0].length);
        decompressor.decompressChrome(cbMatrix,(short)cbMatrix.length,(short)cbMatrix[0].length);
        decompressor.decompressChrome(crMatrix,(short)crMatrix.length,(short)crMatrix[0].length);

        return convertYCbCrToRGB(yMatrix, cbMatrix, crMatrix);
    }

    private int rescale(int value) {
        return Math.min(255, Math.max(0, value));
    }

    private int[][] convertYCbCrToRGB(final short[][] yMatrix, final short[][] cbMatrix, final short[][] crMatrix) {
        final int height = yMatrix.length;
        final int width = yMatrix[0].length;
        final int[][] rgb = new int[height][width];

        // Chroma matrices may be padded to multiples of 8, but we only need height/2 x width/2
        final int chromaHeight = Math.min(cbMatrix.length, height / 2);
        final int chromaWidth = Math.min(cbMatrix[0].length, width / 2);

        // Process chroma subsampled data - only use the first height/2 x width/2 elements
        // even if chroma matrices are padded to multiples of 8
        for (int i = 0; i < chromaHeight; ++i) {
            for (int j = 0; j < chromaWidth; ++j) {
                // Level shift: [-128, 127] → [0, 255]
                final int cbOffset = rescale(cbMatrix[i][j] + 128);
                final int crOffset = rescale(crMatrix[i][j] + 128);

                // Center chroma for YCbCr → RGB conversion
                final int cbCentered = cbOffset - 128;
                final int crCentered = crOffset - 128;

                for (int ii = 0; ii < 2; ++ii) {
                    for (int jj = 0; jj < 2; ++jj) {
                        final int yRow = 2 * i + ii;
                        final int yCol = 2 * j + jj;

                        // Skip if we're beyond the Y matrix bounds (due to chroma padding or edge cases)
                        if (yRow >= height || yCol >= width) {
                            continue;
                        }

                        final int y = rescale(yMatrix[yRow][yCol] + 128);

                        // YCbCr → RGB with centered chroma
                        int r = (int) Math.round(y + 1.402 * crCentered);
                        int g = (int) Math.round(y - 0.344136 * cbCentered - 0.714136 * crCentered);
                        int b = (int) Math.round(y + 1.772 * cbCentered);

                        // Clamp final RGB
                        r = Math.min(255, Math.max(0, r));
                        g = Math.min(255, Math.max(0, g));
                        b = Math.min(255, Math.max(0, b));

                        rgb[yRow][yCol] = (MASK << A_OFFSET) | (r << R_OFFSET) | (g << G_OFFSET) | b;
                    }
                }
            }
        }

        return rgb;
    }


}