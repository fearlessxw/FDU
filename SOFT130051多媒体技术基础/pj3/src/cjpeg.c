/** 
 * @file cjpeg.c
 * @brief main file, convert BMP to JPEG image.
 */

#include "cjpeg.h"
#include "cio.h"
#include <string.h>

/* YCbCr to RGB transformation */

/*
 * precalculated tables for a faster YCbCr->RGB transformation.
 * use a INT32 table because we'll scale values by 2^16 and
 * work with integers.
 */

ycbcr_tables ycc_tables;
void write_file_header(compress_io *cio);
void write_frame_header(compress_io *cio, bmp_info *binfo);
void write_scan_header(compress_io *cio);
void write_file_trailer(compress_io *cio);
void jpeg_fdct(float *data);
void read_bmp(FILE *bmp_fp, bmp_info *binfo);

void
init_ycbcr_tables()
{
    UINT16 i;
    for (i = 0; i < 256; i++) {
        ycc_tables.r2y[i]  = (INT32)(65536 *  0.299   + 0.5) * i;
        ycc_tables.r2cb[i] = (INT32)(65536 * -0.16874 + 0.5) * i;
        ycc_tables.r2cr[i] = (INT32)(32768) * i;
        ycc_tables.g2y[i]  = (INT32)(65536 *  0.587   + 0.5) * i;
        ycc_tables.g2cb[i] = (INT32)(65536 * -0.33126 + 0.5) * i;
        ycc_tables.g2cr[i] = (INT32)(65536 * -0.41869 + 0.5) * i;
        ycc_tables.b2y[i]  = (INT32)(65536 *  0.114   + 0.5) * i;
        ycc_tables.b2cb[i] = (INT32)(32768) * i;
        ycc_tables.b2cr[i] = (INT32)(65536 * -0.08131 + 0.5) * i;
    }
}

void
rgb_to_ycbcr(UINT8 *rgb_unit, ycbcr_unit *ycc_unit, int x, int w) {
    ycbcr_tables *tbl = &ycc_tables;
    UINT8 r, g, b;
    // int src_pos = x * 3;
    int src_pos = 0;
    int dst_pos = 0;
    int i, j;
    for (j = 0; j < DCTSIZE; j++) {
        for (i = 0; i < DCTSIZE; i++) {
            b = rgb_unit[src_pos];
            g = rgb_unit[src_pos + 1];
            r = rgb_unit[src_pos + 2];
            ycc_unit->y[dst_pos] = (INT8) ((UINT8)
                                                   ((tbl->r2y[r] + tbl->g2y[g] + tbl->b2y[b]) >> 16) - 128);
            ycc_unit->cb[dst_pos] = (INT8) ((UINT8)
                    ((tbl->r2cb[r] + tbl->g2cb[g] + tbl->b2cb[b]) >> 16));
            ycc_unit->cr[dst_pos] = (INT8) ((UINT8)
                    ((tbl->r2cr[r] + tbl->g2cr[g] + tbl->b2cr[b]) >> 16));
            src_pos += 3;
            dst_pos++;
        }
//        src_pos += (w - DCTSIZE) * 3;
    }
}


/* quantization */

quant_tables q_tables;

void
init_quant_tables(UINT32 scale_factor)
{
    quant_tables *tbl = &q_tables;
    int temp1, temp2;
    int i;
    for (i = 0; i < DCTSIZE2; i++) {
        temp1 = ((UINT32) STD_LU_QTABLE[i] * scale_factor + 50) / 100;
        if (temp1 < 1)
            temp1 = 1;
        if (temp1 > 255)
            temp1 = 255;
        tbl->lu[ZIGZAG[i]] = (UINT8) temp1;

        temp2 = ((UINT32) STD_CH_QTABLE[i] * scale_factor + 50) / 100;
        if (temp2 < 1)
            temp2 = 1;
        if (temp2 > 255)
            temp2 = 255;
        tbl->ch[ZIGZAG[i]] = (UINT8) temp2;
    }
}

