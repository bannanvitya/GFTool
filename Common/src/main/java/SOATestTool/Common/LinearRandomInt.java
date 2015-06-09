package SOATestTool.Common;

import java.math.BigInteger;


public class LinearRandomInt {

    private final BigInteger a = BigInteger.valueOf(25214903917L);;  // Multiplier
    private final BigInteger b = BigInteger.valueOf(11);  // Increment
    private final BigInteger m = BigInteger.ONE.shiftLeft(48);  // 2^48
    private final BigInteger aInv;  // Multiplicative inverse of 'a' modulo m

    private BigInteger x;  // State


    public LinearRandomInt(BigInteger seed) {
        if (a == null || b == null || m == null || seed == null)
            throw new NullPointerException();
        if (seed.signum() == -1 || seed.compareTo(m) >= 0)
            throw new IllegalArgumentException("Arguments out of range");

        this.aInv = a.modInverse(m);
        this.x = seed;
    }


    public BigInteger getState() {
        return x;
    }


    public void next() {
        x = x.multiply(a).add(b).mod(m);  // x = (a*x + b) mod m
    }


    public void previous() {
        x = x.subtract(b).multiply(aInv).mod(m);  // x = (a^-1 * (x - b)) mod m
    }


    public void skip(int n) {
        if (n >= 0)
            skip(a, b, BigInteger.valueOf(n));
        else
            skip(aInv, aInv.multiply(b).negate(), BigInteger.valueOf(n).negate());
    }


    private void skip(BigInteger a, BigInteger b, BigInteger n) {
        BigInteger a1 = a.subtract(BigInteger.ONE);  // a - 1
        BigInteger ma = a1.multiply(m);              // (a - 1) * m
        BigInteger y = a.modPow(n, ma).subtract(BigInteger.ONE).divide(a1).multiply(b);  // (a^n - 1) / (a - 1) * b, sort of
        BigInteger z = a.modPow(n, m).multiply(x);   // a^n * x, sort of
        x = y.add(z).mod(m);  // (y + z) mod m
    }

}