# API Examples

Base URL:

```text
http://localhost:8080
```

## Schedule a Meeting

```http
POST /users/{organizerId}/slots/{slotId}/meetings
```

Example:

```bash
curl -X POST "http://localhost:8080/users/{organizerId}/slots/{slotId}/meetings" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Backend Design Discussion",
    "description": "Discuss scheduling architecture",
    "participantIds": [
      "{participantId}"
    ]
  }'
```

The meeting is created only if the organizer and all participants have matching free slots.

---

## Query Availability

```http
GET /availability
```

Example:

```bash
curl -G "http://localhost:8080/availability" \
  --data-urlencode "userIds={userId1}" \
  --data-urlencode "userIds={userId2}" \
  --data-urlencode "startTime=2026-08-13T09:00:00Z" \
  --data-urlencode "endTime=2026-08-13T12:00:00Z"
```

Returns the free/busy slots of the selected users within the requested time range.