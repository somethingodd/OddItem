package info.somethingodd.bukkit.OddItem.bktree;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple DistanceStrategy objects.
 */

public class DistanceStrategies {
    private static final Map<String, DistanceStrategy<String>> strategies = new HashMap<String, DistanceStrategy<String>>();
    private static final LevenshteinDistance levenshtein = LevenshteinDistance.getInstance();

    public static DistanceStrategy<String> get(String name) {
        return strategies.get(name);
    }

    static {
        strategies.put("levenshtein", levenshtein);
        strategies.put("l", levenshtein);

        DistanceStrategy<String> caverphone = fromStringEncoder(new Caverphone2());
        strategies.put("caverphone", caverphone);
        strategies.put("c", caverphone);

        DistanceStrategy<String> cologne = fromStringEncoder(new ColognePhonetic());
        strategies.put("cologne", cologne);
        strategies.put("k", cologne);

        DistanceStrategy<String> metaphone = fromStringEncoder(new Metaphone());
        strategies.put("metaphone", metaphone);
        strategies.put("m", metaphone);

        DistanceStrategy<String> refinedSoundex = new DistanceStrategy<String>() {
            RefinedSoundex r = new RefinedSoundex();
            @Override
            public int distance(String left, String right) {
                try {
                    return -r.difference(left, right);
                } catch (EncoderException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                return "RefinedSoundex";
            }
        };
        strategies.put("refinedsoundex", refinedSoundex);
        strategies.put("r", refinedSoundex);

        DistanceStrategy soundex = new DistanceStrategy<String>() {
            Soundex s = new Soundex();
            @Override
            public int distance(String left, String right) {
                try {
                    return -s.difference(left, right);
                } catch (EncoderException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                return "Soundex";
            }
        };
        strategies.put("soundex", soundex);
        strategies.put("s", soundex);
    }

    private static DistanceStrategy<String> fromStringEncoder(final StringEncoder encoder) {
        return new DistanceStrategy<String>() {
            @Override
            public int distance(String left, String right) {
                String leftPrime;
                String rightPrime;

                try {
                    leftPrime = encoder.encode(left);
                    rightPrime = encoder.encode(right);
                } catch (EncoderException e) {
                    throw new RuntimeException(e);
                }

                return levenshtein.distance(leftPrime, rightPrime);
            }

            @Override
            public String toString() {
                return encoder.getClass().getName();
            }
        };
    }
}
