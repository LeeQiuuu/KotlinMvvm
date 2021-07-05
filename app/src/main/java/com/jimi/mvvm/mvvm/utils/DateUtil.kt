package com.jimi.app.mvvm.utils

import android.util.Log
import com.jimi.app.utils.TimeZoneUtil
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * const 只读属性的值在编译期是已知的，那么可以使用 const 修饰符将其标记为编译期常量，等价于 public static final
 *
 * @JvmStatic 只能在 object或者companion object里才能使用，可以修饰属性和方法，通过setter和getter方法设置属性,
 *
 * @JvmField 只能修饰属性 会消除属性的setter和getter方法，直接调用属性本身。不能修饰方法，不限制使用是否是单例
 *
 * @author : 林泽鑫
 * @e-mail : linzexin@jimimax.com
 * @date : 2021/6/21
 * @desc : 日期工具类
 * @version : 1.0
 */
object DateUtil {
    private const val TAG = "DateUtil"
    const val SECOND = "ss"
    const val MINUTE = "mm"
    const val HOUR = "HH"
    const val DAY = "dd"
    const val MONTH = "MM"
    const val YEAR = "yyyy"
    const val HH_MM = "HH:mm"
    const val HH_MM_SS = "HH:mm:ss"
    const val YYYY_MM_DD = "yyyy-MM-dd"
    const val YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm"
    const val YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss"

    val WEEK_DAYS = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

    //╔═════════════════════════════════════════════════
    //║---------------------------------------日期对比 Begin---------------------------------------
    //╚═════════════════════════════════════════════════
    /**
     * 将字符串转年月日进行比对
     *
     * @param format 字符串格式
     * @param date1  日期一
     * @param date2  日期二
     * @return true or false
     */
    @JvmStatic
    fun equalsDate(format: String, date1: String, date2: String): Boolean {
        val newDate1 = convertDateStrToNewDateStr(format, YYYY_MM_DD, date1)
        val newDate2 = convertDateStrToNewDateStr(format, YYYY_MM_DD, date2)
        return newDate1.equals(newDate2, ignoreCase = true)
    }

    /**
     * 对比两个日期文本是否一致
     *
     * @param format1 日期格式一
     * @param date1   日期一
     * @param format2 日期格式二
     * @param date2   2日期二
     * @return true or false
     */
    @JvmStatic
    fun equalsDate(format1: String, date1: String, format2: String, date2: String): Boolean {
        val newDate1 = convertDateStrToNewDateStr(format1, YYYY_MM_DD, date1)
        val newDate2 = convertDateStrToNewDateStr(format2, YYYY_MM_DD, date2)
        return newDate1.equals(newDate2, ignoreCase = true)
    }

    /**
     * 对比两个日期文本是否一致
     *
     * @param format1 日期格式一
     * @param date1   日期对象
     * @param format2 日期格式二
     * @param date2   2日期对象二
     * @return true or false
     */
    @JvmStatic
    fun equalsDate(format1: String, date1: Date, format2: String, date2: Date): Boolean {
        val newDate1 = getDate(format1, date1)
        val newDate2 = getDate(format2, date2)
        return newDate1.equals(newDate2, ignoreCase = true)
    }
    //╔═════════════════════════════════════════════════
    //║----------------------------------------日期对比 End----------------------------------------
    //╚═════════════════════════════════════════════════
    //╔═════════════════════════════════════════════════
    //║-------------------------------------日期类型互转 Begin-------------------------------------
    //╚═════════════════════════════════════════════════
    /**
     * 日期字符串转新日期格式的字符串
     *
     * @param inFormat  输入的日期字符串的格式
     * @param outFormat 输出的日期字符串的格式
     * @param dateStr   日期字符串
     * @return 日期字符串
     */
    @JvmStatic
    fun convertDateStrToNewDateStr(inFormat: String, outFormat: String, dateStr: String): String {
        val date = convertDateStrToDate(inFormat, dateStr)
        return if (date == null) {
            Log.e(TAG, "convertDateStrToNewDateStr: 日期格式重新格式化失败")
            dateStr
        } else {
            getDate(outFormat, date)
        }
    }

    /**
     * 将日期字符串转日期对象
     *
     * @param format  日期格式
     * @param dateStr 日期字符串
     * @return Date
     */
    @JvmStatic
    fun convertDateStrToDate(format: String, dateStr: String): Date? {
        val timeMillis = convertDateStrToTimeMillis(format, dateStr)
        return if (timeMillis == -1L) {
            null
        } else Date(timeMillis)
    }

