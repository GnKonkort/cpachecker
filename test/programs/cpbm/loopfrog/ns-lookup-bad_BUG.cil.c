/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 31 "/usr/include/bits/types.h"
typedef unsigned char __u_char;
#line 141 "/usr/include/bits/types.h"
typedef long __off_t;
#line 142 "/usr/include/bits/types.h"
typedef long __off64_t;
#line 143 "/usr/include/bits/types.h"
typedef int __pid_t;
#line 149 "/usr/include/bits/types.h"
typedef long __time_t;
#line 35 "/usr/include/sys/types.h"
typedef __u_char u_char;
#line 100 "/usr/include/sys/types.h"
typedef __pid_t pid_t;
#line 76 "/usr/include/time.h"
typedef __time_t time_t;
#line 211 "/usr/lib/gcc/x86_64-linux-gnu/4.4.3/include/stddef.h"
typedef unsigned long size_t;
#line 196 "/usr/include/sys/types.h"
typedef short int16_t;
#line 202 "/usr/include/sys/types.h"
typedef unsigned short u_int16_t;
#line 203 "/usr/include/sys/types.h"
typedef unsigned int u_int32_t;
#line 52 "/usr/include/stdint.h"
typedef unsigned int uint32_t;
#line 141 "/usr/include/netinet/in.h"
typedef uint32_t in_addr_t;
#line 142 "/usr/include/netinet/in.h"
struct in_addr {
   in_addr_t s_addr ;
};
#line 45 "/usr/include/stdio.h"
struct _IO_FILE;
#line 45
struct _IO_FILE;
#line 49 "/usr/include/stdio.h"
typedef struct _IO_FILE FILE;
#line 170 "/usr/include/libio.h"
struct _IO_FILE;
#line 180 "/usr/include/libio.h"
typedef void _IO_lock_t;
#line 186 "/usr/include/libio.h"
struct _IO_marker {
   struct _IO_marker *_next ;
   struct _IO_FILE *_sbuf ;
   int _pos ;
};
#line 271 "/usr/include/libio.h"
struct _IO_FILE {
   int _flags ;
   char *_IO_read_ptr ;
   char *_IO_read_end ;
   char *_IO_read_base ;
   char *_IO_write_base ;
   char *_IO_write_ptr ;
   char *_IO_write_end ;
   char *_IO_buf_base ;
   char *_IO_buf_end ;
   char *_IO_save_base ;
   char *_IO_backup_base ;
   char *_IO_save_end ;
   struct _IO_marker *_markers ;
   struct _IO_FILE *_chain ;
   int _fileno ;
   int _flags2 ;
   __off_t _old_offset ;
   unsigned short _cur_column ;
   signed char _vtable_offset ;
   char _shortbuf[1] ;
   _IO_lock_t *_lock ;
   __off64_t _offset ;
   void *__pad1 ;
   void *__pad2 ;
   void *__pad3 ;
   void *__pad4 ;
   size_t __pad5 ;
   int _mode ;
   char _unused2[(15UL * sizeof(int ) - 4UL * sizeof(void *)) - sizeof(size_t )] ;
};
#line 341 "/usr/include/libio.h"
typedef struct _IO_FILE _IO_FILE;
#line 75 "my-named.h"
struct databuf {
   struct databuf *d_next ;
   u_int32_t d_ttl ;
   unsigned int d_flags : 7 ;
   unsigned int d_cred : 3 ;
   unsigned int d_clev : 6 ;
   int16_t d_zone ;
   int16_t d_class ;
   int16_t d_type ;
   int16_t d_mark ;
   int16_t d_size ;
   int16_t d_rcnt ;
   u_int32_t d_nstime ;
   u_char d_data[sizeof(char *)] ;
};
#line 114 "my-named.h"
struct zoneinfo {
   char *z_origin ;
   time_t z_time ;
   time_t z_lastupdate ;
   u_int32_t z_refresh ;
   u_int32_t z_retry ;
   u_int32_t z_expire ;
   u_int32_t z_minimum ;
   u_int32_t z_serial ;
   char *z_source ;
   time_t z_ftime ;
   struct in_addr z_xaddr ;
   struct in_addr z_addr[16] ;
   u_char z_addrcnt ;
   u_char z_type ;
   u_int16_t z_flags ;
   pid_t z_xferpid ;
   int z_class ;
};
#line 152 "my-named.h"
struct namebuf {
   char *n_dname ;
   struct databuf *n_data ;
};
#line 365 "/usr/include/netinet/in.h"
extern  __attribute__((__nothrow__)) uint32_t ntohl(uint32_t __netlong )  __attribute__((__const__)) ;
#line 54 "/usr/include/arpa/inet.h"
extern  __attribute__((__nothrow__)) char *inet_ntoa(in_addr_t __ins_addr ) ;
#line 190 "/usr/include/sys/syslog.h"
extern void ( /* format attribute */  syslog)(int __pri , char const   *__fmt  , ...) ;
#line 460 "/usr/include/libio.h"
extern int _IO_getc(_IO_FILE *__fp ) ;
#line 214 "/usr/include/stdio.h"
extern int fclose(FILE *__stream ) ;
#line 249
extern FILE *fopen(char const   * __restrict  __filename , char const   * __restrict  __modes ) ;
#line 339
extern int printf(char const   * __restrict  __format  , ...) ;
#line 341
extern  __attribute__((__nothrow__)) int sprintf(char * __restrict  __s , char const   * __restrict  __format 
                                                 , ...) ;
