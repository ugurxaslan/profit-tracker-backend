package com.ugurxaslan.profit_tracker_backend.mapper;

import com.ugurxaslan.profit_tracker_backend.dto.request.UserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "walletList", ignore = true)
    User toEntity(UserRequestDTO requestDTO);

    UserResponseDTO toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "walletList", ignore = true)
    void updateEntityFromDto(UserRequestDTO dto, @MappingTarget User entity);
}