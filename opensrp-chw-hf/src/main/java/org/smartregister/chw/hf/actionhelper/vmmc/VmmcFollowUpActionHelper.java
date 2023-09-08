package org.smartregister.chw.hf.actionhelper.vmmc;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.dao.HfVmmcDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.chw.vmmc.domain.VisitDetail;
import org.smartregister.chw.vmmc.model.BaseVmmcVisitAction;

import java.util.List;
import java.util.Map;

public class VmmcFollowUpActionHelper implements BaseVmmcVisitAction.VmmcVisitActionHelper {

    protected String visit_type;

    protected String notifiable_adverse_event_occured;

    protected String jsonPayload;

    protected String baseEntityId;

    public Integer noOfDayPostOP;


    public VmmcFollowUpActionHelper(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.getJSONObject("global");

            String method_used = HfVmmcDao.getMcMethodUsed(baseEntityId);
            global.put("method_used", method_used);

            LocalDate todayDate = LocalDate.now();;
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
            String male_circumcision_date = HfVmmcDao.getMcDoneDate(baseEntityId);

            LocalDate mcProcedureDate = formatter.parseDateTime(male_circumcision_date).toLocalDate();

            noOfDayPostOP = dayDifference(mcProcedureDate, todayDate);

            JSONArray fields = jsonObject.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            JSONObject post_op_dates = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "post_op_dates");
            post_op_dates.put("text", "Day(s) Post-OP: " + noOfDayPostOP.toString());

            JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
            visitNumber.put(JsonFormUtils.VALUE, HfVmmcDao.getFollowUpVisit(baseEntityId));

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int dayDifference(LocalDate date1, LocalDate date2) {
        return Days.daysBetween(date1, date2).getDays();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            visit_type = CoreJsonFormUtils.getValue(jsonObject, "post_op_adverse_event_occur");
            notifiable_adverse_event_occured = CoreJsonFormUtils.getValue(jsonObject, "notifiable_adverse_event_occured");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseVmmcVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String s) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseVmmcVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(visit_type))
            return BaseVmmcVisitAction.Status.PENDING;
        else {
            return BaseVmmcVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseVmmcVisitAction baseVmmcVisitAction) {
        //overridden
    }
}
