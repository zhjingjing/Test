package com.zh.test.test4;


import android.content.Context;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.zh.test.test4.LogImpl.getMessage;

/**
 * Author: zeal zuo
 * Date: 18/10/26.
 * <p>
 * 日志模块
 * 采用内存映射，流畅性，性能高，防丢失.
 * <p>
 * 支持日志级别设置 {@link #setLogLevel(int)}
 * <p>
 * 支持日志自定义目录 {@link #open(Context, int, String)}
 * <p>
 * 支持缓冲同步和异常刷新 {@link #appenderFlush(boolean)}
 * 默认日志会保存10天
 */
@SuppressWarnings({"WeakerAccess"})
public class ZLog {
    private static final String TAG = "ZLog";
    /**
     * 允许全部
     */
    public static final int LEVEL_ALL = 0;
    /**
     * 详细级别
     */
    public static final int LEVEL_VERBOSE = 0;
    /**
     * 调试级别
     */
    public static final int LEVEL_DEBUG = 1;
    /**
     * 消息级别
     */
    public static final int LEVEL_INFO = 2;
    /**
     * 警告级别
     */
    public static final int LEVEL_WARNING = 3;
    /**
     * 错误级别
     */
    public static final int LEVEL_ERROR = 4;
    /**
     * 严重错误级别
     */
    public static final int LEVEL_FATAL = 5;
    /**
     * 不输出日志
     */
    public static final int LEVEL_NONE = 6;
    /**
     * 异步模式，推荐
     */
    public static final int AppenderModeAsync = 0;
    /**
     * 同步模式
     */
    public static final int AppenderModeSync = 1;

    /**
     * 日志记录
     */
    public static class ZLoggerInfo {
        /**
         * 日志级别
         */
        public int level;
        /**
         * 标签
         */
        public String tag;
        /**
         * 文件名
         */
        public String filename;
        /**
         * 方法名
         */
        public String funcname;
        /**
         * 行号
         */
        public int line;
        /**
         * 进程号
         */
        public long pid;
        /**
         * 线程号
         */
        public long tid;
        /**
         * 主线程号
         */
        public long maintid;
    }

    private static int sSelfPid;

