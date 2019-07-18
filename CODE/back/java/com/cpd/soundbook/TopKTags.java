package com.cpd.soundbook;

import org.springframework.stereotype.Component;

import java.util.*;

@Component(value = "topKTags")
public class TopKTags {

    public List<Map.Entry<String,Integer>> getTopKTags(HashMap<String,Integer> inputs, int k){
        List<Map.Entry<String,Integer>> result = new ArrayList<>();
        try{

            PriorityQueue<Map.Entry<String,Integer>> maxHeap = new PriorityQueue<Map.Entry<String,Integer>>(k, new Comparator<Map.Entry<String,Integer>>() {
                @Override
                public int compare(Map.Entry<String,Integer> o1, Map.Entry<String,Integer> o2) {
                   return o1.getValue().compareTo(o2.getValue());
                }
            });

           Iterator<Map.Entry<String,Integer>> entryIterator = inputs.entrySet().iterator();
           while (entryIterator.hasNext()){
               Map.Entry<String,Integer> entry = entryIterator.next();
               if(maxHeap.size() != k){
                   maxHeap.offer(entry);
               }else if(maxHeap.peek().getValue() < entry.getValue()){
                   maxHeap.poll();
                   maxHeap.offer(entry);
               }
           }
            result.addAll(maxHeap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args){
        HashMap<String,Integer> testSet = new HashMap<>();
        for(int i=0;i<20;i++){
            testSet.put(String.valueOf(i),i);
        }

        TopKTags topKTags = new TopKTags();
        System.out.println(topKTags.getTopKTags(testSet,3));
    }
}
