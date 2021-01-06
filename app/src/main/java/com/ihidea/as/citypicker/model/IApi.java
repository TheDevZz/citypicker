package com.ihidea.as.citypicker.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IApi {

    @POST("/address/regionList")
    Call<ResultDto<List<RegionDto>>> getRegionList(@Body RegionQueryDto queryDto);

    @POST("/address/regionList2")
    Call<ResultDto<List<RegionDto>>> getRegionListAll();
}
