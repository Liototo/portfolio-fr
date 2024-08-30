#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "direntv6.h"
#include "inode.h"
#include "error.h"
#include "unixv6fs.h"
#include "filev6.h"
#include "util.h"
#include "u6fs_utils.h"

#define CHILD_SIZE 16

/**
 * @brief opens a directory reader for the specified inode 'inr'
*/
int direntv6_opendir(const struct unix_filesystem *u, uint16_t inr, struct directory_reader *d)
{

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(d);
    struct inode inode;

    int inodeReadCheck = inode_read(u, inr, &inode);
    if (inodeReadCheck != ERR_NONE)
    {
        return inodeReadCheck;
    }
    if (inode.i_mode & IFDIR)
    {
        int fileOpeningCheck = filev6_open(u, inr, &(d->fv6));
        if (fileOpeningCheck != ERR_NONE)
        {
            return fileOpeningCheck;
        }
        d->cur = 0;
        d->last = 0;
        memset(d->dirs, 0, DIRENTRIES_PER_SECTOR);
        return ERR_NONE;
    }
    else
    {
        return ERR_INVALID_DIRECTORY_INODE;
    }
}

/**
 * @brief return the next directory entry.
*/
int direntv6_readdir(struct directory_reader *d, char *name, uint16_t *child_inr)
{
    M_REQUIRE_NON_NULL(d);
    M_REQUIRE_NON_NULL(name);
    M_REQUIRE_NON_NULL(child_inr);
    if (d->cur == d->last) {

        int readBlockCheck = filev6_readblock(&(d->fv6), d->dirs);
        
        if (readBlockCheck < ERR_NONE) {
            return readBlockCheck;
        }
            

        if (readBlockCheck == ERR_NONE) {
            name = strncpy(name, d->dirs[d->cur].d_name, DIRENT_MAXLEN);
            name[DIRENT_MAXLEN] = '\0';
            *child_inr = (d->dirs[d->cur].d_inumber);
            return ERR_NONE;
        }
        else
        {
            d->cur = 0;
            d->last = (readBlockCheck / CHILD_SIZE) - 1;
        }
    }
    name = strncpy(name, d->dirs[d->cur].d_name, DIRENT_MAXLEN);
    name[DIRENT_MAXLEN] = '\0';
    *child_inr = (d->dirs[d->cur].d_inumber);
    d->cur += 1;
    return 1;
}

/**
 * @brief debugging routine; print a subtree (note: recursive)
*/
int direntv6_print_tree(const struct unix_filesystem *u, uint16_t inr, const char *prefix)
{
    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(prefix);
    struct inode inode;
    int inodeReadCheck = inode_read(u, inr, &inode);

    if (inodeReadCheck != ERR_NONE)
    {
        return inodeReadCheck;
    }

    if (!(inode.i_mode & IFDIR)) {
        pps_printf("FIL %s\n", prefix);
        return ERR_NONE;
    }

    pps_printf("DIR %s\n", prefix);
    struct directory_reader d;
    int openDirCheck = direntv6_opendir(u, inr, &d);

    if (openDirCheck != ERR_NONE)
    {
        return openDirCheck;
    }
    
    else
    {
        int readDirCheck = 0;

        do {
            char next[DIRENT_MAXLEN + 1];

            uint16_t childInr = 0;

            readDirCheck = direntv6_readdir(&d, next, &childInr);
            if (readDirCheck < ERR_NONE) {
                return readDirCheck;
            } 

            char *nextPrinted = calloc(strlen(prefix) + DIRENT_MAXLEN + 2, sizeof(char));
            strncpy(nextPrinted, prefix, strlen(prefix));
            nextPrinted[strlen(prefix)] = '/';
            strncat(nextPrinted, next, strlen(next));
            
            int recTreeCheck = direntv6_print_tree(u, childInr, nextPrinted);
            free(nextPrinted);
            nextPrinted = NULL;
            if (recTreeCheck != ERR_NONE){
                return recTreeCheck;
            }

        } while (readDirCheck == 1);
        return ERR_NONE;
    }
}