    /**
     * 日期字符串转毫秒数
     *
     * @param format  日期格式 例：yyyy-MM-dd HH:mm:ss
     * @param dateStr 日期字符串 例：2019-09-16 10:00:00
     * @return 毫秒数
     */
    @JvmStatic
    fun convertDateStrToTimeMillis(format: String, dateStr: String): Long {
        val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        var date: Date? = null
        try {
            date = simpleDateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date?.time ?: -1
    }

    /**
     * Date对象转毫秒数
     *
     * @param date Date对象
     * @return 毫秒数
     */
    @JvmStatic
    fun convertDateToTimeMillis(date: Date): Long {
        return date.time
    }

    /**
     * Calendar对象转Date对象
     *
     * @param calendar Calendar对象
     * @return Date
     */
    @JvmStatic
    fun convertCalendarToDate(calendar: Calendar): Date {
        return Date(convertCalendarToTimeMillis(calendar))
    }

    /**
     * Calendar对象转时间毫秒数
     *
     * @param calendar Calendar对象
     * @return 毫秒数
     */
    @JvmStatic
    fun convertCalendarToTimeMillis(calendar: Calendar): Long {
        return calendar.timeInMillis
    }

    /**
     * 时间毫秒数转Calendar对象
     *
     * @param timeMillis 毫秒数
     * @return Calendar
     */
    @JvmStatic
    fun convertTimeMillisToCalendar(timeMillis: Long): Calendar {
        return convertDateToCalendar(convertTimeMillisToDate(timeMillis))
    }

    /**
     * 时间毫秒数转Date对象
     *
     * @param timeMillis 毫秒数
     * @return Date
     */
    @JvmStatic
    fun convertTimeMillisToDate(timeMillis: Long): Date {
        return Date(timeMillis)
    }

    /**
     * Date日期转为Calendar日期
     *
     * @param date Date对象
     * @return Calendar
     */
    @JvmStatic
    fun convertDateToCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
    //╔═════════════════════════════════════════════════
    //║--------------------------------------日期类型互转 End--------------------------------------
    //╚═════════════════════════════════════════════════
    //╔═════════════════════════════════════════════════
    //║---------------------------------------获取日期 Begin---------------------------------------
    //╚═════════════════════════════════════════════════

    /**
     * 获取想要的日期字符串
     *
     * @param dayOffset 偏移天数
     * @return
     */
    @JvmStatic
    fun getWhichDateByDay(dayOffset: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset)
        return calendar
    }


    /**
     * 获取想要的日期字符串
     *
     * @param dayOffset 偏移天数，例如：昨天传-1
     * @return 年月日String
     */
    @JvmStatic
    fun getWhichDateByDayOffset(dayOffset: Int): String {
        val calendar = getWhichDateByDay(dayOffset)
        return getDate(YYYY_MM_DD, calendar)
    }

    /**
     * 获取想要的日期字符串
     *
     * @param format    格式
     * @param dayOffset 偏移天数，例如：昨天传-1
     * @return String
     */
    @JvmStatic
    fun getWhichDateByDay(format: String, dayOffset: Int): String {
        val calendar = getWhichDateByDay(dayOffset)
        return getDate(format, calendar)
    }

    /**
     * 获取想要的日期字符串
     *
     * @param monthOffset 偏移月份
     * @return
     */
    @JvmStatic
    fun getWhichDateByMonth(monthOffset: Int): Calendar {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.add(Calendar.MONTH, monthOffset)
        return calendar
    }

    /**
     * 获取想要的日期字符串
     *
     * @param format      格式
     * @param monthOffset 偏移月份，例如：三个月前，传-3
     * @return
     */
    @JvmStatic
    fun getWhichDateByMonth(format: String, monthOffset: Int): String {
        val calendar = getWhichDateByMonth(monthOffset)
        return getDate(format, calendar)
    }


    /**
     * 获取星期字符串（中文）
     *
     * @param calendar  日历对象
     * @return 星期几
     */
    @JvmStatic
    fun getWeekStr(calendar: Calendar): String {
        return WEEK_DAYS[getWeekIndex(calendar)]
    }


    /**
     * 获取星期字符串（中文）
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 星期几
     */
    @JvmStatic
    fun getWeekStr(year: Int, month: Int, day: Int): String {
        return WEEK_DAYS[getWeekIndex(year, month, day)]
    }

    /**
     * 获取当前星期几的下标
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 星期的下标从0开始星期日~星期六
     */
    @JvmStatic
    fun getWeekIndex(year: Int, month: Int, day: Int): Int {
        val calendar = Calendar.getInstance()
        calendar[year, month - 1] = day
        return calendar[Calendar.DAY_OF_WEEK] - 1 //星期是从1开始计算的
    }

