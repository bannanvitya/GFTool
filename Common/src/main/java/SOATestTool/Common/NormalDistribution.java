package SOATestTool.Common;

import java.util.Random;

/**
 * Created by vkhozhaynov on 09.06.2015.
 */
public class NormalDistribution {
    private Random fRandom = new Random();

    public double getGaussian(double aMean, double aVariance){
        fRandom.setSeed(System.currentTimeMillis());
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
