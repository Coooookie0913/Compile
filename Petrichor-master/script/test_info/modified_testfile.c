
        #include <stdio.h>
        #include <stdlib.h>
        int getint() {
            int input;
            scanf("%d", &input);
            return input;
        }
        const int A = 1;
const int B = 2, C = 3, D = 4;

int n = 10;
int m = 20, p = 99, q = 66;

void f0() {
    return;
}

int f1(int x) {
    if (x >= 0) {
        x = x + 1;;;;
    }

    if (x <= 0) {
        x = x + 1;
    } else {
    }

    {
        x = x + 1;
    }

    while (x < 0) {
        x = -x;
    }

    while (x > 0) {
        x = -x;
        continue;
    }

    while (x == 0)
        x = x + 1;
    
    while (x != 0) {
        break;
    }

    while (1) {
        while (!1) {
        }
        break;
    }

    return x;
}

int f2(int x, int y, int z) {
    int a;
    a = x + n * B - m / C + z % (A + B) + 233 + -y + f1(y);
    a = a * +A;
    return x + y + a;
}

int gi() {
    int r;
    r = getint();
    printf("Got a number: ");
    printf("%d!\n", r);
    return r;
}

int main() {
    printf("19373348\n");

    int a = f2(1, 2, gi());
    f0();
    printf("%d\n", a);
    printf("%d\n", f1(gi()));
    int b = f2(a, A, gi()), c, d;
    printf("%d\n", b);
    c = f2(b, n, gi());
    printf("%d\n", c);
    printf("%d\n", f2(c, gi(), q));
    d = 0;
    return d;
}
