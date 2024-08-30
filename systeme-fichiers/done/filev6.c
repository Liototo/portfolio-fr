#include <string.h>
#include <stdlib.h>
#include "filev6.h"
#include "unixv6fs.h"
#include "error.h"
#include "inode.h"
#include "sector.h"

#define MAX_FILE_SIZE 7*256*SECTOR_SIZE

/**
 * @brief open the file corresponding to a given inode; set offset to zero
 * @param u the filesystem (IN)
 * @param inr the inode number (IN)
 * @param fv6 the complete filev6 data structure (OUT)
 * @return 0 on success; the appropriate error code (<0) on error
 */
int filev6_open(const struct unix_filesystem *u, uint16_t inr, struct filev6 *fv6) {

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(fv6);

    // copy u into the file
    fv6->u = u;

    // copy inr into the file
    fv6->i_number = inr;

    int inodeReadCheck = inode_read(u, inr, &(fv6->i_node));

    if (inodeReadCheck != ERR_NONE) {

        return inodeReadCheck;

    }

    // initialise the offset to 0
    fv6->offset = 0;

    return ERR_NONE;

}

/**
 * @brief read at most SECTOR_SIZE from the file at the current cursor
 * @param fv6 the filev6 (IN-OUT; offset will be changed)
 * @param buf points to SECTOR_SIZE bytes of available memory (OUT)
 * @return >0: the number of bytes of the file read; 0: end of file;
 *             the appropriate error code (<0) on error
 */
int filev6_readblock(struct filev6 *fv6, void *buf) {

    M_REQUIRE_NON_NULL(fv6);
    M_REQUIRE_NON_NULL(buf);

    if (!(fv6->i_node.i_mode & IALLOC))
        return ERR_UNALLOCATED_INODE;

    if (inode_getsize(&(fv6->i_node)) - fv6->offset == 0) 
        return 0;

    int sector = inode_findsector(fv6->u, &(fv6->i_node), fv6->offset);

    if (sector < ERR_NONE)
        return sector;

    int sectorReadCheck = sector_read(fv6->u->f, sector, buf);

    if (sectorReadCheck != ERR_NONE)
        return sectorReadCheck;

    int remainingCheck = (inode_getsize(&(fv6->i_node)) - fv6->offset < SECTOR_SIZE);

    int32_t prevOffset = fv6->offset;

    fv6->offset = (remainingCheck) ? inode_getsize(&(fv6->i_node)) : fv6->offset + SECTOR_SIZE;

    return fv6->offset - prevOffset;

}

/**
 * @brief change the current offset of the given file to the one specified
 * @param fv6 the filev6 (IN-OUT; offset will be changed)
 * @param off the new offset of the file
 * @return 0 on success; <0 on error
 */
int filev6_lseek(struct filev6 *fv6, int32_t offset) {

    M_REQUIRE_NON_NULL(fv6);

    if (offset < 0 || offset > inode_getsize(&(fv6->i_node))) {
        return ERR_OFFSET_OUT_OF_RANGE;
    }

    if (offset == inode_getsize(&(fv6->i_node))) {
        fv6->offset = offset;
        return ERR_NONE;
    }

    if (offset % SECTOR_SIZE != 0)
        return ERR_BAD_PARAMETER;

    fv6->offset = offset;

    return ERR_NONE;

}

/**
 * @brief create a new filev6
 * @param u the filesystem (IN)
 * @param mode the mode of the file
 * @param fv6 the filev6 (OUT; i_node and i_number will be changed)
 * @return 0 on success; <0 on error
 */
int filev6_create(struct unix_filesystem *u, uint16_t mode, struct filev6 *fv6) {

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(fv6);

    int inr = inode_alloc(u);

    if (inr < ERR_NONE)
        return inr;

    struct inode inode = { 0 };

    inode.i_mode = mode;

    int writeCheck = inode_write(u, inr, &inode);

    if (writeCheck != ERR_NONE)
        return writeCheck;

    fv6->i_number = inr;
    fv6->i_node = inode;

    return ERR_NONE;

}

/**
 * @brief write the len bytes of the given buffer on disk to the given filev6
 * @param fv6 the filev6 (IN)
 * @param buf the data we want to write (IN)
 * @param len the length of the bytes we want to write
 * @return 0 on success; <0 on error
 */
int filev6_writebytes(struct filev6 *fv6, const void *buf, size_t len) {

    M_REQUIRE_NON_NULL(fv6);
    M_REQUIRE_NON_NULL(buf);

    int left = len;

    while (left > 0) {

        int writeSectorCheck = filev6_writesector(fv6, buf + len - left, left);

        if (writeSectorCheck != ERR_NONE)
            return writeSectorCheck;

        left -= SECTOR_SIZE;

    }

    int setSizeCheck = inode_setsize(&(fv6->i_node), inode_getsize(&(fv6->i_node)) + len);

    if (setSizeCheck != ERR_NONE)
        return setSizeCheck;

    return ERR_NONE;

}


/**
 * @brief local helper function for filev6_writebytes, writes 512 bytes of data on the given filev6
 * @param fv6 the filev6 (IN)
 * @param buf the data we want to write (IN)
 * @param len the length of the bytes we want to write
 * @return 0 on success; <0 on error
*/
int filev6_writesector(struct filev6 *fv6, const void *buf, size_t len) {

    int32_t inodeSize = inode_getsize(&(fv6->i_node));

    if (inodeSize > MAX_FILE_SIZE)
        return ERR_FILE_TOO_LARGE;

    int nbBytes;

    if (inodeSize % SECTOR_SIZE == 0) {

        nbBytes = (len > SECTOR_SIZE) ? SECTOR_SIZE : len;

        int newSect = bm_find_next(fv6->u->fbm);

        if (newSect < ERR_NONE)
            return newSect;

        bm_set(fv6->u->fbm, newSect);

        char* data = malloc(SECTOR_SIZE);

        if (data == NULL)
            return ERR_NOMEM;

        memset(data, 0, SECTOR_SIZE);

        memcpy(data, buf, nbBytes);

        int sectorWriteCheck = sector_write(fv6->u->f, newSect, data); 

        if (sectorWriteCheck != ERR_NONE) {

            free(data);
            data = NULL;

            return sectorWriteCheck;

        }

        free(data);
        data = NULL;

        fv6->i_node.i_addr[inode_getsize(fv6->i_node)/SECTOR_SIZE] = newSect;

    } else {

        int unusedSectorSize = SECTOR_SIZE - (inodeSize % SECTOR_SIZE);

        nbBytes = (len > unusedSectorSize) ? unusedSectorSize : len;

        char* sector = malloc(SECTOR_SIZE);

        if (sector == NULL)
            return ERR_NOMEM;

        uint32_t sectorNr = inodeSize/SECTOR_SIZE;

        int sectorReadCheck = sector_read(fv6->u->f, sectorNr, sector);

        if (sectorReadCheck != ERR_NONE) {

            free(sector);
            sector = NULL;

            return sectorReadCheck;

        }

        memcpy(sector + (inodeSize % SECTOR_SIZE), buf, unusedSectorSize);

        int sectorWriteCheck = sector_write(fv6->u->f, sectorNr, sector);

        if (sectorWriteCheck != ERR_NONE) {

            free(sector);
            sector = NULL;

            return sectorWriteCheck;

        }

        free(sector);
        sector = NULL;

        fv6->i_node.i_addr[(inode_getsize(fv6->i_node) + SECTOR_SIZE - 1)/SECTOR_SIZE] = sectorNr;

    }

    return ERR_NONE;

}