#line 604
extern char *fgets(char * __restrict  __s , int __n , FILE * __restrict  __stream ) ;
#line 471 "/usr/include/stdlib.h"
extern  __attribute__((__nothrow__)) void *malloc(size_t __size )  __attribute__((__malloc__)) ;
#line 473
extern  __attribute__((__nothrow__)) void *calloc(size_t __nmemb , size_t __size )  __attribute__((__malloc__)) ;
#line 397 "/usr/include/string.h"
extern  __attribute__((__nothrow__)) size_t strlen(char const   *__s )  __attribute__((__pure__,
__nonnull__(1))) ;
#line 453
extern  __attribute__((__nothrow__)) void bcopy(void const   *__src , void *__dest ,
                                                size_t __n )  __attribute__((__nonnull__(1,2))) ;
#line 71 "/usr/include/assert.h"
extern  __attribute__((__nothrow__, __noreturn__)) void __assert_fail(char const   *__assertion ,
                                                                      char const   *__file ,
                                                                      unsigned int __line ,
                                                                      char const   *__function ) ;
#line 85 "ns-lookup-bad.c"
struct zoneinfo *zones  =    (struct zoneinfo *)((void *)0);
#line 87
struct in_addr data_inaddr(u_char const   *data ) ;
#line 88
struct namebuf *nlookup(u_char *dname ) ;
#line 89
struct databuf **create_databuf_list(int num ) ;
#line 101 "ns-lookup-bad.c"
static void nslookupComplain(char const   *sysloginfo , char const   *queryname ,
                             char const   *complaint , char const   *dname , struct databuf  const  *a_rr ,
                             struct databuf  const  *nsdp ) 
{ char *a ;
  char *ns ;
  char buf[999] ;
  struct in_addr tmp ;
  char *tmp___0 ;
  char const   *tmp___1 ;
  char const   *tmp___2 ;
  struct in_addr tmp___3 ;
  char *tmp___4 ;
  struct in_addr tmp___5 ;
  char *tmp___6 ;
  struct in_addr tmp___7 ;
  char *tmp___8 ;
  size_t tmp___9 ;
  char const   * __restrict  __cil_tmp21 ;
  void *__cil_tmp22 ;
  void *__cil_tmp23 ;
  unsigned long __cil_tmp24 ;
  unsigned long __cil_tmp25 ;
  void *__cil_tmp26 ;
  unsigned long __cil_tmp27 ;
  unsigned long __cil_tmp28 ;
  char const   * __restrict  __cil_tmp29 ;
  unsigned long __cil_tmp30 ;
  unsigned long __cil_tmp31 ;
  unsigned long __cil_tmp32 ;
  unsigned long __cil_tmp33 ;
  u_char const   *__cil_tmp34 ;
  u_char const   *__cil_tmp35 ;
  char const   * __restrict  __cil_tmp36 ;
  unsigned long __cil_tmp37 ;
  unsigned long __cil_tmp38 ;
  unsigned long __cil_tmp39 ;
  unsigned long __cil_tmp40 ;
  u_char const   *__cil_tmp41 ;
  u_char const   *__cil_tmp42 ;
  unsigned long __cil_tmp43 ;
  unsigned long __cil_tmp44 ;
  char *__cil_tmp45 ;
  char * __restrict  __cil_tmp46 ;
  char const   * __restrict  __cil_tmp47 ;
  char const   * __restrict  __cil_tmp48 ;
  unsigned long __cil_tmp49 ;
  unsigned long __cil_tmp50 ;
  unsigned long __cil_tmp51 ;
  unsigned long __cil_tmp52 ;
  u_char const   *__cil_tmp53 ;
  u_char const   *__cil_tmp54 ;
  char const   * __restrict  __cil_tmp55 ;
  unsigned long __cil_tmp56 ;
  unsigned long __cil_tmp57 ;
  unsigned long __cil_tmp58 ;
  unsigned long __cil_tmp59 ;
  u_char const   *__cil_tmp60 ;
  u_char const   *__cil_tmp61 ;
  unsigned long __cil_tmp62 ;
  unsigned long __cil_tmp63 ;
  char *__cil_tmp64 ;
  char * __restrict  __cil_tmp65 ;
  char const   * __restrict  __cil_tmp66 ;
  unsigned long __cil_tmp67 ;
  unsigned long __cil_tmp68 ;
  char *__cil_tmp69 ;
  char const   *__cil_tmp70 ;
  unsigned long __cil_tmp71 ;
  unsigned long __cil_tmp72 ;
  char *__cil_tmp73 ;
  char const   *__cil_tmp74 ;
  char const   * __restrict  __cil_tmp75 ;

