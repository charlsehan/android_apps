package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.app.Fragment;
import android.view.KeyEvent;

import com.cloudminds.cit.Utils;

public abstract class CaseFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    protected void onTestPass() {
        if (mListener != null) {
            mListener.onTestFinish(getCaseName(), Utils.RESULT_PASS);
        }
    }

    protected void onTestFail() {
        if (mListener != null) {
            mListener.onTestFinish(getCaseName(), Utils.RESULT_FAIL);
        }
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    public abstract String getCaseName();

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public interface OnFragmentInteractionListener {
        void onTestFinish(String caseName, int result);
    }
}
