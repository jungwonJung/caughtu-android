#!/usr/bin/env python3
"""Runs the 8 Lab 2 access-control scenarios against the deployed Firestore rules.

Real REST calls with real user ID tokens (no admin rights), so Security Rules apply.
Two existing test users are needed; credentials come from the environment, never the repo:

  RULES_A_EMAIL / RULES_A_PASSWORD   user A (owns the test record)
  RULES_B_EMAIL / RULES_B_PASSWORD   user B (a different user)

Usage:  python3 scripts/rules_scenarios.py   → prints a Markdown results table, exit 1 on any mismatch.
"""
import json, os, sys, urllib.request, urllib.error

PROJECT = "caughtu-se7team"
API_KEY = json.load(open(os.path.join(os.path.dirname(__file__), "..", "app", "google-services.json")))["client"][0]["api_key"][0]["current_key"]
DB = f"projects/{PROJECT}/databases/(default)/documents"
BASE = f"https://firestore.googleapis.com/v1/{DB}"


def http(method, url, token=None, body=None):
    req = urllib.request.Request(url, method=method, data=json.dumps(body).encode() if body is not None else None)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read() or b"{}")
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b"{}")


def sign_in(prefix):
    status, data = http("POST", f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}",
                        body={"email": os.environ[f"{prefix}_EMAIL"], "password": os.environ[f"{prefix}_PASSWORD"], "returnSecureToken": True})
    if status != 200:
        sys.exit(f"sign-in failed for {prefix}: {data}")
    return data["idToken"], data["localId"]


def fields(owner, nfb=25, following=150):
    return {
        "ownerId": {"stringValue": owner}, "label": {"stringValue": "Rules test"}, "note": {"stringValue": ""},
        "typeId": {"stringValue": "weekly"}, "typeName": {"stringValue": "Weekly check"},
        "exportedAt": {"timestampValue": "2026-11-01T00:00:00Z"},
        "followerCount": {"integerValue": "120"}, "followingCount": {"integerValue": str(following)},
        "notFollowingBackCount": {"integerValue": str(nfb)},
    }


def commit(token, doc_path, flds, mask=None, create=True):
    write = {"update": {"name": f"{DB}/{doc_path}", "fields": flds},
             "updateTransforms": [{"fieldPath": "updatedAt", "setToServerValue": "REQUEST_TIME"}]}
    if create:
        write["updateTransforms"].append({"fieldPath": "createdAt", "setToServerValue": "REQUEST_TIME"})
        write["currentDocument"] = {"exists": False}
    else:
        write["updateMask"] = {"fieldPaths": mask}
        write["currentDocument"] = {"exists": True}
    return http("POST", f"{BASE}:commit", token, {"writes": [write]})[0]


def new_id():
    return "rules-" + os.urandom(6).hex()


def main():
    a_tok, a_uid = sign_in("RULES_A")
    b_tok, b_uid = sign_in("RULES_B")

    # Setup: user A creates a valid record through the rules (also proves valid writes pass).
    rec = new_id()
    setup = commit(a_tok, f"snapshots/{rec}", fields(a_uid))
    rows = [("0", "Setup – A creates a valid record (control)", "Allowed", setup)]

    rows += [
        ("1", "Read own record", "Allowed", http("GET", f"{BASE}/snapshots/{rec}", a_tok)[0]),
        ("2", "Read the same record as a different user", "Denied", http("GET", f"{BASE}/snapshots/{rec}", b_tok)[0]),
        ("3", "Read without being signed in", "Denied", http("GET", f"{BASE}/snapshots/{rec}")[0]),
        ("4", "Create a record whose owner id is another user", "Denied", commit(a_tok, f"snapshots/{new_id()}", fields(b_uid))),
        ("5", "Create a record breaking the logical rule (notFollowingBack 200 > following 150)", "Denied",
         commit(a_tok, f"snapshots/{new_id()}", fields(a_uid, nfb=200, following=150))),
        ("6", "Update own record, changing its owner id", "Denied",
         commit(a_tok, f"snapshots/{rec}", {"ownerId": {"stringValue": b_uid}}, mask=["ownerId"], create=False)),
        ("7", "Create a record in the reference collection", "Denied",
         http("PATCH", f"{BASE}/snapshotTypes/{new_id()}", a_tok, {"fields": {"name": {"stringValue": "Hacked"}}})[0]),
        ("8", "Read a reference record as a signed-in user", "Allowed", http("GET", f"{BASE}/snapshotTypes/weekly", a_tok)[0]),
    ]
    # Control for 6: the same kind of update without touching ownerId is allowed.
    rows.append(("6b", "Update own record, changing only the label (control)", "Allowed",
                 commit(a_tok, f"snapshots/{rec}", {"label": {"stringValue": "Rules test edited"}}, mask=["label"], create=False)))

    # Cleanup: A deletes the test record (owner delete is allowed).
    cleanup = http("DELETE", f"{BASE}/snapshots/{rec}", a_tok)[0]
    rows.append(("9", "Delete own record (cleanup)", "Allowed", cleanup))

    ok = True
    print("| # | Scenario | Expected | HTTP | Result |\n|---|---|---|---|---|")
    for n, name, expected, status in rows:
        got = "Allowed" if status == 200 else "Denied"
        match = got == expected
        ok &= match
        print(f"| {n} | {name} | {expected} | {status} | {'✅' if match else '❌'} {got} |")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
