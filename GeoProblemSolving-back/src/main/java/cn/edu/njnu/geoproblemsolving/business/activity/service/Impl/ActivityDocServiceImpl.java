package cn.edu.njnu.geoproblemsolving.business.activity.service.Impl;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityDocRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityDocService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActivityDocServiceImpl implements ActivityDocService {

    private final ActivityDocRepository activityDocRepository;

    public ActivityDocServiceImpl(ActivityDocRepository activityDocRepository) {
        this.activityDocRepository = activityDocRepository;
    }

    @Override
    public JsonResult saveDocument(ActivityDoc activityDoc){
        try {
            return ResultUtils.success(activityDocRepository.save(activityDoc));
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult findDocument(String aid){
        try {
            // confirm
            Optional result = activityDocRepository.findById(aid);
            if (!result.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            ActivityDoc activityDoc = (ActivityDoc) result.get();

            return ResultUtils.success(activityDoc.getDocument());
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult deleteDocument(String aid){
        try {
            // confirm
            Optional result = activityDocRepository.findById(aid);
            if (!result.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            activityDocRepository.deleteById(aid);

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult updateDocument(String aid, String document){
        try {
            // confirm
            Optional result = activityDocRepository.findById(aid);
            if (!result.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");

            ActivityDoc activityDoc = (ActivityDoc) result.get();
            activityDoc.setDocument(document);
            activityDocRepository.save(activityDoc);

            return ResultUtils.success(activityDoc);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }
}
