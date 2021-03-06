package cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.georule.rule_property;

import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.georule.rule_enum.Type_Property;
import lombok.Data;

import java.util.List;

@Data
public class Rule_Property {
    String id;
    String from;
    List<String> to;
    Type_Property type;
    String description;
}
