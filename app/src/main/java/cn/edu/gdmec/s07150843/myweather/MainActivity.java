package cn.edu.gdmec.s07150843.myweather;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {
    private MainActivity seft;
    /**/
    private Map<String,List<String>> cityMap;
    /*省份列表*/
    private Spinner province_spinner;
    /*相应的城市列表*/
    private Spinner city_spinner;
    /*选择城市的dialog*/
    private AlertDialog choose_dialog;
    private LinearLayout choose_layout;
    private ImageButton settingBtn;
    private ImageButton refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        seft=this;
        setContentView(R.layout.activity_main);
        /*初始化省，市map*/
        initProvinces();

        initChooseDialog();

        settingBtn= (ImageButton) this.findViewById(R.id.setting);
        refreshBtn= (ImageButton) this.findViewById(R.id.refresh);

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose_dialog.show();
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName=((TextView)seft.findViewById(R.id.city)).getText().toString();
                /*获取天气的资源*/
                new GetWeatherInfoTask(seft).execute(cityName);
            }
        });
        new GetWeatherInfoTask(this).execute("广州");
    }

    private void initChooseDialog() {
        choose_dialog=new AlertDialog.Builder(seft).setTitle("选择城市").setPositiveButton("确定",new ChooseCityListenner()).setNegativeButton("取消",null).create();
        LayoutInflater inflater=LayoutInflater.from(this);
        choose_layout= (LinearLayout) inflater.inflate(R.layout.choose,null);
        province_spinner= (Spinner) choose_layout.findViewById(R.id.province_spinner);
        ArrayAdapter<String> provinceAdapter=new ArrayAdapter<String>(this,R.layout.simple_list_item,new ArrayList<String>(cityMap.keySet()));
        province_spinner.setAdapter(provinceAdapter);
        city_spinner= (Spinner) choose_layout.findViewById(R.id.city_spinner);
        province_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province_name=province_spinner.getSelectedItem().toString();
                ArrayAdapter<String> cityAdapter=new ArrayAdapter<String>(seft,R.layout.simple_list_item,cityMap.get(province_name));
                city_spinner.setAdapter(cityAdapter);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        choose_dialog.setView(choose_layout);

    }
    private class ChooseCityListenner implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String city_name=city_spinner.getSelectedItem().toString();
            TextView cityName= (TextView)seft.findViewById(R.id.city);
            cityName.setText(city_name);
            new GetWeatherInfoTask(seft).execute(city_name);
        }
    }
    /*解析XML文件，初始化省份与城市*/
    private void initProvinces() {
        AssetManager assetManager =getAssets();
        SaxHandler handler=new SaxHandler();
        InputStream inputStream=null;

        try {
            inputStream=assetManager.open("City.xml");
            SAXParserFactory factory=SAXParserFactory.newInstance();
            SAXParser parser=factory.newSAXParser();
            parser.parse(inputStream,handler);
            cityMap=handler.getCityMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}