package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.Draft;
import org.springframework.stereotype.Component;

@Component(value = "draftDAO")
public interface DraftDAO {
    String getDraft(int bookid);

    void storeDraft(Draft draft);

    void deleteDraftByBookid(int bookid);
}