void
jpeg_quant(ycbcr_unit *ycc_unit, quant_unit *q_unit)
{
    quant_tables *tbl = &q_tables;
    float q_lu, q_ch;
    int x, y, i = 0;
    for (x = 0; x < DCTSIZE; x++) {
        for (y = 0; y < DCTSIZE; y++) {
            q_lu = 1.0 / ((double) tbl->lu[ZIGZAG[i]] * \
                    AAN_SCALE_FACTOR[x] * AAN_SCALE_FACTOR[y] * 8.0);
            q_ch = 1.0 / ((double) tbl->ch[ZIGZAG[i]] * \
                    AAN_SCALE_FACTOR[x] * AAN_SCALE_FACTOR[y] * 8.0);

            q_unit->y[i] = (INT16)(ycc_unit->y[i]*q_lu + 16384.5) - 16384;
            q_unit->cb[i] = (INT16)(ycc_unit->cb[i]*q_ch + 16384.5) - 16384;
            q_unit->cr[i] = (INT16)(ycc_unit->cr[i]*q_ch + 16384.5) - 16384;

            i++;
        }
    }
}


/* huffman compression */

huff_tables h_tables;

void
set_huff_table(UINT8 *nrcodes, UINT8 *values, BITS *h_table)
{
    int i, j, k;
    j = 0;
    UINT16 value = 0;
    for (i = 1; i <= 16; i++) {
        for (k = 0; k < nrcodes[i]; k++) {
            h_table[values[j]].len = i;
            h_table[values[j]].val = value;
            j++;
            value++;
        }
        value <<= 1;
    }
}

void
init_huff_tables()
{
    huff_tables *tbl = &h_tables;
    set_huff_table(STD_LU_DC_NRCODES, STD_LU_DC_VALUES, tbl->lu_dc);
    set_huff_table(STD_LU_AC_NRCODES, STD_LU_AC_VALUES, tbl->lu_ac);
    set_huff_table(STD_CH_DC_NRCODES, STD_CH_DC_VALUES, tbl->ch_dc);
    set_huff_table(STD_CH_AC_NRCODES, STD_CH_AC_VALUES, tbl->ch_ac);
}

void
set_bits(BITS *bits, INT16 data)
{
    /******************************************************/
    // Declare an unsigned 16-bit integer to store the absolute value of the input data.
    // If the input data is negative, calculate its absolute value.
    INT16 abs_data = data < 0 ? (~(data) + (INT16) 1) : data;

    // Declare an unsigned 8-bit integer to store the length.
    UINT8 length = 0;
    // Determine the bit length of the absolute value by finding the leftmost set bit.
    while (abs_data > (INT16) 0) {
        length = length + (UINT8) 1;
        abs_data = abs_data >> 1;
    }
    // Set the length of the BITS structure to the calculated bit length.
    bits->len = length;

    // If the original data was negative, map it to the specified range [0, 2^bits->len - 1].
    UINT16 byte = (UINT16) (data < 0 ? ((data + (((INT16) 1) << length) - (INT16) 1)) : data);
    bits->val = byte;
    /******************************************************/
}

#ifdef DEBUG
void
print_bits(BITS bits)
{
    printf("%hu %hu\t", bits.len, bits.val);
}
#endif

/*
 * compress JPEG
 */
void
jpeg_compress(compress_io *cio,
        INT16 *data, INT16 *dc, BITS *dc_htable, BITS *ac_htable)
{
    INT16 zigzag_data[DCTSIZE2];
    BITS bits;
    INT16 diff;
    int i, j;
    int zero_num;
    int mark;

    /* zigzag encode */
    for (i = 0; i < DCTSIZE2; i++)
        zigzag_data[ZIGZAG[i]] = data[i];

    /* write DC */
    diff = zigzag_data[0] - *dc;
    *dc = zigzag_data[0];

    if (diff == 0)
        write_bits(cio, dc_htable[0]);
    else {
        set_bits(&bits, diff);
        write_bits(cio, dc_htable[bits.len]);
        write_bits(cio, bits);
    }

    /* write AC */
    int end = DCTSIZE2 - 1;
    while (zigzag_data[end] == 0 && end > 0)
        end--;
    for (i = 1; i <= end; i++) {
        j = i;
        while (zigzag_data[j] == 0 && j <= end)
            j++;
        zero_num = j - i;
        for (mark = 0; mark < zero_num / 16; mark++)
            write_bits(cio, ac_htable[0xF0]);
        zero_num = zero_num % 16;
        set_bits(&bits, zigzag_data[j]);
        write_bits(cio, ac_htable[zero_num * 16 + bits.len]);
        write_bits(cio, bits);
        i = j;
    }

    /* write end of unit */
    if (end != DCTSIZE2 - 1)
        write_bits(cio, ac_htable[0]);
}


