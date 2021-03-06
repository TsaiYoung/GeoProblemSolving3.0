package cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.conceptmap;


import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.support.support.*;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel(value = "ConceptMap")
@Data
public class ConceptMap {
    String id;
    String geoId;
    String conceptId;
    String name;

    String description;
    String graphXml;
    String xml;
    String pathUrl;
    String mapClass;
    String[] tags;

    ShapeStructure shapeInfo;
    SpacePositionStructure spacePosition;
    List<ConceptStructure> concept;
    List<PropertyStructure> properties;
    List<ProcessStructure> processes;
    List<ElementRelationStructure> elementRelations;

    Integer width;
    Integer height;
}
