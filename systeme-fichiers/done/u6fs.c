/**
 * @file u6fs.c
 * @brief Command line interface
 *
 * @author Édouard Bugnion, Ludovic Mermod
 * @date 2022
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "error.h"
#include "mount.h"
#include "u6fs_utils.h"
#include "inode.h"
#include "direntv6.h"
#include "u6fs_fuse.h"

/* *************************************************** *
 * TODO WEEK 04-07: Add more messages                  *
 * *************************************************** */
static void usage(const char *execname, int err)
{
    if (err == ERR_INVALID_COMMAND) {
        pps_printf("Available commands:\n");
        pps_printf("%s <disk> sb\n", execname);
        pps_printf("%s <disk> inode\n", execname);
        pps_printf("%s <disk> cat1 <inr>\n", execname);
        pps_printf("%s <disk> shafiles\n", execname); 
        pps_printf("%s <disk> tree\n", execname);
        pps_printf("%s <disk> fuse <mountpoint>\n", execname);
        pps_printf("%s <disk> bm\n", execname);
        pps_printf("%s <disk> mkdir </path/to/newdir>\n", execname);
    } else if (err > ERR_FIRST && err < ERR_LAST) {
        pps_printf("%s: Error: %s\n", execname, ERR_MESSAGES[err - ERR_FIRST]);
    } else {
        pps_printf("%s: Error: %d (UNDEFINED ERROR)\n", execname, err);
    }
}

#define CMD(a, b) (strcmp(argv[2], a) == 0 && argc == (b))

/* *************************************************** *
 * TODO WEEK 04-11: Add more commands                  *
 * *************************************************** */
/**
 * @brief Runs the command requested by the user in the command line, or returns ERR_INVALID_COMMAND if the command is not found.
 *
 * @param argc (int) the number of arguments in the command line
 * @param argv (char*[]) the arguments of the command line, as passed to main()
 */
int u6fs_do_one_cmd(int argc, char *argv[])
{
    if (argc < 3) return ERR_INVALID_COMMAND;

    struct unix_filesystem u = {0};
    int error = mountv6(argv[1], &u), err2 = 0;

    if (error != ERR_NONE) {
        debug_printf("Could not mount fs%s", "\n");
        return error;
    }

    if (CMD("sb", 3)) {

        error = utils_print_superblock(&u);
        
    } else if (CMD("inode", 3)) {
    
        error = inode_scan_print(&u);
    
    } else if (CMD("cat1", 4)) {

        uint16_t convert = (uint16_t) atoi(argv[3]);
        if (convert == 0) {
            if (argv[3][0] == '0') {
                error = utils_cat_first_sector(&u, convert);
            } else {
                error = ERR_INVALID_COMMAND;
            }
        } else {
            error = utils_cat_first_sector(&u, convert);
        }
        
    } else if (CMD("shafiles", 3)) {

        error = utils_print_sha_allfiles(&u);

    }  else if (CMD("tree", 3)) {

        error = direntv6_print_tree(&u, ROOT_INUMBER, "");
    
    } else if (CMD("fuse", 4)) {
        
        error = u6fs_fuse_main(&u, argv[3]);

    } else if (CMD("bm", 3)) {

        error = utils_print_bitmaps(&u);

    } else if (CMD("mkdir", 4)) {

        error = direntv6_create(&u, argv[3], IWRITE); // mode might be wrong
        
    } else {

        error = ERR_INVALID_COMMAND;

    }

    err2 = umountv6(&u);
    return (error == ERR_NONE ? err2 : error);
}

#ifndef FUZZ
/**
 * @brief main function, runs the requested command and prints the resulting error if any.
 */
int main(int argc, char *argv[])
{
    int ret = u6fs_do_one_cmd(argc, argv);
    if (ret != ERR_NONE) {
        usage(argv[0], ret);
    }
    return ret;
}
#endif