/*
 * main JPEG encoding
 */
void
jpeg_encode(compress_io *cio, bmp_info *binfo)
{
    /*
     * Initialize Tables
     * 1. Initialize YCbCr tables for color space conversion.
     * 2. Initialize quantization tables using the specified scale.
     * 3. Initialize Huffman tables for entropy encoding.
     */
    UINT32 scale = 50; // Scale parameter for quantization tables
    init_ycbcr_tables(); // Initialize YCbCr tables for color space conversion
    init_quant_tables(scale); // Initialize quantization tables with the specified scale
    init_huff_tables(); // Initialize Huffman tables for entropy encoding

    /*
     * Write Information Headers
     * 1. Write the file header containing basic JPEG file information.
     * 2. Write the frame header containing image dimensions and color space information.
     * 3. Write the scan header containing information about the image scan.
     */
    write_file_header(cio); // Write JPEG file header
    write_frame_header(cio, binfo); // Write JPEG frame header with image information
    write_scan_header(cio); // Write JPEG scan header for image scanning

    /* encode */
    // Create pointers and variables for input memory management, YCbCr units, and quantization units
    mem_mgr *in = cio->in;  // Input memory manager
    ycbcr_unit ycc_unit;    // YCbCr unit structure
    quant_unit q_unit;      // Quantization unit structure

    // Initialize DC coefficients for Y, Cb, and Cr components
    INT16 dc_y = 0,        // DC coefficient for Y component
    dc_cb = 0,       // DC coefficient for Cb component
    dc_cr = 0;       // DC coefficient for Cr component
    // Loop variables for image iteration
    int x, y;

    // Retrieve the offset value from the BMP information structure
    int offset = binfo->offset;
    // Set the file pointer to the specified offset in the input file
    fseek(in->fp, offset, SEEK_SET);

    // Get the width and height of the BMP image
    int bmp_width = binfo->width;
    int bmp_height = binfo->height;

    // Adjust BMP width and height to be multiples of 8 for 8x8 block processing
    int jpeg_width = (bmp_width % 8 == 0) ? bmp_width : (bmp_width / 8 + 1) * 8;
    int jpeg_height = (bmp_height % 8 == 0) ? bmp_height : (bmp_height / 8 + 1) * 8;

    // Number of columns and rows for 8x8 blocks
    int column = jpeg_width / 8;
    int row = jpeg_height / 8;

    // initialize arrays to store RGB/ycbcr/quantized data for each 8x8 block
    UINT8 *rgb_array[column * row];
    ycbcr_unit *ycbcr_array[column * row];
    quant_unit *quant_array[column * row];
    size_t rgb_block_size = sizeof(UINT8) * 3 * DCTSIZE2;

    // Allocate memory for RGB, YCbCr, and quantized arrays
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < column; j++) {
            int index = i * column + j;
            // Allocate memory for RGB array
            // The element is an 8*8*3 array to store the RGB data of a block
            rgb_array[index] = malloc(rgb_block_size);
            memset(rgb_array[index], 0, rgb_block_size);

            // Allocate memory for YCbCr array and initialize
            // The element is a ycbcr_unit to store the ycbcr data of a block
            ycbcr_array[index] = malloc(sizeof(ycbcr_unit));
            memset(ycbcr_array[index], 0, sizeof(ycc_unit));

            // Allocate memory for quantized data array and initialize
            // The element is a quant_unit to store the quantized data of a block
            quant_array[index] = malloc(sizeof(quant_unit));
            memset(quant_array[index], 0, sizeof(quant_unit));
        }
    }

    UINT8 *rgb_unit;
    // Read RGB data and store it
    for (int i = 0; i < bmp_height; ++i) {
        // Calculate the row index and offset within the 8x8 block
        int temp_row = (bmp_height - i - 1) / DCTSIZE;
        int temp_y = (bmp_height - i - 1) % DCTSIZE;
        for (int j = 0; j < bmp_width; ++j) {
            // Calculate the column index and offset within the 8x8 block
            int temp_column = j / DCTSIZE;
            int temp_x = j % DCTSIZE;
            // Calculate the index within the 8x8 block multiplied by the number of color channels (3)
            int index = (temp_y * DCTSIZE + temp_x) * 3;

            // Get the corresponding block and read and store RGB data
            rgb_unit = rgb_array[temp_row * column + temp_column];
            fread(&rgb_unit[index], sizeof(UINT8), 3, in->fp);
        }
    }
    // Convert, DCT, quantize, and compress
    for (int i = 0; i < row; ++i) {
        for (int j = 0; j < column; ++j) {
            // Calculate the index of current block
            int index = i * column + j;

            // Convert RGB unit to YCbCr unit for the 8x8 pixel block
            rgb_to_ycbcr(rgb_array[index], ycbcr_array[index], 0, 0);

            // Apply DCT (Discrete Cosine Transform) to Y, Cb, and Cr components in sequential mode
            jpeg_fdct(ycbcr_array[index]->y);
            jpeg_fdct(ycbcr_array[index]->cb);
            jpeg_fdct(ycbcr_array[index]->cr);

            // Quantize the transformed block
            jpeg_quant(ycbcr_array[index], quant_array[index]);

            // Compress and encode the Y/Cb/Cr component using Huffman coding
            jpeg_compress(cio, quant_array[index]->y, &dc_y, h_tables.lu_dc, h_tables.lu_ac);
            jpeg_compress(cio, quant_array[index]->cb, &dc_cb, h_tables.ch_dc, h_tables.ch_ac);
            jpeg_compress(cio, quant_array[index]->cr, &dc_cr, h_tables.ch_dc, h_tables.ch_ac);

            // Free allocated memory
            free(rgb_array[index]);
            free(ycbcr_array[index]);
            free(quant_array[index]);
        }
    }
    write_align_bits(cio);

    /* write file end */
    write_file_trailer(cio);
}