/**
 * @brief get the inode number for the given path (Recursive function)
*/
int direntv6_dirlookup_core(const struct unix_filesystem *u, uint16_t inr, const char *entry, size_t size)
{
    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(entry);
    if (size == 0) {
        return inr;
    }
    struct directory_reader d;
    int openDirCheck = direntv6_opendir(u, inr, &d);
    if (openDirCheck != ERR_NONE) {
        return openDirCheck;
    }

    char next[DIRENT_MAXLEN];
    char compare[DIRENT_MAXLEN];

    int index = 0;
    while (entry[index] == '/') {
        index++;
    }

    for (size_t i = index; i < size; i++) {
        if (entry[i] == '/') {
            index = i;
            break;
        }
        compare[i - index] = entry[i];
    }

    uint16_t nextInode = 0;

    while (strcmp(next, compare) != 0) {
        int recReadDirCheck = direntv6_readdir(&d, next, &nextInode);
        if (recReadDirCheck < ERR_NONE) {
            return recReadDirCheck;
        } else if (recReadDirCheck == 0) {
            return ERR_NO_SUCH_FILE;
        }
    }

    char newString[size - index];
    for (int i = index; i < size; i++) {
        newString[i - index] = entry[i];
    }
    newString[size - index] = '\0';
    
    return direntv6_dirlookup_core(u, nextInode, newString, strlen(newString));
}

/**
 * @brief get the inode number for the given path (Recursive function)
*/
int direntv6_dirlookup(const struct unix_filesystem *u, uint16_t inr, const char *entry)
{

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(entry);
    return direntv6_dirlookup_core(u, inr, entry, strlen(entry));
    
}

/**
 * @brief separate a path into the parent directory and the relative name
 * @param entry the path to be separeted
 * @param parent (out) writes the parent part of the path
 * @param relativeName (out) writes the relative name part of the path
 * @return 0 if successfull or <0 if there is an error
*/
int separate_path(const char* entry, char* parent, char* relativeName) {
    size_t mark = 0;
    for(size_t i = 0; i < strlen(entry); i++) {
        if (entry[i] == '/') {
            mark = i;
        }
    }
    if (strlen(entry) - mark > DIRENT_MAXLEN) {
        return ERR_FILENAME_TOO_LONG;
    } 
    if (mark == 0) {
        strncpy(relativeName, entry, strlen(entry));
        return 1;
    }
    for(size_t i = 0; i < strlen(entry); i++) {
        if (i <= mark) {
            parent[i] = entry[i];
        } else {
            relativeName[i-mark-1] = entry[i];
        }
    }
    return 0;
}

/**
 * @brief create a new direntv6 with the given name and given mode
*/
int direntv6_create(struct unix_filesystem *u, const char *entry, uint16_t mode) {
    
    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(entry);
    
    char parent[strlen(entry)];
    char relativeName[DIRENT_MAXLEN];
    int checkRelativeName = separate_path(entry, parent, relativeName);
    if (checkRelativeName < 0) {
        return checkRelativeName;
    }
    int dirLookUpCheck = direntv6_dirlookup(u, ROOT_INUMBER, entry);
    if (dirLookUpCheck >= 0) {
        return ERR_FILENAME_ALREADY_EXISTS;
    } else if (dirLookUpCheck != ERR_NO_SUCH_FILE) {
        return dirLookUpCheck;
    }
    dirLookUpCheck = direntv6_dirlookup(u, ROOT_INUMBER, parent);
    if (dirLookUpCheck < 0) {
        return dirLookUpCheck;
    }
    
    struct filev6 fv6;
    int filev6CreateCheck = filev6_create(u, mode, &fv6);
    if (filev6CreateCheck != ERR_NONE) {
        return filev6CreateCheck;
    }
    struct direntv6 dv6;
    dv6.d_inumber = fv6.i_number;
    strncpy(dv6.d_name, relativeName, strlen(relativeName));
    filev6_writebytes(&fv6, &dv6, sizeof(struct direntv6));
    return dv6.d_inumber;
}


/**
 * @brief create a new direntv6 for a file
 */
int direntv6_addfile(struct unix_filesystem *u, const char *entry, uint16_t mode, char *buf, size_t size) {

    M_REQUIRE_NON_NULL(u);
    M_REQUIRE_NON_NULL(entry);
    M_REQUIRE_NON_NULL(buf);

    int inr = direntv6_create(u, entry, mode);

    if (inr < ERR_NONE)
        return inr;

    struct filev6 fv6;

    int fv6openCheck = filev6_open(u, inr, &fv6);

    if (fv6openCheck != ERR_NONE) 
        return fv6openCheck;

    int writeBytesCheck = filev6_writebytes(&fv6, buf, size);

    return writeBytesCheck;

}
