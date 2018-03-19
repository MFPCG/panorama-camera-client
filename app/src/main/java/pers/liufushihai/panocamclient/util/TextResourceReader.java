package pers.liufushihai.panocamclient.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Date        : 2018/3/12
 * Author      : liufushihai
 * Description : glsl资源加载类
 */

public class TextResourceReader {
    public static String readTextFileFromResource(Context context,
                                                  int resourceId){
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);

            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);

            BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null){
                body.append(nextLine).append('\n');
            }
        }catch (IOException e){
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        }catch (Resources.NotFoundException nfe){
            throw new RuntimeException(
                    "Resource not found: " + resourceId,nfe);
        }finally {

        }
        return body.toString();
    }
}
