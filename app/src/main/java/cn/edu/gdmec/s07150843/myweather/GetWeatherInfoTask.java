package cn.edu.gdmec.s07150843.myweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chen on 2016/12/11.
 */
public class GetWeatherInfoTask extends AsyncTask<String,Void,List<Map<String,Object>>> {
    /*Activity上下文*/
    private Activity context;
    /*加载提示窗口*/
    private ProgressDialog progressDialog;
    /*错误信息*/
    private String errorMsg="网络错误!";
    /*天气信息列表*/
    private ListView weather_info;
    /*APL接口的格式*/
    private static String BASE_URL="http://v.juhe.cn/weather/index?format=2&cityname=";
    private static String key="&key=b739b342860da3818649ada0e3cf4f8b";

    public GetWeatherInfoTask(Activity context){
        this.context=context;
       /*获取提示框*/
        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("正在获取天气，请稍等...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<Map<String, Object>> result) {
        super.onPostExecute(result);
        progressDialog.dismiss();

        if(result.size()>0){
            weather_info= (ListView) context.findViewById(R.id.weather_info);
            SimpleAdapter simpleAdapter=new SimpleAdapter(context,result,R.layout.weather_item,new String[]{"temperature","weather","data","week","weather_icon"},new int[]{R.id.temperature,R.id.weather,R.id.date,R.id.week,R.id.weather_icon});
            weather_info.setAdapter(simpleAdapter);
        }else{
            Toast.makeText(context,errorMsg,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected List<Map<String, Object>> doInBackground(String... params) {

        List <Map<String,Object>> list=new ArrayList<Map<String,Object>>();

        try {
            HttpClient httpClient=new DefaultHttpClient();
            String url=BASE_URL+ URLEncoder.encode(params
                    [0],"UTF-8")+key;
            HttpGet httpget=new HttpGet(url);
            HttpResponse response=httpClient.execute(httpget);
            if(response.getStatusLine().getStatusCode()==200){
                String jsonString= EntityUtils.toString(response.getEntity(),"UTF-8");
                JSONObject jsondata=new JSONObject(jsonString);
                if(jsondata.getInt("resultcode")==200){
                    JSONObject result=jsondata.getJSONObject("result");
                    JSONArray weatherList=result.getJSONArray("future");
                    for(int i=0;i<7;i++){
                        Map<String,Object>item=new HashMap<String,Object>();
                        JSONObject weatObject=weatherList.getJSONObject(i);
                        item.put("temperature",weatObject.getString("temperature"));
                        item.put("weather",weatObject.getString("weather"));
                        item.put("date",weatObject.getString("date"));
                        item.put("week",weatObject.getString("week"));
                        item.put("wind",weatObject.getString("wind"));
                        JSONObject wid=weatObject.getJSONObject("weather_id");
                        int weather_icon=wid.getInt("fa");
                        item.put("weather_icon",WeatherIcon.weather_icons[weather_icon]);
                        list.add(item);
                    }
                }else{
                    errorMsg="非常抱歉,本应用暂不支持你所请求的城市!";
                }
            }else{
                errorMsg="网络错误，请检查手机是否开启了网络!!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}