package cn.edu.njnu.geoproblemsolving.business.tool.IToll;

import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ToolController
 * @Description tool 相关接口
 * @Author zhngzhng
 * @Date 2021/5/13
 **/
@RestController
@RequestMapping(value = "/tool")
public class ToolRestController {
    @Autowired
    ToolServiceImpl toolService;

    @RequestMapping(value = "/computableModel/public/{page}/{size}", method = RequestMethod.GET)
    public JsonResult getPublicModel(HttpServletRequest req, @PathVariable int page, @PathVariable int size){
        String email = (String)req.getSession().getAttribute("email");
        if (email == null){
            return ResultUtils.error(-2, "fail");
        }
        JSONObject computableModelList = toolService.getComputableModelList(email, page, size);
        return ResultUtils.success(computableModelList);
    }

    @RequestMapping(value = "/dataMethod/public/{page}/{size}", method = RequestMethod.GET)
    public JsonResult getDataMethodList(HttpServletRequest req, @PathVariable int page, @PathVariable int size){
        String email = (String)req.getSession().getAttribute("email");
        if (email == null){
            return ResultUtils.error(-2, "fail");
        }
        JSONObject dataMethodList = toolService.getDataMethodList(email, page, size);
        return ResultUtils.success(dataMethodList);
    }

    @RequestMapping(value = "/computableModel/public/{searchText}/{page}/{size}", method = RequestMethod.GET)
    public JsonResult queryPublicComputableModelByName(@PathVariable String searchText, @PathVariable int page){
        JSONObject modelData = toolService.queryPublicComputeModelByName(searchText, page);
        return ResultUtils.success(modelData);
    }


    @RequestMapping(value = "/dataMethod/public/{searchText}/{page}/{size}",method = RequestMethod.GET)
    public JsonResult queryPublicDataMethodByName(@PathVariable String searchText, @PathVariable int page){
        JSONObject dataMethodData = toolService.queryPublicDataMethodByName(searchText, page);
        return ResultUtils.success(dataMethodData);
    }





    /*
    工具创建成功后的一系列操作
    第一类 computableModel
    1. Invoke 页面内容
    2. Init
    3. Invoke
    4. task 监控

    第二类 DataMethod
    没有执行状态查询

    全部使用restFul API 风格
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public JsonResult createToolOrToolSet(@RequestBody JSONObject toolJson) throws UnsupportedEncodingException {
        Tool tool = toolService.createTool(toolJson);
        if (tool != null){
            return ResultUtils.success(tool);
        }
        return ResultUtils.error(-2, "Fail");
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JsonResult getToolByTid(@PathVariable String id){
        Tool tool = toolService.getToolByTid(id);
        if (tool != null){
            return ResultUtils.success(tool);
        }
        return ResultUtils.error(-2, "Fail");
    }

    @RequestMapping(value = "/{tid}", method = RequestMethod.DELETE)
    public JsonResult delTool(@PathVariable String tid){
        long delNum = toolService.delToolService(tid);
        if (delNum == 1){
            return ResultUtils.success();
        }else {
            return ResultUtils.error(-2, "Fail");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    public JsonResult updateToolInfo(@RequestBody Tool putTool){
        return ResultUtils.success(toolService.updateToolService(putTool));
    }


    @RequestMapping(value = "/provider/{providerId}", method = RequestMethod.GET)
    public JsonResult queryToolByUserId(@PathVariable String providerId){
        return ResultUtils.success(toolService.getToolByProviderService(providerId));
    }

    /**
     * 返回工具列表
     * @param ids
     * @return
     */
    @RequestMapping(value = "/all/{ids}", method = RequestMethod.GET)
    public JsonResult getToolListByToolIds(@PathVariable ArrayList<String> ids){
        List<Tool> toolList = toolService.getToolByIds(ids);
        return ResultUtils.success(toolList);
    }







}
