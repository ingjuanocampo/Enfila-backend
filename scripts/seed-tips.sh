#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://204.168.149.108/api/v1/tips/publish}"

publish_tip() {
  local order="$1"
  local milestone="$2"
  local question="$3"
  local answer="$4"

  curl -sS -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "$(jq -n \
      --arg order "$order" \
      --arg milestone "$milestone" \
      --arg question "$question" \
      --arg answer "$answer" \
      '{order: ($order|tonumber), milestone: $milestone, question: $question, answer: $answer, published: true}')"
  echo
}

publish_tip 1 ON_LOGIN \
  "How to assign a turn?" \
  "Tap '+ Assign Turn' on the Home screen. Enter the client's phone number, optionally their name and a note, then confirm. The turn is instantly added to the queue."

publish_tip 2 FIRST_SHIFT_ASSIGNED \
  "How to call the next client?" \
  "On the Home screen, the 'Up next' card shows the first waiting turn. Tap 'Call this turn' to move it to CALLING status. An SMS notification is sent automatically."

publish_tip 3 FIRST_SHIFT_CALLED \
  "How does SMS notification work?" \
  "When you call a turn, EnFila sends an SMS to the client's phone number via the backend messaging service. No additional configuration is needed on your side."

publish_tip 4 FIRST_SHIFT_COMPLETED \
  "How to finish or cancel a turn?" \
  "From the Home screen or the Shifts list, tap 'Finish' to mark a turn as completed, or 'Cancel' to remove it from the active queue."

publish_tip 5 HAS_CLIENTS \
  "How to manage your client list?" \
  "Go to Clients in the navigation. You can search by name or phone. Tap a client to see their full shift history and edit their name."

publish_tip 6 PROFILE_COMPLETE \
  "How to update company information?" \
  "Go to Account and tap 'Edit' next to your company name or your own name. Changes are saved immediately to the backend."

echo "All tips seeded."
