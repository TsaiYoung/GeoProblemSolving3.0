package cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.support;


import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.userimage.UserImage;
import lombok.Data;

import java.util.List;

@Data
public class ConceptStructure {
    String definition;
    String source;
    String[] tags;
    List<String> relatedConcepts;
    List<ConceptClassification> classifications;
    List<UserImage> relateImages;
}
