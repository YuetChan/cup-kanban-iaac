package com.tycorp.simplekanban.user;

import com.google.gson.JsonObject;
import com.tycorp.simplekanban.core.util.GsonHelper;

import com.tycorp.simplekanban.user_secret.UserSecret;
import com.tycorp.simplekanban.user_secret.UserSecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping(value = "/users")
public class UserController {
   private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
   private ResponseEntity NOT_FOUND_RES = new ResponseEntity(HttpStatus.NOT_FOUND);

   @Autowired
   private UserRepository userRepository;
   @Autowired
   private UserSecretRepository userSecretRepository;

   @GetMapping(value = "", produces = "application/json")
   public ResponseEntity<String> getUserByEmail(@RequestParam(name = "email") String email) {
      LOGGER.trace("Enter getUserByParams(email)");
      LOGGER.info("Getting user");
      
      Optional<User> userMaybe = userRepository.findByEmail(email);
      if(!userMaybe.isPresent()) {
         LOGGER.debug("User not found");
         return NOT_FOUND_RES;
      }

      JsonObject dataJson = new JsonObject();
      dataJson.add(
              "user",
              GsonHelper.getExposeSensitiveGson().toJsonTree(userMaybe.get(), User.class));

      JsonObject resJson = new JsonObject();
      resJson.add("data", dataJson);

      LOGGER.debug("Response json built");

      LOGGER.info("User obtained");

      return new ResponseEntity(resJson.toString(), HttpStatus.OK);
   }

   @GetMapping(value = "/{id}/role", produces = "application/json")
   public ResponseEntity<String> getUserRoleById(@PathVariable(name = "id") String id) {
      LOGGER.trace("Enter getUserRoleById(id)");
      LOGGER.info("Getting user role");

      Optional<User> userMaybe = userRepository.findById(id);
      if(!userMaybe.isPresent()) {
         LOGGER.debug("User not found");
         return NOT_FOUND_RES;
      }

      JsonObject dataJson = new JsonObject();
      dataJson.addProperty("role", userMaybe.get().getRole());

      JsonObject resJson = new JsonObject();
      resJson.add("data", dataJson);

      LOGGER.debug("Response json built", resJson);

      LOGGER.info("User role obtained");

      return new ResponseEntity(resJson.toString(), HttpStatus.OK);
   }

   @PostMapping(value = "", produces = "application/json")
   public ResponseEntity<String> createUser(@RequestBody String reqJsonStr) {
      LOGGER.trace("Enter createUser(reqJsonStr)");
      LOGGER.info("Creating user");

      JsonObject dataJson = GsonHelper.decodeJsonStrForData(reqJsonStr);
      JsonObject userJson = dataJson.get("user").getAsJsonObject();

      String email = userJson.get("email").getAsString();
      String role = userJson.get("role").getAsString();

      if(!userRepository.findByEmail(email).isPresent()) {
         LOGGER.debug("User not found");

         User user = new User(email, role);
         LOGGER.debug("User properties set");

         user = userRepository.save(user);

         userSecretRepository.save(userSecretRepository.save(generateUserSecret(user)));
         LOGGER.debug("User secret created");

         javax.json.JsonObject resJavaxJson = Json.createObjectBuilder()
                 .add("data",
                         Json.createObjectBuilder()
                                 .add("user",
                                         Json.createObjectBuilder()
                                                 .add("id",
                                                         user.getId())))
                 .build();

         LOGGER.debug("Response json built", resJavaxJson.toString());

         LOGGER.info("User creation done");

         return new ResponseEntity(resJavaxJson.toString(), HttpStatus.CREATED);
      }else {
         return new ResponseEntity(HttpStatus.CONFLICT);
      }
   }

   @GetMapping(value = "/{id}/userSecret", produces = "application/json")
   public ResponseEntity<String> getUserSecretById(@PathVariable(name = "id") String id) {
      LOGGER.trace("Enter getUserSecretById(reqJsonStr)");
      LOGGER.info("Getting user secret");
      
      Optional<User> userMaybe = userRepository.findById(id);
      if(userMaybe.isPresent()) {
         LOGGER.debug("User is present");

         JsonObject dataJson = new JsonObject();
         dataJson.addProperty("secret", userMaybe.get().getUserSecret().getSecret());

         JsonObject resJson = new JsonObject();
         resJson.add("data", dataJson);

         LOGGER.debug("Response json built");

         LOGGER.info("User secret obtained");

         return new ResponseEntity(resJson.toString(), HttpStatus.OK);
      }else {
         LOGGER.debug("User not found");
         return NOT_FOUND_RES;
      }
   }

   @PutMapping(value = "/{id}/userSecret", produces = "application/json")
   public ResponseEntity<String> generateUserSecretById(@PathVariable(name = "id") String id) {
      LOGGER.trace("Enter generateUserSecretById(reqJsonStr)");
      LOGGER.info("Generating secret");
      
      Optional<User> userMaybe = userRepository.findById(id);
      if(userMaybe.isPresent()) {
         LOGGER.debug("User is present");

         User user = userMaybe.get();

         UserSecret userSecret = user.getUserSecret();
         userSecret.setSecret(generateUserSecretStr());

         userSecret = userSecretRepository.save(userSecret);

         LOGGER.debug("User secret generated");

         JsonObject dataJson = new JsonObject();
         dataJson.addProperty("secret", userSecret.getSecret());

         JsonObject resJson = new JsonObject();
         resJson.add("data", dataJson);

         LOGGER.debug("Response json built");

         LOGGER.info("Secret generation done");

         return new ResponseEntity(resJson.toString(), HttpStatus.CREATED);
      }else {
         LOGGER.debug("User not found");
         return NOT_FOUND_RES;
      }
   }

   public UserSecret generateUserSecret(User user) {
      LOGGER.trace("Enter generateUserSecret(user)");

      UserSecret userSecret = new UserSecret(generateUserSecretStr());
      userSecret.setUser(user);
      return userSecret;
   }

   public String generateUserSecretStr() {
      LOGGER.trace("Enter generateUserSecretStr()");

      String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      StringBuilder sb = new StringBuilder();

      Random random = new Random();

      for(int i = 0; i < 5; i++) {
         int index = random.nextInt(alphabet.length());
         char randomChar = alphabet.charAt(index);

         sb.append(randomChar);
      }

      return sb.toString();
   }

}