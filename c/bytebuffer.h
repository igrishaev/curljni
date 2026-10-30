#ifndef __BYTEBUFFER_H__
#define __BYTEBUFFER_H__

char * bb_put_byte(char *bb, char value);

char * bb_put_int(char *bb, int value);

char * bb_put_long(char *bb, long value);

char * bb_put_char(char *bb, char *str);

#endif /* __BYTEBUFFER_H__ */
