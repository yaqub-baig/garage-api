# Voice Notes — Frontend Integration Guide

Every note, comment and remark field in the Garage API can now carry one optional voice recording alongside the text. This guide lists, screen by screen, exactly what the frontend has to change.

---

## 1. Key rules

1. **Voice notes are optional.** Text-only saves work exactly as before. Nothing breaks if the frontend ignores this feature.
2. **One recording per field.** A text field has at most one voice note. Recording again and saving replaces it.
3. **Upload first, then save.** Upload the audio to `/mediafiles/upload`, take the returned `id`, and send it in the save request next to the text field (see [§3](#3-the-flow)).
4. **Text and voice are independent.** The user can send text only, voice only, or both.
5. **On update endpoints, leaving the id out keeps the current recording.** There is currently no way to remove a voice note without replacing it.
6. **Only audio files are accepted.** Sending the id of an image or PDF returns `400`.

---

## 2. Base URL

| Environment | Base URL |
|---|---|
| Local | `http://localhost:8020/api/v1` |
| Production | `https://garage.autoadda.mylstech.com/api/v1` |

All paths below are relative to the base URL.

Most endpoints wrap their result in the standard `GenericResponse` envelope (`{ status, code, message, body }`). In the tables below, `body.…` marks those. Paths without `body.` (vehicle received, work orders, pickup/drop, roadside, insurance claims) return the object directly.

---

## 3. The flow

```
 record audio ──► POST /mediafiles/upload ──► [{ id: 812, ... }]
                                                    │
 save the form ──► PUT /jobcards/... { comments: "...", voiceNoteId: 812 }
                                                    │
 load the screen ◄── { comments: "...", voiceNote: { id: 812, fileUrl: "...", ... } }
```

### 3.1 Upload

`POST /mediafiles/upload`, `multipart/form-data`, form field **`files`** (the same endpoint used for photos).

Accepted audio:

- any `audio/*` content type (`audio/webm`, `audio/mp4`, `audio/mpeg`, `audio/ogg`, `audio/wav`, `audio/aac`, …), or
- a file name ending in `.mp3 .m4a .aac .wav .ogg .opus .amr .flac`. This covers mobile uploads whose content type is `application/octet-stream`.

> A browser recording with type `audio/webm` is stored as audio. A file **named** `.webm` with no audio content type is stored as video and will be **rejected** as a voice note, so always set the blob's type when uploading.

Max size: **20 MB** per file (a few minutes of audio is well under 1 MB).

The response is **not** wrapped in `GenericResponse`. It is an array with one entry per uploaded file:

```json
[
  {
    "id": 812,
    "fileName": "3f1c…_voice-note.webm",
    "originalFileName": "voice-note.webm",
    "filePath": "audios/3f1c…_voice-note.webm",
    "mediaType": "audio/webm",
    "size": 48213,
    "createdAt": "2026-11-03T15:40:12"
  }
]
```

Use `[0].id` as the voice note id.

### 3.2 Sample code (web)

```ts
// Record
const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
const recorder = new MediaRecorder(stream);            // Chrome/Firefox: audio/webm, Safari: audio/mp4
const chunks: Blob[] = [];
recorder.ondataavailable = e => chunks.push(e.data);
recorder.start();
// … later
recorder.stop();
await new Promise(r => (recorder.onstop = r));
stream.getTracks().forEach(t => t.stop());
const blob = new Blob(chunks, { type: recorder.mimeType });

// Upload
export async function uploadVoiceNote(blob: Blob): Promise<number> {
  const ext = blob.type.includes('mp4') ? 'm4a' : blob.type.includes('ogg') ? 'ogg' : 'webm';
  const form = new FormData();
  form.append('files', blob, `voice-note.${ext}`);
  const res = await fetch(`${BASE_URL}/mediafiles/upload`, { method: 'POST', body: form });
  if (!res.ok) throw new Error('Voice note upload failed');
  const [file] = await res.json();
  return file.id;
}

// Play: works for both response shapes (see §3.3)
export function voiceNoteUrl(v?: { fileUrl?: string; filePath?: string } | null) {
  if (!v) return null;
  return v.fileUrl ?? `${BASE_URL}/uploads/${v.filePath}`;
}
// <audio controls src={voiceNoteUrl(item.voiceNote)} />
```

**Mobile:** record to AAC/M4A (`audio/mp4`) and upload with file name `voice-note.m4a` and type `audio/mp4`.

### 3.3 What comes back

Voice notes appear in responses in one of two shapes, depending on the endpoint:

| Shape | Looks like | Endpoints |
|---|---|---|
| **A: response DTO** (most endpoints) | `{ id, fileName, originalFileName, filePath, mediaType, size, fileUrl, createdAt }`, with `fileUrl` ready to play | everything not listed under B |
| **B: raw media file** (endpoints that return the database entity directly) | same fields **without `fileUrl`**; build the URL as `${BASE_URL}/uploads/${filePath}` | appointment status update, additional requests, pickup/drop, roadside assistance, insurance claims |

The `voiceNoteUrl()` helper above handles both. When there is no recording, the field is `null`.

---

## 4. Changes by screen

In every table, **Text field** is what the form already sends. Add the **Send** field next to it, and read the **Read** field to show a player.

### 4.1 Part requests (technician → customer)

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Create part request | `POST /part-requests` | `notes` | `voiceNoteId` | `body.voiceNote` |
| Edit part request | `PUT /part-requests/{id}` | `notes` | `voiceNoteId` | `body.voiceNote` |
| Job card → "Waiting for parts" | `PUT /part-requests/jobcard/status` | `partRequestNotes` | `partRequestVoiceNoteId` | (on the part request) `voiceNote` |
| Customer responds (self-provide / garage procure) | `PUT /part-requests/{id}/customer-response` | `customerComments` | `customerVoiceNoteId` | `body.customerResponse.customerVoiceNote` |
| Garage logs procurement | `POST /part-requests/{id}/procurement` | `notes` | `voiceNoteId` | `body.items[].procurement.voiceNote` |

Edit part request: the backend used to require "notes or items". It now also accepts a request carrying **only** `voiceNoteId`.

```json
PUT /part-requests/jobcard/status
{
  "jobcardId": 55,
  "status": "WAITING_FOR_PARTS",
  "memberId": 9,
  "partRequestItems": [ { "partName": "Brake pad", "quantity": 2 } ],
  "partRequestNotes": "Front pads worn out",
  "partRequestVoiceNoteId": 812
}
```

### 4.2 Appointments

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Update appointment status | `PUT /appointments/status` | `comments` | `voiceNoteId` | `body.voiceNote` (**shape B**) |
| Any appointment GET (details, lists, inside work orders, vehicle received) | — | `comments` | — | `voiceNote` (shape A) |

The status update stores the recording both on the appointment and on that status-history entry.

> **Every status update overwrites the appointment's comment and voice note**, as it already does for `comments`. A status change sent without `voiceNoteId` clears the appointment's recording. Earlier recordings stay on their own status-history entries.

### 4.3 Job cards & work orders

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Assign technician to a service | `POST /jobcards/assign` | `comments` | `voiceNoteId` | `body.voiceNote` |
| Create work order with assignments | `POST /work-orders` → `assignServiceRequestList[]` | `comments` | `voiceNoteId` (per item) | `jobCards[].voiceNote` |
| Update work order: add assignments | `PUT /work-orders/update/{id}` → `assignServiceRequestList[]` | `comments` | `voiceNoteId` (per item) | `jobCards[].voiceNote` |
| Update work order: customer counter-offer | `PUT /work-orders/update/{id}` | `customerNegotiatedComment` | `customerNegotiatedVoiceNoteId` | `customerNegotiatedVoiceNote` |
| Update work order: garage counter-offer | `PUT /work-orders/update/{id}` | `garageNegotiatedComment` | `garageNegotiatedVoiceNoteId` | `garageNegotiatedVoiceNote` |
| Additional request on a job card | `POST /additional-requests` | `comments` | `voiceNoteId` | `body.voiceNote` (**shape B**) |

```json
PUT /work-orders/update/31
{
  "garageNegotiatedAmount": 4200,
  "garageNegotiatedComment": "Includes wheel alignment",
  "garageNegotiatedVoiceNoteId": 813
}
```

### 4.4 Vehicle check-in & delivery

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Receive vehicle | `POST /vehicle-received` | `comment` | `voiceNoteId` | `voiceNote` |
| Edit received vehicle | `PUT /vehicle-received/update/{id}` | `comment` | `voiceNoteId` | `voiceNote` |
| Delivery checklist (technician) | `PUT /vehicle-delivery/{deliveryId}/checklist` | `comments` | `voiceNoteId` | `customerVoiceNote` |
| Customer approves / rejects delivery | `PUT /vehicle-delivery/{deliveryId}/customer-approval` | `customerComments` | `customerVoiceNoteId` | `customerVoiceNote` |
| Delivery inside service history | — | `customerComments` | — | `latestDelivery.customerVoiceNote` |

> Delivery has **one** comment slot (`customerComments`), and the checklist and customer-approval calls both write to it, so they share one voice note too: `customerVoiceNote`. On the customer-approval call the voice note is always overwritten, so if the customer doesn't record one, the checklist recording is cleared.

### 4.5 Invoices

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Generate invoice | `POST /invoices/generate` | `notes` | `voiceNoteId` | `body.voiceNote` |
| Auto-generate from work order | `POST /invoices/work-order/{workOrderId}/auto-generate?notes=…` | `notes` (query) | **`voiceNoteId` (query param)** | `body.voiceNote` |
| Pay invoice | `PUT /invoices/pay` | `comments` | `voiceNoteId` | invoice `voiceNote` |
| Invoice in service history | — | `notes` | — | `invoice.voiceNote` |

```
POST /invoices/work-order/31/auto-generate?taxRate=5&notes=Paid%20in%20cash&voiceNoteId=814
```

### 4.6 Inventory & tools

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Add inventory item | `POST /garage-inventory` | `notes` | `voiceNoteId` | `voiceNote` |
| Edit inventory item | `PUT /garage-inventory/{itemId}` | `notes` | `voiceNoteId` | `voiceNote` |
| Add stock | `POST /garage-inventory/{itemId}/add-stock` | `notes` | `voiceNoteId` | transaction `voiceNote` |
| Remove stock | `POST /garage-inventory/{itemId}/remove-stock` | `notes` | `voiceNoteId` | transaction `voiceNote` |
| Adjust stock | `POST /garage-inventory/{itemId}/adjust-stock` | `notes` | `voiceNoteId` | transaction `voiceNote` |
| Resolve alert | `PUT /inventory-alerts/{alertId}/resolve` | `notes` | `voiceNoteId` | **`resolutionVoiceNote`** |
| Dismiss alert | `PUT /inventory-alerts/{alertId}/dismiss` | `notes` | `voiceNoteId` | **`resolutionVoiceNote`** |
| Add working tool | `POST /working-tools` | `notes` | `voiceNoteId` | `voiceNote` |
| Edit working tool | `PUT /working-tools/{toolId}` | `notes` | `voiceNoteId` | `voiceNote` |

An alert that is auto-resolved by a restock clears `resolutionVoiceNote`.

### 4.7 Garage business sale

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Finalize sale | `POST /garage-business-listings/{id}/finalize-sale` | `notes` | `voiceNoteId` | `body.voiceNote` |

### 4.8 Pickup / drop, roadside assistance, insurance claims

These endpoints return the database entity directly (**shape B**: no `fileUrl`).

| Action | Endpoint | Text field | Send | Read |
|---|---|---|---|---|
| Create pickup/drop request | `POST /api/pickup-drop` | `notes` | `voiceNoteId` (body) | `voiceNote` |
| Update roadside status | `PUT /api/roadside-assistance/{id}/status?status=…&remarks=…` | `remarks` (query) | **`voiceNoteId` (query param)** | `voiceNote` |
| Update claim status | `PUT /api/insurance-claims/{claimId}/status?status=…&remarks=…` | `remarks` (query) | **`voiceNoteId` (query param)** | **`approvalVoiceNote`** |

> **Roadside and insurance status updates overwrite the voice note on every call.** Leaving `voiceNoteId` out **clears** the existing recording, just as leaving out `remarks` clears the text. Resend the current id if you want to keep it.

---

## 5. Field name cheat sheet

| Text field | Request field | Response field |
|---|---|---|
| `notes` / `comments` / `comment` / `remarks` | `voiceNoteId` | `voiceNote` |
| `partRequestNotes` | `partRequestVoiceNoteId` | part request `voiceNote` |
| `customerComments` | `customerVoiceNoteId` (`voiceNoteId` on the delivery checklist) | `customerVoiceNote` |
| `customerNegotiatedComment` | `customerNegotiatedVoiceNoteId` | `customerNegotiatedVoiceNote` |
| `garageNegotiatedComment` | `garageNegotiatedVoiceNoteId` | `garageNegotiatedVoiceNote` |
| inventory alert `notes` | `voiceNoteId` | `resolutionVoiceNote` |
| insurance claim `remarks` | `voiceNoteId` | `approvalVoiceNote` |

---

## 6. Errors

| Situation | Status | Message |
|---|---|---|
| `voiceNoteId` does not exist | `404` | `Voice note not found with id: 812` |
| `voiceNoteId` is not an audio file | `400` | `Media file 812 is not an audio recording` |
| Upload with an unsupported type/extension | `400` | `Unsupported file type: …` |
| Upload over 20 MB | `400`/`413` | upload rejected |

Upload the recording **before** submitting the form. If the upload fails, let the user retry or save without the voice note. Don't block the text save.

---

## 7. Suggested UI

- A mic button next to every notes/comments textarea: tap to record, tap to stop, then a small player with "re-record" and "discard".
- Upload as soon as recording stops, and keep the returned id in form state. Discarding before save simply means not sending the id. The orphaned upload is harmless.
- Wherever the text note is shown (lists, details, timelines), show an `<audio controls>` player when the voice-note field is not `null`.
- Ask for microphone permission only when the mic is first tapped.