bool
is_bmp(FILE *fp)
{
    UINT8 marker[3];
    if (fread(marker, sizeof(UINT16), 2, fp) != 2)
        err_exit(FILE_READ_ERR);
    if (marker[0] != 0x42 || marker[1] != 0x4D)
        return false;
    rewind(fp);
    return true;
}

void
err_exit(const char *error_string, int exit_num)
{
    printf("%s", error_string);
    exit(exit_num);
}


void
print_help()
{
    printf("compress BMP file into JPEG file.\n");
    printf("Usage:\n");
    printf("    cjpeg {BMP} {JPEG}\n");
    printf("\n");
    printf("Author: Yu, Le <yeolar@gmail.com>\n");
}

int
main(int argc, char *argv[])
{

    if (argc == 3) {
        /* open bmp file */
        FILE *bmp_fp = fopen(argv[1], "rb");
        if (!bmp_fp)
            err_exit(FILE_OPEN_ERR);
//        if (!is_bmp(bmp_fp))
//            err_exit(FILE_TYPE_ERR);

        /* open jpeg file */
        FILE *jpeg_fp = fopen(argv[2], "wb");
        if (!jpeg_fp)
            err_exit(FILE_OPEN_ERR);

        /* get bmp info */
        bmp_info binfo;
        read_bmp(bmp_fp, &binfo);

        /* init memory for input and output */
        compress_io cio;
        int in_size = (binfo.width * 3 + 3) / 4 * 4 * DCTSIZE;
        int out_size = MEM_OUT_SIZE;
        init_mem(&cio, bmp_fp, in_size, jpeg_fp, out_size);

        /* main encode process */
        jpeg_encode(&cio, &binfo);

        /* flush and free memory, close files */
        if (! (cio.out->flush_buffer) (&cio))
            err_exit(BUFFER_WRITE_ERR);
        free_mem(&cio);
        fclose(bmp_fp);
        fclose(jpeg_fp);
    }
    else
        print_help();
    exit(0);
}