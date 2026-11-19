#!/usr/bin/env bash
# Seeds the read-only `snapshotTypes` reference collection.
# Uses the Firebase CLI login (project owner), which bypasses Security Rules;
# the app itself can never write this collection.
set -euo pipefail
PROJECT=caughtu-se7team
firebase projects:list >/dev/null # refreshes the CLI access token
TOKEN=$(python3 -c "import json,os;print(json.load(open(os.path.expanduser('~/.config/configstore/firebase-tools.json')))['tokens']['access_token'])")
BASE="https://firestore.googleapis.com/v1/projects/$PROJECT/databases/(default)/documents/snapshotTypes"

put() { # id name intervalDays order
  curl -sf -X PATCH "$BASE/$1" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d "{\"fields\":{\"name\":{\"stringValue\":\"$2\"},\"intervalDays\":{\"integerValue\":\"$3\"},\"order\":{\"integerValue\":\"$4\"}}}" >/dev/null
  echo "seeded snapshotTypes/$1"
}

put weekly         "Weekly check"   7  1
put monthly        "Monthly check"  30 2
put before-cleanup "Before cleanup" 0  3
put after-cleanup  "After cleanup"  0  4
