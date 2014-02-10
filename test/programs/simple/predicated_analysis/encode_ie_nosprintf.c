#define LEADERSZ 1
#define BASE_SZ 2
#define BUFSZ BASE_SZ + LEADERSZ + 3
#define IESZ BUFSZ - LEADERSZ

typedef unsigned int u_int;
typedef unsigned char u_int8_t;
typedef int size_t;

int flag=0;

void *memcpy(void *dest, const void *src, size_t n)
{
  int i;
  unsigned char *s = (unsigned char *)src;
  unsigned char *d = (unsigned char *)dest;

  for (i = n-1; i >= 0; i--) {
    flag = i;
    d[i] = s[i];
  }
  return dest;
}


static u_int
encode_ie(void *buf, size_t bufsize,                  // 8-byte character array
               const u_int8_t *ie, size_t ielen,          // 8-byte uint array
	       const char *leader, size_t leader_len)
{
  /* buf is treated as an array of unsigned 8-byte ints */
  u_int8_t *p;
  int i;
  int index = 0;

  // copy the contents of leader into buf
  if (bufsize < leader_len)
    return 0;
  p = buf;
  memcpy(p, leader, leader_len);
  bufsize -= leader_len;
  index += leader_len;

  for (i = 0; i < ielen && bufsize > 2; i++) {
    /* This was originally
     *    p += sprintf(p, "%02x", ie[i]);
     * This would print two digits from ie[i] into p, and 
     * return the number of bytes written.
     *
     * Simplified to remove sprintf.
     *
     */
    /* OK */
    flag = index;
    p[index] = 'x';
    /* OK */
    flag = index+1;
    p[index+1] = 'x';
    index += 2;
    bufsize -= 2;
  }

  // if we wrote all of ie[], say how many bytes written in total, 
  // otherwise, claim we wrote nothing
  return (i == ielen ? index : 0);
}

int main()
{
  u_int8_t buf [BUFSZ];
  u_int8_t ie [IESZ];
  char leader [LEADERSZ];

  encode_ie (buf, BUFSZ,
             ie, IESZ,
             leader, LEADERSZ);

  return 0;
}

