/* Generated by CIL v. 1.5.1 */
/* print_CIL_Input is true */

#line 3 "fraction.c"
struct fraction {
   int c ;
   int d ;
};
#line 1
extern int __VERIFIER_nondet_int() ;
#line 9 "fraction.c"
int flag  =    1;
#line 10 "fraction.c"
int inter  ;
#line 13 "fraction.c"
int gcd(int x , int y ) 
{ 


  {
#line 14
  if (x != 0) {
#line 15
    return (1);
  } else
#line 14
  if (y != 0) {
#line 15
    return (1);
  } else {

  }
#line 18
  return (0);
}
}
#line 21 "fraction.c"
void reduceFraction(struct fraction frac ) 
{ 


  {
#line 22
  inter = gcd(frac.c, frac.d);
#line 23
  flag = inter;
#line 24
  frac.c = frac.c / inter;
#line 25
  flag = inter;
#line 26
  frac.d = frac.d / inter;
#line 27
  return;
}
}
#line 29 "fraction.c"
void main(void) 
{ 
  struct fraction frac ;
  struct fraction frac2 ;

  {
#line 31
  frac.c = __VERIFIER_nondet_int();
#line 32
  while (1) {
#line 33
    frac.d = __VERIFIER_nondet_int();
#line 32
    if (frac.d == 0) {

    } else {
#line 32
      break;
    }
  }
#line 36
  if (frac.c != 0) {
#line 37
    frac2.c = frac.d;
#line 38
    frac2.d = frac.c;
#line 39
    reduceFraction(frac2);
  } else {

  }
#line 42
  reduceFraction(frac);
#line 43
  return;
}
}
