package com.zh.test.test4;

import android.text.TextUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:  <br>
 * Date: 2020-04-23 10:16 <br>
 * Author: zealzuo  <br>
 */
public class LogImpl {
    private static Map<String, String> sensitiveKeyword = null;

    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * designed to ignore unknown host address
     */
    public static String getMessage(String message, Throwable throwable) {
        if (throwable != null && message != null) {
            message += " : " + getStackTraceString(throwable);
        }
        if (throwable != null && message == null) {
            message = getStackTraceString(throwable);
        }
        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }
        return message;
    }

    static synchronized void setSensitiveKeyList(final Collection<String> keyList, final char replaceChar) {
        final HashMap<String, String> map = new HashMap<>();
        for (String key : keyList) {
            final char[] replaceArray = new char[key.length()];
            Arrays.fill(replaceArray, replaceChar);

            final char[] keyRegex = new char[key.length() * 4];
            int index = 0;
            for (char c : key.toCharArray()) {
                keyRegex[index] = '[';
                keyRegex[index + 1] = Character.toLowerCase(c);
                keyRegex[index + 2] = Character.toUpperCase(c);
                keyRegex[index + 3] = ']';
                index += 4;
            }
            map.put(new String(keyRegex), new String(replaceArray));
        }
        sensitiveKeyword = map;
    }


    static void logWrite2(int level, String tag, int pid, long tid, String log) {
        final Map<String, String> keywordMap;
        synchronized (LogImpl.class) {
            keywordMap = sensitiveKeyword;
        }
        if (keywordMap != null) {
            for (Map.Entry<String, String> keyword : keywordMap.entrySet()) {
                tag = tag.replaceAll(keyword.getKey(), keyword.getValue());
                log = log.replaceAll(keyword.getKey(), keyword.getValue());
            }
        }
        ZLog.logWrite2(level, tag, pid, tid, log);
    }
}
