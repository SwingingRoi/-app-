package com.cpd.soundbook.Service.ServiceImpl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class DraftServiceTest {
    @Autowired
    private DraftService draftService;

    @Test
    public void getDraft() {
        //测试草稿保存
        Assert.assertEquals("测试用草稿",draftService.getDraft(4));
    }

    @Test
    public void storeDraft(){
        draftService.storeDraft(5,"保存用草稿");
    }

    @Test
    public void deleteDraft(){
        draftService.deleteDraft(5);
    }
}