package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreFamilyProfileInteractor;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.contract.MalariaProfileContract;
import org.smartregister.chw.hf.interactor.HfFamilyInteractor;
import org.smartregister.chw.hf.interactor.HfFamilyProfileInteractor;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.domain.Task;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.contract.FamilyProfileContract;

import java.util.Set;

public class FamilyOtherMemberActivityPresenter extends CoreFamilyOtherMemberActivityPresenter {

    public FamilyOtherMemberActivityPresenter(FamilyOtherMemberProfileExtendedContract.View view,
                                              FamilyOtherMemberContract.Model model, String viewConfigurationIdentifier,
                                              String familyBaseEntityId, String baseEntityId, String familyHead,
                                              String primaryCaregiver, String villageTown, String familyName) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }

    @Override
    protected CoreFamilyProfileInteractor getFamilyProfileInteractor() {
        if (profileInteractor == null) {
            this.profileInteractor = new HfFamilyProfileInteractor();
        }
        return (CoreFamilyProfileInteractor) profileInteractor;
    }

    @Override
    protected FamilyProfileContract.Model getFamilyProfileModel(String familyName) {
        if (profileModel == null) {
            this.profileModel = new FamilyProfileModel(familyName);
        }
        return profileModel;
    }

    @Override
    protected void setProfileInteractor() {
        if (familyInteractor == null) {
            familyInteractor = new HfFamilyInteractor();
        }
    }

    public void getReferralTasks(String planId, String baseEntityId, MalariaProfileContract.InteractorCallback callback) {
        Set<Task> taskList = HealthFacilityApplication.getInstance().getTaskRepository()
                .getTasksByEntityAndStatus(planId, baseEntityId, Task.TaskStatus.READY);

        callback.updateReferralTasks(taskList);
    }
}
