(ns main-test
  (:require [test-cloudflare-worker :as tu]
            ["node:test" :as t]))

(t/before (fn [] (tu/before {:TELEGRAM_BOT_TOKEN {:type "plain_text" :value "test-token"}
                             :TELEGRAM_CHAT_ID {:type "plain_text" :value "test-chat"}})))
(t/after (fn [] (tu/after)))

(t/test "GET /"
        (tu/create-assert-fetch-snapshot
         (Request. "http://localhost/")
         "eyJlZmZlY3RzIjpbXSwicmVzcG9uc2UiOiI8aHRtbCA+PGhlYWQgPjxtZXRhICBjaGFyc2V0PSdVVEYtOCc+PC9tZXRhPjxtZXRhICBuYW1lPSd2aWV3cG9ydCcgY29udGVudD0nd2lkdGg9ZGV2aWNlLXdpZHRoLCBpbml0aWFsLXNjYWxlPTEnPjwvbWV0YT48dGl0bGUgPtCg0LXQutC+0LzQtdC90LTQvtCy0LDRgtGMINGB0L7QsdGL0YLQuNC1PC90aXRsZT48bGluayAgcmVsPSdzdHlsZXNoZWV0JyBocmVmPSdodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL0BwaWNvY3NzL3BpY29AMi9jc3MvcGljby5taW4uY3NzJz48L2xpbms+PC9oZWFkPjxib2R5ID48bWFpbiAgY2xhc3M9J2NvbnRhaW5lcic+PGZvcm0gIG1ldGhvZD0nUE9TVCcgYWN0aW9uPScvc3VibWl0Jz48ZmllbGRzZXQgPjxsYWJlbCA+0KHRgdGL0LvQutCwINC90LAg0YHQvtCx0YvRgtC40LUgKGxpbmsgdG8gZXZlbnQpPGlucHV0ICBuYW1lPSdsaW5rX3RvX2V2ZW50JyBwbGFjZWhvbGRlcj0naHR0cHM6Ly8nIHR5cGU9J3VybCcgcmVxdWlyZWQ9J3RydWUnPjwvaW5wdXQ+PC9sYWJlbD48L2ZpZWxkc2V0PjxpbnB1dCAgdHlwZT0nc3VibWl0JyB2YWx1ZT0n0KHQvtCy0LXRgtGD0Y4g0YHRhdC+0LTQuNGC0YwhJz48L2lucHV0PjwvZm9ybT48L21haW4+PC9ib2R5PjwvaHRtbD4ifQ=="))

(t/test "POST /submit"
        (tu/create-assert-fetch-snapshot
         (Request. "http://localhost/submit"
                   {:method "POST"
                    :headers {"Content-Type" "application/x-www-form-urlencoded"}
                    :body (str "link_to_event=" (encodeURIComponent "https://example.com/event"))})
         "eyJlZmZlY3RzIjpbeyJ1cmwiOiJodHRwczovL2FwaS50ZWxlZ3JhbS5vcmcvYm90dGVzdC10b2tlbi9zZW5kTWVzc2FnZSIsInByb3BzIjp7Im1ldGhvZCI6IlBPU1QiLCJoZWFkZXJzIjp7IkNvbnRlbnQtVHlwZSI6ImFwcGxpY2F0aW9uL2pzb24ifSwiZGVjb2RlciI6Impzb24iLCJib2R5Ijoie1wiY2hhdF9pZFwiOlwidGVzdC1jaGF0XCIsXCJ0ZXh0XCI6XCLQndC+0LLQsNGPINGA0LXQutC+0LzQtdC90LTQsNGG0LjRjzogaHR0cHM6Ly9leGFtcGxlLmNvbS9ldmVudFwifSJ9LCJ0eXBlIjoiZWZmZWN0c19wcm9taXNlLmZldGNoOmZldGNoIn1dLCJyZXNwb25zZSI6IjxodG1sID48aGVhZCA+PG1ldGEgIGNoYXJzZXQ9J1VURi04Jz48L21ldGE+PG1ldGEgIG5hbWU9J3ZpZXdwb3J0JyBjb250ZW50PSd3aWR0aD1kZXZpY2Utd2lkdGgsIGluaXRpYWwtc2NhbGU9MSc+PC9tZXRhPjx0aXRsZSA+0KDQtdC60L7QvNC10L3QtNC+0LLQsNGC0Ywg0YHQvtCx0YvRgtC40LU8L3RpdGxlPjxsaW5rICByZWw9J3N0eWxlc2hlZXQnIGhyZWY9J2h0dHBzOi8vY2RuLmpzZGVsaXZyLm5ldC9ucG0vQHBpY29jc3MvcGljb0AyL2Nzcy9waWNvLm1pbi5jc3MnPjwvbGluaz48L2hlYWQ+PGJvZHkgPjxtYWluICBjbGFzcz0nY29udGFpbmVyJz48YXJ0aWNsZSA+PHAgPtCh0L/QsNGB0LjQsdC+ISDQodGB0YvQu9C60LAg0L/QvtC70YPRh9C10L3QsDogaHR0cHM6Ly9leGFtcGxlLmNvbS9ldmVudDwvcD48YSAgaHJlZj0nLyc+0JLQtdGA0L3Rg9GC0YzRgdGPPC9hPjwvYXJ0aWNsZT48L21haW4+PC9ib2R5PjwvaHRtbD4ifQ=="))
