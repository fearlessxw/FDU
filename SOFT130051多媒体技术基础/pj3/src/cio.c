/** 
 * @file cio.c
 * @brief memory manager and operations for compressing JPEG IO.
 */

#include <string.h>
#include "cjpeg.h"
#include "cio.h"


/*
 * flush input and output of compress IO.
 */


bool
flush_cin_buffer(void *cio)
{
    mem_mgr *in = ((compress_io *) cio)->in;
    size_t len = in->end - in->set;
    memset(in->set, 0, len);
    if (fread(in->set, sizeof(UINT8), len, in->fp) != len)
        return false;
    in->pos = in->set;
    return true;
}

bool
flush_cout_buffer(void *cio)
{
    mem_mgr *out = ((compress_io *) cio)->out;
    size_t len = out->pos - out->set;
    if (fwrite(out->set, sizeof(UINT8), len, out->fp) != len)
        return false;
    memset(out->set, 0, len);
    out->pos = out->set;
    return true;
}


/*
 * init memory manager.
 */

void
init_mem(compress_io *cio,
         FILE *in_fp, int in_size, FILE *out_fp, int out_size)
{
    cio->in = (mem_mgr *) malloc(sizeof(mem_mgr));
    if (!cio->in)
        err_exit(BUFFER_ALLOC_ERR);
    cio->in->set = (UINT8 *) malloc(sizeof(UINT8) * in_size);
    if (!cio->in->set)
        err_exit(BUFFER_ALLOC_ERR);
    cio->in->pos = cio->in->set;
    cio->in->end = cio->in->set + in_size;
    cio->in->flush_buffer = flush_cin_buffer;
    cio->in->fp = in_fp;

    cio->out = (mem_mgr *) malloc(sizeof(mem_mgr));
    if (!cio->out)
        err_exit(BUFFER_ALLOC_ERR);
    cio->out->set = (UINT8 *) malloc(sizeof(UINT8) * out_size);
    if (!cio->out->set)
        err_exit(BUFFER_ALLOC_ERR);
    cio->out->pos = cio->out->set;
    cio->out->end = cio->out->set + out_size;
    cio->out->flush_buffer = flush_cout_buffer;
    cio->out->fp = out_fp;

    cio->temp_bits.len = 0;
    cio->temp_bits.val = 0;
}

void
free_mem(compress_io *cio)
{
    fflush(cio->out->fp);
    free(cio->in->set);
    free(cio->out->set);
    free(cio->in);
    free(cio->out);
}


/*
 * write operations.
 */

void
write_byte(compress_io *cio, UINT8 val)
{
    mem_mgr *out = cio->out;
    *(out->pos)++ = val & 0xFF;
    if (out->pos == out->end) {
        if (!(out->flush_buffer)(cio))
            err_exit(BUFFER_WRITE_ERR);
    }
}

void
write_word(compress_io *cio, UINT16 val)
{
    write_byte(cio, (val >> 8) & 0xFF);
    write_byte(cio, val & 0xFF);
}

void
write_marker(compress_io *cio, JPEG_MARKER mark)
{
    write_byte(cio, 0xFF);
    write_byte(cio, (int) mark);
}

void
write_bits(compress_io *cio, BITS bits)
{
    /******************************************************/
    // Create a pointer to the temporary bits structure in the compress_io.
    BITS *temp_bits = &(cio->temp_bits);
    // Declare two 8-bit unsigned integers to store byte representations of the bits.
    UINT8 temp_byte1, temp_byte2;

    // Calculate the total length of the current and temporary bit sequences.
    int len = bits.len + temp_bits->len - 16;
    // If the total length is non-negative, indicating that the bits can be processed in full.
    if (len >= 0) {
        // Merge the temporary and current bit sequences into a 16-bit word.
        UINT16 word = temp_bits->val | bits.val >> len;

        // Extract the high 8 bits of the word and store in temp_byte1.
        temp_byte1 = word >> 8;
        // Write temp_byte1 to the output stream.
        write_byte(cio, temp_byte1);
        // If temp_byte1 is 0xFF, write an additional 0 byte.
        if (temp_byte1 == 0xFF) {
            write_byte(cio, 0);
        }

        // Extract the low 8 bits of the word and store in temp_byte2.
        temp_byte2 = word & 0xFF;
        // Write temp_byte2 to the output stream.
        write_byte(cio, temp_byte2);
        // If temp_byte2 is 0xFF, write an additional 0 byte.
        if (temp_byte2 == 0xFF) {
            write_byte(cio, 0);
        }

        // Update the length and value of the temporary bits structure.
        temp_bits->len = len;
        temp_bits->val = bits.val << (16 - len);
    } else {
        // If the total length is negative, indicating incomplete processing.

        // Update the length of the temporary bits structure.
        temp_bits->len = 16 + len;
        // Merge the temporary and current bit sequences into the temporary bits structure.
        temp_bits->val |= bits.val << -len;
    }
    /******************************************************/
}

void
write_align_bits(compress_io *cio)
{
    /******************************************************/
    // Obtain a pointer to the temporary bits structure in the compress_io.
    BITS *temp_bits = &(cio->temp_bits);
    // Declare a BITS structure to represent the alignment bits.
    BITS align_bits;

    // Calculate the number of bits needed for alignment to the next byte.
    align_bits.len = 8 - (temp_bits->len % 8);
    // Set the value of align_bits to a bitmask representing the required alignment.
    align_bits.val = (UINT16) ~0x0 >> (temp_bits->len % 8);

    // Declare a temporary 8-bit unsigned integer to store a byte.
    UINT8 temp_byte;

    // Write the alignment bits to the output stream.
    write_bits(cio, align_bits);

    // If the temporary bits structure is aligned to a byte boundary (len == 8),
    // write the high byte to the output stream.
    if (temp_bits->len == 8) {
        // Extract the high 8 bits of the temporary bits value.
        temp_byte = temp_bits->val >> 8;
        // Write temp_byte to the output stream.
        write_byte(cio, temp_byte);

        // If temp_byte is 0xFF, write an additional 0 byte.
        if (temp_byte == 0xFF) {
            write_byte(cio, 0);
        }
    }
    /******************************************************/
}

