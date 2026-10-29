# TripCraft REST API contract

All JSON errors use `{ "timestamp", "status", "code", "message", "path", "fieldErrors": [] }`. DTOs are used at every boundary; entities are never serialized. Dates use ISO-8601 (`yyyy-MM-dd`) and timestamps use ISO-8601 UTC. Authenticated requests send `Authorization: Bearer <accessToken>`.

## Authentication and profile

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public | `{email,password,fullName,phone,interests[]}` | 201 `{user,accessToken}` |
| POST | `/api/auth/login` | Public | `{email,password}` | 200 `{user,accessToken}` |
| POST | `/api/auth/logout` | Tourist/Admin | No body | 204 |
| GET | `/api/users/me` | Tourist/Admin | — | 200 `UserResponse` |
| PUT | `/api/users/me` | Tourist/Admin | `{fullName,phone,interests[]}` | 200 `UserResponse` |

## Public browsing

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/api/destinations` | Public | `page,size,search,season,interest` | 200 `Page<DestinationCard>` |
| GET | `/api/destinations/{id}` | Public | — | 200 `DestinationDetail` |
| GET | `/api/packages` | Public | `destinationId,minPrice,maxPrice,duration,date,interests,page,size,sort` | 200 `Page<PackageCard>` |
| GET | `/api/packages/{id}` | Public | — | 200 `PackageDetail` |
| GET | `/api/packages/{id}/places` | Public | — | 200 ordered `PackagePlaceResponse[]` |
| GET | `/api/packages/{id}/reviews` | Public | — | 200 `ReviewResponse[]` |

## Booking wizard

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/api/bookings` | Tourist | `{packageId}` | 201 `BookingDraft` |
| PUT | `/api/bookings/{id}/details` | Owner | `{travelDate,adults,children,childAges[]}` | 200 `BookingDraft` |
| POST | `/api/bookings/{id}/places` | Owner | `{placeId,dayNumber,orderNo}` | 200 `BookingDraft` |
| DELETE | `/api/bookings/{id}/places/{placeId}` | Owner | — | 200 `BookingDraft` |
| PUT | `/api/bookings/{id}/places/reorder` | Owner | `{places:[{placeId,dayNumber,orderNo}]}` | 200 `BookingDraft` |
| GET | `/api/hotels/{id}/rooms?checkin=&checkout=` | Tourist | Query dates | 200 `RoomAvailability[]` |
| POST | `/api/bookings/{id}/rooms` | Owner | `{rooms:[{roomId,quantity}]}` | 200 `BookingDraft` |
| GET | `/api/vehicles?destinationId=&date=&seats=` | Tourist | Query filters | 200 `VehicleAvailability[]` |
| POST | `/api/bookings/{id}/vehicles` | Owner | `{vehicles:[{vehicleId,quantity}]}` | 200 `BookingDraft` |
| PUT | `/api/bookings/{id}/travelers` | Owner | `{travelers:[{fullName,age,gender,idType,idNumber}]}` | 200 `BookingDraft` |
| POST | `/api/bookings/{id}/price-preview` | Owner | — | 200 `PriceBreakdown` |
| POST | `/api/bookings/{id}/extend-day` | Owner | — | 200 draft with one extra day and extension charge |
| POST | `/api/bookings/{id}/pay` | Owner | `{paymentMethod:"MOCK_CARD",paymentToken}` | 201 `ConfirmedBooking` |
| POST | `/api/bookings/{id}/cancel` | Owner | — | 200 `{booking,refundAmount}` |
| GET | `/api/bookings` | Owner | `status,page,size` | 200 `Page<BookingSummary>` |
| GET | `/api/bookings/{id}` | Owner | — | 200 `BookingDetail` |
| GET | `/api/bookings/{id}/invoice` | Owner | — | 200 `application/pdf` |

The pay operation runs availability checks and increments package seats, room units, and vehicle units inside one transaction with pessimistic locks. It rejects insufficient capacity with 409.

