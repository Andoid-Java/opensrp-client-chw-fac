package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.anc.util.VisitUtils.getVisitDetailsOnly;
import static org.smartregister.chw.anc.util.VisitUtils.getVisitGroups;
import static org.smartregister.chw.anc.util.VisitUtils.getVisitsOnly;

import android.content.Context;

import com.google.gson.Gson;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.CoreBaseAncMedicalHistoryInteractor;
import org.smartregister.chw.hf.domain.SortableVisit;
import org.smartregister.chw.vmmc.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VmmcMedicalHistoryInteractor extends CoreBaseAncMedicalHistoryInteractor {
    public static List<SortableVisit> getVisits(String memberID, String... eventTypes) {

        List<Visit> visits = new ArrayList<>();
        if (eventTypes != null && eventTypes.length > 0) {
            for (String eventType : eventTypes) {
                List<Visit> visit = getVisitsOnly(memberID, eventType);
                visits.addAll(visit);
            }
        }

        int x = 0;
        while (visits.size() > x) {
            Visit visit = visits.get(x);
            List<VisitDetail> detailList = getVisitDetailsOnly(visit.getVisitId());
            visits.get(x).setVisitDetails(getVisitGroups(detailList));
            x++;
        }

        List<SortableVisit> sortableVisits = new ArrayList<>();
        for (Visit visit : visits) {
            Gson gson = new Gson();
            SortableVisit sortableVisit = gson.fromJson(gson.toJson(visit), SortableVisit.class);
            sortableVisits.add(sortableVisit);
        }

        Collections.sort(sortableVisits);

        return sortableVisits;
    }

    @Override
    public void getMemberHistory(final String memberID, final Context context, final BaseAncMedicalHistoryContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {

            String[] eventTypes = new String[]{
                    Constants.EVENT_TYPE.VMMC_SERVICES,
                    Constants.EVENT_TYPE.VMMC_PROCEDURE,
                    Constants.EVENT_TYPE.VMMC_DISCHARGE,
                    Constants.EVENT_TYPE.VMMC_FOLLOW_UP_VISIT,
                    Constants.EVENT_TYPE.VMMC_NOTIFIABLE_EVENTS
            };
            List<SortableVisit> visits = getVisits(memberID, eventTypes);
            final List<Visit> all_visits = new ArrayList<>(visits);

            appExecutors.mainThread().execute(() -> callBack.onDataFetched(all_visits));
        };

        appExecutors.diskIO().execute(runnable);
    }
}
