package blog.sammi.lab.notes.application.mapper;

import blog.sammi.lab.notes.application.dto.UserDto;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.presentation.dto.RegisterRequestDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {
    
    @Named("userToDto")
    UserDto toDto(User user);
    
    List<UserDto> toDtoList(List<User> users);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", constant = "false")
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "otpCode", ignore = true)
    @Mapping(target = "otpExpiry", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiry", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User fromRegisterRequest(RegisterRequestDto request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateUserFromDto(UserDto dto, @MappingTarget User user);
}
