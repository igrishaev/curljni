#include <string.h>
#include "bytebuffer.h"


// TODO rename

char * put_byte(char *bb, char value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * put_int(char *bb, int value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * put_long(char *bb, long value) {
    memcpy(bb, &value, sizeof value);
    return bb += sizeof value;
}

char * put_char(char *bb, char *str) {
    return stpcpy(bb, str);
}
