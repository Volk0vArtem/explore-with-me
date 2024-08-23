package ru.practicum.admin.compilations.model;

import org.mapstruct.Mapper;
import ru.practicum.admin.compilations.model.dto.CompilationDto;
import ru.practicum.admin.compilations.model.dto.NewCompilationDto;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    CompilationDto toCompilationDto(Compilation compilation);

    Compilation toCompilation(NewCompilationDto newCompilationDto);
}