## Reviews

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/api/packages/{id}/reviews` | Tourist | `{bookingId,rating,reviewText}` | 201 `ReviewResponse` |

The booking must belong to the user, be confirmed, have a travel date in the past, and not already have a review.

## AI

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| GET | `/api/ai/recommendations?bookingId=` | Tourist | — | 200 `{recommendations[]}` |
| POST | `/api/ai/budget-plan` | Tourist/Anonymous | `{prompt}` or `{budget,people,days,startingCity,interests,constraints}` | 200 `{followUpQuestion?,plans[],estimateNote}` |
| POST | `/api/ai/budget-plan/{planId}/book` | Tourist | — | 201 `BookingDraft` |

AI uses database-backed items only. The LLM parses intent and writes explanations; Java performs all matching and price math. If the LLM is unavailable, the response is produced by the rule-based fallback and `provider` is recorded in `ai_logs`.

## Admin (`ROLE_ADMIN`)

| Method | Path | Request | Response |
|---|---|---|---|
| GET/POST | `/api/admin/destinations`, `/api/admin/destinations/{id}` | Destination DTO | 200/201 destination |
| PUT/DELETE | `/api/admin/destinations/{id}` | Destination DTO / — | 200/204 |
| GET/POST | `/api/admin/places`, `/api/admin/places/{id}` | Place DTO | 200/201 place |
| PUT/DELETE | `/api/admin/places/{id}` | Place DTO / — | 200/204 |
| GET/POST | `/api/admin/packages`, `/api/admin/packages/{id}` | Package DTO with ordered places | 200/201 package |
| PUT/DELETE | `/api/admin/packages/{id}` | Package DTO / — | 200/204 |
| PUT | `/api/admin/packages/{id}/places` | `{places:[{placeId,dayNumber,orderNo,type,addOnPrice}]}` | 200 ordered package places |
| GET/POST | `/api/admin/hotels`, `/api/admin/hotels/{id}` | Hotel DTO | 200/201 hotel |
| PUT/DELETE | `/api/admin/hotels/{id}` | Hotel DTO / — | 200/204 |
| GET/POST | `/api/admin/rooms`, `/api/admin/rooms/{id}` | Room DTO | 200/201 room |
| PUT/DELETE | `/api/admin/rooms/{id}` | Room DTO / — | 200/204; future bookings cause 409 with deactivate option |
| POST | `/api/admin/hotels/{id}/rooms/bulk-upload` | multipart `file` CSV/XLSX | 200 `{imported,errors[]}` |
| GET | `/api/admin/hotels/{id}/rooms/bulk-template` | — | 200 CSV template |
| GET/POST | `/api/admin/vehicles`, `/api/admin/vehicles/{id}` | Vehicle DTO + destination IDs | 200/201 vehicle |
| PUT/DELETE | `/api/admin/vehicles/{id}` | Vehicle DTO / — | 200/204 |
| POST | `/api/admin/destinations/{id}/vehicles/bulk-upload` | multipart `file` CSV/XLSX | 200 `{imported,errors[]}` |
| GET | `/api/admin/destinations/{id}/vehicles/bulk-template` | — | 200 CSV template |
| POST | `/api/admin/images` | multipart `file`, `entityType`, `entityId` | 201 `{url}` |
| DELETE | `/api/admin/images/{id}` | — | 204 |
| GET | `/api/admin/bookings` | `status,dateFrom,dateTo,page,size` | 200 `Page<BookingSummary>` |
| PATCH | `/api/admin/bookings/{id}/status` | `{status}` | 200 booking |
| GET | `/api/admin/stats` | `from,to` | 200 `{monthlyBookings,revenue,popularDestinations,recentBookings}` |
| GET | `/api/admin/ai/status` | — | 200 `{configured,model}`; never key |
| POST | `/api/admin/ai/test` | — | 200 `{success,message,latencyMs}` |

## Status and validation notes

- 400: malformed JSON or Bean Validation failure; 401: missing/invalid JWT; 403: role or ownership failure; 404: missing resource; 409: availability, duplicate, or immutable business conflict; 413: image over 2 MB; 415: unsupported image/media type; 429: AI rate limit; 500: unexpected server failure.
- Accepted image types are JPG, PNG, and WebP only. Uploads are renamed randomly and only their disk path/URL is persisted.
- AI endpoints are rate limited and have bounded upstream timeouts. Secrets come from environment variables and are never returned to clients.
