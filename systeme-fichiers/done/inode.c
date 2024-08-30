#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "inode.h"
#include "error.h"
#include "unixv6fs.h"
#include "sector.h"

#define INODE_ID_START 0
#define SMALL_FILE_SECTOR_NBR 8
#define MAX_FILE_SIZE 7*256*SECTOR_SIZE

/**
 * @brief read all inodes from disk and print out their content to
 *        stdout according to the assignment
 * @param u the filesystem
 * @return 0 on success; < 0 on error.
 */
int inode_scan_print(const struct unix_filesystem *u) {

    M_REQUIRE_NON_NULL(u);

    uint16_t sizeInode = u->s.s_isize;

    for (uint16_t inr = 1; inr < sizeInode*INODES_PER_SECTOR; inr++) {

        struct inode inode;

        int inodeReadCheck = inode_read(u, inr, &inode);

        if (inodeReadCheck != ERR_NONE) {

            if (inodeReadCheck == ERR_UNALLOCATED_INODE) {
                return ERR_NONE;
            }

            return inodeReadCheck;

        }  
        else {

            pps_printf("inode %d (%s) len %d\n", inr, (inode.i_mode & IFDIR) ? SHORT_DIR_NAME : SHORT_FIL_NAME, inode_getsize(&inode));

        }  
    }

    return ERR_NONE;

}

/**
 * @brief read the content of an inode from disk
 * @param u the filesystem (IN)
 * @param inr the inode number of the inode to read (IN)
 * @param inode the inode structure, read from disk (OUT)
 * @return 0 on success; <0 on error
 */
int inode_read(const struct unix_filesystem *u, uint16_t inr, struct inode *inode) {

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(inode);

    uint16_t inodeStart = u->s.s_inode_start;
    uint16_t sizeInode = u->s.s_isize;

    if (inr >= INODES_PER_SECTOR*sizeInode || inr <= INODE_ID_START) 
        return ERR_INODE_OUT_OF_RANGE;

    struct inode x[INODES_PER_SECTOR];
    uint32_t sectorToRead = inr/INODES_PER_SECTOR;

    int sectorReadCheck = sector_read(u->f, inodeStart + sectorToRead, x);

    if (sectorReadCheck != ERR_NONE) 
        return sectorReadCheck;

    memcpy(inode, &(x[inr - sectorToRead*INODES_PER_SECTOR]), sizeof(struct inode));

    return (inode->i_mode & IALLOC) ? ERR_NONE : ERR_UNALLOCATED_INODE;
    
}

int inode_findsector(const struct unix_filesystem *u, const struct inode *i, int32_t file_sec_off) {
    
    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(i);

    if (!(i->i_mode & IALLOC))
        return ERR_UNALLOCATED_INODE;
    
    int32_t inodeSize = inode_getsize(i);

    if (inodeSize > MAX_FILE_SIZE)
        return ERR_FILE_TOO_LARGE;

    if (file_sec_off < 0 || file_sec_off > inodeSize/SECTOR_SIZE)
        return ERR_OFFSET_OUT_OF_RANGE;

    if (SMALL_FILE_SECTOR_NBR*SECTOR_SIZE > inodeSize)
        return i->i_addr[file_sec_off];

    uint16_t data[SECTOR_SIZE];
    int sectorReadCheck = sector_read(u->f, i->i_addr[inodeSize/(ADDRESSES_PER_SECTOR*SECTOR_SIZE)], data);
    if (sectorReadCheck != ERR_NONE)
        return sectorReadCheck;

    return data[(SECTOR_SIZE/ADDRESSES_PER_SECTOR)*file_sec_off];
    
}

/**
 * @brief write the content of an inode to disk
 * @param u the filesystem (IN)
 * @param inr the inode number of the inode to write (IN)
 * @param inode the inode structure, written to disk (IN)
 * @return 0 on success; <0 on error
 */
int inode_write(struct unix_filesystem *u, uint16_t inr, const struct inode *inode) {

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(inode);

    struct inode* readInode = malloc(sizeof(struct inode));

    if (readInode == NULL)
        return ERR_NOMEM;

    int inodeReadCheck = inode_read(u, inr, readInode);

    if (inodeReadCheck != ERR_NONE) {

        free(readInode);
        readInode = NULL;

        return inodeReadCheck;

    }

    int32_t offset = 0; // to change

    int findSectorCheck = inode_findsector(u, readInode, offset);

    if (findSectorCheck < ERR_NONE) {

        free(readInode);
        readInode = NULL;

        return findSectorCheck;

    }

    free(readInode);
    readInode = NULL;

    char sector[SECTOR_SIZE];

    int sectorReadCheck = sector_read(u->f, findSectorCheck, sector);

    if (sectorReadCheck != ERR_NONE)
        return sectorReadCheck;

    memcpy(&(sector[inr - INODES_PER_SECTOR*findSectorCheck]), inode, sizeof(struct inode));

    int sectorWriteCheck = sector_write(u->f, findSectorCheck, sector);

    if (sectorWriteCheck != ERR_NONE)
        return sectorWriteCheck;

    return ERR_NONE;

}

/**
 * @brief alloc a new inode (returns its inr if possible)
 * @param u the filesystem (IN)
 * @return the inode number of the new inode or error code on error
 */
int inode_alloc(struct unix_filesystem *u) {

    M_REQUIRE_NON_NULL(u);

    int inr = bm_find_next(u->ibm);

    if (inr < ERR_NONE)
        return ERR_BITMAP_FULL;

    bm_set(u->ibm, inr);

    return inr;

}

/**
 * @brief set the size of a given inode to the given size
 * @param inode the inode
 * @param new_size the new size
 * @return 0 on success; <0 on error
 */
int inode_setsize(struct inode *inode, int new_size) {

    M_REQUIRE_NON_NULL(inode);

    if (new_size < 0)
        return ERR_BAD_PARAMETER;

    inode->i_size0 = (new_size >> 16) & 0xFF;
    inode->i_size1 = new_size & 0xFFFF;

    return ERR_NONE;

}
