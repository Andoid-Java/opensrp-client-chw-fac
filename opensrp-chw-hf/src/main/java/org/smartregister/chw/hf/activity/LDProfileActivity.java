package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.util.Constants;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static final String LD_PROFILE_ACTION = "LD_PROFILE_ACTION";

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        setTextViewRecordLDText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextViewRecordLDText();
    }

    protected void setupViews() {
        super.setupViews();
        textViewRecordLD.setText("Examination/Consultation");

        Visit lastLDVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), "Visit Type");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textview_record_ld) {
            if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_labour_stage_title))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryLabourStage());
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle))) {
                openExaminationConsultation();
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_cervix_dilation_monitoring_button_tittle))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryCervixDilationMonitoring());
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_partograph_button_title))) {
                LDPartographActivity.startMe(this, memberObject.getBaseEntityId(), false,
                        getName(memberObject), String.valueOf(new Period(new DateTime(this.memberObject.getAge()), new DateTime()).getYears()));
            }
        } else {
            super.onClick(view);
        }
    }

    private String getName(MemberObject memberObject) {
        return getName(getName(memberObject.getFirstName(), memberObject.getMiddleName()), memberObject.getLastName());
    }

    private String getName(String nameOne, String nameTwo) {
        return nameOne + " " + nameTwo;
    }

    private void setTextViewRecordLDText() {
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null)
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("1")) {
            if (LDDao.getCervixDilation(memberObject.getBaseEntityId()) == null) {
                textViewRecordLD.setText(R.string.labour_and_delivery_examination_and_consultation_button_tittle);
            } else if (Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) < 3) {
                textViewRecordLD.setText(R.string.labour_and_delivery_cervix_dilation_monitoring_button_tittle);
            } else {
                textViewRecordLD.setText(R.string.labour_and_delivery_partograph_button_title);
            }
        }
    }

    public static void startLDForm(Activity activity, String baseEntityID, String formName) {
        Intent intent = new Intent(activity, LDRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.LD_FORM_NAME, formName);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.ACTION, LD_PROFILE_ACTION);
        activity.startActivity(intent);
    }

    private void openExaminationConsultation() {
        String baseEntityId = null;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            baseEntityId = extras.getString(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        }

        LDVisitActivity.startLDVisitActivity(this, baseEntityId, false);
    }

}