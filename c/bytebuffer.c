#include <string.h>
#include "bytebuffer.h"

char * bb_put_byte(char *bb, char value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * bb_put_int(char *bb, int value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * bb_put_long(char *bb, long value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * bb_put_char(char *bb, char *str) {
    return stpcpy(bb, str);
}
