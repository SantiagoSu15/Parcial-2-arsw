package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.controllers.DTO.ApiResponseR;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @Operation(summary = "Obtener todos los blueprints",
            description = "Devuelve todos los blueprints de la DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de blueprints obtenida ",
                    content = @Content(schema = @Schema(implementation = Set.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseR> getAll() {

       ApiResponseR d = new ApiResponseR(200,"Execute ok ",services.getAllBlueprints());

        return ResponseEntity.ok(d);
    }

    // GET /blueprints/{author}
    @Operation(summary = "Obtener blueprints por autor",
            description = "Devuelve todos los blueprints creados por un autor ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blueprints encontrados",
                    content = @Content(schema = @Schema(implementation = Set.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron blueprints para el autor")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponseR> byAuthor(@PathVariable String author) {
        try {
            ApiResponseR d = new ApiResponseR(200,"Execute ok ",services.getBlueprintsByAuthor(author));
            return ResponseEntity.ok(d);
        } catch (BlueprintNotFoundException e) {
            ApiResponseR d = new ApiResponseR(404,"Resource Not found",e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(d);
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Obtener blueprint ",
            description = "Devuelve un blueprint por autor y nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blueprint encontrado",
                    content = @Content(schema = @Schema(implementation = Blueprint.class))),
            @ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponseR> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            ApiResponseR d = new ApiResponseR(200,"Execute ok ",services.getBlueprint(author, bpname));
            return ResponseEntity.ok(d);
        } catch (BlueprintNotFoundException e) {
            ApiResponseR d = new ApiResponseR(404,"Resource Not found",e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(d);
        }
    }

    // POST /blueprints
    @Operation(summary = "Crear un nuevo blueprint",
            description = "Registra un nuevo blueprint en la  DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blueprint creado "),
            @ApiResponse(responseCode = "400", description = "El blueprint ya existe o error")
    })
    @PostMapping
    public ResponseEntity<ApiResponseR> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            ApiResponseR d = new ApiResponseR(201,"Execute ok ",bp);
            services.addNewBlueprint(bp);
            return ResponseEntity.ok(d);
        } catch (BlueprintPersistenceException e) {
            ApiResponseR d = new ApiResponseR(400,"Execute Error ",e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(d);
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar punto a un blueprint",
            description = "Agrega un nuevo punto a un blueprint existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Punto agregado "),
            @ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponseR> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponseR d = new ApiResponseR(202,"Execute Error ","Accepted");
            return ResponseEntity.ok(d);
        } catch (BlueprintNotFoundException e) {
            ApiResponseR d = new ApiResponseR(404,"Not Found ",e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(d);
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}


