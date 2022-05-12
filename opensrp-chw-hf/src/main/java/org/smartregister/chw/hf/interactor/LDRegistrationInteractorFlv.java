package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAdmissionAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAncClinicFindingsAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationCurrentLabourAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTriageAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTrueLabourConfirmationAction;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationInteractorFlv implements LDRegistrationInteractor.Flavor {
    static JSONObject obstetricForm = null;
    static JSONObject ancClinicForm = null;
    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

    public LDRegistrationInteractorFlv(String baseEntityId) {
    }

    private void populateObstetricForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject gravida = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gravida");
        JSONObject childrenAlive = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "children_alive");
        JSONObject parity = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "para");
        JSONObject lastMenstrualPeriod = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "last_menstrual_period");

        gravida.put(org.smartregister.family.util.JsonFormUtils.VALUE, memberObject.getGravida());
        parity.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getParity(memberObject.getBaseEntityId()));
        childrenAlive.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getNumberOfSurvivingChildren(memberObject.getBaseEntityId()));
        lastMenstrualPeriod.put(org.smartregister.family.util.JsonFormUtils.VALUE, memberObject.getLastMenstrualPeriod());
    }

    private void populateAncFindingsForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject numberOfVisits = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "number_of_visits");
        JSONObject iptDoses = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "ipt_doses");
        JSONObject TTDoses = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "tt_doses");
        JSONObject LLINUsed = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "llin_used");
        JSONObject lastMeasuredHB = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hb_level");
        JSONObject lastMeasuredHBDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hb_test_date");
        JSONObject syphilis = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "syphilis");
        JSONObject bloodGroup = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "blood_group");
        JSONObject rhFactor = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "rh_factor");
        JSONObject pmtct = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "pmtct");
        JSONObject pmtctTestDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "pmtct_test_date");
        JSONObject artPrescription = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "art_prescription");

        numberOfVisits.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getVisitNumber(memberObject.getBaseEntityId()));
        iptDoses.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getIptDoses(memberObject.getBaseEntityId()));
        TTDoses.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getTTDoses(memberObject.getBaseEntityId()));
        LLINUsed.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.isLLINProvided(memberObject.getBaseEntityId()) ? "Yes" : "No");
        lastMeasuredHB.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHB(memberObject.getBaseEntityId()));
        lastMeasuredHBDate.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHBDate(memberObject.getBaseEntityId()));
        syphilis.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getSyphilisTestResult(memberObject.getBaseEntityId()));
        bloodGroup.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getBloodGroup(memberObject.getBaseEntityId()));
        rhFactor.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getRhFactor(memberObject.getBaseEntityId()));
        if (!HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("null"))
            pmtct.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("positive") ? "chk_one" : "chk_two");
        pmtctTestDate.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getHivTestDate(memberObject.getBaseEntityId()));
        artPrescription.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.isClientKnownOnArt(memberObject.getBaseEntityId()) ? "yes" : "no");
    }

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();
        obstetricForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryObstetricHistory());
        ancClinicForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAncClinicFindings());

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (AncDao.isANCMember(memberObject.getBaseEntityId())) {
            if (obstetricForm != null) {
                try {
                    JSONArray fields = obstetricForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    populateObstetricForm(fields, AncDao.getMember(memberObject.getBaseEntityId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (ancClinicForm != null) {
                try {
                    JSONArray fields = ancClinicForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    populateAncFindingsForm(fields, AncDao.getMember(memberObject.getBaseEntityId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        evaluateLDRegistration(actionList, details, memberObject, context, callBack);

        return actionList;
    }

    private void evaluateLDRegistration(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context,
                                        BaseLDVisitContract.InteractorCallBack callBack
    ) throws BaseLDVisitAction.ValidationException {
        BaseLDVisitAction ldRegistrationTriage = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_triage_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTriage())
                .withHelper(new LDRegistrationTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.ld_registration_triage_title), ldRegistrationTriage);

        BaseLDVisitAction ldRegistrationTrueLabourConfirmation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_true_labour_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTrueLabourConfirmation())
                .withHelper(new TrueLabourConfirmationAction(memberObject, actionList, details, callBack, context))
                .build();
        actionList.put(context.getString(R.string.ld_registration_true_labour_title), ldRegistrationTrueLabourConfirmation);
    }

    private static class TrueLabourConfirmationAction extends LDRegistrationTrueLabourConfirmationAction {
        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final Context context;
        private final Map<String, List<VisitDetail>> details;
        private final BaseLDVisitContract.InteractorCallBack callBack;

        public TrueLabourConfirmationAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.context = context;
            this.details = details;
            this.callBack = callBack;
        }

        @Override
        public String postProcess(String s) {
            if (labourConfirmation.equalsIgnoreCase("true")) {
                //Adding the next actions when true labour confirmation is completed and the client is confirmed with True Labour.
                try {
                    BaseLDVisitAction ldRegistrationAdmissionInformation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_admission_information_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation())
                            .withHelper(new LDRegistrationAdmissionAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_admission_information_title), ldRegistrationAdmissionInformation);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationObstetricHistory = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_obstetric_history_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryObstetricHistory())
                            .withJsonPayload(obstetricForm.toString())
                            .withHelper(new LDRegistrationObstetricHistoryAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_obstetric_history_title), ldRegistrationObstetricHistory);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationAncClinicFindings = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_anc_clinic_findings_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAncClinicFindings())
                            .withJsonPayload(ancClinicForm.toString())
                            .withHelper(new LDRegistrationAncClinicFindingsAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_anc_clinic_findings_title), ldRegistrationAncClinicFindings);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationCurrentLabour = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_current_labour_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCurrentLabour())
                            .withHelper(new LDRegistrationCurrentLabourAction(memberObject))
                            .build();
                    actionList.put(context.getString(R.string.ld_registration_current_labour_title), ldRegistrationCurrentLabour);
                } catch (BaseLDVisitAction.ValidationException e) {
                    Timber.e(e);
                }


            } else {
                //Removing the next actions  the client is confirmed with False Labour.
                if (actionList.containsKey(context.getString(R.string.ld_registration_admission_information_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_admission_information_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_obstetric_history_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_obstetric_history_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_anc_clinic_findings_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_anc_clinic_findings_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_current_labour_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_current_labour_title));
                }

            }

            //Calling the callback method to preload the actions in the actionns list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }


}
