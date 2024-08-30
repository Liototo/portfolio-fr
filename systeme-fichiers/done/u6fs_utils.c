/**
 * @file u6fs_utils.c
 * @brief Utilities (mostly dump) for UV6 filesystem
 * @author Aurélien Soccard / EB
 * @date 2022
 */

#include <string.h> // memset
#include <inttypes.h>
#include <openssl/sha.h>
#include <stdlib.h>
#include "mount.h"
#include "sector.h"
#include "error.h"
#include "u6fs_utils.h"
#include "filev6.h"
#include "unixv6fs.h"
#include "inode.h"
#include "bmblock.h"

#define UINT16_T_SIZE 16

int utils_print_superblock(const struct unix_filesystem *u)
{
    M_REQUIRE_NON_NULL(u);
    pps_printf("**********FS SUPERBLOCK START**********\n");
    pps_printf("%-20s: %" PRIu16 "\n", "s_isize",       u->s.s_isize      );
    pps_printf("%-20s: %" PRIu16 "\n", "s_fsize",       u->s.s_fsize      );
    pps_printf("%-20s: %" PRIu16 "\n", "s_fbmsize",     u->s.s_fbmsize    );
    pps_printf("%-20s: %" PRIu16 "\n", "s_ibmsize",     u->s.s_ibmsize    );
    pps_printf("%-20s: %" PRIu16 "\n", "s_inode_start", u->s.s_inode_start);
    pps_printf("%-20s: %" PRIu16 "\n", "s_block_start", u->s.s_block_start);
    pps_printf("%-20s: %" PRIu16 "\n", "s_fbm_start",   u->s.s_fbm_start  );
    pps_printf("%-20s: %" PRIu16 "\n", "s_ibm_start",   u->s.s_ibm_start  );
    pps_printf("%-20s: %" PRIu8 "\n", "s_flock",        u->s.s_flock      );
    pps_printf("%-20s: %" PRIu8 "\n", "s_ilock",        u->s.s_ilock      );
    pps_printf("%-20s: %" PRIu8 "\n", "s_fmod",         u->s.s_fmod       );
    pps_printf("%-20s: %" PRIu8 "\n", "s_ronly",        u->s.s_ronly      );
    pps_printf("%-20s: [%" PRIu16 "] %" PRIu16 "\n", "s_time", u->s.s_time[0], u->s.s_time[1]);
    pps_printf("**********FS SUPERBLOCK END**********\n");
    return ERR_NONE;
}

static void utils_print_SHA_buffer(unsigned char *buffer, size_t len)
{
    unsigned char sha[SHA256_DIGEST_LENGTH];
    SHA256(buffer, len, sha);
    for (int i = 0; i < SHA256_DIGEST_LENGTH; ++i) {
        pps_printf("%02x", sha[i]);
    }
    pps_printf("\n");
}

/**
 * More details @u6fs_utils.h
*/
int utils_print_inode(const struct inode *in) {

    pps_printf("**********FS INODE START**********\n");
    if (in != NULL) {
        pps_printf("i_mode: %d\ni_nlink: %d\ni_uid: %d\ni_gid: %d\ni_size0: %d\ni_size1: %d\nsize: %d\n",
         in->i_mode, in->i_nlink, in->i_uid, in->i_gid, in->i_size0, in->i_size1, inode_getsize(in));
        pps_printf("**********FS INODE END************\n");
        return ERR_NONE;
    } else {
        pps_printf("NULL ptr");
        pps_printf("**********FS INODE END************\n");
        return ERR_BAD_PARAMETER;
    }
    
}

/**
 * @brief print to stdout the first sector of a file
 * @param u - the mounted filesytem
 * @param inr - the inode number of the file
 * @return 0 on success, <0 on error
 */
