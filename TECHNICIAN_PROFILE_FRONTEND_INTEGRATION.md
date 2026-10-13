# Technician CV Profile — Frontend Integration Guide

How to build the step-wise technician CV form (personal details → qualifications → … → social profiles), together with its profile-completion progress bar, on top of the Garage API.

---

## 1. Key rules

1. **Nothing is mandatory.** Any field can be omitted, any item can be partially filled, and any step can be skipped. The backend never rejects a save because something is missing. Do **not** block "Next" or "Save" on empty fields.
2. **Each step saves on its own** with a single `PUT`. A natural place to call it is the "Save & Next" button. Steps can be saved in any order and as often as you like.
3. **Every save returns the new completion** (`completion`), so update the progress bar from the save response. No extra request is needed.
4. **List steps send the whole list.** For qualifications, certifications, skills, employment, languages, documents, references and social links, the request body is the full current list for that step (see [§5](#5-list-step-semantics-important)).
5. **Files are uploaded first.** Upload to `/mediafiles/upload`, then send the returned `id` in the step payload (see [§7](#7-file-uploads-qualifications-certifications-documents)).

---

## 2. Base URL and response envelope

| Environment | Base URL |
|---|---|
| Local | `http://localhost:8020/api/v1` |
| Production | `https://garage.autoadda.mylstech.com/api/v1` |

All profile endpoints live under:

```
/technicians/{technicianId}/profile
```

Every profile endpoint wraps its result in the standard `GenericResponse` envelope. **The payload is in `body`.**

```json
{
  "status": "success",
  "code": "200",
  "message": "Languages saved successfully",
  "body": { ... }
}
```

Errors use the same envelope with `"status": "failed"`; see [§9](#9-errors).

> `/mediafiles/upload` and the older `/technicians/...` endpoints (for example `GET /technicians/{id}`) are **not** wrapped. They return the object or array directly.

---

## 3. Endpoints

| # | Step | Method & path | Request body |
|---|---|---|---|
| – | Load whole profile | `GET /technicians/{id}/profile` | – |
| – | Completion only | `GET /technicians/{id}/profile/completion` | – |
| 1 | Personal details | `PUT /technicians/{id}/profile/personal` | `PersonalDetailsRequest` (object) |
| 2 | Qualifications | `PUT /technicians/{id}/profile/qualifications` | `QualificationRequest[]` |
| 3 | Certifications & licenses | `PUT /technicians/{id}/profile/certifications` | `CertificationRequest[]` |
| 4 | Technical skills | `PUT /technicians/{id}/profile/skills` | `SkillsStepRequest` (object) |
| 5 | Work history (jobs + career breaks) | `PUT /technicians/{id}/profile/employment` | `EmploymentRequest[]` (response `data` is an object, see Step 5) |
| 6 | Languages | `PUT /technicians/{id}/profile/languages` | `LanguageRequest[]` |
| 7 | Documents | `PUT /technicians/{id}/profile/documents` | `DocumentRequest[]` |
| 8 | References & emergency contact | `PUT /technicians/{id}/profile/references` | `ReferencesStepRequest` (object) |
| 9 | Social profiles | `PUT /technicians/{id}/profile/social-links` | `SocialLinkRequest[]` |

Every `PUT` returns:

```json
{
  "status": "success",
  "code": "200",
  "message": "...",
  "body": {
    "data": { ... the saved section, same shape as in GET /profile ... },
    "completion": { ... ProfileCompletion ... }
  }
}
```

---

## 4. Recommended UI flow

```
Open form
  └─ GET /technicians/{id}/profile
       ├─ prefill every step from body.personal, body.qualifications, ...
       ├─ draw progress bar from body.completion.percent / status
       ├─ mark each step chip from body.completion.steps[].status
       └─ jump to body.completion.nextIncompleteStep   (resume where they left off)

On "Save & Next" for step N
  └─ PUT /technicians/{id}/profile/<step>
       ├─ replace the step's local state with body.data   (it now contains server ids)
       ├─ update progress bar from body.completion
       └─ go to step N+1   (or to body.completion.nextIncompleteStep)

On "Skip"
  └─ no request; just go to the next step
```

**Always replace local list state with `body.data` after saving.** New items get their `id` only from the server. If the next save sends them again without an `id`, they will be created a second time as duplicates.

---

## 5. List step semantics (important)

All list steps follow the same rules:

| What you send | What happens |
|---|---|
| Item **with** `id` | That existing item is updated. **All** its fields are overwritten, so send the full item, not just the changed fields. |
| Item **without** `id` | A new item is created. |
| A stored item you **don't** send | It is **deleted**. |
| `[]` (empty array) | All items in that step are deleted, and the step becomes "not started". |
| No body / `null` | Nothing changes. The current list is returned. |
| `{}` item (all fields empty) | Saved as an empty item. This is allowed. |

- **Order:** items are stored in the order you send them, and `displayOrder` (0, 1, 2…) comes back in the response. To reorder items, send the list in the new order.
- **Invalid ids:** sending an `id` that isn't in this technician's list, or the same `id` twice, returns **400** and nothing in that step is changed.

Example: the stored languages are Arabic (id 10) and Hindi (id 11). The user edits Arabic, removes Hindi, and adds English:

```http
PUT /technicians/42/profile/languages
Content-Type: application/json

[
  { "id": 10, "language": "Arabic",  "proficiency": "FLUENT" },
  {           "language": "English", "proficiency": "CONVERSATIONAL" }
]
```

Result: Arabic is updated, Hindi is deleted and English is created.

---

## 6. Steps in detail

Dates are `"YYYY-MM-DD"` strings. Every field is optional.

### Step 1 — Personal details (`PUT /personal`)

This step is an object, not a list. **A `null` or missing field means "keep the current value"**, so you can send only the fields that changed. To blank out a text field, send `""`.

```json
{
  "firstName": "Ahmed",
  "lastName": "Khan",
  "emailId": "ahmed@example.com",
  "mobileNumber": "+971501234567",
  "city": "Dubai",
  "profileImg": "https://.../photo.jpg",
  "yearOfExperience": 8,

  "dateOfBirth": "1992-04-18",
  "gender": "MALE",
  "nationality": "Pakistani",
  "maritalStatus": "MARRIED",
  "alternatePhone": "+971551234567",
  "addressLine": "Al Quoz Industrial Area 3",
  "country": "UAE",
  "passportNumber": "AB1234567",
  "passportExpiry": "2030-01-31",
  "visaStatus": "EMPLOYMENT",
  "visaExpiry": "2027-06-30",
  "drivingLicenseNumber": "DL-998877",
  "professionalSummary": "Engine & transmission specialist with 8 years in German cars."
}
```

- **Shared fields:** the first group (`firstName` … `yearOfExperience`) is also the technician's basic record, so it's shared with the existing technician screens.
- **Mobile number conflict:** changing `mobileNumber` to a number another technician already uses returns **409**.
- **Prefilling for new technicians:** for a technician who has never saved this step, `GET /profile` already returns the basic fields from onboarding (`firstName`, `mobileNumber`, …), with the rest `null`.

### Step 2 — Qualifications (`PUT /qualifications`)

```json
[
  {
    "id": 5,
    "qualificationType": "DIPLOMA",
    "title": "Diploma in Automobile Engineering",
    "fieldOfStudy": "Automobile",
    "institution": "Govt. Polytechnic Lahore",
    "country": "Pakistan",
    "startDate": "2009-09-01",
    "endDate": "2012-06-30",
    "isCurrent": false,
    "grade": "A",
    "certificateFileId": 301
  }
]
```

Response items also include `certificateFileUrl` and `certificateFileName`.

### Step 3 — Certifications & licenses (`PUT /certifications`)

```json
[
  {
    "certificationType": "OEM_TRAINING",
    "name": "BMW Level 2 Diagnostics",
    "issuingOrganization": "BMW Academy",
    "credentialId": "BMW-L2-55621",
    "issueDate": "2021-03-10",
    "expiryDate": null,
    "documentFileId": 302
  }
]
```

Response items also include `documentFileUrl` and `documentFileName`.

### Step 4 — Technical skills (`PUT /skills`)

This step is an object made of three independent parts. **Each part follows the list rules in §5, and `null` means "leave that part unchanged".**

```json
{
  "skillSetIds": [1, 4, 7],
  "expertise": [
    { "expertiseCategoryId": 2, "expertiseLevel": "L4" },
    { "expertiseCategoryId": 5, "expertiseLevel": "L2" }
  ],
  "skills": [
    { "id": 12, "skillName": "ECU diagnostics", "level": "L3", "yearsOfExperience": 5 },
    {           "skillName": "Bosch KTS",       "level": "L2", "yearsOfExperience": 2 }
  ]
}
```

| Part | Meaning | Source of options |
|---|---|---|
| `skillSetIds` | Services the technician can perform (ids only; send `[]` to clear) | `GET /available-services` (existing) |
| `expertise` | Expertise category + level `L1`–`L5` (no `id`; the whole list is replaced) | `GET /expertise-categories` (existing) |
| `skills` | Free-text skills | user input |

An `expertise` entry with no `expertiseCategoryId` or no `expertiseLevel` is **ignored, not rejected**.

Response `data`:

```json
{ "skillSet": [ ...AvailableService ], "expertise": [ ...TechnicianExpertise ], "skills": [ ...Skill ] }
```

### Step 5 — Work history (`PUT /employment`)

The work history is one timeline holding two kinds of entries:

| `entryType` | Use for | Relevant fields |
|---|---|---|
| `EMPLOYMENT` (the default when omitted) | A job | `employerName`, `jobTitle`, `city`, `country`, `startDate`, `endDate`, `isCurrent`, `responsibilities`, `reasonForLeaving` |
| `CAREER_BREAK` | A period without work, with the reason for it | `breakReason`, `breakDescription`, `startDate`, `endDate`, `isCurrent` |

Send both kinds together in one list, using the usual list rules from §5. In this example, a one-year gap between two jobs is explained by a career break:

```json
[
  {
    "employerName": "Al Futtaim Motors",
    "jobTitle": "Technician",
    "startDate": "2015-01-01",
    "endDate": "2019-12-31",
    "reasonForLeaving": "Relocated to UAE"
  },
  {
    "entryType": "CAREER_BREAK",
    "breakReason": "VISA_PROCESSING",
    "breakDescription": "Moved to UAE and waited for my work visa.",
    "startDate": "2020-01-01",
    "endDate": "2020-12-31"
  },
  {
    "employerName": "AutoAdda Garage",
    "jobTitle": "Senior Technician",
    "city": "Dubai",
    "country": "UAE",
    "startDate": "2021-01-01",
    "isCurrent": true,
    "responsibilities": "Diagnostics, engine overhaul, mentoring juniors"
  }
]
```

`breakReason` values: `EDUCATION`, `HEALTH`, `FAMILY_CARE`, `RELOCATION`, `VISA_PROCESSING`, `JOB_SEARCH`, `TRAVEL`, `PERSONAL`, `OTHER`. To change an entry's type, save it with the new `entryType`.

**Response.** Unlike the other list steps, `data` (and `employment` in `GET /profile`) is an **object**, not an array:

```json
{
  "entries": [ { "id": 21, "displayOrder": 0, "entryType": "EMPLOYMENT", ... }, ... ],
  "unexplainedGaps": [
    { "startDate": "2020-01-01", "endDate": "2020-12-31", "months": 12, "ongoing": false }
  ],
  "gapThresholdMonths": 6
}
```

**Gaps.** The backend lists every period of at least `gapThresholdMonths` months (6 by default) that is covered by neither a job nor a career break.

How the backend decides what counts as covered:
- An entry covers the period from `startDate` to `endDate`.
- A current entry (`isCurrent: true`) covers up to today.
- An entry with no `endDate` that isn't current covers only its start date.
- Entries without a `startDate` are ignored.
- Gaps **before the first entry** are not reported, since that's usually education.
- A gap **from the last entry until today**, when nothing is current, is reported with `ongoing: true`.

Gaps are **only a hint and never block saving**. Suggested UI:
- Below the timeline, show each gap: *"No work history from Jan 2020 to Dec 2020 (12 months). [Add a reason] [Ignore]"*.
- **Add a reason** opens a new `CAREER_BREAK` entry prefilled with the gap's `startDate` and `endDate`. Once it's saved, the gap disappears from `unexplainedGaps`.
- For an `ongoing` gap, word the prompt differently: *"Are you currently working? Mark your latest job as current, or add a career break."*

**Completion.** This step counts as completed once there is at least one `EMPLOYMENT` entry. Career breaks alone don't complete it, and unexplained gaps don't lower the percent.

> This is the technician's **own work history**, entered by the technician. It is separate from the job-card history at `GET /technicians/jobCardHistoryByTechnicianId/{id}`.
>
> **Visibility:** career break reasons and descriptions are returned to anyone who can load the profile. The API has no separate restriction for them, so decide in the UI where they should be shown.

### Step 6 — Languages (`PUT /languages`)

```json
[
  { "language": "Arabic",  "proficiency": "CONVERSATIONAL" },
  { "language": "English", "proficiency": "FLUENT" }
]
```

### Step 7 — Documents (`PUT /documents`)

```json
[
  { "documentType": "RESUME",   "fileId": 310 },
  { "documentType": "PASSPORT", "documentNumber": "AB1234567", "expiryDate": "2030-01-31", "fileId": 311 }
]
```

Response items also include `fileUrl` and `fileName`.

**Fields per document type.** `documentNumber` is the identification number printed on the document, and `expiryDate` is the expiry date printed on it. Both are optional. The backend does **not** check them against `documentType`; it stores whatever is sent. The frontend therefore decides which inputs to show for each type:

| `documentType` | Show number? | Label | Example / format | Show expiry? |
|---|---|---|---|---|
| `RESUME` | No | – | – | No |
| `PASSPORT` | Yes | Passport number | `AB1234567` (letters and digits, usually 6–9 characters) | Yes |
| `VISA` | Yes | Visa file number / UID | `201/2023/1234567` | Yes |
| `EMIRATES_ID` | Yes | Emirates ID number | `784-1990-1234567-1` (15 digits) | Yes |
| `DRIVING_LICENSE` | Yes | Licence number | `1234567` | Yes |
| `OTHER` | Yes | Document number (optional) | free text | Yes |

The file upload (`fileId`) applies to every type.

```ts
const DOCUMENT_FIELDS: Record<DocumentType, { number?: { label: string; placeholder: string }; expiry: boolean }> = {
  RESUME:          { expiry: false },
  PASSPORT:        { number: { label: 'Passport number',        placeholder: 'AB1234567' },          expiry: true },
  VISA:            { number: { label: 'Visa file number / UID', placeholder: '201/2023/1234567' },   expiry: true },
  EMIRATES_ID:     { number: { label: 'Emirates ID number',     placeholder: '784-1990-1234567-1' }, expiry: true },
  DRIVING_LICENSE: { number: { label: 'Licence number',         placeholder: '1234567' },            expiry: true },
  OTHER:           { number: { label: 'Document number',        placeholder: 'Optional' },           expiry: true },
};
```

- **Hidden fields:** when a field is hidden for the selected type, send it as `null`. For example, if the user switches a document to `RESUME`, any number or expiry entered earlier won't stay stored.
- **Prefill from data the technician already entered.** These values may already exist elsewhere in the profile:

  | `documentType` | Prefill from |
  |---|---|
  | `PASSPORT` | `personal.passportNumber`, `personal.passportExpiry` |
  | `VISA` | `personal.visaExpiry` |
  | `DRIVING_LICENSE` | `personal.drivingLicenseNumber` |
  | `EMIRATES_ID` | the technician's `emiratesId` from onboarding (`GET /technicians/{id}`) |

  The backend does **not** keep these copies in sync, so prefilling is what keeps them consistent.

### Step 8 — References & emergency contact (`PUT /references`)

This step is an object. Each emergency contact field follows the personal-step rule (`null` = keep the current value), and `references` follows the list rules in §5.

```json
{
  "emergencyContactName": "Sara Khan",
  "emergencyContactRelation": "Wife",
  "emergencyContactPhone": "+971509998877",
  "references": [
    {
      "name": "John Mathew",
      "company": "Al Futtaim Motors",
      "designation": "Workshop Manager",
      "relationship": "Former manager",
      "phone": "+971501112233",
      "email": "john.m@example.com"
    }
  ]
}
```

### Step 9 — Social profiles (`PUT /social-links`)

```json
[
  { "platform": "INSTAGRAM", "url": "https://instagram.com/ahmed.fixes", "handle": "@ahmed.fixes" },
  { "platform": "YOUTUBE",   "url": "https://youtube.com/@ahmedgarage",   "handle": "@ahmedgarage" }
]
```

---

## 7. File uploads (qualifications, certifications, documents)

1. Upload the file(s) as `multipart/form-data` with the field name **`files`** (one or many). Each file must be smaller than 30 MB.

   ```http
   POST /mediafiles/upload
   Content-Type: multipart/form-data
   files=<binary>
   ```

   The response is **not wrapped**; it's a plain array:

   ```json
   [
     { "id": 310, "fileName": "a1b2c3_cv.pdf", "originalFileName": "cv.pdf",
       "filePath": "https://.../a1b2c3_cv.pdf", "mediaType": "application/pdf", "size": 182044 }
   ]
   ```

2. Put `id` in the step item: `certificateFileId` (qualifications), `documentFileId` (certifications) or `fileId` (documents).
3. Save the step.
4. **To remove a file from an item**, save the item with the file id set to `null`. **To delete the item entirely**, leave it out of the list.

An unknown file id returns **400** (`Media file not found with id: …`).

---

## 8. Profile completion (progress bar)

Returned as `completion` on every `PUT`, in `GET /profile` and in `GET /profile/completion`:

```json
{
  "percent": 56,
  "status": "IN_PROGRESS",
  "nextIncompleteStep": "QUALIFICATIONS",
  "updatedAt": "2026-10-13T14:05:12.123",
  "steps": [
    { "step": "PERSONAL",       "order": 1, "status": "COMPLETED",   "percent": 100 },
    { "step": "QUALIFICATIONS", "order": 2, "status": "NOT_STARTED", "percent": 0 },
    { "step": "CERTIFICATIONS", "order": 3, "status": "COMPLETED",   "percent": 100 },
    { "step": "SKILLS",         "order": 4, "status": "COMPLETED",   "percent": 100 },
    { "step": "EMPLOYMENT",     "order": 5, "status": "COMPLETED",   "percent": 100 },
    { "step": "LANGUAGES",      "order": 6, "status": "NOT_STARTED", "percent": 0 },
    { "step": "DOCUMENTS",      "order": 7, "status": "NOT_STARTED", "percent": 0 },
    { "step": "REFERENCES",     "order": 8, "status": "PARTIAL",     "percent": 67 },
    { "step": "SOCIAL_LINKS",   "order": 9, "status": "COMPLETED",   "percent": 100 }
  ]
}
```

**How it is calculated.** The 9 steps carry equal weight, and `percent` is the rounded average of the step percents.

| Step | Counts as COMPLETED when | PARTIAL |
|---|---|---|
| PERSONAL | All 7 are filled: first name, last name, mobile, city, date of birth, nationality, professional summary | When only some are filled; percent = filled / 7 |
| REFERENCES | Emergency contact name, emergency contact phone, and at least 1 reference | When only some are filled; percent = filled / 3 |
| SKILLS | Any of skill set, expertise or free-text skills is non-empty | – |
| EMPLOYMENT | At least 1 job (`entryType: EMPLOYMENT`); career breaks don't count | – |
| All other steps | At least 1 item | – |

| `status` (overall) | When |
|---|---|
| `NOT_STARTED` | `percent == 0` |
| `IN_PROGRESS` | `1–99` |
| `COMPLETED` | `100` |

**UI suggestions**
- Draw the progress bar from `percent`, with a label based on `status`.
- Step chips: ✓ for `COMPLETED`, a half-filled icon for `PARTIAL`, and an empty icon for `NOT_STARTED`.
- Use `nextIncompleteStep` for a "Continue profile" button. When it's `null`, the profile is 100% complete.
- `updatedAt` is the last time the profile was changed, and is `null` if it never was. Show it as "Last updated …".

**In technician lists.** Every existing technician response (`GET /technicians/{id}`, `/technicians/garage/{garageId}`, the search endpoints) now includes:

```json
{ "lastName": "Khan", "profileCompletionPercent": 56, "profileCompletionStatus": "IN_PROGRESS" }
```

Use these values for progress badges in lists without calling the profile endpoints.

> A technician who was onboarded before this feature may show `0` / `NOT_STARTED` in lists until their profile is opened (`GET /profile` or `/completion`) or saved once. Opening the profile refreshes the stored value.

**Filtering incomplete profiles (admin).** `GET /technicians/search/paginated` accepts two new query parameters:

| Param | Example | Meaning |
|---|---|---|
| `profileCompletionStatus` | `IN_PROGRESS` | Exact status |
| `minProfileCompletion` | `50` | Percent ≥ value |

```
/technicians/search/paginated?profileCompletionStatus=IN_PROGRESS&minProfileCompletion=50&page=0&size=10
```

---

## 9. Errors

| HTTP | When | What to show |
|---|---|---|
| 400 | Item `id` not in this technician's list, or duplicated; unknown file id | Reload the step (`GET /profile`) and retry. This usually means stale local state. |
| 400 | Invalid enum value or malformed JSON | Developer error. Check the enum lists in §10. |
| 404 | Technician not found or deleted | "Technician not found". |
| 409 | `mobileNumber` already used by another technician (personal step) | "This mobile number is already registered". |

```json
{ "status": "failed", "code": "409", "message": "Technician with this phone number already exists", "body": null }
```

Each step is saved atomically: if a request fails, **nothing** in that step is saved.

---

## 10. Enum values

| Enum | Values |
|---|---|
| `gender` | `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY` |
| `maritalStatus` | `SINGLE`, `MARRIED`, `DIVORCED`, `WIDOWED` |
| `visaStatus` | `EMPLOYMENT`, `RESIDENCE`, `GOLDEN`, `FREELANCE`, `VISIT`, `CITIZEN`, `OTHER` |
| `qualificationType` | `SCHOOL`, `DIPLOMA`, `ITI_VOCATIONAL`, `APPRENTICESHIP`, `BACHELORS`, `MASTERS`, `OTHER` |
| `certificationType` | `CERTIFICATION`, `LICENSE`, `OEM_TRAINING`, `OTHER` |
| `level` (skills) / `expertiseLevel` | `L1` (beginner), `L2` (basic), `L3` (intermediate), `L4` (advanced), `L5` (master) |
| `proficiency` (languages) | `BASIC`, `CONVERSATIONAL`, `FLUENT`, `NATIVE` |
| `entryType` (work history) | `EMPLOYMENT`, `CAREER_BREAK` |
| `breakReason` | `EDUCATION`, `HEALTH`, `FAMILY_CARE`, `RELOCATION`, `VISA_PROCESSING`, `JOB_SEARCH`, `TRAVEL`, `PERSONAL`, `OTHER` |
| `documentType` | `RESUME`, `PASSPORT`, `VISA`, `EMIRATES_ID`, `DRIVING_LICENSE`, `OTHER` |
| `platform` (social) | `INSTAGRAM`, `YOUTUBE`, `TIKTOK`, `FACEBOOK`, `LINKEDIN`, `X`, `SNAPCHAT`, `WEBSITE`, `OTHER` |
| `ProfileStep` | `PERSONAL`, `QUALIFICATIONS`, `CERTIFICATIONS`, `SKILLS`, `EMPLOYMENT`, `LANGUAGES`, `DOCUMENTS`, `REFERENCES`, `SOCIAL_LINKS` |
| step `status` | `NOT_STARTED`, `PARTIAL`, `COMPLETED` |
| overall `status` | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` |

---

## 11. TypeScript types

```ts
// ---------- envelope ----------
export interface GenericResponse<T> {
  status: 'success' | 'failed';
  code: string;
  message: string;
  body: T;
}
export interface StepSaveResponse<T> {
  data: T;
  completion: ProfileCompletion;
}

// ---------- enums ----------
export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
export type MaritalStatus = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
export type VisaStatus = 'EMPLOYMENT' | 'RESIDENCE' | 'GOLDEN' | 'FREELANCE' | 'VISIT' | 'CITIZEN' | 'OTHER';
export type QualificationType = 'SCHOOL' | 'DIPLOMA' | 'ITI_VOCATIONAL' | 'APPRENTICESHIP' | 'BACHELORS' | 'MASTERS' | 'OTHER';
export type CertificationType = 'CERTIFICATION' | 'LICENSE' | 'OEM_TRAINING' | 'OTHER';
export type ExpertiseLevel = 'L1' | 'L2' | 'L3' | 'L4' | 'L5';
export type LanguageProficiency = 'BASIC' | 'CONVERSATIONAL' | 'FLUENT' | 'NATIVE';
export type DocumentType = 'RESUME' | 'PASSPORT' | 'VISA' | 'EMIRATES_ID' | 'DRIVING_LICENSE' | 'OTHER';
export type SocialPlatform = 'INSTAGRAM' | 'YOUTUBE' | 'TIKTOK' | 'FACEBOOK' | 'LINKEDIN' | 'X' | 'SNAPCHAT' | 'WEBSITE' | 'OTHER';
export type ProfileStep = 'PERSONAL' | 'QUALIFICATIONS' | 'CERTIFICATIONS' | 'SKILLS' | 'EMPLOYMENT' | 'LANGUAGES' | 'DOCUMENTS' | 'REFERENCES' | 'SOCIAL_LINKS';
export type ProfileStepStatus = 'NOT_STARTED' | 'PARTIAL' | 'COMPLETED';
export type ProfileCompletionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

type ISODate = string; // "YYYY-MM-DD"

// ---------- completion ----------
export interface ProfileStepCompletion {
  step: ProfileStep;
  order: number;
  status: ProfileStepStatus;
  percent: number;
}
export interface ProfileCompletion {
  percent: number;
  status: ProfileCompletionStatus;
  steps: ProfileStepCompletion[];
  nextIncompleteStep: ProfileStep | null;
  updatedAt: string | null;
}

// ---------- requests (all fields optional) ----------
export interface PersonalDetailsRequest {
  firstName?: string; lastName?: string; emailId?: string; mobileNumber?: string;
  city?: string; profileImg?: string; yearOfExperience?: number;
  dateOfBirth?: ISODate; gender?: Gender; nationality?: string; maritalStatus?: MaritalStatus;
  alternatePhone?: string; addressLine?: string; country?: string;
  passportNumber?: string; passportExpiry?: ISODate; visaStatus?: VisaStatus; visaExpiry?: ISODate;
  drivingLicenseNumber?: string; professionalSummary?: string;
}
interface Item { id?: number | null }
export interface QualificationRequest extends Item {
  qualificationType?: QualificationType; title?: string; fieldOfStudy?: string; institution?: string;
  country?: string; startDate?: ISODate; endDate?: ISODate; isCurrent?: boolean; grade?: string;
  certificateFileId?: number | null;
}
export interface CertificationRequest extends Item {
  certificationType?: CertificationType; name?: string; issuingOrganization?: string; credentialId?: string;
  issueDate?: ISODate; expiryDate?: ISODate; documentFileId?: number | null;
}
export interface SkillRequest extends Item { skillName?: string; level?: ExpertiseLevel; yearsOfExperience?: number }
export interface SkillsStepRequest {
  skillSetIds?: number[] | null;
  expertise?: { expertiseCategoryId: number; expertiseLevel: ExpertiseLevel }[] | null;
  skills?: SkillRequest[] | null;
}
export type EmploymentEntryType = 'EMPLOYMENT' | 'CAREER_BREAK';
export type CareerBreakReason = 'EDUCATION' | 'HEALTH' | 'FAMILY_CARE' | 'RELOCATION' | 'VISA_PROCESSING' | 'JOB_SEARCH' | 'TRAVEL' | 'PERSONAL' | 'OTHER';
export interface EmploymentRequest extends Item {
  entryType?: EmploymentEntryType;   // defaults to EMPLOYMENT
  breakReason?: CareerBreakReason;   // CAREER_BREAK only
  breakDescription?: string;         // CAREER_BREAK only
  employerName?: string; jobTitle?: string; city?: string; country?: string;
  startDate?: ISODate; endDate?: ISODate; isCurrent?: boolean; responsibilities?: string; reasonForLeaving?: string;
}
export interface LanguageRequest extends Item { language?: string; proficiency?: LanguageProficiency }
export interface DocumentRequest extends Item {
  documentType?: DocumentType; documentNumber?: string; expiryDate?: ISODate; fileId?: number | null;
}
export interface ReferenceRequest extends Item {
  name?: string; company?: string; designation?: string; relationship?: string; phone?: string; email?: string;
}
export interface ReferencesStepRequest {
  emergencyContactName?: string; emergencyContactRelation?: string; emergencyContactPhone?: string;
  references?: ReferenceRequest[] | null;
}
export interface SocialLinkRequest extends Item { platform?: SocialPlatform; url?: string; handle?: string }

// ---------- responses ----------
type Saved<T> = T & { id: number; displayOrder: number };
export type PersonalDetails = Required<{ [K in keyof PersonalDetailsRequest]: PersonalDetailsRequest[K] | null }>;
export type Qualification = Saved<Omit<QualificationRequest, 'id'>> & { certificateFileUrl: string | null; certificateFileName: string | null };
export type Certification = Saved<Omit<CertificationRequest, 'id'>> & { documentFileUrl: string | null; documentFileName: string | null };
export type Skill = Saved<Omit<SkillRequest, 'id'>>;
export type Employment = Saved<Omit<EmploymentRequest, 'id'>> & { entryType: EmploymentEntryType };
export interface EmploymentGap { startDate: ISODate; endDate: ISODate; months: number; ongoing: boolean }
export interface WorkHistory { entries: Employment[]; unexplainedGaps: EmploymentGap[]; gapThresholdMonths: number }
export type Language = Saved<Omit<LanguageRequest, 'id'>>;
export type ProfileDocument = Saved<Omit<DocumentRequest, 'id'>> & { fileUrl: string | null; fileName: string | null };
export type Reference = Saved<Omit<ReferenceRequest, 'id'>>;
export type SocialLink = Saved<Omit<SocialLinkRequest, 'id'>>;

export interface SkillsStep { skillSet: unknown[]; expertise: unknown[]; skills: Skill[] }
export interface ReferencesStep {
  emergencyContactName: string | null; emergencyContactRelation: string | null; emergencyContactPhone: string | null;
  references: Reference[];
}

export interface TechnicianProfile {
  technicianId: number;
  personal: PersonalDetails;
  qualifications: Qualification[];
  certifications: Certification[];
  skills: SkillsStep;
  employment: WorkHistory;
  languages: Language[];
  documents: ProfileDocument[];
  references: ReferencesStep;
  socialLinks: SocialLink[];
  completion: ProfileCompletion;
}
```

`skillSet` and `expertise` use the same shapes as `skillSet` / `expertise` in the existing technician response.

---

## 12. Example API client

```ts
const BASE = `${API_URL}/technicians`;

async function call<T>(method: 'GET' | 'PUT', path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const json = (await res.json()) as GenericResponse<T>;
  if (!res.ok || json.status !== 'success') throw new Error(json.message);
  return json.body;
}

export const technicianProfileApi = {
  get: (id: number) => call<TechnicianProfile>('GET', `/${id}/profile`),
  completion: (id: number) => call<ProfileCompletion>('GET', `/${id}/profile/completion`),

  savePersonal: (id: number, b: PersonalDetailsRequest) =>
    call<StepSaveResponse<PersonalDetails>>('PUT', `/${id}/profile/personal`, b),
  saveQualifications: (id: number, b: QualificationRequest[]) =>
    call<StepSaveResponse<Qualification[]>>('PUT', `/${id}/profile/qualifications`, b),
  saveCertifications: (id: number, b: CertificationRequest[]) =>
    call<StepSaveResponse<Certification[]>>('PUT', `/${id}/profile/certifications`, b),
  saveSkills: (id: number, b: SkillsStepRequest) =>
    call<StepSaveResponse<SkillsStep>>('PUT', `/${id}/profile/skills`, b),
  saveEmployment: (id: number, b: EmploymentRequest[]) =>
    call<StepSaveResponse<WorkHistory>>('PUT', `/${id}/profile/employment`, b),
  saveLanguages: (id: number, b: LanguageRequest[]) =>
    call<StepSaveResponse<Language[]>>('PUT', `/${id}/profile/languages`, b),
  saveDocuments: (id: number, b: DocumentRequest[]) =>
    call<StepSaveResponse<ProfileDocument[]>>('PUT', `/${id}/profile/documents`, b),
  saveReferences: (id: number, b: ReferencesStepRequest) =>
    call<StepSaveResponse<ReferencesStep>>('PUT', `/${id}/profile/references`, b),
  saveSocialLinks: (id: number, b: SocialLinkRequest[]) =>
    call<StepSaveResponse<SocialLink[]>>('PUT', `/${id}/profile/social-links`, b),
};

// Upload (not wrapped in GenericResponse)
export async function uploadFiles(files: File[]): Promise<{ id: number; filePath: string; fileName: string }[]> {
  const form = new FormData();
  files.forEach((f) => form.append('files', f));
  const res = await fetch(`${API_URL}/mediafiles/upload`, { method: 'POST', body: form });
  if (!res.ok) throw new Error('Upload failed');
  return res.json();
}
```

Saving a step and refreshing state:

```ts
const { data, completion } = await technicianProfileApi.saveLanguages(techId, languages);
setLanguages(data);          // take server ids — prevents duplicates on the next save
setCompletion(completion);   // progress bar + step chips
goTo(completion.nextIncompleteStep ?? 'DONE');
```

---

## 13. Checklist

- [ ] Load the form with `GET /profile`; prefill all steps and the progress bar from that one call.
- [ ] Never block Save/Next on empty fields.
- [ ] Send **full lists** for list steps and keep the `id`s of existing items.
- [ ] After each save, replace the step state with `body.data` and the progress bar with `body.completion`.
- [ ] Upload files first and send the returned `id`.
- [ ] Personal step: send only the changed fields if you like. `null` keeps the current value, and `""` clears it.
- [ ] Handle 409 (mobile already used) on the personal step.
- [ ] Use `profileCompletionPercent` / `profileCompletionStatus` from the technician list responses for badges.
