package cn.edu.njnu.geoproblemsolving.business.resource.controller;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.AddIResourceDTO;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.IResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.IResourceServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.user.service.Impl.UserServiceImpl;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.mongodb.client.result.UpdateResult;
import com.sun.javafx.collections.MappingChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping(value = "/resource")
public class IResourceController {
    @Autowired
    IResourceServiceImpl IResourceService;
    @Autowired
    UserServiceImpl userService;
    /**
     * 单文件上传
     * POST, form-data
     * @param req
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Object uploadResource(HttpServletRequest req){
        Object uploadResult = IResourceService.uploadRemote(req);
        return uploadResult;
    }


    /**
     * 删除：单个文件
     * GET，uid, String
     * /resource/deleteRemote?uid=0b86cb92-4380-4075-b6bb-a9a3ac94ad07
     * @param sourceStoreId
     * @return
     */
    @RequestMapping(value = "/deleteRemote/{uid}", method = RequestMethod.DELETE)
    public Object deleteRemote(@PathVariable("uid") String userId, @RequestParam("rid") String sourceStoreId){
        return IResourceService.deleteRemote(userId, sourceStoreId);
    }

    /**
     * 删除：多文件
     * @param sourceStoreIds
     * @return
     */
    @RequestMapping(value = "/delSomeRemote/{uid}", method = RequestMethod.DELETE)
    public Object delSomeRemote(@PathVariable("uid") String userId, @RequestParam("rids") ArrayList<String> sourceStoreIds){
        Object delResult = IResourceService.delSomeRemote(userId, sourceStoreIds);
        return delResult;
    }


    /**
     * 单文件下载
     * GET，sourceStoreId,String
     * /resource/downloadRemote?uid=0b86cb92-4380-4075-b6bb-a9a3ac94ad07
     * @param sourceStoreId
     * @return
     */
    @RequestMapping(value = "/downloadRemote", method = RequestMethod.GET)
    public Object downloadResource(@RequestParam("sourceStoreId") String sourceStoreId){
        ResponseEntity<byte[]> responseEntity = IResourceService.downloadRemote(sourceStoreId);
        return responseEntity;
    }

    @RequestMapping(value = "/shareRes", method = RequestMethod.GET)
    public JsonResult shareRes(@RequestParam("email") String email, @RequestParam("rids") ArrayList<String> rids){
        //发邮件，省略通知这一步，直接加入用户 user resources 字段中。
        //先从 collection resource 中找到这些资源，然后到给传过去
        ArrayList<ResourceEntity> res = IResourceService.getRes(rids);
        return userService.uploadResourceField(email, res);
    }



    /**
     * 文件批量下载
     * GET，sourceStoreId，ArrayList<String>
     * /resource/downSomeRemote?sourceStoreIds=0b86cb92-4380-4075-b6bb-a9a3ac94ad07,52f77d35-9420-4f0f-9ad5-a678d3c58f4a
     * @param sourceStoreIds
     * @return
     */
    @RequestMapping(value = "/downSomeRemote", method = RequestMethod.GET)
    public Object downSomeResource(@RequestParam("sourceStoreIds")ArrayList<String> sourceStoreIds){
        ResponseEntity<byte[]> responseEntity = IResourceService.downSomeRemote(sourceStoreIds);
        return responseEntity;
    }



    @RequestMapping(produces = "application/json;charset=UTF-8", value = "/{uid}", method = RequestMethod.GET)
    public JsonResult selectResourceByUidAndType(@PathVariable("uid") String userId, @RequestParam("type") String resType){
        HashMap<String, String> filedMap = new HashMap<String, String>();
        filedMap.put("uploaderId", userId);
        filedMap.put("type", resType);
        return IResourceService.inquiryLocal(filedMap);
    }

    @RequestMapping(produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    public JsonResult selectResourceByPrivacy(@RequestParam("privacy") String privacyType){
        HashMap<String, String> filedMap = new HashMap<>();
        filedMap.put("privacy", privacyType);
        return IResourceService.inquiryLocal(filedMap);
    }


    @RequestMapping(produces = "application/json;charset=UTF-8", method = RequestMethod.PUT)
    public JsonResult updateRes(@RequestBody IResourceEntity resource){
        String rid = resource.getResourceId();
        UpdateResult updateResult = IResourceService.updateRes(rid, resource);
        if (updateResult.getMatchedCount() == 1){
            return ResultUtils.success();
        }else {
            return ResultUtils.error(-2, "Fail.");
        }

    }

    /**
     * 单文件上传
     * POST, form-data
     * @param
     * @return
     */
    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    public Object bindResource( @RequestBody AddIResourceDTO iResourceEntity) throws IOException, URISyntaxException {
        Object uploadResult = IResourceService.saveResource(iResourceEntity);
        return uploadResult;
    }

    @RequestMapping(value = "/image", method = RequestMethod.POST)
    public JsonResult uploadPicture(HttpServletRequest request) {
        JsonResult result = IResourceService.uploadImage(request);
        return result;
    }


}
