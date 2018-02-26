package com.shen;


import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Iterator;


public class SimilarityUtil {
    private static Dictionary dictionary;

    //加载资源文件
    static {
        //读取语料库
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    //计算单词的相似度 需要给定单词的词性 以下都基于给的词性进行操作
    public static double similarity(String word1, String word2, POS pos) throws Exception {
        //创建索引单词对象
        IndexWord indexWord1 = dictionary.getIndexWord(pos, word1);
        IndexWord indexWord2 = dictionary.getIndexWord(pos, word2);

        //统计语料库中概念节点的个数
        int count = 0;
        //获取到概念节点迭代器
        Iterator<Synset> iterator = dictionary.getSynsetIterator(pos);
        //迭代所有概念节点 并计数
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }

        double maxSim = 0;

        //遍历单词的所有意思 比较每个意思下单词之间的相似度
        //再取最大相似度作为两个单词的相似度
        for (Synset synset1 :
                indexWord1.getSenses()) {
            for (Synset synset2 :
                    indexWord2.getSenses()) {
                //得到两个词的概念节点对应的最深公共概念节点
                Synset commonSynSet = getCommonSynset(synset1, synset2);
                //计算在当前意思下两个词的相似度
                double sim = sim(IC(synset1, count), IC(synset2, count), IC(commonSynSet, count));
                //如果在当前单词下相似度最高则取用当前意思下的相似度
                if (sim > maxSim)
                    maxSim = sim;
            }
        }

        return maxSim;

    }


    //找到两个概念节点的最深公共概念节点 时间杂度为O(n)
    private static Synset getCommonSynset(Synset synset1, Synset synset2) throws Exception {

        //获取两个概念节点链表的长度
        int len1 = getLen(synset1);
        int len2 = getLen(synset2);

        //得到长短链表以及长的链表比短的链表长多少
        int moreStep = 0;
        PointerTargetNode nodeLong, nodeShort;

        if (len1 > len2) {
            nodeLong = PointerUtils.getHypernymTree(synset1).getRootNode();
            nodeShort = PointerUtils.getHypernymTree(synset2).getRootNode();
            moreStep = len1 - len2;
        } else {
            nodeLong = PointerUtils.getHypernymTree(synset2).getRootNode();
            nodeShort = PointerUtils.getHypernymTree(synset1).getRootNode();
            moreStep = len2 - len1;
        }

        //用一个指针在长的链表上先动多出的节点数
        for (int i = 0; i < moreStep; i++)
            nodeLong = PointerUtils.getDirectHypernyms(nodeLong.getSynset()).get(0);

        //接下来两个指针距离公共节点的距离相同 只需要往后遍历得到第一个公共节点即可
        //直到找到公共概念节点
        while (!nodeLong.getSynset().equals(nodeShort.getSynset())) {
            nodeLong = PointerUtils.getDirectHypernyms(nodeLong.getSynset()).get(0);
            nodeShort = PointerUtils.getDirectHypernyms(nodeShort.getSynset()).get(0);
        }

        return nodeLong.getSynset();
    }

    //获取一个概念节点到最顶部概念节点的长度
    private static int getLen(Synset synset) throws Exception {
        //以当前概念节点为起点 向上部移动
        PointerTargetNode node = PointerUtils.getHypernymTree(synset).getRootNode();
        PointerTargetNode preNode = null;
        int count = 0;

        //直到无法再 往上移动为止 则到达概念节点的最顶端
        while (!node.equals(preNode)) {
            count++;
            preNode = node;
            if (PointerUtils.getDirectHypernyms(node.getSynset()).size() != 0)
                node = PointerUtils.getDirectHypernyms(node.getSynset()).get(0);
            else
                break;
        }

        //返回从目标概念节点到最顶端的距离长度
        return count;
    }

    //统计概念节点的子节点个数
    private static int hypo(Synset synset) throws Exception {
        return PointerUtils.getHyponymTree(synset).toList().size();
    }

    //计算IC max为语料库中所有概念节点个数
    private static double IC(Synset synset, double max) throws Exception {
        return 1 - ((log2(hypo(synset) + 1)) / log2(max));
    }

    //计算两个概念的相似度
    private static double sim(double IC1, double IC2, double commonIC) {
        return 2 * commonIC / (IC1 + IC2);
    }

    //进行log2计算
    private static double log2(double num) {
        return (double) ((double) Math.log(num) / (double) Math.log(2));
    }
}