  {
  {
#line 112
  __cil_tmp21 = (char const   * __restrict  )"NS \'%s\' %s\n";
#line 112
  printf(__cil_tmp21, dname, complaint);
  }
#line 114
  if (sysloginfo) {
#line 114
    if (queryname) {
#line 118
      __cil_tmp22 = (void *)0;
#line 118
      ns = (char *)__cil_tmp22;
#line 118
      a = ns;
      {
#line 136
      __cil_tmp23 = (void *)0;
#line 136
      __cil_tmp24 = (unsigned long )__cil_tmp23;
#line 136
      __cil_tmp25 = (unsigned long )a;
#line 136
      if (__cil_tmp25 != __cil_tmp24) {
#line 136
        goto _L;
      } else {
        {
#line 136
        __cil_tmp26 = (void *)0;
#line 136
        __cil_tmp27 = (unsigned long )__cil_tmp26;
#line 136
        __cil_tmp28 = (unsigned long )ns;
#line 136
        if (__cil_tmp28 != __cil_tmp27) {
          _L: /* CIL Label */ 
          {
#line 138
          __cil_tmp29 = (char const   * __restrict  )"Calling sprintf!\n";
#line 138
          printf(__cil_tmp29);
#line 139
          __cil_tmp30 = 0 * 1UL;
#line 139
          __cil_tmp31 = 32 + __cil_tmp30;
#line 139
          __cil_tmp32 = (unsigned long )a_rr;
#line 139
          __cil_tmp33 = __cil_tmp32 + __cil_tmp31;
#line 139
          __cil_tmp34 = (u_char const   *)__cil_tmp33;
#line 139
          __cil_tmp35 = (u_char const   *)__cil_tmp34;
#line 139
          tmp = data_inaddr(__cil_tmp35);
#line 139
          tmp___0 = inet_ntoa(tmp.s_addr);
#line 139
          __cil_tmp36 = (char const   * __restrict  )"sprintf args: %s: query(%s) %s (%s:%s)";
#line 139
          printf(__cil_tmp36, sysloginfo, queryname, complaint, dname, tmp___0);
          }
#line 146
          if (ns) {
#line 146
            tmp___1 = (char const   *)ns;
          } else {
#line 146
            tmp___1 = "<Not Available>";
          }
#line 146
          if (a) {
#line 146
            tmp___2 = (char const   *)a;
          } else {
#line 146
            tmp___2 = "<Not Available>";
          }
          {
#line 146
          __cil_tmp37 = 0 * 1UL;
#line 146
          __cil_tmp38 = 32 + __cil_tmp37;
#line 146
          __cil_tmp39 = (unsigned long )a_rr;
#line 146
          __cil_tmp40 = __cil_tmp39 + __cil_tmp38;
#line 146
          __cil_tmp41 = (u_char const   *)__cil_tmp40;
#line 146
          __cil_tmp42 = (u_char const   *)__cil_tmp41;
#line 146
          tmp___3 = data_inaddr(__cil_tmp42);
#line 146
          tmp___4 = inet_ntoa(tmp___3.s_addr);
#line 146
          __cil_tmp43 = 0 * 1UL;
#line 146
          __cil_tmp44 = (unsigned long )(buf) + __cil_tmp43;
#line 146
          __cil_tmp45 = (char *)__cil_tmp44;
#line 146
          __cil_tmp46 = (char * __restrict  )__cil_tmp45;
#line 146
          __cil_tmp47 = (char const   * __restrict  )"%s: query(%s) %s (%s:%s) learnt (A=%s:NS=%s)";
#line 146
          sprintf(__cil_tmp46, __cil_tmp47, sysloginfo, queryname, complaint, dname,
                  tmp___4, tmp___2, tmp___1);
          }
        } else {
          {
#line 155
          __cil_tmp48 = (char const   * __restrict  )"Calling sprintf!\n";
#line 155
          printf(__cil_tmp48);
#line 156
          __cil_tmp49 = 0 * 1UL;
#line 156
          __cil_tmp50 = 32 + __cil_tmp49;
#line 156
          __cil_tmp51 = (unsigned long )a_rr;
#line 156
          __cil_tmp52 = __cil_tmp51 + __cil_tmp50;
#line 156
          __cil_tmp53 = (u_char const   *)__cil_tmp52;
#line 156
          __cil_tmp54 = (u_char const   *)__cil_tmp53;
#line 156
          tmp___5 = data_inaddr(__cil_tmp54);
#line 156
          tmp___6 = inet_ntoa(tmp___5.s_addr);
#line 156
          __cil_tmp55 = (char const   * __restrict  )"sprintf args: %s: query(%s) %s (%s:%s)";
#line 156
          printf(__cil_tmp55, sysloginfo, queryname, complaint, dname, tmp___6);
#line 162
          __cil_tmp56 = 0 * 1UL;
#line 162
          __cil_tmp57 = 32 + __cil_tmp56;
#line 162
          __cil_tmp58 = (unsigned long )a_rr;
#line 162
          __cil_tmp59 = __cil_tmp58 + __cil_tmp57;
#line 162
          __cil_tmp60 = (u_char const   *)__cil_tmp59;
#line 162
          __cil_tmp61 = (u_char const   *)__cil_tmp60;
#line 162
          tmp___7 = data_inaddr(__cil_tmp61);
#line 162
          tmp___8 = inet_ntoa(tmp___7.s_addr);
#line 162
          __cil_tmp62 = 0 * 1UL;
#line 162
          __cil_tmp63 = (unsigned long )(buf) + __cil_tmp62;
#line 162
          __cil_tmp64 = (char *)__cil_tmp63;
#line 162
          __cil_tmp65 = (char * __restrict  )__cil_tmp64;
#line 162
          __cil_tmp66 = (char const   * __restrict  )"%s: query(%s) %s (%s:%s)";
#line 162
          sprintf(__cil_tmp65, __cil_tmp66, sysloginfo, queryname, complaint, dname,
                  tmp___8);
#line 166
          __cil_tmp67 = 0 * 1UL;
#line 166
          __cil_tmp68 = (unsigned long )(buf) + __cil_tmp67;
#line 166
          __cil_tmp69 = (char *)__cil_tmp68;
#line 166
          __cil_tmp70 = (char const   *)__cil_tmp69;
#line 166
          syslog(6, __cil_tmp70);
#line 167
          __cil_tmp71 = 0 * 1UL;
#line 167
          __cil_tmp72 = (unsigned long )(buf) + __cil_tmp71;
#line 167
          __cil_tmp73 = (char *)__cil_tmp72;
#line 167
          __cil_tmp74 = (char const   *)__cil_tmp73;
#line 167
          tmp___9 = strlen(__cil_tmp74);
#line 167
          __cil_tmp75 = (char const   * __restrict  )"strlen(buf) = %d\n";
#line 167
          printf(__cil_tmp75, tmp___9);
          }
        }
        }
      }
      }
    } else {

    }
  } else {

  }
#line 170
  return;
}
}
#line 213 "ns-lookup-bad.c"
static struct in_addr nsa  ;
#line 250
int nslookup(struct databuf **nsp , char const   *syslogdname , char const   *sysloginfo ) ;
#line 250 "ns-lookup-bad.c"
static char *complaint  =    (char *)"Bogus (0.0.0.0) A RR";
#line 259
int nslookup(struct databuf **nsp , char const   *syslogdname , char const   *sysloginfo ) ;
#line 259 "ns-lookup-bad.c"
static char *complaint___0  =    (char *)"Bogus LOOPBACK A RR";
#line 268
int nslookup(struct databuf **nsp , char const   *syslogdname , char const   *sysloginfo ) ;
#line 268 "ns-lookup-bad.c"
static char *complaint___1  =    (char *)"Bogus BROADCAST A RR";
#line 204 "ns-lookup-bad.c"
int nslookup(struct databuf **nsp , char const   *syslogdname , char const   *sysloginfo ) 
{ register struct namebuf *np ;
  register struct databuf *dp ;
  register struct databuf *nsdp ;
  u_char *dname ;
  int i ;
  int class ;
  int found_arr ;
  struct namebuf *tmp ;
  struct in_addr tmp___0 ;
  struct in_addr tmp___1 ;
  uint32_t tmp___2 ;
  struct in_addr tmp___3 ;
  uint32_t tmp___4 ;
  char const   * __restrict  __cil_tmp17 ;
  char const   * __restrict  __cil_tmp18 ;
  char const   * __restrict  __cil_tmp19 ;
  struct databuf **__cil_tmp20 ;
  unsigned long __cil_tmp21 ;
  unsigned long __cil_tmp22 ;
  int16_t __cil_tmp23 ;
  char const   * __restrict  __cil_tmp24 ;
  char const   * __restrict  __cil_tmp25 ;
  unsigned long __cil_tmp26 ;
  unsigned long __cil_tmp27 ;
  unsigned long __cil_tmp28 ;
  unsigned long __cil_tmp29 ;
  unsigned long __cil_tmp30 ;
  unsigned long __cil_tmp31 ;
  void *__cil_tmp32 ;
  unsigned long __cil_tmp33 ;
  unsigned long __cil_tmp34 ;
  char const   * __restrict  __cil_tmp35 ;
  char const   * __restrict  __cil_tmp36 ;
  unsigned long __cil_tmp37 ;
  unsigned long __cil_tmp38 ;
  int16_t __cil_tmp39 ;
  int __cil_tmp40 ;
  unsigned long __cil_tmp41 ;
  unsigned long __cil_tmp42 ;
  int16_t __cil_tmp43 ;
  int __cil_tmp44 ;
  unsigned long __cil_tmp45 ;
  unsigned long __cil_tmp46 ;
  int16_t __cil_tmp47 ;
  int __cil_tmp48 ;
  unsigned long __cil_tmp49 ;
  unsigned long __cil_tmp50 ;
  int16_t __cil_tmp51 ;
  int __cil_tmp52 ;
  unsigned long __cil_tmp53 ;
  unsigned long __cil_tmp54 ;
  int16_t __cil_tmp55 ;
  int __cil_tmp56 ;
  unsigned long __cil_tmp57 ;
  unsigned long __cil_tmp58 ;
  unsigned long __cil_tmp59 ;
  unsigned long __cil_tmp60 ;
  u_char *__cil_tmp61 ;
  u_char const   *__cil_tmp62 ;
  char const   * __restrict  __cil_tmp63 ;
  char const   *__cil_tmp64 ;
  char const   *__cil_tmp65 ;
  struct databuf  const  *__cil_tmp66 ;
  struct databuf  const  *__cil_tmp67 ;
  unsigned long __cil_tmp68 ;
  unsigned long __cil_tmp69 ;
  unsigned long __cil_tmp70 ;
  unsigned long __cil_tmp71 ;
  u_char *__cil_tmp72 ;
  u_char const   *__cil_tmp73 ;
  char const   *__cil_tmp74 ;
  char const   *__cil_tmp75 ;
  struct databuf  const  *__cil_tmp76 ;
  struct databuf  const  *__cil_tmp77 ;
  unsigned long __cil_tmp78 ;
  unsigned long __cil_tmp79 ;
  unsigned long __cil_tmp80 ;
  unsigned long __cil_tmp81 ;
  u_char *__cil_tmp82 ;
  u_char const   *__cil_tmp83 ;
  char const   *__cil_tmp84 ;
  char const   *__cil_tmp85 ;
  struct databuf  const  *__cil_tmp86 ;
  struct databuf  const  *__cil_tmp87 ;
  unsigned long __cil_tmp88 ;
  unsigned long __cil_tmp89 ;
  unsigned long __cil_tmp90 ;
  unsigned long __cil_tmp91 ;
  u_char *__cil_tmp92 ;
  u_char const   *__cil_tmp93 ;

  {
  {
#line 212
  found_arr = 0;
#line 215
  __cil_tmp17 = (char const   * __restrict  )"syslogdname = %s\n";
#line 215
  printf(__cil_tmp17, syslogdname);
#line 216
  __cil_tmp18 = (char const   * __restrict  )"sysloginfo = %s\n";
#line 216
  printf(__cil_tmp18, sysloginfo);
#line 220
  i = 0;
  }
  {
#line 220
  while (1) {
    while_continue: /* CIL Label */ ;
#line 220
    if (i < 2) {

    } else {
#line 220
      goto while_break;
    }
    {
#line 221
    __cil_tmp19 = (char const   * __restrict  )"i=%d\n";
#line 221
    printf(__cil_tmp19, i);
#line 222
    __cil_tmp20 = nsp + i;
#line 222
    nsdp = *__cil_tmp20;
#line 224
    __cil_tmp21 = (unsigned long )nsdp;
#line 224
    __cil_tmp22 = __cil_tmp21 + 16;
#line 224
    __cil_tmp23 = *((int16_t *)__cil_tmp22);
#line 224
    class = (int )__cil_tmp23;
#line 226
    __cil_tmp24 = (char const   * __restrict  )"Class = %d\n";
#line 226
    printf(__cil_tmp24, class);
#line 228
    __cil_tmp25 = (char const   * __restrict  )"C_IN = %d, class = %d\n";
#line 228
    printf(__cil_tmp25, 1, class);
#line 229
    __cil_tmp26 = 0 * 1UL;
#line 229
    __cil_tmp27 = 32 + __cil_tmp26;
#line 229
    __cil_tmp28 = (unsigned long )nsdp;
#line 229
    __cil_tmp29 = __cil_tmp28 + __cil_tmp27;
#line 229
    dname = (u_char *)__cil_tmp29;
#line 231
    tmp = nlookup(dname);
#line 231
    np = tmp;
#line 236
    __cil_tmp30 = (unsigned long )np;
#line 236
    __cil_tmp31 = __cil_tmp30 + 8;
#line 236
    dp = *((struct databuf **)__cil_tmp31);
    }
    {
#line 236
    while (1) {
      while_continue___0: /* CIL Label */ ;
      {
#line 236
      __cil_tmp32 = (void *)0;
#line 236
      __cil_tmp33 = (unsigned long )__cil_tmp32;
#line 236
      __cil_tmp34 = (unsigned long )dp;
#line 236
      if (__cil_tmp34 != __cil_tmp33) {

      } else {
#line 236
        goto while_break___0;
      }
      }
      {
#line 237
      __cil_tmp35 = (char const   * __restrict  )"We\'re inside for loop!\n";
#line 237
      printf(__cil_tmp35);
#line 243
      __cil_tmp36 = (char const   * __restrict  )"T_A = %d, dp->d_type = %d\n";
#line 243
      __cil_tmp37 = (unsigned long )dp;
#line 243
      __cil_tmp38 = __cil_tmp37 + 18;
#line 243
      __cil_tmp39 = *((int16_t *)__cil_tmp38);
#line 243
      __cil_tmp40 = (int )__cil_tmp39;
#line 243
      printf(__cil_tmp36, 1, __cil_tmp40);
      }
      {
#line 245
      __cil_tmp41 = (unsigned long )dp;
#line 245
      __cil_tmp42 = __cil_tmp41 + 18;
#line 245
      __cil_tmp43 = *((int16_t *)__cil_tmp42);
#line 245
      __cil_tmp44 = (int )__cil_tmp43;
#line 245
      if (__cil_tmp44 == 5) {
        {
#line 245
        __cil_tmp45 = (unsigned long )dp;
#line 245
        __cil_tmp46 = __cil_tmp45 + 16;
#line 245
        __cil_tmp47 = *((int16_t *)__cil_tmp46);
#line 245
        __cil_tmp48 = (int )__cil_tmp47;
#line 245
        if (__cil_tmp48 == class) {
#line 246
          goto finish;
        } else {

        }
        }
      } else {

      }
      }
      {
#line 247
      __cil_tmp49 = (unsigned long )dp;
#line 247
      __cil_tmp50 = __cil_tmp49 + 18;
#line 247
      __cil_tmp51 = *((int16_t *)__cil_tmp50);
#line 247
      __cil_tmp52 = (int )__cil_tmp51;
#line 247
      if (__cil_tmp52 != 1) {
#line 248
        goto __Cont;
      } else {
        {
#line 247
        __cil_tmp53 = (unsigned long )dp;
#line 247
        __cil_tmp54 = __cil_tmp53 + 16;
#line 247
        __cil_tmp55 = *((int16_t *)__cil_tmp54);
#line 247
        __cil_tmp56 = (int )__cil_tmp55;
#line 247
        if (__cil_tmp56 != class) {
#line 248
          goto __Cont;
        } else {

        }
        }
      }
      }
      {
#line 249
      __cil_tmp57 = 0 * 1UL;
#line 249
      __cil_tmp58 = 32 + __cil_tmp57;
#line 249
      __cil_tmp59 = (unsigned long )dp;
#line 249
      __cil_tmp60 = __cil_tmp59 + __cil_tmp58;
#line 249
      __cil_tmp61 = (u_char *)__cil_tmp60;
#line 249
      __cil_tmp62 = (u_char const   *)__cil_tmp61;
#line 249
      tmp___0 = data_inaddr(__cil_tmp62);
      }
#line 249
      if (tmp___0.s_addr == 0U) {
        {
#line 251
        __cil_tmp63 = (char const   * __restrict  )"Calling nslookupComplain!\n";
#line 251
        printf(__cil_tmp63);
#line 252
        __cil_tmp64 = (char const   *)complaint;
#line 252
        __cil_tmp65 = (char const   *)dname;
#line 252
        __cil_tmp66 = (struct databuf  const  *)dp;
#line 252
        __cil_tmp67 = (struct databuf  const  *)nsdp;
#line 252
        nslookupComplain(sysloginfo, syslogdname, __cil_tmp64, __cil_tmp65, __cil_tmp66,
                         __cil_tmp67);
        }
#line 254
        goto __Cont;
      } else {

      }
      {
#line 257
      __cil_tmp68 = 0 * 1UL;
#line 257
      __cil_tmp69 = 32 + __cil_tmp68;
#line 257
      __cil_tmp70 = (unsigned long )dp;
#line 257
      __cil_tmp71 = __cil_tmp70 + __cil_tmp69;
#line 257
      __cil_tmp72 = (u_char *)__cil_tmp71;
#line 257
      __cil_tmp73 = (u_char const   *)__cil_tmp72;
#line 257
      tmp___1 = data_inaddr(__cil_tmp73);
#line 257
      tmp___2 = ntohl(tmp___1.s_addr);
      }
#line 257
      if (tmp___2 == 2130706433U) {
        {
#line 260
        __cil_tmp74 = (char const   *)complaint___0;
#line 260
        __cil_tmp75 = (char const   *)dname;
#line 260
        __cil_tmp76 = (struct databuf  const  *)dp;
#line 260
        __cil_tmp77 = (struct databuf  const  *)nsdp;
#line 260
        nslookupComplain(sysloginfo, syslogdname, __cil_tmp74, __cil_tmp75, __cil_tmp76,
                         __cil_tmp77);
        }
#line 262
        goto __Cont;
      } else {

      }
      {
#line 266
      __cil_tmp78 = 0 * 1UL;
#line 266
      __cil_tmp79 = 32 + __cil_tmp78;
#line 266
      __cil_tmp80 = (unsigned long )dp;
#line 266
      __cil_tmp81 = __cil_tmp80 + __cil_tmp79;
#line 266
      __cil_tmp82 = (u_char *)__cil_tmp81;
#line 266
      __cil_tmp83 = (u_char const   *)__cil_tmp82;
#line 266
      tmp___3 = data_inaddr(__cil_tmp83);
#line 266
      tmp___4 = ntohl(tmp___3.s_addr);
      }
#line 266
      if (tmp___4 == 4294967295U) {
        {
#line 269
        __cil_tmp84 = (char const   *)complaint___1;
#line 269
        __cil_tmp85 = (char const   *)dname;
#line 269
        __cil_tmp86 = (struct databuf  const  *)dp;
#line 269
        __cil_tmp87 = (struct databuf  const  *)nsdp;
#line 269
        nslookupComplain(sysloginfo, syslogdname, __cil_tmp84, __cil_tmp85, __cil_tmp86,
                         __cil_tmp87);
        }
#line 271
        goto __Cont;
      } else {

      }
      {
#line 274
      found_arr = found_arr + 1;
#line 275
      __cil_tmp88 = 0 * 1UL;
#line 275
      __cil_tmp89 = 32 + __cil_tmp88;
#line 275
      __cil_tmp90 = (unsigned long )dp;
#line 275
      __cil_tmp91 = __cil_tmp90 + __cil_tmp89;
#line 275
      __cil_tmp92 = (u_char *)__cil_tmp91;
#line 275
      __cil_tmp93 = (u_char const   *)__cil_tmp92;
#line 275
      nsa = data_inaddr(__cil_tmp93);
      }
      __Cont: /* CIL Label */ 
#line 236
      dp = *((struct databuf **)dp);
    }
    while_break___0: /* CIL Label */ ;
    }
#line 278
    nsp = nsp + 1;
#line 220
    i = i + 1;
  }
  while_break: /* CIL Label */ ;
  }
  finish: 
#line 281
  return (found_arr);
}
}
#line 288 "ns-lookup-bad.c"
struct namebuf *nlookup(u_char *dname ) 
{ struct databuf **double_nb ;
  struct namebuf *nbuf ;
  void *tmp ;
  struct databuf **tmp___0 ;
  struct databuf **tmp___1 ;
  size_t __cil_tmp7 ;
  unsigned long __cil_tmp8 ;
  unsigned long __cil_tmp9 ;

