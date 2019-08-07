package com.example.mindtray.memolist;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemoListPagerAdapter extends PagerAdapter {
    public static int dateToPos(Calendar date) {
        if (date == null) throw new RuntimeException("date is null");

        Calendar minDate = new GregorianCalendar();

        minDate.set(Calendar.YEAR, minDate.getActualMinimum(Calendar.YEAR));
        minDate.set(Calendar.MONTH, minDate.getActualMinimum(Calendar.MONTH));
        minDate.set(Calendar.DAY_OF_MONTH, minDate.getActualMinimum(Calendar.DAY_OF_MONTH));

        int daysD = (int) TimeUnit.MILLISECONDS.toDays((date.getTimeInMillis() - minDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));

        return daysD;
    }

    public static Calendar posToDate(int pos) {
        Calendar minDate = new GregorianCalendar();

        minDate.set(Calendar.YEAR, minDate.getActualMinimum(Calendar.YEAR));
        minDate.set(Calendar.MONTH, minDate.getActualMinimum(Calendar.MONTH));
        minDate.set(Calendar.DAY_OF_MONTH, minDate.getActualMinimum(Calendar.DAY_OF_MONTH));

        minDate.add(Calendar.DAY_OF_MONTH, pos);

        return minDate;
    }

    public interface Listener {
        void onInstantiateItem(ViewGroup container, Calendar date, int pos);
        void onDestroyItem(ViewGroup container, Calendar date, int pos);
    }

    private Listener _listener;

    @Override
    public int getCount() {
        Calendar maxDate = new GregorianCalendar();

        maxDate.set(Calendar.YEAR, maxDate.getActualMinimum(Calendar.YEAR));
        maxDate.set(Calendar.MONTH, maxDate.getActualMinimum(Calendar.MONTH));
        maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMinimum(Calendar.DAY_OF_MONTH));

        return dateToPos(maxDate);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);

        _listener.onInstantiateItem(container, posToDate(position), position);

        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        _listener.onDestroyItem(container, posToDate(position), position);
    }

    public MemoListPagerAdapter(Listener listener) {
        _listener = listener;
    }
}
