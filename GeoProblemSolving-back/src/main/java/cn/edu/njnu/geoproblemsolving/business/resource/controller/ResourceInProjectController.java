package cn.edu.njnu.geoproblemsolving.business.resource.controller;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.ResInProjectServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.service.ResourceInProjectService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ResourceInProjectController
 * @Description 项目中资源的相关操作
 * 项目中资源与项目绑定，需要与用户解耦
 * 参数说明
 * aid: 活动 id
 * uid: 资源 id
 * paths: 文件路径 "0"代表根目录
 * Resource: 资源，包含 folder与 file
 * folder
 * file
 *
 * rip = resource in project
 * @Author zhngzhng
 * @Date 2021/4/20
 **/
@RestController
@RequestMapping(value = "/rip")
public class ResourceInProjectController {

    @Autowired
    ResInProjectServiceImpl resService;

    /**
     * 获取路径下内容
     *
     * @param paths "0" 代表根目录
     * @param aid   活动id
     * @param req
     * @return
     */
    @RequestMapping(value = "/{aid}/{paths}", method = RequestMethod.GET)
    public JsonResult getAllResource(@PathVariable String aid, @PathVariable ArrayList<String> paths, HttpServletRequest req) {
        ArrayList<ResourceEntity> allRes = resService.getAllRes(aid, paths);
        return ResultUtils.success(allRes);
    }

    /**
     * 新建资源文件夹在项目中
     *
     * @param req
     * @param folderName
     * @param paths
     * @param aid
     * @return
     */
    @RequestMapping(value = "/folder", method = RequestMethod.POST)
    public JsonResult createFolder(HttpServletRequest req,
                                   @RequestParam String folderName,
                                   @RequestParam ArrayList<String> paths,
                                   @RequestParam String aid) {
        String userId = (String) req.getSession().getAttribute("userId");
        JSONObject createResult = resService.createFolder(folderName, aid, paths, userId);
        if (createResult != null) {
            return ResultUtils.success(createResult);
        } else {
            return ResultUtils.error(-2, "fail");
        }
    }


    @RequestMapping(value = "/folder", method = RequestMethod.PUT)
    public JsonResult putFolder(@RequestParam ArrayList<String> paths,
                                @RequestParam String folderId,
                                @RequestParam String aid,
                                @RequestParam String newFolderName
                                ) {
        ResourceEntity res = new ResourceEntity();
        res.setName(newFolderName);
        res.setUid(folderId);
        String putResult = resService.putResourceByPath(aid, res, paths);
        if (putResult.equals("suc")){
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }

    /**
     * 上传资源到项目中
     * 直接返回三类内容
     * uploaded， sizeOvered, failed
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    public Object uploadFile(HttpServletRequest req) {
        return resService.uploadFileInProject(req);
    }


    @RequestMapping(value = "/file/{aid}/{paths}", method = RequestMethod.PUT)
    public JsonResult putFile(@RequestBody ResourceEntity putRes,
                          @PathVariable String aid,
                          @PathVariable ArrayList<String> paths) {
        String putResult = resService.putResourceByPath(aid, putRes, paths);
        if (putResult.equals("suc")) {
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }


    /**
     * 删除资源，支持批量删除
     *
     * @param uids
     * @param aid   用于属于本项目的资源查找出来
     * @param paths
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public JsonResult delResource(@RequestParam ArrayList<String> uids,
                                  @RequestParam String aid,
                                  @RequestParam ArrayList<String> paths) {
        String delResult = resService.delResource(aid, uids, paths);
        if (delResult.equals("suc")) {
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }

    /**
     * 将用户资源分享到
     * @param req
     * @param uids
     * @return
     */
    @RequestMapping(value = "/shareToProject/{aid}/{uids}/{paths}", method = RequestMethod.GET)
    public JsonResult resToProject(HttpServletRequest req,
                                   @PathVariable String aid,
                                   @PathVariable String uids,
                                   @PathVariable ArrayList<String> paths){
        String userId = (String) req.getSession().getAttribute("userId");
        List<ResourceEntity> shareResult = resService.resourceToProject(userId, aid, uids, paths);
        if (shareResult != null){
            return ResultUtils.success(shareResult);
        }
        return ResultUtils.error(-2, "fail");
    }

    @RequestMapping(value = "/file/{aid}/{key}/{value}", method = RequestMethod.GET)
    public JsonResult searchResource(@PathVariable String aid, @PathVariable String key, @PathVariable String value){
        ArrayList<ResourceEntity> res = resService.searchRes(aid, key, value);
        return ResultUtils.success(res);
    }

    @RequestMapping(value = "/file/bind/{aid}", method = RequestMethod.POST)
    public JsonResult bindProject(@RequestBody ResourceEntity modelOutput, @PathVariable String aid){
        String bindResult = resService.bindResToProject(modelOutput, aid);
        if (bindResult.equals("suc")){
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }

    /**
     * 返回 activity 中所有文件
     * 包括 privacy 及 public 内容
     * @param aid
     * @return
     */
    @RequestMapping(value = "/file/all/{aid}", method = RequestMethod.GET)
    public JsonResult getAllFileInProject(@PathVariable String aid){
        ArrayList<ResourceEntity> allFileInProject = resService.getAllFileInProject(aid);
        return ResultUtils.success(allFileInProject);
    }




}
