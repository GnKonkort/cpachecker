/*
The program AbsMinus (implemented in function foo) takes 
two integers i and j as input and returns the absolute value of i-j.

There is an error in this program that is in the assignment 
"result=i-j", which should be "result=j-i. By putting as input 
{i=0, j=1}, the program returns the value -1 as the absolute value 
of i-j, however, the returned value should be 1. This program shows 
a a case where all if-condition are free from faults. 

@author: Mohammed Bekkouche
@Web:    http://www.i3s.unice.fr
*/

extern int __VERIFIER_nondet_uint();
extern void __VERIFIER_error();


void __VERIFIER_assert(int cond) {
  if (!(cond)) {
    ERROR: __VERIFIER_error();
  }
  return;
}

/* returns |i-j|, the absolute value of i minus j */
int foo (int i, int j) {
    int result;
    int k = 0;
    if (i > j) {// error in the condition : i > j instead of i <= j
        k = k+1;
    }
    if (k == 1 && i != j) {
        result = j-i;
    }
    else {
        result = i-j;
    }
    __VERIFIER_assert( (i<j && result==j-i) || (i>=j && result==i-j));
}


int main() 
{ 
  
  foo(__VERIFIER_nondet_int(),__VERIFIER_nondet_int());
    return 0; 
} 