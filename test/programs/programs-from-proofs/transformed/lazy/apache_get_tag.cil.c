/* Generated by CIL v. 1.5.1 */
/* print_CIL_Input is true */

#line 9 "apache.c"
typedef int size_t;
#line 11
extern int nondet_char() ;
#line 12
extern char *ap_cpystrn(char *dst , char const   *src , size_t dst_size ) ;
#line 15 "apache.c"
int flag  =    0;
#line 17 "apache.c"
char *get_tag(char *tag , int tagbuf_len ) 
{ 
  char *tag_val ;
  char c ;
  char term ;
  int t ;
  int tmp ;
  int tmp___0 ;
  int tmp___1 ;
  int tmp___2 ;
  int tmp___3 ;
  int tmp___4 ;
  int tmp___5 ;

  {
#line 22
  t = 0;
#line 24
  tagbuf_len = tagbuf_len - 1;
#line 26
  tmp = nondet_char();
#line 26
  c = (char )tmp;
#line 28
  if ((int )c == 45) {
#line 29
    tmp___0 = nondet_char();
#line 29
    c = (char )tmp___0;
#line 30
    if ((int )c == 45) {
#line 31
      tmp___1 = nondet_char();
#line 31
      c = (char )tmp___1;
#line 32
      if ((int )c == 62) {
#line 33
        ap_cpystrn(tag, "done", tagbuf_len);
#line 34
        return (tag);
      } else {

      }
    } else {

    }
#line 37
    return ((char *)((void *)0));
  } else {

  }
#line 40
  while (1) {
#line 41
    if (t == tagbuf_len) {
#line 42
      flag = t;
#line 43
      *(tag + t) = (char)0;
#line 44
      return ((char *)((void *)0));
    } else {

    }
#line 46
    if ((int )c == 61) {
#line 47
      break;
    } else {

    }
#line 49
    flag = t;
#line 50
    *(tag + t) = c;
#line 51
    t = t + 1;
#line 52
    tmp___2 = nondet_char();
#line 52
    c = (char )tmp___2;
  }
#line 54
  flag = t;
#line 55
  *(tag + t) = (char)0;
#line 56
  t = t + 1;
#line 57
  tag_val = tag + t;
#line 59
  if ((int )c != 61) {
#line 60
    return ((char *)((void *)0));
  } else {

  }
#line 63
  tmp___3 = nondet_char();
#line 63
  c = (char )tmp___3;
#line 65
  if ((int )c != 34) {
#line 65
    if ((int )c != 39) {
#line 66
      return ((char *)((void *)0));
    } else {

    }
  } else {

  }
#line 68
  term = c;
#line 69
  while (1) {
#line 70
    tmp___4 = nondet_char();
#line 70
    c = (char )tmp___4;
#line 71
    if (t == tagbuf_len) {
#line 72
      flag = t;
#line 73
      *(tag + t) = (char)0;
#line 74
      return ((char *)((void *)0));
    } else {

    }
#line 77
    if ((int )c == 92) {
#line 78
      tmp___5 = nondet_char();
#line 78
      c = (char )tmp___5;
#line 79
      if ((int )c != (int )term) {
#line 81
        flag = t;
#line 82
        *(tag + t) = (char )'\\';
#line 83
        t = t + 1;
#line 84
        if (t == tagbuf_len) {
#line 86
          flag = t;
#line 87
          *(tag + t) = (char)0;
#line 88
          return ((char *)((void *)0));
        } else {

        }
      } else {

      }
    } else
#line 92
    if ((int )c == (int )term) {
#line 93
      break;
    } else {

    }
  }
#line 98
  flag = t;
#line 99
  *(tag + t) = (char)0;
#line 101
  return (tag);
}
}
#line 104 "apache.c"
int main(void) 
{ 
  char tag[4] ;

  {
#line 109
  get_tag(tag, 4);
#line 111
  return (0);
}
}