  {
  {
#line 290
  __cil_tmp7 = (size_t )1;
#line 290
  tmp = calloc(__cil_tmp7, 16UL);
#line 290
  nbuf = (struct namebuf *)tmp;
#line 292
  tmp___0 = create_databuf_list(1);
#line 292
  double_nb = tmp___0;
#line 293
  tmp___1 = double_nb;
#line 293
  double_nb = double_nb + 1;
#line 293
  __cil_tmp8 = (unsigned long )nbuf;
#line 293
  __cil_tmp9 = __cil_tmp8 + 8;
#line 293
  *((struct databuf **)__cil_tmp9) = *tmp___1;
  }
#line 295
  return (nbuf);
}
}
#line 304 "ns-lookup-bad.c"
struct in_addr data_inaddr(u_char const   *data ) 
{ struct in_addr ret ;
  u_int32_t tmp ;
  char *__cil_tmp4 ;
  void const   *__cil_tmp5 ;
  char *__cil_tmp6 ;
  void *__cil_tmp7 ;
  size_t __cil_tmp8 ;
  u_int32_t *__cil_tmp9 ;

  {
  {
#line 311
  __cil_tmp4 = (char *)data;
#line 311
  __cil_tmp5 = (void const   *)__cil_tmp4;
#line 311
  __cil_tmp6 = (char *)(& tmp);
#line 311
  __cil_tmp7 = (void *)__cil_tmp6;
#line 311
  __cil_tmp8 = (size_t )4;
#line 311
  bcopy(__cil_tmp5, __cil_tmp7, __cil_tmp8);
#line 313
  __cil_tmp9 = & tmp;
#line 313
  ret.s_addr = *__cil_tmp9;
  }
#line 315
  return (ret);
}
}
#line 324 "ns-lookup-bad.c"
struct databuf **create_databuf_list(int num ) 
{ struct databuf **dbl ;
  struct databuf **temp ;
  int i ;
  FILE *f ;
  void *tmp ;
  void *tmp___0 ;
  int tmp___1 ;
  int tmp___2 ;
  int tmp___3 ;
  int tmp___4 ;
  unsigned long __cil_tmp12 ;
  unsigned long __cil_tmp13 ;
  char const   * __restrict  __cil_tmp14 ;
  char const   * __restrict  __cil_tmp15 ;
  size_t __cil_tmp16 ;
  unsigned long __cil_tmp17 ;
  unsigned long __cil_tmp18 ;
  struct databuf *__cil_tmp19 ;
  unsigned long __cil_tmp20 ;
  unsigned long __cil_tmp21 ;
  unsigned long __cil_tmp22 ;
  unsigned long __cil_tmp23 ;
  struct databuf *__cil_tmp24 ;
  unsigned long __cil_tmp25 ;
  unsigned long __cil_tmp26 ;
  unsigned long __cil_tmp27 ;
  unsigned long __cil_tmp28 ;
  struct databuf *__cil_tmp29 ;
  unsigned long __cil_tmp30 ;
  unsigned long __cil_tmp31 ;
  unsigned long __cil_tmp32 ;
  unsigned long __cil_tmp33 ;
  struct databuf *__cil_tmp34 ;
  unsigned long __cil_tmp35 ;
  unsigned long __cil_tmp36 ;
  struct databuf *__cil_tmp37 ;
  unsigned long __cil_tmp38 ;
  unsigned long __cil_tmp39 ;
  struct databuf *__cil_tmp40 ;
  unsigned long __cil_tmp41 ;
  unsigned long __cil_tmp42 ;
  struct databuf *__cil_tmp43 ;
  void *__cil_tmp44 ;