    static {
        sSelfPid = Process.myPid();
        //allow disk read
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("zplatformlog");
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    /**
     * 打开日志, 默认存储 {@link Environment#getExternalStorageDirectory()}
     * 进程启动时初始化
     *
     * @param context 应用程序上下文
     * @param level   日志级别
     * @param logdir  日志目录
     */
    public static void open(Context context, int level, String logdir) {
        open(level,
                AppenderModeAsync,
                new File(context.getFilesDir(), "platform-zlog").getAbsolutePath(),
                logdir,
                null,
                null);
    }

    /**
     * 打开日志
     * 进程启动时初始化
     *
     * @param context    应用程序上下文
     * @param level      日志级别
     * @param logDir     日志目录
     * @param namePrefix 日志前缀
     */
    public static void open(Context context, int level, String logDir, String namePrefix) {
        open(level,
                AppenderModeAsync,
                new File(context.getFilesDir(), "platform-zlog").getAbsolutePath(),
                logDir,
                namePrefix,
                null);
    }


    /**
     * 打开日志
     * 进程启动时初始化
     *
     * @param context    应用程序上下文
     * @param level      日志级别
     * @param logDir     日志目录
     * @param namePrefix 日志前缀
     * @param key        加密秘钥
     */
    public static void open(Context context, int level, String logDir, String namePrefix, String key) {
        open(level,
                AppenderModeAsync,
                new File(context.getFilesDir(), "platform-zlog").getAbsolutePath(),
                logDir,
                namePrefix,
                key);
    }

    /**
     * 打开日志
     *
     * @param level      日志级别
     * @param mode       日志模式
     * @param cacheDir   候补目录
     * @param logDir     日志目录
     * @param namePrefix 日志文件前缀 注意如果需要支持多进程，该字段最好为null，否则需要确保不同进程，有不同的文件前缀
     * @param pubkey     加密秘钥
     */
    public static void open(int level, int mode, String cacheDir,
                            String logDir, String namePrefix, String pubkey) {
        if (namePrefix == null) {
            namePrefix = getCurrentProcessNameUsingProc();
        }
        open(level, mode, cacheDir, logDir, namePrefix, pubkey, 10);
    }

    /**
     * 打开日志
     *
     * @param level       日志级别
     * @param mode        日志模式
     * @param cacheDir    候补目录
     * @param logDir      日志目录
     * @param nameprefix  日志文件前缀 注意如果需要支持多进程，该字段最好为null，否则需要确保不同进程，有不同的文件前缀
     * @param pubkey      加密秘钥
     * @param maxKeepTime 日志保存时间，单位为天
     */
    public static void open(int level, int mode, String cacheDir,
                            String logDir, String nameprefix, String pubkey, int maxKeepTime) {
        if (nameprefix == null) {
            nameprefix = getCurrentProcessNameUsingProc();
        }
        appenderOpen(level, mode, cacheDir, logDir, nameprefix, pubkey, maxKeepTime);
    }


    private static String sProcessName;

    /**
     * Helper method to get the name of the process this instance is running in using information
     * published by the kernel in the /proc process.
     * <p>
     * <p>This method is unfortunately only reliable if the current process name is relatively short.
     * We have empircally seen process names truncated at 75 chars but it could be even less.
     *
     * @return returns the current process name or an empty sting if one cannot be found.
     */
    private static String getCurrentProcessNameUsingProc() {
        if (sProcessName != null) {
            return sProcessName;
        }
        BufferedReader br = null;
        String processName = "";
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/self/cmdline"), "UTF-8"));
            processName = br.readLine().trim();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
        int index = processName.lastIndexOf(":");
        if (index == -1 || index == processName.length() - 1) {
            processName = "main";
        } else {
            processName = processName.substring(index + 1);
        }
        return sProcessName = processName;
    }

    private static String decryptTag(String tag) {
        return tag;
    }

    private static native void logWrite(ZLoggerInfo loggerInfo, String log);

    static native void logWrite2(int level, String tag, int pid, long tid, String log);

    /**
     * 获取日志级别
     */
    public static native int getLogLevel();

    /**
     * 设置日志级别
     */
    public static native void setLogLevel(int logLevel);

    /**
     * 获取日志打开目录
     */
    public static native String getLogDir();

    /**
     * 设置记录模式
     *
     * @param mode 同步/异步
     */
    public static native void setAppenderMode(int mode);

    /**
     * 是否打印日志文件到控制台(Logcat输出)
     *
     * @param isOpen 是否打开, 默认关闭
     */
    public static native void setConsoleLogOpen(boolean isOpen);

    /**
     * 是否将控制台的所有日志输出为错误级别
     *
     * @param allError 是否为错误级别, 默认关闭
     */
    public static native void setConsoleLogAllError(boolean allError);

    /**
     * 打开日志记录
     *
     * @param level      级别
     * @param mode       模式
     * @param cacheDir   候补文件目录
     * @param logDir     日志文件目录
     * @param namePrefix 文件前缀 注意如果需要支持多进程，该字段最好为null，否则需要确保不同进程，有不同的文件前缀
     * @param pubkey     加密秘钥，null则不加密
     */
    private static native void appenderOpen(int level, int mode, String cacheDir, String logDir, String namePrefix, String pubkey, int maxKeepTime);


    /**
     * 关闭日志记录
     */
    public static native void appenderClose();

    /**
     * 刷新缓冲到日志文件
     *
     * @param isSync 是否同步刷新
     */
    public static native void appenderFlush(boolean isSync);


    /**
     * 获取上一次写文件路径
     * just for debug
     */
    public static native String getLastWriteFilePath();

    /**
     * 获取当前进程Id
     */
    public static native int getMyPid();

    /**
     * 获取父进程Id
     *
     * @param pid 进程Id
     * @return 父进程Id
     */
    public static native int getParentPid(int pid);

    /**
     * 获取指定进程名称的进程名称
     *
     * @param pid 进程ID
     * @return 进程名称
     */
    public static native String getProcessName(int pid);

    /**
     * 是否输出详细日志
     */
    public static boolean isVerbose() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_VERBOSE;
    }

