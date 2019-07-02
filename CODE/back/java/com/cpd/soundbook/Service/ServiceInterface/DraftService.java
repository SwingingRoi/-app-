package com.cpd.soundbook.Service.ServiceInterface;

import org.springframework.stereotype.Component;

@Component(value = "draftService")
public interface DraftService {
    String getDraft(int bookid);

    void storeDraft(int bookid,String draft);

    void deleteDraft(int bookid);
}
