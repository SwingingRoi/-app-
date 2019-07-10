package com.baidu.ai;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;

public class Divide {
    public static void main(String[] args)
    {
        Segment segment = HanLP.newSegment();
        segment.enableJapaneseNameRecognize(true);
        segment.enablePlaceRecognize(true);
        segment.enableOrganizationRecognize(true);

        List<Term> termList=segment.seg("りみ，歌を作るのが大変でした，私たちはみんなであなたに頑張れパンを焼いてあげました，あなたの大好きなチョココロネがありますよ");
        termList=segment.seg("巴黎有埃菲尔铁塔，埃及有金字塔");
        //termList=segment.seg("风和日丽");
        System.out.print(termList);
    }
}