    /**
     * 获取当前星期几下标
     *
     * @param calendar 日历对象
     * @return 星期的下标从0开始星期日~星期六
     */
    @JvmStatic
    fun getWeekIndex(calendar: Calendar): Int {
//        val instance = Calendar.getInstance()
//        instance[Calendar.YEAR, Calendar.MONTH - 1] = Calendar.DAY_OF_MONTH
        return calendar[Calendar.DAY_OF_WEEK] - 1
    }

    /**
     * 获取当前的毫秒数，精确到分钟
     *
     * @return 毫秒数
     */
    @JvmStatic
    val currentTimeMillisAccurateToMinute: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.timeInMillis
        }

    /**
     * 获取当前的毫秒数，精确到秒
     *
     * @return 毫秒数
     */
    @JvmStatic
    val currentTimeMillisAccurateToSecond: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar[Calendar.MILLISECOND] = 0
            return calendar.timeInMillis
        }

    /**
     * 获取当前的日期字符串
     *
     * @param format 输出的字符串格式，例：yyyy-MM-dd
     * @return 日期的字符串
     */
    @JvmStatic
    fun getCurrentDate(format: String): String {
        return getDate(format, System.currentTimeMillis())
    }

    /**
     * 获取输入毫秒数的日期字符串
     *
     * @param format     输出日期字符串的格式
     * @param timeMillis 毫秒数
     * @return 日期字符串
     */
    @JvmStatic
    fun getDate(format: String, timeMillis: Long): String {
        val date = Date(timeMillis)
        return getDate(format, date)
    }

    /**
     * 获取输入Date对象的日期字符串
     *
     * @param format 输出日期的格式
     * @param date   Date对象
     * @return 日期字符串
     */
    @JvmStatic
    fun getDate(format: String, date: Date): String {
        return SimpleDateFormat(format, Locale.ENGLISH).format(date)
    }

    /**
     * 获取输入Calendar对象的日期字符串
     *
     * @param format   输出日期的格式
     * @param calendar Calendar对象
     * @return 日期字符串
     */
    @JvmStatic
    fun getDate(format: String, calendar: Calendar): String {
        return SimpleDateFormat(format, Locale.ENGLISH).format(calendar.timeInMillis)
    }

    /**
     * 获取年份
     *
     * @param calendar 日历对象
     * @return 年
     */
    @JvmStatic
    fun getYear(calendar: Calendar): Int {
        return calendar[Calendar.YEAR]
    }

    /**
     * 获取月份
     *
     * @param calendar 日历对象
     * @return 月
     */
    @JvmStatic
    fun getMonth(calendar: Calendar): Int {
        return calendar[Calendar.MONTH] + 1 //月份是从0开始计算的
    }

    /**
     * 获取号，这个月的第几号
     *
     * @param calendar 日历对象
     * @return 号
     */
    @JvmStatic
    fun getDay(calendar: Calendar): Int {
        return calendar[Calendar.DAY_OF_MONTH]
    }

    /**
     * 获取最大日期
     *
     * @param calendar 日历对象
     * @return
     */
    @JvmStatic
    fun getMaxDay(calendar: Calendar): Int {
        return calendar.getActualMaximum(Calendar.DATE)
    }

    /**
     * 获取小时，今天的第几小时
     *
     * @param calendar 日历对象
     * @return 时
     */
    @JvmStatic
    fun getHour(calendar: Calendar): Int {
        return calendar[Calendar.HOUR_OF_DAY]
    }

    /**
     * 获取分钟
     *
     * @param calendar 日历对象
     * @return 分钟
     */
    @JvmStatic
    fun getMinute(calendar: Calendar): Int {
        return calendar[Calendar.MINUTE]
    }

    /**
     * 获取秒
     *
     * @param calendar 日历对象
     * @return 秒
     */
    @JvmStatic
    fun getSecond(calendar: Calendar): Int {
        return calendar[Calendar.SECOND]
    }

    /**
     * 拷贝Calendar对象
     *
     * @param calendar calendar对象
     * @return
     */
    @JvmStatic
    fun copy(calendar: Calendar): Calendar {
        val currentCalendar = currentCalendar
        currentCalendar.timeInMillis = calendar.timeInMillis
        return currentCalendar
    }

    /**
     * 得到当前Calendar
     *
     * @return Calendar对象
     */
    @JvmStatic
    val currentCalendar: Calendar
        get() {
            val c = Calendar.getInstance(Locale.ENGLISH)
            c.timeInMillis = System.currentTimeMillis()
            return c
        }
    //╔═════════════════════════════════════════════════
    //║----------------------------------------获取日期 End----------------------------------------
    //╚═════════════════════════════════════════════════

    //╔═════════════════════════════════════════════════
    //║----------------------------------------整合工具类 start-------------------------------------
    //╚═════════════════════════════════════════════════

    /**
     * 根据当前的时区获取当前的日期
     * @return 返回 YYYY_MM_DD_HH_MM格式的时间戳
     */
    @JvmStatic
    val todayDate: String
        get() {
            val vCalendar = Calendar.getInstance()
            vCalendar.timeZone = TimeZone.getTimeZone(TimeZoneUtil.getCurrentTimeZone())
            return getDate(YYYY_MM_DD_HH_MM, vCalendar)
        }

    /**
     * 今天到本周一的天数
     */
    @JvmStatic
    val todayToMondayNum: Int
        get() {
            val weekIndex = getWeekIndex(currentCalendar)
            return if (weekIndex == 0) 6 else weekIndex - 1
        }

    /**
     * 今天到上周末的天数
     */
    @JvmStatic
    val todayToLastWeekendNum: Int
        get() {
            val weekIndex = getWeekIndex(currentCalendar)
            return if (weekIndex == 0) 7 else weekIndex
        }

    /**
     * 今天到上周一的天数
     */
    @JvmStatic
    val todayToLastMondayNum: Int
        get() {
            val weekIndex = getWeekIndex(currentCalendar)
            return if (weekIndex == 0) 13 else weekIndex + 6
        }

    /**
     * 将毫秒数转日期
     *
     * @param timeMillis 毫秒数
     * @return yyyy-MM-dd HH:mm:ss
     */
    @JvmStatic
    fun getDate(timeMillis: Long): String = getDate(YYYY_MM_DD_HH_MM_SS, timeMillis)

    /**
     * 毫秒数转时间戳
     * @param format 日期格式，@DateStr限定
     * @param timeMillis 毫秒数
     * @return 返回去除横线冒号空格后的日期字符串
     */
    @JvmStatic
    fun timeMillisToTimeStamp(format: String, timeMillis: Long): String {
        return getDate(format, timeMillis)
                .replace("-", "")
                .replace(":", "")
                .replace(" ", "")
    }

    /**
     * 毫秒数转时间戳
     * @param timeMillis 毫秒数
     * @return 返回yyyyMMddHHmmss
     */
    @JvmStatic
    fun timeMillisToTimeStamp(timeMillis: Long): String {
        return timeMillisToTimeStamp(YYYY_MM_DD_HH_MM_SS, timeMillis)
    }

    /**
     * 将YYYY-MM-dd格式的日期转为YYYY/MM/dd
     * @param dateStr 日期字符串
     */
    @JvmStatic
    fun dateStrStrYMD(dateStr: String?): String {
        return if (dateStr.isNullOrEmpty()) {
            ""
        } else {
            convertDateStrToNewDateStr(YYYY_MM_DD, "yyyy/MM/dd", dateStr)
        }
    }

    /**
     * 根据传入的日期字符串获取今天星期几
     * @param dateTime 日期字符串 默认为yyyy-MM-dd格式
     * @param type 类型
     * @return 星期几(中文)
     */
    @JvmStatic
    fun getDayOfWeek(dateTime: String?, type: Int): String {
        if (type != 1 || dateTime.isNullOrEmpty()) {
            return ""
        }
        val date = convertDateStrToDate(YYYY_MM_DD, dateTime) ?: return ""
        val calendar = convertDateToCalendar(date)
        return getWeekStr(calendar)
    }

    /**
     * 根据传入的日期字符串获取今天星期几
     * @param dateTime 日期字符串 默认为yyyy-MM-dd格式
     * @return 星期几(中文)
     */
    @JvmStatic
    fun getDayOfWeek(dateTime: String?): String {
        return getDayOfWeek(dateTime, 1)
    }

    @JvmStatic
    fun convertDateStrToTimeMillis(dateStr: String): Long {
        val timeMillis = convertDateStrToTimeMillis(YYYY_MM_DD_HH_MM_SS, dateStr)
        return if (timeMillis == -1L) {
            System.currentTimeMillis()
        } else {
            timeMillis
        }
    }

    @JvmStatic
    fun convertDateStrToTimeMillisWithoutSecond(dateStr: String): Long {
        val timeMillis = convertDateStrToTimeMillis(YYYY_MM_DD_HH_MM, dateStr)
        return if (timeMillis == -1L) {
            System.currentTimeMillis()
        } else {
            timeMillis
        }
    }

    //╔═════════════════════════════════════════════════
    //║----------------------------------------整合工具类 end---------------------------------------
    //╚═════════════════════════════════════════════════

}