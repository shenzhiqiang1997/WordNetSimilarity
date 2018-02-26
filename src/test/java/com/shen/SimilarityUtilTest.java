package com.shen;

import net.sf.extjwnl.data.POS;
import org.junit.Test;

public class SimilarityUtilTest {
    @Test
    public void test() throws Exception {
        System.out.println("similarity between car and automobile is "
                + SimilarityUtil.similarity("car", "automobile", POS.NOUN));

        System.out.println("similarity between gem and jewel is " +
                SimilarityUtil.similarity("gem", "jewel", POS.NOUN));

        System.out.println("similarity between boy and lad is " +
                SimilarityUtil.similarity("boy", "lad", POS.NOUN));

        System.out.println("similarity between hill and coast is "
                + SimilarityUtil.similarity("hill", "coast", POS.NOUN));

        System.out.println("similarity between furnace and stove is " +
                SimilarityUtil.similarity("furnace", "stove", POS.NOUN));

        System.out.println("similarity between journey and coast car " +
                SimilarityUtil.similarity("journey", "car", POS.NOUN));
    }
}
