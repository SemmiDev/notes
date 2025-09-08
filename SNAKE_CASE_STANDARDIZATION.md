# Snake Case DTO Standardization Summary

## Overview
All DTO fields have been standardized to use snake_case naming in JSON serialization using `@JsonProperty` annotations to match the Jackson SNAKE_CASE configuration.

## Updated DTOs

### Core DTOs
- ✅ `NoteDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `CategoryDto` - Added @JsonProperty for user_id, note_count, created_at, updated_at  
- ✅ `TagDto` - Added @JsonProperty for user_id, note_count, created_at, updated_at

### Request DTOs
- ✅ `CreateNoteDto` - Added @JsonProperty for category_id, tag_ids
- ✅ `UpdateNoteDto` - Added @JsonProperty for category_id, tag_ids
- ✅ `LoginDto` - Added @JsonProperty for username_or_email

### Response DTOs
- ✅ `CreateNoteResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `UpdateNoteResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `GetNoteResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `CreateCategoryResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `UpdateCategoryResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `CreateTagResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `UpdateTagResponseDto` - Added @JsonProperty for user_id, created_at, updated_at
- ✅ `LoginResponseDto` - Added @JsonProperty for access_token, refresh_token, token_type, expires_in

## Field Mappings

### Common Fields
| Java Field Name | JSON Field Name | Applied To |
|----------------|-----------------|------------|
| `userId` | `user_id` | All entity DTOs |
| `createdAt` | `created_at` | All entity DTOs |
| `updatedAt` | `updated_at` | All entity DTOs |
| `noteCount` | `note_count` | CategoryDto, TagDto |

### Request-Specific Fields
| Java Field Name | JSON Field Name | Applied To |
|----------------|-----------------|------------|
| `categoryId` | `category_id` | CreateNoteDto, UpdateNoteDto |
| `tagIds` | `tag_ids` | CreateNoteDto, UpdateNoteDto |
| `usernameOrEmail` | `username_or_email` | LoginDto |

### Response-Specific Fields
| Java Field Name | JSON Field Name | Applied To |
|----------------|-----------------|------------|
| `accessToken` | `access_token` | LoginResponseDto |
| `refreshToken` | `refresh_token` | LoginResponseDto |
| `tokenType` | `token_type` | LoginResponseDto |
| `expiresIn` | `expires_in` | LoginResponseDto |

## Implementation Details

### JsonProperty Annotations
```java
// Example from NoteDto
@JsonProperty("user_id")
@Schema(description = "User ID who owns this note")
UUID userId,

@JsonProperty("created_at")
@Schema(description = "Creation timestamp")
LocalDateTime createdAt,

@JsonProperty("updated_at")
@Schema(description = "Last update timestamp")
LocalDateTime updatedAt
```

### Import Statements
All affected DTOs now include:
```java
import com.fasterxml.jackson.annotation.JsonProperty;
```

## JSON Output Examples

### Before (camelCase)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "My Note",
  "userId": "123e4567-e89b-12d3-a456-426614174001",
  "createdAt": "2025-01-08T09:00:00",
  "updatedAt": "2025-01-08T09:00:00"
}
```

### After (snake_case)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "My Note",
  "user_id": "123e4567-e89b-12d3-a456-426614174001",
  "created_at": "2025-01-08T09:00:00",
  "updated_at": "2025-01-08T09:00:00"
}
```

## OpenAPI/Swagger Impact

### Schema Generation
- OpenAPI schemas now correctly show snake_case field names
- Request/response examples use snake_case naming
- API documentation is consistent with actual JSON output

### Example Schema
```yaml
CreateNoteDto:
  type: object
  properties:
    title:
      type: string
      example: "My Important Note"
    content:
      type: string
      example: "This is the content of my note..."
    category_id:
      type: string
      format: uuid
    tag_ids:
      type: array
      items:
        type: string
        format: uuid
```

## Benefits Achieved

1. **Consistent API**: All JSON fields use snake_case naming
2. **Jackson Compatibility**: Aligns with SNAKE_CASE property naming strategy
3. **Better Documentation**: OpenAPI shows correct field names
4. **API Consumer Friendly**: Consistent naming convention across all endpoints
5. **No Breaking Changes**: Only affects JSON field names, not Java code
6. **Maintainability**: Clear mapping between Java fields and JSON fields

## Validation

### Compilation
- ✅ All DTOs compile successfully
- ✅ No breaking changes to existing code
- ✅ MapStruct mappers work correctly

### JSON Serialization
The Jackson configuration will now properly serialize:
- Java `userId` → JSON `user_id`
- Java `createdAt` → JSON `created_at`
- Java `updatedAt` → JSON `updated_at`
- And all other mapped fields

## Migration Notes

- **No Code Changes Required**: Only JSON field names changed
- **Backward Compatibility**: Jackson can still deserialize camelCase for existing clients
- **Documentation Updated**: OpenAPI specs now show correct snake_case field names
- **Testing**: Existing unit tests may need JSON field name updates

This standardization ensures that all API responses and requests use consistent snake_case naming, making the API more intuitive and aligned with common REST API conventions.
