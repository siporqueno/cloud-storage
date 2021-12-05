package com.porejemplo.nube.api;

import com.porejemplo.nube.model.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2021-12-05T18:19:37.224984700+03:00[Europe/Moscow]")
@Controller
@RequestMapping("${openapi.serverAdminREST.base-path:/admin/api/v1/user}")
public class UserApiController implements UserApi {

    private final NativeWebRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public UserApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserDto>> userGet() {
        return new ResponseEntity<>(List.of(
                new UserDto(1, "user1", "pass1", "nick1"),
                new UserDto(2, "user2", "pass2", "nick2"),
                new UserDto(3, "user3", "pass3", "nick3")
        ), HttpStatus.OK);
    }

    @Override
    @PostMapping
    public ResponseEntity<String> userPost(UserDto createOrUpdateUserDto) {
        return new ResponseEntity<>("user created", HttpStatus.CREATED);
    }

    @Override
    @PutMapping
    public ResponseEntity<String> userPut(UserDto createOrUpdateUserDto) {
        return new ResponseEntity<>("user updated", HttpStatus.ACCEPTED);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> userDelete(Integer id) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
