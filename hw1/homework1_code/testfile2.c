#include<stdio.h>

const int a_const = 1;
const int b_const = 2,c_const = 3;
const int d_const[2] = {};
const int e_const[2+1] = {4,5,6};
const int f_const[2][2] = {{6*1,7*1},{8,9}};

int a = 1;
int b = 2,c = 3;
int d[2] = {};
int e[2] = {4/2,5};
int f[2][2] = {{6,7},{8,(9%2)}};
int g[2];
int h[2][2];

int func1(int x) {
	if (x % 2 == 1) {
		return 1;
	} else {
		return 0;
	}
	return 0;
}

void func2(int s[]) {
	if (s[0] % 2 == 1) {
		s[1] = 1;
		return;
	} else {
		s[1] = 0;
		return; 
	}
}


int main() {
	printf("21373293\n");
	a = b + a_const * c;
	printf("%d\n",a);
	b = c - e_const[0] / b_const;
	printf("%d\n",b);
	c = 10;
	printf("%d\n",c);
	e[1] = (e_const[0] + f[0][0]) - (f_const[1][0] +-+ f[0][1]);
	printf("%d\n",e[1]);
	{
		int a = 0;
		int b;
		b = (a + 5) % 3;
		int c = b*(b*b)/b;
		int d[2] = {(c % 2 + 1) - (c + 100),2+-+5*(-8)-+-1};
		printf("%d\n",d[0] + d[1] + c);//0
	}
	e[1] = 3;
	func2(e);
	e[0] = func1(e_const[5]);
	int i;
	for (i = d[0]; i < 2; i = i + 1) {
		g[i] = d[i];
		printf("%d\n",e[i]);
	}
	func2(f[1]);
	for (i = d[1]; i < 2; i = i+1) {
		h[1][i] = f[1][i];
		printf("%d\n",f[1][i]);
	}
	return 0;
}

