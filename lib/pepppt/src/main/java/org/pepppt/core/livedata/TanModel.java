package org.pepppt.core.livedata;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.pepppt.core.tan.Tan;

/**
 * The TanModel for live data.
 */
public class TanModel {
    private MutableLiveData<Tan> liveData_Tan;

    public MutableLiveData<Tan> getLiveData_Tan () {
        if (liveData_Tan == null) {
            liveData_Tan = new MutableLiveData<>();
        }

        return liveData_Tan;
    }

    private MutableLiveData<String> state;

    public MutableLiveData<String> getRequestState() {
        if (state == null) {
            state = new MutableLiveData<>();
        }

        return state;
    }

    public void setNewTan(Tan newtan) {

        try
        {
            MutableLiveData<Tan> bla = this.getLiveData_Tan();
            bla.postValue(newtan);
        }catch (Exception ex)
        {
            Log.e("dfgafda", ex.getMessage(),ex);
        }

    }

    public void requestTan() {
        this.getLiveData_Tan().setValue(new Tan (null, 0));
        this.getRequestState().setValue("requested");
    }
}
