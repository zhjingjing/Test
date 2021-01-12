package com.zh.test.test5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DeviceUtil {

    // Android 6.0以上获取WiFi的Mac地址

    //由于android6.0对wifi mac地址获取进行了限制，用原来的方法获取会获取到02:00:00:00:00:00这个固定地址。

    //但是可以通过读取节点进行获取"/sys/class/net/wlan0/address"

    public static String getMacAddr() {

        try {

            return loadFileAsString("/sys/class/net/wlan0/address")

                    .toUpperCase().substring(0, 17);

        } catch (IOException e) {

            e.printStackTrace();

            return "";

        }

    }

    private static String loadFileAsString(String filePath)

            throws java.io.IOException {

        StringBuffer fileData = new StringBuffer(1000);

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        char[] buf = new char[1024];

        int numRead = 0;

        while ((numRead = reader.read(buf)) != -1) {

            String readData = String.valueOf(buf, 0, numRead);

            fileData.append(readData);

        }

        reader.close();

        return fileData.toString();

    }

}
