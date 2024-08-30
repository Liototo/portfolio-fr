/**
 * @file u6fs_fuse.c
 * @brief interface to FUSE (Filesystem in Userspace)
 *
 * @date 2022
 * @author Édouard Bugnion, Ludovic Mermod
 *  Inspired from hello.c from:
 *    FUSE: Filesystem in Userspace
 *    Copyright (C) 2001-2007  Miklos Szeredi <miklos@szeredi.hu>
 *
 *  This program can be distributed under the terms of the GNU GPL.
 */

#define FUSE_USE_VERSION 26

#include <fuse.h>
#include <string.h>
#include <fcntl.h>

#include <stdlib.h> // for exit()
#include "mount.h"
#include "error.h"
#include "inode.h"
#include "direntv6.h"
#include "u6fs_utils.h"
#include "u6fs_fuse.h"
#include "util.h"

static struct unix_filesystem* theFS = NULL; // useful for tests

int fs_getattr(const char *path, struct stat *stbuf)
{

    M_REQUIRE_NON_NULL(path);
    M_REQUIRE_NON_NULL(stbuf);
    M_REQUIRE_NON_NULL(theFS);

    stbuf->st_ino = direntv6_dirlookup(theFS, ROOT_INUMBER, path);
    
    if (stbuf->st_ino < ERR_NONE)
        return stbuf->st_ino;

    struct inode inode;
    
    int inodeReadCheck = inode_read(theFS, stbuf->st_ino, &inode);

    if (inodeReadCheck != ERR_NONE)
        return inodeReadCheck;

    stbuf->st_mode = S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH | (inode.i_mode & IFDIR ? __S_IFDIR : __S_IFREG);
    stbuf->st_nlink = inode.i_nlink;
    stbuf->st_uid = inode.i_uid;
    stbuf->st_gid = inode.i_gid;
    stbuf->st_size = theFS->s.s_fsize;
    stbuf->st_blksize = SECTOR_SIZE;
    stbuf->st_blocks = (stbuf->st_size / stbuf->st_blksize) + 1;
    stbuf->st_dev = 0;
    stbuf->st_rdev = 0;
    stbuf->st_atime = 0;
    stbuf->st_mtime = 0;
    stbuf->st_ctime = 0;


    return ERR_NONE;
}

// Insert directory entries into the directory structure, which is also passed to it as buf
int fs_readdir(const char *path, void *buf, fuse_fill_dir_t filler, off_t offset _unused, struct fuse_file_info *fi)
{
    
    M_REQUIRE_NON_NULL(path);
    M_REQUIRE_NON_NULL(buf);
    M_REQUIRE_NON_NULL(fi);

    int filler1check = filler(buf, ".", NULL, 0);

    if (filler1check != ERR_NONE)
        return ERR_NOMEM;

    int filler2check = filler(buf, "..", NULL, 0);
    
    if (filler2check != ERR_NONE)
        return ERR_NOMEM;

    return ERR_NONE;
}

int fs_read(const char *path, char *buf, size_t size, off_t offset, struct fuse_file_info *fi)
{
    struct filev6* fv6 = malloc(sizeof(struct filev6));

    if (fv6 == NULL) 
        return ERR_NOMEM;

    memset(fv6, 0, sizeof(struct filev6));

    struct inode* inode = malloc(sizeof(struct inode));

    if (inode == NULL) {
        free(fv6);
        fv6 = NULL;
        return ERR_NOMEM;
    }

    int inr = direntv6_dirlookup(theFS, ROOT_INUMBER, path);
    if (inr < ERR_NONE) {
        free(fv6);
        fv6 = NULL;
        free(inode);
        inode = NULL;
        return inr;
    }

    int inodeReadCheck = inode_read(theFS, inr, inode);

    if (inodeReadCheck != ERR_NONE) {
        free(fv6);
        fv6 = NULL;
        free(inode);
        inode = NULL;
        return inodeReadCheck;
    } 

    int32_t fileSize = inode_getsize(inode);

    free(inode);
    inode = NULL;

    if (fileSize > size) {
        int filev6OpenCheck = filev6_open(theFS ,inode->i_addr, fv6);
        if (filev6OpenCheck != ERR_NONE) {
            free(fv6);
            fv6 = NULL;
            return filev6OpenCheck;
        }
        int filelseekCheck = filev6_lseek(fv6, offset);
        if (filelseekCheck != 0) {
            free(fv6);
            fv6 = NULL;
            return filelseekCheck;
        } 
    } 
    int filev6OpenCheck = filev6_open(theFS, inr, fv6);
    if (filev6OpenCheck != ERR_NONE) {
        free(fv6);
        fv6 = NULL;
        return filev6OpenCheck;
    }

    int remaining = 0;
    for (size_t i = 0; i < size/SECTOR_SIZE; i++) {
        remaining = filev6_readblock(fv6, buf + i*size%SECTOR_SIZE);
        if (remaining < ERR_NONE) {
            free(fv6);
            fv6 = NULL;
            return remaining;
        }
    }

    free(fv6);
    fv6 = NULL;

    return remaining;
}

static struct fuse_operations available_ops = {
    .getattr = fs_getattr,
    .readdir = fs_readdir,
    .read    = fs_read,
};

int u6fs_fuse_main(struct unix_filesystem *u, const char *mountpoint)
{
    M_REQUIRE_NON_NULL(mountpoint);

    theFS = u;  // /!\ GLOBAL ASSIGNMENT
    const char *argv[] = {
        "u6fs",
        "-s",               // * `-s` : single threaded operation
        "-f",              // foreground operation (no fork).  alternative "-d" for more debug messages
        "-odirect_io",      //  no caching in the kernel.
#ifdef DEBUG
        "-d",
#endif
        //  "-ononempty",    // unused
        mountpoint
    };
    // very ugly trick when a cast is required to avoid a warning
    void *argv_alias = argv;

    utils_print_superblock(theFS);
    int ret = fuse_main(sizeof(argv) / sizeof(char *), argv_alias, &available_ops, NULL);
    theFS = NULL; // /!\ GLOBAL ASSIGNMENT
    return ret;
}

#ifdef CS212_TEST
void fuse_set_fs(struct unix_filesystem *u)
{
    theFS = u;
}
#endif
