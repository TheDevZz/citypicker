package com.ihidea.as.citypicker.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ihidea.as.citypicker.R;
import com.ihidea.as.citypicker.model.Api;
import com.ihidea.as.citypicker.model.ApiResponse;
import com.ihidea.as.citypicker.model.IApi;
import com.ihidea.as.citypicker.model.RegionDto;
import com.ihidea.as.citypicker.model.RegionQueryDto;
import com.lljjcoder.Interface.OnCityItemClickListener;
import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;
import com.lljjcoder.citywheel.CityParseHelper;
import com.lljjcoder.style.cityjd.JDCityConfig;
import com.lljjcoder.style.cityjd.JDCityPicker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CitypickerJDActivity extends AppCompatActivity {
    JDCityPicker cityPicker;
    private Button jdBtn;
    private TextView resultV;
    TextView mTwoTv;
    TextView mThreeTv;

    public JDCityConfig.ShowType mWheelType = JDCityConfig.ShowType.PRO_CITY;


    private JDCityConfig jdCityConfig = new JDCityConfig.Builder().build();
    private JDCityPicker.IDataSource iDataSource;
    private IApi iApi;
    private CityParseHelper cityParseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citypicker_jd);

        jdBtn = (Button) findViewById(R.id.jd_btn);
        resultV = (TextView) findViewById(R.id.result_tv);
        mTwoTv = (TextView) findViewById(R.id.two_tv);
        mThreeTv = (TextView) findViewById(R.id.three_tv);

        jdCityConfig.setShowType(mWheelType);

        //二级联动，只显示省份， 市，不显示区
        mTwoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWheelType = JDCityConfig.ShowType.PRO_CITY;
                setWheelType(mWheelType);
                jdCityConfig.setShowType(mWheelType);
            }
        });

        //三级联动，显示省份， 市和区
        mThreeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWheelType = JDCityConfig.ShowType.PRO_CITY_DIS;
                setWheelType(mWheelType);
                jdCityConfig.setShowType(mWheelType);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        iApi = retrofit.create(IApi.class);

        iDataSource = new JDCityPicker.IDataSource() {
            @Override
            public List<ProvinceBean> getProvinceList() {
                Log.d("ZZZ","getProvinceList");
                ArrayList<ProvinceBean> list = new ArrayList<>();
                ApiResponse<List<RegionDto>> apiResponse = ApiResponse.execute(iApi.getRegionList(new RegionQueryDto(-1)));
                List<RegionDto> data = apiResponse.data;
                if (data != null) {
                    for (RegionDto item : data) {
                        ProvinceBean provinceBean = new ProvinceBean();
                        provinceBean.setId(String.valueOf(item.id));
                        provinceBean.setName(item.name);
                        list.add(provinceBean);
                    }
                }
                return list;
            }

            @Override
            public List<CityBean> getCityList(ProvinceBean provinceBean) {
                Log.d("ZZZ","getCityList");
                String idStr = provinceBean.getId();
                int id = Integer.parseInt(idStr);
                ArrayList<CityBean> list = new ArrayList<>();
                ApiResponse<List<RegionDto>> apiResponse = ApiResponse.execute(iApi.getRegionList(new RegionQueryDto(id)));
                List<RegionDto> data = apiResponse.data;
                if (data != null) {
                    for (RegionDto item : data) {
                        CityBean cityBean = new CityBean();
                        cityBean.setId(String.valueOf(item.id));
                        cityBean.setName(item.name);
                        list.add(cityBean);
                    }
                }
                return list;
            }

            @Override
            public List<DistrictBean> getDistrictList(CityBean cityBean) {
                Log.d("ZZZ","getDistrictList");
                String idStr = cityBean.getId();
                int id = Integer.parseInt(idStr);
                ArrayList<DistrictBean> list = new ArrayList<>();
                ApiResponse<List<RegionDto>> apiResponse = ApiResponse.execute(iApi.getRegionList(new RegionQueryDto(id)));
                List<RegionDto> data = apiResponse.data;
                if (data != null) {
                    for (RegionDto item : data) {
                        DistrictBean districtBean = new DistrictBean();
                        districtBean.setId(String.valueOf(item.id));
                        districtBean.setName(item.name);
                        list.add(districtBean);
                    }
                }
                return list;
            }

            @Override
            public void execute(final Runnable asyncTask) {
                new Thread(asyncTask).start();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                cityParseHelper = new CityParseHelper();
                ApiResponse<List<RegionDto>> apiResponse = ApiResponse.execute(iApi.getRegionListAll());
                List<RegionDto> data = apiResponse.data;
                if (data == null) {
                    Log.e("ZZZ", "data is null");
                    return;
                }
                ArrayList<ProvinceBean> provinceBeanArrayList = new ArrayList<>(data.size());
                for (RegionDto province : data) {
                    ProvinceBean provinceBean = new ProvinceBean();
                    provinceBeanArrayList.add(provinceBean);
                    provinceBean.setId(String.valueOf(province.id));
                    provinceBean.setName(province.name);
                    ArrayList<CityBean> cityBeans = new ArrayList<>();
                    provinceBean.setCityList(cityBeans);
                    if (province.children != null) {
                        for (RegionDto city : province.children) {
                            CityBean cityBean = new CityBean();
                            cityBeans.add(cityBean);
                            cityBean.setId(String.valueOf(city.id));
                            cityBean.setName(city.name);
                            ArrayList<DistrictBean> districtBeans = new ArrayList<>();
                            cityBean.setCityList(districtBeans);
                            if (city.children != null) {
                                for (RegionDto district : city.children) {
                                    DistrictBean districtBean = new DistrictBean();
                                    districtBeans.add(districtBean);
                                    districtBean.setId(String.valueOf(district.id));
                                    districtBean.setName(district.name);
                                }
                            }
                        }
                    }
                }
                cityParseHelper.setProvinceBeanArrayList(provinceBeanArrayList);
                cityPicker.init(CitypickerJDActivity.this, cityParseHelper);
            }
        }).start();

        Log.d("ZZZ", "activity");
        initPicker();
        jdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJD();
            }
        });
    }

    private void initPicker() {
        cityPicker = new JDCityPicker();
        //初始化数据
//        cityPicker.init(this, iDataSource);
//        cityPicker.init(this, cityParseHelper);
//        cityPicker.init(this, iDataSource);
        //设置JD选择器样式位只显示省份和城市两级
        cityPicker.setConfig(jdCityConfig);
        cityPicker.setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {

                String proData = null;
                if (province != null) {
                    proData = "name:  " + province.getName() + "   id:  " + province.getId();
                }

                String cituData = null;
                if (city != null) {
                    cituData = "name:  " + city.getName() + "   id:  " + city.getId();
                }


                String districtData = null;
                if (district != null) {
                    districtData = "name:  " + district.getName() + "   id:  " + district.getId();
                }


                if (mWheelType == JDCityConfig.ShowType.PRO_CITY_DIS) {
                    resultV.setText("城市选择结果：\n" + proData + "\n"
                            + cituData + "\n"
                            + districtData);
                } else {
                    resultV.setText("城市选择结果：\n" + proData + "\n"
                            + cituData + "\n"
                    );
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }


    /**
     * @param wheelType
     */
    private void setWheelType(JDCityConfig.ShowType wheelType) {
        if (wheelType == JDCityConfig.ShowType.PRO_CITY) {
            mTwoTv.setBackgroundResource(R.drawable.city_wheeltype_selected);
            mThreeTv.setBackgroundResource(R.drawable.city_wheeltype_normal);
            mTwoTv.setTextColor(Color.parseColor("#ffffff"));
            mThreeTv.setTextColor(Color.parseColor("#333333"));
        } else {
            mTwoTv.setBackgroundResource(R.drawable.city_wheeltype_normal);
            mThreeTv.setBackgroundResource(R.drawable.city_wheeltype_selected);
            mTwoTv.setTextColor(Color.parseColor("#333333"));
            mThreeTv.setTextColor(Color.parseColor("#ffffff"));
        }
    }


    private void showJD() {
        cityPicker.showCityPicker();
    }
}
