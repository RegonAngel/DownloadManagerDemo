package com.example.regon.rxjavademo;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author shuai
 * @date 2019/3/26
 * @description:
 */
public class CloseUtils {

    public static void closeIO(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
