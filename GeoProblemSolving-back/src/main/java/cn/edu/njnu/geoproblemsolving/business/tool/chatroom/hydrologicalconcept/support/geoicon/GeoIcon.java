package cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.geoicon;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("GeoIcon")
@Data
public class GeoIcon {
    String geoId;
    String name;
    String description;
    String pathUrl;
//    String iconClass;
    List<String> tags;

//    List<ConceptMap> relatedConceptMaps;
//    List<GeoIcon> relatedGeoIcons;
}
