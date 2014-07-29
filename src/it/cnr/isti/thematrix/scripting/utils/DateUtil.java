/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.utils;

import dexter.lexter.QualifiedToken;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author edoardovacchi
 */
public class DateUtil {
    
    
    static final DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
    static final DateFormat datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    static final DateFormat datetimesec = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    static final DateFormat datetimesecmill = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");

    // same as in Neverlang...
    // changed: added matching for the tenth of seconds... Oracle adds them and we fail to parse them 
    static final Pattern dtpattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}(( )+[0-9]{2}:[0-9]{2}(:[0-9]{2}(.[0-9])?)?)?");
    //    static final Pattern dtpattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}(( )+[0-9]{2}:[0-9]{2}(:[0-9]{2})?)?");

    public static java.util.Date parse(String input) {
		/**
		 *  deal with special case 0000-00-00 which we consider synonym of missing date
		 *  
		 * FIXME relies on the assumption that it will be used in a Symbol.setValue()
		 */
    	if (input.equals("0000-00-00")) return null; 

    	Matcher m = dtpattern.matcher(input);
        if (!m.matches()) throw new IllegalArgumentException("DateUtil.parse(String) can't parse "+input);
        return parse(m);
    }
    
    /**
     * FIXME Is this parsing function needed by Neverlang? is it undocumented dependency?
     * @param tok
     * @return
     */
    public static java.util.Date parse(QualifiedToken tok) {
        return parse(tok.matches);
    }
    
    private static java.util.Date parse(MatchResult mr) {
        try {
        	// only date (group 1 is empty)
            if (mr.group(1)==null) 
                    return date.parse(mr.group(0));
            // date and time (group 3 is empty)
            else if (mr.group(3)==null)
                    return datetime.parse(mr.group(0));
            // date and time w/sec + millisec
            else if (mr.group(4)!=null)
                    return datetimesecmill.parse(mr.group(0));
            // date and time w/sec
            else return datetimesec.parse(mr.group(0));
            
        } catch (ParseException ex) {
            throw new IllegalArgumentException("DateUtil.parse(MatchResult) got exception"+ex);
        }
    }

	public static String toDateString(Date value) {
		Calendar c = Calendar.getInstance();
		c.setTime(value);
		int d = c.get(Calendar.DAY_OF_MONTH);
		int m = 1+c.get(Calendar.MONTH);
		int y = c.get(Calendar.YEAR);
		
		return String.format("%d-%02d-%02d", y,m,d);
		
	}
}
