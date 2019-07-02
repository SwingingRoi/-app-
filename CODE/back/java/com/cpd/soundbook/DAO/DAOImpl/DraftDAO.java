package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.Draft;
import com.cpd.soundbook.Repository.DraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public class DraftDAO implements com.cpd.soundbook.DAO.DAOInterface.DraftDAO {

    @Autowired
    private DraftRepository draftRepository;

    @Transactional
    @Override
    public String getDraft(int bookid) {
        Draft draft = draftRepository.findDraftByBookid(bookid);
        if(draft == null){
            return "";
        }
        return draft.getDraft();
    }

    @Transactional
    @Override
    public void storeDraft(Draft draft) {
        deleteDraftByBookid(draft.getBookid());
        draftRepository.save(draft);
    }

    @Transactional
    @Override
    public void deleteDraftByBookid(int bookid) {
        draftRepository.deleteByBookid(bookid);
    }
}
