package com.shen;


import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Iterator;


public class Test {
    public static void main(String[] args) throws Exception {
        //读取语料库
        Dictionary dictionary=Dictionary.getDefaultResourceInstance();
        //创建索引单词对象
        IndexWord word1=dictionary.getIndexWord(POS.NOUN,"hill");
        IndexWord word2=dictionary.getIndexWord(POS.NOUN,"coast");


        //统计语料库中名词概念节点的个数
        int count=0;
        Iterator<Synset> iterator=dictionary.getSynsetIterator(POS.NOUN);
        while (iterator.hasNext()){
            count++;
            iterator.next();
        }

        double maxSim=0;
        for (Synset synset1:
             word1.getSenses()) {
            for (Synset synset2:
                 word2.getSenses()) {
                Synset commonSynSet=getCommonSynset(synset1, synset2);
                double sim=sim(IC(synset1,count),IC(synset2,count),IC(commonSynSet,count));
                if (sim>maxSim)
                    maxSim=sim;
            }
        }

        System.out.println(maxSim);






    }

    //找到两个概念节点的最深公共概念节点
    public static Synset getCommonSynset(Synset synset1, Synset synset2) throws Exception{

        int len1=getLen(synset1);
        int len2=getLen(synset2);

        int moreStep=0;
        PointerTargetNode nodeLong,nodeShort;

        if (len1>len2){
            nodeLong=PointerUtils.getDirectHypernyms(synset1).get(0);
            nodeShort=PointerUtils.getDirectHypernyms(synset2).get(0);
            moreStep=len1-len2;
        }else {
            nodeLong=PointerUtils.getDirectHypernyms(synset2).get(0);
            nodeShort=PointerUtils.getDirectHypernyms(synset1).get(0);
            moreStep=len2-len1;
        }

        for (int i = 0; i < moreStep; i++)
            nodeLong=PointerUtils.getDirectHypernyms(nodeLong.getSynset()).get(0);

        while(!nodeLong.equals(nodeShort)){
            nodeLong=PointerUtils.getDirectHypernyms(nodeLong.getSynset()).get(0);
            nodeShort=PointerUtils.getDirectHypernyms(nodeShort.getSynset()).get(0);
        }

        return nodeLong.getSynset();
    }

    //获取一个概念节点到最顶部概念节点的长度
    private static int getLen(Synset synset) throws Exception {
        PointerTargetNode node=PointerUtils.getDirectHypernyms(synset).get(0);
        PointerTargetNode preNode=null;
        int count=0;
        while (!node.equals(preNode)){
            count++;
            preNode=node;
            if (PointerUtils.getDirectHypernyms(node.getSynset()).size()!=0)
                node=PointerUtils.getDirectHypernyms(node.getSynset()).get(0);
            else
                break;
        }

        return count;
    }

    //统计概念节点的子节点个数
    private static int hypo(Synset synset) throws Exception{
        return PointerUtils.getHyponymTree(synset).toList().size();
    }

    //计算IC max为语料库中所有概念节点个数
    private static double IC(Synset synset,double max) throws Exception{
        return 1-((log2(hypo(synset)+1))/log2(max));
    }

    //计算两个概念的相似度
    private static double sim(double IC1,double IC2,double commonIC){
        return 2*commonIC/(IC1+IC2);
    }

    private static double log2(double num){
        return (double) ((double)Math.log(num)/(double) Math.log(2));
    }
}
