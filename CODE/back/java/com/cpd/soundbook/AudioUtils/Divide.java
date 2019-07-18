package com.cpd.soundbook.AudioUtils;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "divide")
public class Divide {

    public List<Term> divide(String text){
        Segment segment = HanLP.newSegment();
        segment.enableJapaneseNameRecognize(true);
        segment.enablePlaceRecognize(true);
        segment.enableOrganizationRecognize(true);

        return segment.seg(text);
    }
}
