package org.smartregister.chw.hf.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.repository.VisitDetailsRepository;
import org.smartregister.chw.pmtct.repository.VisitRepository;
import org.smartregister.chw.pmtct.util.VisitUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class PmtctVisitUtils extends VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(PmtctLibrary.getInstance().visitRepository(), PmtctLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 0);

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());
        List<Visit> pmtctEacVisitsCompleted = new ArrayList<>();
        List<Visit> pmtctFollowupVisits = new ArrayList<>();


        for (Visit v : visits) {
            if (v.getVisitType().equalsIgnoreCase(Constants.Events.PMTCT_FIRST_EAC_VISIT)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");

                    boolean isEacVisit1Done = computeCompletionStatus(obs, "eac_day_1");
                    boolean isEacVisit2Done = computeCompletionStatus(obs, "eac_day_2");
                    boolean isEacVisit3Done = computeCompletionStatus(obs, "eac_day_3");


                    if (isEacVisit1Done && isEacVisit2Done && isEacVisit3Done) {
                        pmtctEacVisitsCompleted.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else if (v.getVisitType().equalsIgnoreCase(Constants.Events.PMTCT_SECOND_EAC_VISIT)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");

                    boolean isEacVisit1Done = computeCompletionStatus(obs, "eac_month_1");
                    boolean isEacVisit2Done = computeCompletionStatus(obs, "eac_month_2");
                    boolean isEacVisit3Done = computeCompletionStatus(obs, "eac_month_3");

                    if (isEacVisit1Done && isEacVisit2Done && isEacVisit3Done) {
                        pmtctEacVisitsCompleted.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else if (v.getVisitType().equalsIgnoreCase(org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_FOLLOWUP)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    String baseEntityId = jsonObject.getString("baseEntityId");
                    JSONArray obs = jsonObject.getJSONArray("obs");
                    List<Boolean> checks = new ArrayList<Boolean>();

                    boolean isCounsellingDone = computeCompletionStatus(obs, "is_client_counselled");
                    boolean isClinicalStagingDone = computeCompletionStatus(obs, "clinical_staging_disease");
                    boolean isTbScreeningDone = computeCompletionStatus(obs, "on_tb_treatment");
                    boolean isArvPrescriptionDone = computeCompletionStatus(obs, "prescribed_regimes");

                    boolean isBaselineInvestigationComplete = computeCompletionStatus(obs, "liver_function_test_conducted") && computeCompletionStatus(obs, "renal_function_test_conducted");
                    boolean isHvlSampleCollectionComplete = computeCompletionStatus(obs, "hvl_sample_id");
                    boolean isCd4SampleCollectionComplete = computeCompletionStatus(obs, "cd4_sample_id");

                    checks.add(isCounsellingDone);
                    checks.add(isClinicalStagingDone);
                    checks.add(isTbScreeningDone);
                    checks.add(isArvPrescriptionDone);


                    if (HfPmtctDao.isEligibleForHlvTest(baseEntityId)) {
                        checks.add(isHvlSampleCollectionComplete);
                    }

                    if (HfPmtctDao.isEligibleForBaselineInvestigation(baseEntityId) || HfPmtctDao.isEligibleForBaselineInvestigationOnFollowupVisit(baseEntityId)) {
                        checks.add(isBaselineInvestigationComplete);
                    }

                    if (HfPmtctDao.isEligibleForCD4Retest(baseEntityId) || HfPmtctDao.isEligibleForCD4Test(baseEntityId)) {
                        checks.add(isCd4SampleCollectionComplete);
                    }

                    if (!checks.contains(false)) {
                        pmtctEacVisitsCompleted.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }
        if (pmtctEacVisitsCompleted.size() > 0) {
            processVisits(pmtctEacVisitsCompleted, visitRepository, visitDetailsRepository);
        }
        if (pmtctFollowupVisits.size() > 0) {
            processVisits(pmtctFollowupVisits, visitRepository, visitDetailsRepository);
        }
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }

}
