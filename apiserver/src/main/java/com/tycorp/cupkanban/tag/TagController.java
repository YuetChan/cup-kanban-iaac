package com.tycorp.cupkanban.tag;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tycorp.cupkanban.core.util.GsonHelper;
import com.tycorp.cupkanban.project.Project;
import com.tycorp.cupkanban.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/tags")
public class TagController {
   private static final Logger LOGGER = LoggerFactory.getLogger(TagController.class);
   private ResponseEntity NOT_FOUND_RES = new ResponseEntity(HttpStatus.NOT_FOUND);

   @Autowired
   private TagRepository tagRepository;
   @Autowired
   private ProjectRepository projectRepository;

   @GetMapping(value = "", produces = "application/json")
   public ResponseEntity<String> searchTagsByProjectIdAndPrefix(@RequestParam(name = "projectId") String projectId,
                                                                @RequestParam(name = "prefix") String prefix,
                                                                @RequestParam(name = "start") int start) {
      LOGGER.trace("Enter searchTagsByProjectIdAndPrefix(projectId, prefix, start)");
      LOGGER.debug("GetMapping searchTagsByProjectIdAndPrefix with parameters: projectId: {}, prefix: {}, start: {}", projectId, prefix, start);

      Optional<Project> projectMaybe = projectRepository.findById(projectId);
      if(!projectMaybe.isPresent()) {
         LOGGER.debug("Project not found");
         return NOT_FOUND_RES;
      }

      LOGGER.debug("Find tags by project id and name like");
      Page<Tag> page = tagRepository.findByProjectIdAndNameLike(projectId,
              prefix + "%",
              PageRequest.of(start, 20, Sort.by("createdAt").descending()));
      List<Tag> tagList = page.getContent();

      for(var tag : tagList) {
         tag.setProjectId(projectId);
      }

      Type tagListType = new TypeToken<ArrayList<Tag>>() {}.getType();

      JsonObject dataJson = new JsonObject();
      dataJson.add(
              "tags",
              GsonHelper.getExposeSensitiveGson().toJsonTree(tagList, tagListType));
      dataJson.addProperty("page", start);
      dataJson.addProperty("totalPage", page.getTotalPages());

      JsonObject resJson = new JsonObject();
      resJson.add("data", dataJson);

      LOGGER.debug("Response json built", resJson.toString());
      return new ResponseEntity(resJson.toString(), HttpStatus.OK);
   }
}
