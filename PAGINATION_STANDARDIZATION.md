# Pagination Standardization Summary

## Overview
All pagination query parameters have been standardized across all controllers to use consistent naming and behavior.

## Standardized Query Parameters

### Parameter Names
- ✅ `per_page` - Items per page (limit items per page)
- ✅ `current_page` - Current page number (0-based for page navigation)
- ✅ `sort_by` - Column name to sort by
- ✅ `sort_direction` - Sort direction (asc or desc)

### Parameter Details
- **per_page**: Integer, range 1-100, default varies by endpoint
- **current_page**: Integer, minimum 0, default 0
- **sort_by**: String, column name, default varies by endpoint
- **sort_direction**: String, "asc" or "desc", default varies by endpoint

## Updated Endpoints

### Notes API
- `GET /api/notes` - Default: per_page=10, sort_by=updatedAt, sort_direction=desc
- `GET /api/notes/search` - Default: per_page=10, sort_direction=desc
- `GET /api/notes/category/{id}` - Default: per_page=10, sort_by=updatedAt, sort_direction=desc
- `GET /api/notes/tag/{id}` - Default: per_page=10, sort_by=updatedAt, sort_direction=desc

### Categories API
- `GET /api/categories` - Default: per_page=20, sort_by=name, sort_direction=asc

### Tags API
- `GET /api/tags` - Default: per_page=20, sort_by=name, sort_direction=asc

## Implementation Details

### PaginationParams DTO
Created `PaginationParams` record with:
- Validation constraints (per_page: 1-100, current_page: ≥0)
- `toPageable()` method for Spring Data conversion
- Proper handling of sort direction and column names

### Changes Made
1. **Removed**: `@PageableDefault` annotations
2. **Added**: Explicit `@RequestParam` parameters with standardized names
3. **Created**: `PaginationParams` utility class for conversion
4. **Updated**: All controller methods to use new parameter structure

## API Usage Examples

### Basic Pagination
```bash
GET /api/notes?per_page=20&current_page=1
```

### With Sorting
```bash
GET /api/notes?per_page=10&current_page=0&sort_by=title&sort_direction=asc
```

### Combined with Filters
```bash
GET /api/notes?search=important&per_page=5&current_page=0&sort_by=updatedAt&sort_direction=desc
```

## Benefits

1. **Consistent API**: All endpoints use the same parameter names
2. **Clear Documentation**: OpenAPI/Swagger shows explicit parameter names
3. **Better UX**: Intuitive parameter names (per_page vs size)
4. **Validation**: Built-in validation for parameter ranges
5. **Flexibility**: Easy to customize defaults per endpoint
6. **Maintainability**: Centralized pagination logic in PaginationParams

## OpenAPI Documentation Impact

Each paginated endpoint now clearly shows:
- `per_page` (integer, 1-100): Items per page
- `current_page` (integer, ≥0): Current page number
- `sort_by` (string): Column name to sort by
- `sort_direction` (string): Sort direction (asc/desc)

This standardization makes the API more intuitive and consistent for API consumers.
