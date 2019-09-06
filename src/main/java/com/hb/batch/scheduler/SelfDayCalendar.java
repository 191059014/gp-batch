package com.hb.batch.scheduler;

import org.apache.commons.collections4.CollectionUtils;
import org.quartz.Calendar;
import org.quartz.impl.calendar.HolidayCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ========== 自定义日期日历 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.scheduler.SelfDayCalendar.java, v1.0
 * @date 2019年09月06日 11时10分
 */
public class SelfDayCalendar extends HolidayCalendar {

    private static Logger logger = LoggerFactory.getLogger(SelfDayCalendar.class);

    //默认的时间格式。
    private static String DEFAULT_DATE_FROMART = "yyyyMMdd";

    public SelfDayCalendar() {
    }

    public SelfDayCalendar(Calendar baseCalendar) {
        this(baseCalendar, null);
    }

    public SelfDayCalendar(String excludeDates) {
        this(null, excludeDates);
    }

    /**
     * 构造一个日历对象，排除指定的那些法定节假日。
     *
     * @param baseCalendar, 与本日历对象关联的基础日历对象，在基础日历对象的基础上再排除指定的法定节假日，可以是null。
     * @param excludeDates  日期列表字符串,一个日期列表字符串，多个日期以逗号隔开，默认的日期格式是 yyyyMMdd。
     */
    public SelfDayCalendar(Calendar baseCalendar, String excludeDates) {
        super(baseCalendar);
        //将日期字符串解析成字符数组。
        Set<String> set = new HashSet<>();
        String[] arr = excludeDates.split(",");
        for (String s : arr) {
            set.add(s);
        }
        List<Date> dates = getDatesFromStrings(set);
        if (CollectionUtils.isNotEmpty(dates)) {
            // 循环添加数组中的日期到被排除的日期列表中
            dates.forEach(excludeDate -> addExcludedDate(excludeDate));
        }
    }

    /**
     * 将日历字符串数组，按照默认的日期格式转换为Date类型的数组。
     *
     * @param excludeDates 日期字符串数组。
     * @return 转换后的Date型的数组。
     */
    private List<Date> getDatesFromStrings(Set<String> excludeDates) {
        if (CollectionUtils.isEmpty(excludeDates)) {
            return null;
        }
        List<Date> dateList = new ArrayList<>();
        for (String stringDate : excludeDates) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FROMART);
            try {
                Date parse = simpleDateFormat.parse(stringDate);
                dateList.add(parse);
            } catch (ParseException e) {
                logger.error("日期解析失败:{}，{}", stringDate, e);
                continue;
            }
        }
        if (CollectionUtils.isEmpty(dateList)) {
            return null;
        }
        return dateList;
    }

}
