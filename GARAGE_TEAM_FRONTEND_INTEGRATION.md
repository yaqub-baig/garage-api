# Garage Team Management — Frontend Integration Guide

Each garage now has its own team: an owner plus staff such as Admin, Manager, HR, Front Desk and Technicians. The owner defines **roles** (a name plus a set of permissions), invites people **by phone**, and the backend **enforces** those permissions on garage actions.

This guide covers the new team screens and the changes needed on existing screens.

---

## 1. Key rules

1. **Send `X-Auto-Adda-Id` on every garage-side call.** It identifies who is acting. Protected endpoints return **403** without it (see [§7](#7-existing-endpoints-that-now-check-permissions)).
2. **The owner can always do everything.** Anyone else needs an active team membership whose role grants the permission.
3. **Hide what the user can't do.** Call `GET /garages/{garageId}/team/me` when entering a garage, and use its `permissions` to show or hide buttons and menus. The backend still enforces, so this is only for UX.
4. **Roles are chosen before inviting.** Every invite carries a role, and the person gets that role's permissions when they accept. Their role can be changed at any time after they join.
5. **Editing a role changes everyone who has it**, including pending invites, immediately.
6. **Customer actions are not affected.** Booking, the customer side of work-order negotiation, paying an invoice, approving a delivery and responding to part requests work exactly as before.

---

## 2. Base URL and response envelope

| Environment | Base URL |
|---|---|
| Local | `http://localhost:8020/api/v1` |
| Production | `https://garage.autoadda.mylstech.com/api/v1` |

All team endpoints return the standard `GenericResponse` envelope. The payload is in `body`.

```json
{ "status": "success", "code": "200", "message": "Role created", "body": { ... } }
```

Errors use the same envelope with `"status": "failed"` and a readable `message` (see [§8](#8-errors)).

---

## 3. Concepts

### 3.1 Permissions

A fixed catalogue. Fetch it with `GET /team/permissions` for the role editor. Each entry has a `key`, a `group` and a `description`.

| Group | Key | Lets the member… |
|---|---|---|
| Team | `TEAM_VIEW` | see members, roles and invites |
| Team | `TEAM_MANAGE` | invite, remove, suspend members and change their role |
| Team | `ROLE_MANAGE` | create, edit and delete roles |
| Garage | `GARAGE_PROFILE_EDIT` | edit the garage profile |
| Garage | `SERVICES_MANAGE` | manage garage services and prices |
| Garage | `SUBSCRIPTION_MANAGE` | manage subscription plans |
| Garage | `BUSINESS_LISTING_MANAGE` | list the garage business for sale *(reserved; listing is still owner-only)* |
| Operations | `APPOINTMENT_VIEW` | view appointments *(for UI; not enforced yet)* |
| Operations | `APPOINTMENT_MANAGE` | update appointment status |
| Operations | `WORK_ORDER_MANAGE` | create and update work orders, make the garage's counter-offer |
| Operations | `JOB_CARD_ASSIGN` | assign technicians to services |
| Operations | `JOB_CARD_UPDATE` | change job card status |
| Operations | `VEHICLE_CHECKIN_DELIVERY` | receive vehicles, start deliveries, fill the delivery checklist |
| Operations | `PART_REQUEST_MANAGE` | raise and manage part requests and procurement |
| Inventory | `INVENTORY_VIEW` | view inventory *(for UI; not enforced yet)* |
| Inventory | `INVENTORY_MANAGE` | manage inventory items, stock and alerts |
| Inventory | `TOOLS_MANAGE` | manage working tools |
| Finance | `INVOICE_VIEW` | view invoices *(for UI; not enforced yet)* |
| Finance | `INVOICE_MANAGE` | generate invoices, record payments, send invoices |
| Finance | `ANALYTICS_VIEW` | view analytics *(for UI; not enforced yet)* |

"Not enforced yet" means the backend doesn't block the related read endpoints today. Still use these keys to hide screens.

### 3.2 Default roles

Every garage starts with these roles. They are created automatically for new garages, and for existing garages on the first deploy.

| Role | Permissions | Editable |
|---|---|---|
| **Owner** | all | no (system role, held only by the garage owner) |
| **Admin** | everything except `ROLE_MANAGE` | yes |
| **Manager** | team view, appointments, work orders, job cards, check-in/delivery, part requests, inventory, tools, invoices, analytics | yes |
| **HR** | `TEAM_VIEW`, `TEAM_MANAGE` | yes |
| **Technician** | appointment view, job card status, check-in/delivery, part requests, inventory view | yes |

Owners can rename, edit or delete any role except Owner, and add their own roles (e.g. "Front Desk").

Existing staff were placed into roles automatically: the garage owner → Owner, `SUPERVISOR` → Manager, everyone else → Technician.

### 3.3 Who can hand out what

- **Changing a team:** needs `TEAM_MANAGE`. This covers inviting, changing roles, suspending and removing.
- **Role-editing power:** someone without `ROLE_MANAGE` can't give a role containing `ROLE_MANAGE` to anyone. The same goes for `TEAM_MANAGE`. So HR can hire a Manager or an Admin, but can't create new role editors.
- **Defining roles:** needs `ROLE_MANAGE`. You can only put permissions into a role that you hold yourself.
- **Protected people:** nobody can change their own role or status, and the owner can't be changed or removed.

### 3.4 Member status

| Status | Meaning |
|---|---|
| `ACTIVE` | normal access |
| `SUSPENDED` | still listed, but every protected call returns 403 *"Your access to this garage is suspended"* |
| `REMOVED` | no longer on the team. Hidden from the member list and unlinked from the garage. |

---

## 4. Endpoints

All of these require the `X-Auto-Adda-Id` header.

| Method & path | Permission | Body / params | Returns (`body`) |
|---|---|---|---|
| `GET /garages/{garageId}/team/me` | — | — | `MyTeamAccess` |
| `GET /garages/{garageId}/team/members` | TEAM_VIEW | — | `TeamMember[]` |
| `PUT /garages/{garageId}/team/members/{memberId}/role` | TEAM_MANAGE | `{ "roleId": 6 }` | `TeamMember` |
| `PUT /garages/{garageId}/team/members/{memberId}/status` | TEAM_MANAGE | `{ "status": "SUSPENDED" }` or `"ACTIVE"` | `TeamMember` |
| `DELETE /garages/{garageId}/team/members/{memberId}` | TEAM_MANAGE | — | `null` |
| `GET /garages/{garageId}/team/roles` | TEAM_VIEW | — | `Role[]` |
| `POST /garages/{garageId}/team/roles` | ROLE_MANAGE | `RoleRequest` | `Role` |
| `PUT /garages/{garageId}/team/roles/{roleId}` | ROLE_MANAGE | `RoleRequest` (any field optional) | `Role` |
| `DELETE /garages/{garageId}/team/roles/{roleId}` | ROLE_MANAGE | — | `null` |
| `POST /garages/{garageId}/team/invites` | TEAM_MANAGE | `{ "phone", "roleId", "message"? }` | `Invite` |
| `GET /garages/{garageId}/team/invites` | TEAM_VIEW | — | `Invite[]` (pending, sent by the garage) |
| `DELETE /garages/{garageId}/team/invites/{inviteId}` | TEAM_MANAGE | — | `null` |
| `GET /team/invites/mine` | — | — | `Invite[]` waiting for the caller |
| `GET /team/permissions` | — | — | `{ key, group, description }[]` |

Accepting or rejecting an invite uses the **existing** endpoints (see [§6](#6-invite-flow)).

### 4.1 Shapes

```jsonc
// MyTeamAccess: GET /garages/1/team/me
{
  "garageId": 1,
  "isOwner": false,
  "isMember": true,
  "memberId": 2,
  "roleId": 6,
  "roleName": "Front Desk",
  "status": "ACTIVE",                // null when not a member
  "permissions": ["APPOINTMENT_VIEW", "APPOINTMENT_MANAGE", "INVOICE_VIEW", "INVOICE_MANAGE"]
}

// TeamMember
{
  "id": 2, "technicianId": 2, "autoAddaId": 42,
  "firstName": "Sara", "lastName": null, "email": "...", "mobileNumber": "...", "profileImg": null,
  "roleId": 6, "roleName": "Front Desk",
  "status": "ACTIVE", "isOwner": false, "joinedAt": "2026-11-07T14:27:28"
}

// Role
{
  "id": 6, "name": "Front Desk", "description": "Reception",
  "permissions": ["APPOINTMENT_VIEW", "APPOINTMENT_MANAGE", "INVOICE_VIEW", "INVOICE_MANAGE"],
  "isSystem": false,                 // true only for Owner: disable editing
  "memberCount": 1,
  "pendingInviteCount": 0
}

// RoleRequest
{ "name": "Front Desk", "description": "Reception", "permissions": ["APPOINTMENT_MANAGE", "INVOICE_MANAGE"] }

// Invite
{
  "id": 52,                          // use this to accept / reject / cancel
  "garageId": 1, "garageName": "Speedy Motors",
  "phone": "+971503334444",
  "technicianId": 5,                 // null until the invitee has an AutoAdda account
  "inviteeName": "User60",
  "roleId": 5, "roleName": "Technician",
  "message": "Join us",
  "sendBy": "GARAGE", "status": "HOLD",
  "invitedBy": 7, "invitedAt": "2026-11-07T14:29:02"
}
```

---

## 5. Screens

### 5.1 Team members (needs `TEAM_VIEW`)
- List: `GET /team/members`. Show the role badge, status, and an "Owner" tag when `isOwner` is true.
- Row actions, shown with `TEAM_MANAGE` and hidden on the owner's row and on your own row:
  - **Change role:** pick from `GET /team/roles`, excluding roles where `isSystem` is true, then `PUT …/members/{id}/role`.
  - **Suspend / Reactivate:** `PUT …/members/{id}/status`.
  - **Remove:** `DELETE …/members/{id}`, after a confirm dialog.

### 5.2 Roles (needs `TEAM_VIEW` to see, `ROLE_MANAGE` to edit)
- List: `GET /team/roles` with `memberCount` and `pendingInviteCount`.
- Editor: a checkbox list grouped by `group` from `GET /team/permissions`.
  - Disable permissions the current user doesn't have (their `/me` `permissions`); the backend rejects them anyway.
  - Owner (`isSystem`) is read-only.
- Delete: disable it while `memberCount + pendingInviteCount > 0`, and explain that the people have to be moved first. The backend returns 409 otherwise.

### 5.3 Invite (needs `TEAM_MANAGE`)
- Form: phone number, role (dropdown without Owner), optional message, then `POST /team/invites`.
- Pending list: `GET /team/invites`, with **Cancel** (`DELETE …/invites/{id}`).

### 5.4 "My invites" (any signed-in user, e.g. in the technician app)
- `GET /team/invites/mine`. Show the garage name, the role offered and the message, with Accept and Reject buttons.

### 5.5 Join requests from technicians (existing screen)
- When approving a technician's request to join, let the approver pick a role and pass it as `roleId` (see [§6.2](#62-technician-asks-to-join-existing-flow)). Without it they get the default member role ("Technician").

---

## 6. Invite flow

### 6.1 Garage invites someone

```
Owner / HR                         Backend                                Invitee
POST /team/invites {phone, roleId}
                     ├─ has an AutoAdda account?
                     │    yes → invite linked to them + in-app/SMS/email notification
                     │    no  → invite kept against the phone + sign-up SMS
                                                            GET /team/invites/mine
                                                            (after signing up with that number,
                                                             phone invites are linked to them here)
                                                            PUT /requestingGarageTechnician/technician/{id}/approve?technicianId={technicianId}
                     └─ member added with the invite's role
```

- Accept: `PUT /requestingGarageTechnician/technician/{inviteId}/approve?technicianId={technicianId}`
- Reject: `PUT /requestingGarageTechnician/technician/{inviteId}/reject?technicianId={technicianId}`

Both use the `id` and `technicianId` from `GET /team/invites/mine`. The phone number is matched ignoring spaces and dashes (`+971 50-123 4567` = `+971501234567`).

### 6.2 Technician asks to join (existing flow)

The technician sends the request as before. The garage approves it with an optional role:

```
PUT /requestingGarageTechnician/garage/{requestId}/approve?garageId=1&roleId=3
```

The garage-side approve and reject calls, and `POST /requestingGarageTechnician/garage/send`, now require `TEAM_MANAGE`. `garage/send` also accepts an optional `roleId` in its body, which offers that role to the technician.

### 6.3 Adding technicians directly (existing flow)

`POST /technicians` with `garageId`/`garageIds`, and `PUT /technicians/{id}/update` with `garageIds`, still work. They now need `TEAM_MANAGE` on each garage listed, and the technician joins with the default member role. Prefer invites for new staff.

---

## 7. Existing endpoints that now check permissions

Add the `X-Auto-Adda-Id` header to these calls. Without it the response is `403 "Missing X-Auto-Adda-Id header"`.

| Screen | Endpoint | Permission |
|---|---|---|
| Garage profile | `PUT /garages/{id}` | GARAGE_PROFILE_EDIT |
| Services | `POST /garage-services`, `PUT /garage-services/{id}`, `DELETE /garage-services/{id}` | SERVICES_MANAGE |
| Subscription plans | `POST /garage-subscriptions`, `POST /garage-subscriptions/{id}/update`, `PUT /garage-subscriptions/{id}/status`, `DELETE /garage-subscriptions/{id}` | SUBSCRIPTION_MANAGE |
| Appointments | `PUT /appointments/status` | APPOINTMENT_MANAGE |
| Work orders | `POST /work-orders` | WORK_ORDER_MANAGE (+ JOB_CARD_ASSIGN if it assigns technicians) |
| Work orders | `PUT /work-orders/update/{id}` | the work order's **customer** passes; staff need WORK_ORDER_MANAGE (+ JOB_CARD_ASSIGN for assignments). Garage counter-offer fields always need WORK_ORDER_MANAGE. |
| Job cards | `POST /jobcards/assign` | JOB_CARD_ASSIGN |
| Job cards | `PUT /jobcards/status/{jobCardId}`, `PUT /part-requests/jobcard/status` | JOB_CARD_UPDATE |
| Vehicle check-in | `POST /vehicle-received`, `PUT /vehicle-received/update/{id}` | VEHICLE_CHECKIN_DELIVERY |
| Delivery | `POST /vehicle-delivery/workorder/{id}/initiate`, `PUT /vehicle-delivery/{id}/checklist` | VEHICLE_CHECKIN_DELIVERY |
| Part requests | `POST /part-requests`, `PUT /part-requests/{id}`, `POST /part-requests/{id}/procurement`, `PUT /part-requests/{id}/mark-received`, `PUT /part-requests/item/{itemId}/mark-received` | PART_REQUEST_MANAGE |
| Inventory | `POST /garage-inventory`, `PUT /garage-inventory/{itemId}`, add/remove/adjust-stock, deactivate | INVENTORY_MANAGE |
| Inventory alerts | `PUT /inventory-alerts/{id}/resolve`, `…/dismiss` | INVENTORY_MANAGE |
| Working tools | `POST /working-tools`, `PUT /working-tools/{toolId}`, deactivate | TOOLS_MANAGE |
| Invoices | `POST /invoices/generate`, `POST /invoices/work-order/{id}/auto-generate`, `PUT /invoices/{id}/payment-status`, `POST /invoices/{id}/send-to-customer` | INVOICE_MANAGE |
| Invoices | `PUT /invoices/pay` | the invoice's **customer** passes; staff need INVOICE_MANAGE |
| Team join requests | `POST /requestingGarageTechnician/garage/send`, `PUT …/garage/{id}/approve`, `PUT …/garage/{id}/reject` | TEAM_MANAGE |
| Technicians | `POST /technicians`, `PUT /technicians/{id}/update` (when they include garage ids) | TEAM_MANAGE |

**Not changed:** all GET/list endpoints, booking and cancelling appointments, customer delivery approval, customer part response, ratings, favourites, analytics, and garage business listings (still owner-only, as before).

> If a rollout breaks a screen, ops can set `garage.permissions.enforcement=LOG_ONLY` on the server. Denied calls are then logged but allowed through while the frontend is fixed.

---

## 8. Errors

| Status | When | Example `message` |
|---|---|---|
| `403` | no/invalid header | `Missing X-Auto-Adda-Id header` |
| `403` | not on the team | `You are not a member of this garage's team` |
| `403` | suspended | `Your access to this garage is suspended` |
| `403` | role lacks the permission | `Your role 'Front Desk' does not allow: assign technicians to services` |
| `403` | handing out role-editing power you don't have | `You can't grant permissions you don't have: [ROLE_MANAGE]` |
| `400` | owner or own account changed | `The garage owner can't be changed or removed` / `You can't change your own role or access` |
| `400` | editing the Owner role, or using "Owner" as a name | `The Owner role can't be changed or deleted` / `'Owner' is reserved` |
| `400` | bad input | `roleId is required`, `Invalid phone number: …`, `Role name is required` |
| `404` | wrong garage / member / role / invite id | `Role not found in this garage: 9` |
| `409` | duplicates or role in use | `A role named 'Front Desk' already exists`, `A pending invite already exists for this phone number`, `Role 'HR' is still used by 0 member(s) and 1 pending invite(s)…` |

Show the `message` to the user on 403 and 409 responses; they are written to be readable.

> `400` errors used to come back as a generic `500 "An unexpected internal server error occurred."`. They now return `400` with the real message, for every endpoint.
