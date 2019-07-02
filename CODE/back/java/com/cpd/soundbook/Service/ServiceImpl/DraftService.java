package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOInterface.DraftDAO;
import com.cpd.soundbook.Entity.Draft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DraftService implements com.cpd.soundbook.Service.ServiceInterface.DraftService {

    @Autowired
    private DraftDAO draftDAO;

    @Override
    public String getDraft(int bookid) {
        return draftDAO.getDraft(bookid);
    }

    @Override
    public void storeDraft(int bookid, String draft) {
        try{
            Draft d = new Draft();
            d.setBookid(bookid);
            d.setDraft(draft);
            draftDAO.storeDraft(d);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDraft(int bookid) {
        draftDAO.deleteDraftByBookid(bookid);
    }
}
