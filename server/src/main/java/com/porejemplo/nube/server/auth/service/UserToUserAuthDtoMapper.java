package com.porejemplo.nube.server.auth.service;

import com.porejemplo.nube.common.dto.UserAuthDto;
import com.porejemplo.nube.server.auth.entity.User;

public class UserToUserAuthDtoMapper {

    public UserAuthDto mapUserToUserAuthDto(User user) {
        return new UserAuthDto(user.getUsername(), user.getPassword());
    }
}
