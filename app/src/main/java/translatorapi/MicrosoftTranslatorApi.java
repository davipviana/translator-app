package translatorapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public abstract class MicrosoftTranslatorApi {
    protected static final String ENCODING = "UTF-8";
    protected static String apiKey;
    private static String DatamarketAccessUri = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static String referrer;
    private static String clientId;
    private static String clientSecret;
    private static String token;
    private static long tokenExpiration = 0L;
    private static String contentType = "text/plain";
    protected static final String PARAM_APP_ID = "appId=";
    protected static final String PARAM_TO_LANG = "&to=";
    protected static final String PARAM_FROM_LANG = "&from=";
    protected static final String PARAM_TEXT_SINGLE = "&text=";
    protected static final String PARAM_TEXT_ARRAY = "&texts=";
    protected static final String PARAM_SPOKEN_LANGUAGE = "&language=";
    protected static final String PARAM_SENTENCES_LANGUAGE = "&language=";
    protected static final String PARAM_LOCALE = "&locale=";
    protected static final String PARAM_LANGUAGE_CODES = "&languageCodes=";

    public MicrosoftTranslatorApi() {
    }

    public static void setKey(String pKey) {
        apiKey = pKey;
    }

    public static void setContentType(String pKey) {
        contentType = pKey;
    }

    public static void setClientId(String pClientId) {
        clientId = pClientId;
    }

    public static void setClientSecret(String pClientSecret) {
        clientSecret = pClientSecret;
    }

    public static void setHttpReferrer(String pReferrer) {
        referrer = pReferrer;
    }

    public static String getToken(String apikey) throws Exception {
        URL url = new URL(DatamarketAccessUri);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        if (referrer != null) {
            uc.setRequestProperty("referer", referrer);
        }

        uc.setRequestProperty("Ocp-Apim-Subscription-Key", apikey);
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        uc.setRequestProperty("Accept-Charset", "UTF-8");
        uc.setRequestMethod("POST");
        uc.setDoOutput(true);

        String var8;
        try {
            int responseCode = uc.getResponseCode();
            String result = inputStreamToString(uc.getInputStream());
            if (responseCode != 200) {
                throw new Exception("Error from Microsoft Translator API: " + result);
            }

            var8 = result;
        } finally {
            if (uc != null) {
                uc.disconnect();
            }

        }

        return var8;
    }

    private static String retrieveResponse(URL url) throws Exception {
        if (apiKey != null && System.currentTimeMillis() > tokenExpiration) {
            String tokenStr = getToken(apiKey);
            token = "Bearer " + tokenStr;
        }

        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        if (referrer != null) {
            uc.setRequestProperty("referer", referrer);
        }

        uc.setRequestProperty("Content-Type", contentType + "; charset=" + "UTF-8");
        uc.setRequestProperty("Accept-Charset", "UTF-8");
        if (token != null) {
            uc.setRequestProperty("Authorization", token);
        }

        uc.setRequestMethod("GET");
        uc.setDoOutput(true);

        String var4;
        try {
            int responseCode = uc.getResponseCode();
            String result = inputStreamToString(uc.getInputStream());
            if (responseCode != 200) {
                throw new Exception("Error from Microsoft Translator API: " + result);
            }

            var4 = result;
        } finally {
            if (uc != null) {
                uc.disconnect();
            }

        }

        return var4;
    }

    protected static String retrieveString(URL url) throws Exception {
        try {
            return retrieveResponse(url);
        } catch (Exception var2) {
            throw new Exception("[microsoft-translator-api] Error retrieving translation : " + var2.getMessage(), var2);
        }
    }

    protected static String[] retrieveStringArr(URL url, String jsonProperty) throws Exception {
        try {
            String response = retrieveResponse(url);
            return jsonToStringArr(response, jsonProperty);
        } catch (Exception var3) {
            throw new Exception("[microsoft-translator-api] Error retrieving translation.", var3);
        }
    }

    protected static String[] retrieveStringArr(URL url) throws Exception {
        return retrieveStringArr(url, (String)null);
    }

    protected static Integer[] retrieveIntArray(URL url) throws Exception {
        try {
            String response = retrieveResponse(url);
            return jsonToIntArr(response);
        } catch (Exception var2) {
            throw new Exception("[microsoft-translator-api] Error retrieving translation : " + var2.getMessage(), var2);
        }
    }

    private static Integer[] jsonToIntArr(String inputString) throws Exception {
        JSONArray jsonArr = new JSONArray(inputString);

        Integer[] intArr = new Integer[jsonArr.length()];

        for(int i = 0; i < jsonArr.length(); i++) {
            intArr[i] = jsonArr.optInt(i);
        }

        return intArr;
    }

    private static String jsonToString(String inputString) throws Exception {
        return new JSONObject(inputString).toString();
    }

    private static String[] jsonToStringArr(String inputString, String propertyName) throws Exception {
        JSONArray jsonArr = new JSONArray(inputString);
        String[] values = new String[jsonArr.length()];

        for(int i = 0; i < jsonArr.length(); i++) {
            if (propertyName != null && propertyName.length() != 0) {
                JSONObject json = jsonArr.optJSONObject(i);
                if (json.has(propertyName)) {
                    values[i] = json.optString(propertyName);
                }
            } else {
                values[i] = jsonArr.optString(i);
            }
        }

        return values;
    }

    private static String inputStreamToString(InputStream inputStream) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();

        try {
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String string;
                while(null != (string = reader.readLine())) {
                    outputBuilder.append(string.replaceAll("\ufeff", ""));
                }
            }
        } catch (Exception var4) {
            throw new Exception("[microsoft-translator-api] Error reading translation stream.", var4);
        }

        return outputBuilder.toString();
    }

    protected static void validateServiceState() throws Exception {
        if (apiKey != null && apiKey.length() < 16) {
            throw new RuntimeException("INVALID_API_KEY - Please set the API Key with your Bing Developer's Key");
        } else if (apiKey == null && (clientId == null || clientSecret == null)) {
            throw new RuntimeException("Must provide a Windows Azure Marketplace Client Id and Client Secret - Please see http://msdn.microsoft.com/en-us/library/hh454950.aspx for further documentation");
        }
    }

    protected static String buildStringArrayParam(Object[] values) {
        StringBuilder targetString = new StringBuilder("[\"");
        Object[] arr$ = values;
        int len$ = values.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Object obj = arr$[i$];
            if (obj != null) {
                String value = obj.toString();
                if (value.length() != 0) {
                    if (targetString.length() > 2) {
                        targetString.append(",\"");
                    }

                    targetString.append(value);
                    targetString.append("\"");
                }
            }
        }

        targetString.append("]");
        return targetString.toString();
    }
}

