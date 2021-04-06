package ca.cmpt213.tokimon_server.controller;


import ca.cmpt213.tokimon_server.model.Tokimon;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RestController
public class TokimonController {

    final private String STORAGE_PATH = "/data";
    final private String STORAGE_FILENAME = "/tokimon.json";

    private HashMap<Long, Tokimon> tokimonIdHash;

    @GetMapping("/api/tokimon/all")
    public List<Tokimon> getTokimons(){
        return new ArrayList<>(tokimonIdHash.values());
    }

    @GetMapping("/api/tokimon/{id}")
    public Tokimon getTokimon(@PathVariable long id){
        // referenced, https://stackoverflow.com/questions/25422255/how-to-return-404-response-status-in-spring-boot-responsebody-method-return-t
        if(tokimonIdHash.get(id) == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }

        return tokimonIdHash.get(id);
    }

    @PostMapping("/api/tokimon/add")
    public void addTokimon(@RequestBody Tokimon tokimon){
        if(tokimonIdHash.get(tokimon.getId()) == null){
            tokimonIdHash.put(tokimon.getId(),tokimon);
            saveToGson();

        } else {
            System.out.println("already has a tokimon with same id, please choose a new one");
            return;
        }
        throw new ResponseStatusException(HttpStatus.CREATED, "created");
    }

    @PostMapping("/api/tokimon/change/{id}")
    public String changeTokimon(@PathVariable long id, @RequestBody Tokimon tokimon){

        if(tokimonIdHash.get(id) == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }

        if(id == tokimon.getId()){
            tokimonIdHash.put(id,tokimon);
        } else {
            System.out.println("change failed, not the same id");
        }
        saveToGson();
        throw new ResponseStatusException(HttpStatus.CREATED, "created");
    }

    @DeleteMapping("/api/tokimon/{id}")
    public void deleteTokimon(@PathVariable long id){
        System.out.println("deleting id:"+id);
        if(tokimonIdHash.get(id) == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }

        tokimonIdHash.remove(id);
        saveToGson();

        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "entity not found");

    }

    /*
    @GetMapping(
            value = "/error"
    )
    @ResponseStatus(
            value = HttpStatus.NOT_FOUND,
            reason = "404 not found")
    public String notFound() {
        return "404 not found";
    }

     */

    @PostConstruct
    public void onCreate(){
        readFromGson();
    }

    private void saveToGson(){
        // how to write to json, https://www.youtube.com/watch?v=9Xr-o_QWMeE&t=59s&ab_channel=ChargeAhead
        System.out.println("Log: saving to json");
        File output = null;

        try {
            File dir = new File(STORAGE_PATH);
            if(!dir.exists()){
                dir.mkdirs();
            }
            output = new File(STORAGE_PATH + STORAGE_FILENAME);
            output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if(output!= null){
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(output,tokimonIdHash);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Log: Successfully saved to json");
    }

    private void readFromGson(){
        // how to read from json, https://www.youtube.com/watch?v=9Xr-o_QWMeE&t=59s&ab_channel=ChargeAhead
        System.out.println("Log: reading from json");
        try {
        ObjectMapper mapper = new ObjectMapper();
            InputStream is = new FileInputStream(new File(STORAGE_PATH + STORAGE_FILENAME));
            TypeReference<HashMap<Long,Tokimon>> typeReference = new TypeReference<>(){};
            tokimonIdHash = mapper.readValue(is,typeReference);
            System.out.println("Log: Successfully read " + tokimonIdHash.size() + " items from json file");
            return;
        } catch (FileNotFoundException e) {
            System.out.println("Log: Json read Failed, using empty records");
            tokimonIdHash = new HashMap<>();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
