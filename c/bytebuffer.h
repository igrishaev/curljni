#ifndef __BYTEBUFFER_H__
#define __BYTEBUFFER_H__

char * put_byte(char *bb, char value);

char * put_int(char *bb, int value);

char * put_long(char *bb, long value);

char * put_char(char *bb, char *str);

#endif /* __BYTEBUFFER_H__ */
