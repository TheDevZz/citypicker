package com.ihidea.as.citypicker.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegionDto {
    public int id;

    @SerializedName("regionName")
    public String name;

    @SerializedName("children")
    public List<RegionDto> children;
}
