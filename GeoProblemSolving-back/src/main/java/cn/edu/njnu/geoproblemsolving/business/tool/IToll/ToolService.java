package cn.edu.njnu.geoproblemsolving.business.tool.IToll;

import com.google.common.collect.Lists;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public interface ToolService {
    List<JSONObject> getComputeModelByUserEmail(String email, int page, int size);

    List<JSONObject> getPublicComputeModel(int page, int size);

    JSONObject getComputableModelList(String email, int page, int size);

    JSONObject queryPublicComputeModelByName(String searchText, int page);


    List<JSONObject> getDataMethodByUserEmail(String email, int page, int size);

    List<JSONObject> getPublicDataMethod(int page, int size);

    JSONObject getDataMethodList(String email, int page, int size);

    JSONObject queryPublicDataMethodByName(String searchText, int page);


    Tool createTool(JSONObject tool) throws UnsupportedEncodingException;

    List<Tool> getToolByProviderService(String provider);

    Tool getToolByTid(String tid);

    Tool updateToolService(Tool putTool);

    long delToolService(String tid);

    List<Tool> getToolByIds(ArrayList<String> ids);
}
