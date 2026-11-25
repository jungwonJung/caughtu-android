| # | Scenario | Expected | HTTP | Result |
|---|---|---|---|---|
| 0 | Setup – A creates a valid record (control) | Allowed | 200 | ✅ Allowed |
| 1 | Read own record | Allowed | 200 | ✅ Allowed |
| 2 | Read the same record as a different user | Denied | 403 | ✅ Denied |
| 3 | Read without being signed in | Denied | 403 | ✅ Denied |
| 4 | Create a record whose owner id is another user | Denied | 403 | ✅ Denied |
| 5 | Create a record breaking the logical rule (notFollowingBack 200 > following 150) | Denied | 403 | ✅ Denied |
| 6 | Update own record, changing its owner id | Denied | 403 | ✅ Denied |
| 7 | Create a record in the reference collection | Denied | 403 | ✅ Denied |
| 8 | Read a reference record as a signed-in user | Allowed | 200 | ✅ Allowed |
| 6b | Update own record, changing only the label (control) | Allowed | 200 | ✅ Allowed |
| 9 | Delete own record (cleanup) | Allowed | 200 | ✅ Allowed |
