
//  Класс для отправки запроса и получения ответа от сервера (сайта)
package com.example.converter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class DAO {
    public static String getResponceFromURL(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scan = new Scanner(in);
            scan.useDelimiter("\\A");

            boolean hasInput = scan.hasNext();

            if (hasInput) {
                return scan.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
