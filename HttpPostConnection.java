package sg.azlabs.openam.jira.seraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HttpPostConnection {

    // reuse single http client connection and settings
    private static HttpClient httpclient;
    private static HttpParams httpparams;

    private static ThreadSafeClientConnManager cm;

    static {
        // give default
        int connectionPoolMaxTotal = 200;
        int connectionPoolDefaultMaxPerRoute = 50;

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        cm = new ThreadSafeClientConnManager(schemeRegistry);
        cm.setMaxTotal(connectionPoolMaxTotal);
        // Increase default max connection per route
        cm.setDefaultMaxPerRoute(connectionPoolDefaultMaxPerRoute);

        httpclient = new DefaultHttpClient(cm);
        httpparams = new BasicHttpParams();
        httpparams.setParameter("http.protocol.handle-redirects", false);
    }

    public HttpPostConnection(String url) {
        httppost   = new HttpPost(url);
        formparams = new ArrayList<NameValuePair>();
    }

    public void addParameter(String name, String value) {
        formparams.add(new BasicNameValuePair(name, value));

    }

    public void execute() throws IOException {
        HttpEntity requestEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setParams(httpparams);
        httppost.setEntity(requestEntity);

        response = httpclient.execute(httppost);

        HttpEntity responseEntity = response.getEntity();
        EntityUtils.consume(responseEntity);
    }

    public String executeWithResponse() throws IOException {
        //msg.debug("[PROFILE] HttpPostConnection.executeWithResponse - Current Conn Pool: " + cm.getConnectionsInPool());

        if (formparams != null && formparams.size() > 0) {
            HttpEntity requestEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(requestEntity);
        }
        httppost.setParams(httpparams);
        response = httpclient.execute(httppost);
        HttpEntity responseEntity = response.getEntity();
        return EntityUtils.toString(responseEntity);
    }

    public int getResponseCode() {
        return response.getStatusLine().getStatusCode();
    }

    public HeaderIterator getHeaderIterator() {
        return response.headerIterator();
    }

    public String getHeaderField(String name) {
        Header header = response.getFirstHeader(name);
        if ( header == null ) {
            return "";
        }
        return header.getValue();
    }

    private HttpPost httppost;
    private List<NameValuePair> formparams;
    private HttpResponse response;
