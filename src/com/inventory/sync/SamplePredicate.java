package com.inventory.sync;

import org.apache.poi.ss.formula.functions.T;

import java.util.function.*;

/**
 * Created by Islam-uddin on 7/18/2017.
 */

class SamplePredicate<T> implements Predicate<T>{
    T varc1;
    public boolean test(T varc){
        if(varc1.equals(varc)){
            return true;
        }
        return false;
    }
}