  {
  {
#line 330
  __cil_tmp12 = (unsigned long )num;
#line 330
  __cil_tmp13 = __cil_tmp12 * 8UL;
#line 330
  tmp = malloc(__cil_tmp13);
#line 330
  dbl = (struct databuf **)tmp;
#line 331
  temp = dbl;
#line 333
  __cil_tmp14 = (char const   * __restrict  )"address_file";
#line 333
  __cil_tmp15 = (char const   * __restrict  )"r";
#line 333
  f = fopen(__cil_tmp14, __cil_tmp15);
#line 336
  i = 0;
  }
  {
#line 336
  while (1) {
    while_continue: /* CIL Label */ ;
#line 336
    if (i < num) {

    } else {
#line 336
      goto while_break;
    }
    {
#line 337
    __cil_tmp16 = (size_t )1;
#line 337
    tmp___0 = calloc(__cil_tmp16, 40UL);
#line 337
    *dbl = (struct databuf *)tmp___0;
#line 339
    tmp___1 = _IO_getc(f);
#line 339
    __cil_tmp17 = 0 * 1UL;
#line 339
    __cil_tmp18 = 32 + __cil_tmp17;
#line 339
    __cil_tmp19 = *dbl;
#line 339
    __cil_tmp20 = (unsigned long )__cil_tmp19;
#line 339
    __cil_tmp21 = __cil_tmp20 + __cil_tmp18;
#line 339
    *((u_char *)__cil_tmp21) = (u_char )tmp___1;
#line 340
    tmp___2 = _IO_getc(f);
#line 340
    __cil_tmp22 = 1 * 1UL;
#line 340
    __cil_tmp23 = 32 + __cil_tmp22;
#line 340
    __cil_tmp24 = *dbl;
#line 340
    __cil_tmp25 = (unsigned long )__cil_tmp24;
#line 340
    __cil_tmp26 = __cil_tmp25 + __cil_tmp23;
#line 340
    *((u_char *)__cil_tmp26) = (u_char )tmp___2;
#line 341
    tmp___3 = _IO_getc(f);
#line 341
    __cil_tmp27 = 2 * 1UL;
#line 341
    __cil_tmp28 = 32 + __cil_tmp27;
#line 341
    __cil_tmp29 = *dbl;
#line 341
    __cil_tmp30 = (unsigned long )__cil_tmp29;
#line 341
    __cil_tmp31 = __cil_tmp30 + __cil_tmp28;
#line 341
    *((u_char *)__cil_tmp31) = (u_char )tmp___3;
#line 342
    tmp___4 = _IO_getc(f);
#line 342
    __cil_tmp32 = 3 * 1UL;
#line 342
    __cil_tmp33 = 32 + __cil_tmp32;
#line 342
    __cil_tmp34 = *dbl;
#line 342
    __cil_tmp35 = (unsigned long )__cil_tmp34;
#line 342
    __cil_tmp36 = __cil_tmp35 + __cil_tmp33;
#line 342
    *((u_char *)__cil_tmp36) = (u_char )tmp___4;
#line 343
    __cil_tmp37 = *dbl;
#line 343
    __cil_tmp38 = (unsigned long )__cil_tmp37;
#line 343
    __cil_tmp39 = __cil_tmp38 + 16;
#line 343
    *((int16_t *)__cil_tmp39) = (int16_t )1;
#line 344
    __cil_tmp40 = *dbl;
#line 344
    __cil_tmp41 = (unsigned long )__cil_tmp40;
#line 344
    __cil_tmp42 = __cil_tmp41 + 18;
#line 344
    *((int16_t *)__cil_tmp42) = (int16_t )1;
#line 345
    __cil_tmp43 = *dbl;
#line 345
    __cil_tmp44 = (void *)0;
#line 345
    *((struct databuf **)__cil_tmp43) = (struct databuf *)__cil_tmp44;
#line 350
    dbl = dbl + 1;
#line 336
    i = i + 1;
    }
  }
  while_break: /* CIL Label */ ;
  }
  {
#line 353
  fclose(f);
  }
#line 355
  return (temp);
}
}
#line 360 "ns-lookup-bad.c"
int main(int argc , char **argv ) 
{ struct databuf **nsp ;
  char syslogdname[1000] ;
  char sysloginfo[1000] ;
  FILE *f ;
  struct databuf **tmp ;
  char **__cil_tmp8 ;
  char *__cil_tmp9 ;
  char const   * __restrict  __cil_tmp10 ;
  char const   * __restrict  __cil_tmp11 ;
  void *__cil_tmp12 ;
  unsigned long __cil_tmp13 ;
  unsigned long __cil_tmp14 ;
  unsigned long __cil_tmp15 ;
  unsigned long __cil_tmp16 ;
  char *__cil_tmp17 ;
  char * __restrict  __cil_tmp18 ;
  FILE * __restrict  __cil_tmp19 ;
  unsigned long __cil_tmp20 ;
  unsigned long __cil_tmp21 ;
  char *__cil_tmp22 ;
  char * __restrict  __cil_tmp23 ;
  FILE * __restrict  __cil_tmp24 ;
  unsigned long __cil_tmp25 ;
  unsigned long __cil_tmp26 ;
  char *__cil_tmp27 ;
  char const   *__cil_tmp28 ;
  unsigned long __cil_tmp29 ;
  unsigned long __cil_tmp30 ;
  char *__cil_tmp31 ;
  char const   *__cil_tmp32 ;

  {
#line 365
  if (argc == 2) {

  } else {
    {
#line 365
    __assert_fail("argc == 2", "ns-lookup-bad.c", 365U, "main");
    }
  }
  {
#line 366
  __cil_tmp8 = argv + 1;
#line 366
  __cil_tmp9 = *__cil_tmp8;
#line 366
  __cil_tmp10 = (char const   * __restrict  )__cil_tmp9;
#line 366
  __cil_tmp11 = (char const   * __restrict  )"r";
#line 366
  f = fopen(__cil_tmp10, __cil_tmp11);
  }
  {
#line 367
  __cil_tmp12 = (void *)0;
#line 367
  __cil_tmp13 = (unsigned long )__cil_tmp12;
#line 367
  __cil_tmp14 = (unsigned long )f;
#line 367
  if (__cil_tmp14 != __cil_tmp13) {

  } else {
    {
#line 367
    __assert_fail("f!=((void *)0)", "ns-lookup-bad.c", 367U, "main");
    }
  }
  }
  {
#line 369
  __cil_tmp15 = 0 * 1UL;
#line 369
  __cil_tmp16 = (unsigned long )(syslogdname) + __cil_tmp15;
#line 369
  __cil_tmp17 = (char *)__cil_tmp16;
#line 369
  __cil_tmp18 = (char * __restrict  )__cil_tmp17;
#line 369
  __cil_tmp19 = (FILE * __restrict  )f;
#line 369
  fgets(__cil_tmp18, 1000, __cil_tmp19);
#line 370
  __cil_tmp20 = 0 * 1UL;
#line 370
  __cil_tmp21 = (unsigned long )(sysloginfo) + __cil_tmp20;
#line 370
  __cil_tmp22 = (char *)__cil_tmp21;
#line 370
  __cil_tmp23 = (char * __restrict  )__cil_tmp22;
#line 370
  __cil_tmp24 = (FILE * __restrict  )f;
#line 370
  fgets(__cil_tmp23, 1000, __cil_tmp24);
#line 372
  fclose(f);
#line 374
  tmp = create_databuf_list(2);
#line 374
  nsp = tmp;
#line 375
  __cil_tmp25 = 0 * 1UL;
#line 375
  __cil_tmp26 = (unsigned long )(syslogdname) + __cil_tmp25;
#line 375
  __cil_tmp27 = (char *)__cil_tmp26;
#line 375
  __cil_tmp28 = (char const   *)__cil_tmp27;
#line 375
  __cil_tmp29 = 0 * 1UL;
#line 375
  __cil_tmp30 = (unsigned long )(sysloginfo) + __cil_tmp29;
#line 375
  __cil_tmp31 = (char *)__cil_tmp30;
#line 375
  __cil_tmp32 = (char const   *)__cil_tmp31;
#line 375
  nslookup(nsp, __cil_tmp28, __cil_tmp32);
  }
#line 377
  return (0);
}
}