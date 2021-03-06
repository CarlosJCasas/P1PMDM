package com.example.p1pmdm.core;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

public class InputFilterMinMax implements InputFilter {
    private int min,max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }
    public InputFilterMinMax(String min, String max){
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend);
        newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart);
        if(!newVal.isEmpty()){
            int input = Integer.parseInt(newVal);
            if (isInRange(min, max, input)) {
                return null;
            }
        }
        return "";
    }
    private boolean isInRange(int a, int b, int c){

        return b>a ? c>=a && c<=b : c>=b && c<=a;
    }
}