    /**
     * 是否输出调试
     */
    public static boolean isDebug() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_DEBUG;
    }

    /**
     * 是否输出信息日志
     */
    public static boolean isInfo() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_INFO;
    }

    /**
     * 是否输出警告日志
     */
    public static boolean isWarning() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_WARNING;
    }

    /**
     * 是否输出错误日志
     */
    public static boolean isError() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_ERROR;
    }

    /**
     * 是否输出严重错误日志
     */
    public static boolean isFatal() {
        return ZLog.getLogLevel() <= ZLog.LEVEL_FATAL;
    }

    /**
     * 输出详细日志
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void v(String tag, String log) {
        v(tag, log, null);
    }

    /**
     * 输出消息日志
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void i(String tag, String log) {
        i(tag, log, null);
    }

    /**
     * 输出调试日志
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void d(String tag, String log) {
        d(tag, log, null);
    }

    /**
     * 输出警告
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void w(String tag, String log) {
        w(tag, log, null);
    }

    /**
     * 输出错误
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void e(String tag, String log) {
        e(tag, log, null);
    }

    /**
     * 输出致命错误
     *
     * @param tag 标签
     * @param log 日志内容
     */
    public static void f(String tag, String log) {
        f(tag, log, null);
    }

    /**
     * 输出异常
     *
     * @param e 异常
     * @see #e(String, String, Throwable)
     * @deprecated
     */
    public static void e(Throwable e) {
        if (null != e) {
            logWrite2(LEVEL_ERROR, "", sSelfPid, Process.myTid(), e.getMessage());
        }
    }

    /**
     * 输出详细日志
     */
    public static void v(String tag, String msg, Throwable tr) {
        if (isVerbose()) {
            log(LEVEL_VERBOSE, tag, msg, tr);
        }
    }

    /**
     * 输出调试日志
     */
    public static void d(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            log(LEVEL_DEBUG, tag, msg, tr);
        }
    }

    /**
     * 输出信息日志
     */
    public static void i(String tag, String msg, Throwable tr) {
        if (isInfo()) {
            log(LEVEL_INFO, tag, msg, tr);
        }
    }

    /**
     * 输出警告日志
     */
    public static void w(String tag, String msg, Throwable tr) {
        if (isWarning()) {
            log(LEVEL_WARNING, tag, msg, tr);
        }
    }

    /**
     * 输出错误日志
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (isError()) {
            log(LEVEL_ERROR, tag, msg, tr);
        }
    }

    /**
     * 输出严重错误日志
     */
    public static void f(String tag, String msg, Throwable tr) {
        if (isFatal()) {
            log(LEVEL_FATAL, tag, msg, tr);
        }
    }

    /**
     * General log function that accepts all configurations as parameter
     */
    private static void log(int priority, String tag, String message, Throwable throwable) {
        switch (priority) {
            case LEVEL_VERBOSE:
                LogImpl.logWrite2(LEVEL_VERBOSE, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            case LEVEL_DEBUG:
                LogImpl.logWrite2(LEVEL_DEBUG, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            case LEVEL_INFO:
                LogImpl.logWrite2(LEVEL_INFO, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            case LEVEL_WARNING:
                LogImpl.logWrite2(LEVEL_WARNING, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            case LEVEL_ERROR:
                LogImpl.logWrite2(LEVEL_ERROR, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            case LEVEL_FATAL://FILE FLUX ANR NET
                LogImpl.logWrite2(LEVEL_FATAL, tag, sSelfPid, Process.myTid(), getMessage(message, throwable));
                break;
            default:
                break;
        }
    }

    /**
     * 设置敏感词列表, 大小写不敏感, 默认为空列表
     *
     * @param sensitiveWord 敏感词列表，替换字符默认为'*'
     */
    public static void setSensitiveWord(final Collection<String> sensitiveWord) {
        LogImpl.setSensitiveKeyList(sensitiveWord, '*');
    }

    /**
     * 设置敏感词列表, 大小写不敏感, 默认为空列表
     *
     * @param sensitiveWord 敏感词列表
     * @param replaceChar   替换字符
     */
    public static void setSensitiveWord(final Collection<String> sensitiveWord, char replaceChar) {
        LogImpl.setSensitiveKeyList(sensitiveWord, replaceChar);
    }
}
