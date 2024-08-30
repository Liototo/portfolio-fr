#include <stdio.h>
#include "sector.h"
#include "error.h"
#include "unixv6fs.h"

#define NB_SECT_TO_READ 1

/**
 * @brief read one 512-byte sector from the virtual disk
 * @param f open file of the virtual disk
 * @param sector the location (in sector units, not bytes) within the virtual disk
 * @param data a pointer to 512-bytes of memory (OUT)
 * @return 0 on success; <0 on error
 */
int sector_read(FILE *f, uint32_t sector, void *data) {
    
    M_REQUIRE_NON_NULL(f);
    M_REQUIRE_NON_NULL(data);

    if (f == NULL)
        return ERR_IO;

    long offset = sector * SECTOR_SIZE;

    int fseekCheck = fseek(f, offset, SEEK_SET);

    if (fseekCheck != ERR_NONE) {
        return fseekCheck;
    }

    size_t nb_read = fread(data, SECTOR_SIZE, NB_SECT_TO_READ, f);

    if (nb_read != NB_SECT_TO_READ) 
        return ERR_IO;

    return ERR_NONE;
    
} 

/**
 * @brief write one 512-byte sector from the virtual disk
 * @param f open file of the virtual disk
 * @param sector the location (in sector units, not bytes) within the virtual disk
 * @param data a pointer to 512-bytes of memory (IN)
 * @return 0 on success; <0 on error
 */
int sector_write(FILE *f, uint32_t sector, const void *data) {

    M_REQUIRE_NON_NULL(f);
    M_REQUIRE_NON_NULL(data);

    int writeCheck = fwrite(data, NB_SECT_TO_READ, SECTOR_SIZE, f + sector*SECTOR_SIZE);

    if (writeCheck < ERR_NONE)
        return ERR_IO;

    return ERR_NONE;

}