int utils_cat_first_sector(const struct unix_filesystem *u, uint16_t inr) {

    M_REQUIRE_NON_NULL(u);
    
    struct filev6* fv6 = malloc(sizeof(struct filev6));

    if (fv6 == NULL) 
        return ERR_NOMEM;

    memset(fv6, 0, sizeof(struct filev6));

    int filev6openCheck = filev6_open(u, inr, fv6);

    if (filev6openCheck != ERR_NONE) {

        pps_printf("filev6_open failed for inode #%d.\n", inr);

        free(fv6);
        fv6 = NULL;

        return filev6openCheck;

    }

    pps_printf("\nPrinting inode #%d:\n", inr);
    utils_print_inode(&(fv6->i_node));

    if (fv6->i_node.i_mode & IFDIR) {
        
        pps_printf("which is a directory.\n");
    
    } else {

        pps_printf("the first sector of data of which contains:\n");

        char x[SECTOR_SIZE-1]; 

        int readBlockCheck = filev6_readblock(fv6, &x);

        if (readBlockCheck != ERR_NONE) {

            free(fv6);
            fv6 = NULL;

            return readBlockCheck;
        
        }

        pps_printf("%s", x);

        pps_printf("----\n");

    }

    free(fv6);
    fv6 = NULL;

    return ERR_NONE;
    
}

/**
 * @brief print to stdout the SHA256 digest of the first UTILS_HASHED_LENGTH bytes of the file
 * @param u - the mounted filesystem
 * @param inr - the inode number of the file
 * @return 0 on success, <0 on error
 */
int utils_print_shafile(const struct unix_filesystem *u, uint16_t inr) {

    M_REQUIRE_NON_NULL(u);

    struct filev6* fv6 = malloc(sizeof(struct filev6));

    if (fv6 == NULL) 
        return ERR_NOMEM;

    fv6->u = u;

    memset(fv6, 0, sizeof(struct filev6));

    int filev6openCheck = filev6_open(u, inr, fv6);

    if (filev6openCheck != ERR_NONE) {

        free(fv6);
        fv6 = NULL;

        return filev6openCheck;

    }
    
    pps_printf("SHA inode %d: ", inr);

    if (fv6->i_node.i_mode & IFDIR) {

        pps_printf("DIR\n");

    } else {

        unsigned char buf[UTILS_HASHED_LENGTH];

        size_t size = 0;

        for (int i = 0; i < UTILS_HASHED_LENGTH; i += SECTOR_SIZE) {

            int readBlockCheck = filev6_readblock(fv6, buf + i);

            if (readBlockCheck < 0) {

                free(fv6);
                fv6 = NULL;
                
                return readBlockCheck;

            }

            size += readBlockCheck;

            if (readBlockCheck < SECTOR_SIZE) 
                break;

        }

        utils_print_SHA_buffer(buf, size);

    }

    free(fv6);
    fv6 = NULL;

    return ERR_NONE;

}

/**
 * @brief print to stdout the SHA256 digest of all files, sorted by inode number
 * @param u - the mounted filesystem
 * @return 0 on success, <0 on error
 */
int utils_print_sha_allfiles(const struct unix_filesystem *u) {

    M_REQUIRE_NON_NULL(u);

    pps_printf("Listing inodes SHA\n");

    for (uint16_t inr = 1; inr <= u->s.s_isize*INODES_PER_SECTOR; ++inr) {

        int printCheck = utils_print_shafile(u, inr);

        if (printCheck == ERR_INODE_OUT_OF_RANGE)
            return ERR_NONE;

        if (printCheck != ERR_NONE && printCheck != ERR_UNALLOCATED_INODE)
            return printCheck;

    }

    return ERR_NONE;

}

/**
 * @brief print to stdout the inode and sector bitmaps
 * @param u - the mounted filesystem
 * @return 0 on success, <0 on error
 */
int utils_print_bitmaps(const struct unix_filesystem *u) {

    M_REQUIRE_NON_NULL(u);

    bm_print("INODES", u->ibm);
    bm_print("SECTORS", u->fbm);

    return ERR_NONE;

}
