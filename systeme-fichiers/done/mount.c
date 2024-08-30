/**
 * @file mount.c
 * @brief mounts the filesystem
 *
 * @author Ludovic Mermod / Aurélien Soccard / Edouard Bugnion
 * @date 2022
 */

#include <string.h> // memset()
#include <stdlib.h>
#include <inttypes.h>

#include "error.h"
#include "mount.h"
#include "sector.h"
#include "bmblock.h"
#include "inode.h"

/**
 * @brief  mount a unix v6 filesystem
 * @param filename name of the unixv6 filesystem on the underlying disk (IN)
 * @param u the filesystem (OUT)
 * @return 0 on success; <0 on error
 */
int mountv6(const char *filename, struct unix_filesystem *u)
{

    M_REQUIRE_NON_NULL(filename);
    M_REQUIRE_NON_NULL(u);

    memset(u, 0, sizeof(*u));
    u->f = fopen(filename, "r");
    
    if (u->f == NULL) 
        return ERR_IO;

    uint8_t x[512];

    int bootSectCheck = sector_read(u->f, BOOTBLOCK_SECTOR, &x); 

    if (bootSectCheck != ERR_NONE) {
            
        fclose(u->f);

        return bootSectCheck;

    }

    if (x[BOOTBLOCK_MAGIC_NUM_OFFSET] != BOOTBLOCK_MAGIC_NUM) {
            
        fclose(u->f);

        return ERR_BAD_BOOT_SECTOR;

    }

    int superBlockCheck = sector_read(u->f, SUPERBLOCK_SECTOR, &(u->s));

    if (superBlockCheck != ERR_NONE) {

        fclose(u->f);

        return superBlockCheck;
    
    }

    u->ibm = bm_alloc(ROOT_INUMBER, u->s.s_isize*INODES_PER_SECTOR);

    if (u->ibm == NULL) return ERR_BITMAP_FULL;

    for (uint16_t inr = 1; inr < u->s.s_isize*INODES_PER_SECTOR; inr++) {

        struct inode inode;

        int inodeReadCheck = inode_read(u, inr, &inode);

        if (inodeReadCheck != ERR_UNALLOCATED_INODE && (inode.i_mode & IALLOC))
            bm_set(u->ibm, inr);

    }

    u->fbm = bm_alloc(u->s.s_block_start, u->s.s_fsize);

    if (u->fbm == NULL) return ERR_BITMAP_FULL;

    for (uint16_t inr = 1; inr < u->s.s_isize*INODES_PER_SECTOR; inr++) {

        struct inode inode;

        int inodeReadCheck = inode_read(u, inr, &inode);

        if (inodeReadCheck == ERR_UNALLOCATED_INODE || !(inode.i_mode & IALLOC))
            continue;

        int offset = 0;

        while (1) {
            
            if (inode_getsize(&inode) - offset == 0)
                break;
            
            int sector = inode_findsector(u, &inode, offset);

            if (sector < ERR_NONE) {

                if (sector == ERR_OFFSET_OUT_OF_RANGE)
                    break;

                free(u->ibm);
                u->ibm = NULL;
                
                free(u->fbm);
                u->fbm = NULL;

                return sector;

            }

            bm_set(u->fbm, sector); // sector is not right i think? (works with simple.uv6 but not aiw.uv6)

            int remaining = (inode_getsize(&inode) - offset < SECTOR_SIZE);

            offset += (remaining) ? inode_getsize(&inode) : offset + SECTOR_SIZE;

        }

    }

    return ERR_NONE;
    
}

/**
 * @brief unmount the given filesystem
 * @param u - the mounted filesytem
 * @return 0 on success; <0 on error
 */
int umountv6(struct unix_filesystem *u)
{
    M_REQUIRE_NON_NULL(u);

    if (u->f == NULL) return ERR_IO;

    int ret = fclose(u->f);

    free(u->ibm);
    u->ibm = NULL;

    free(u->fbm);
    u->fbm = NULL;

    memset(u, 0, sizeof(struct unix_filesystem));
    
    return (ret != 0) ? ERR_IO : ERR_NONE;

}
