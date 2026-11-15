# Sol Macro 1.9

## Screen detection only (no screenshots)
- The macro now needs the **live screen capture** (the "Screen capture" switch). Start is refused with a clear message when it is off,
  and the panel sends you straight to the permission screen.
- The reeling bar is read only from the live capture. The slow screenshot fallback is gone (also for calibration and the biome reader).
- If the capture is turned off while fishing, the macro stops cleanly, tells you (and the webhook) and opens the capture screen.

## New
- **Smart auto-fishing**: learns the bite time (min / average / max), scans less often until the earliest bite ever seen (saves battery),
  all reel and result timing comes from the screen (v1.8 detection) instead of fixed waits.
- **Click recovery**: 8 cancelled or refused taps in a row, an open panel blocking the screen for 8 s, or no tap for 1.2 s while the marker
  is low -> clears the blockers, gives taps more room, restarts the tap engine and keeps reeling.
- **Anti-freeze**: the screen did not change at all for N seconds (default 30), or nothing progressed for 3x that -> step 1 clear panels + camera
  nudge + recast, step 2 stronger nudge, step 3 rejoin (if rejoin is on) or stop. Webhook message on every step.
- **Catch verification**: a catch counts only after the result panel was really seen. A reel with no panel is listed as "Not verified" and not counted.
  If the panel is never seen on a phone (4 reels in a row at the start) verification switches itself off so catches are not lost.
- **Customizable auto-sell**: sell every N catches, every N minutes, or whichever comes first; choose whether Confirm is tapped. (Settings > Selling, and the panel.)
- **Fishing statistics**: successful catches, failed attempts, not verified, success rate, fishing time (pauses not counted), catches per hour,
  time per catch, bite min/avg/max, sells, recoveries. This session and all time (saved), with a reset button. App home screen and panel Stats.
- Keeps the screen on while the macro runs.

## Tested here (desktop mirror of the rules, tests/logic_tests.py + tests/run_tests.py)
auto-sell triggers, catch verification states, freeze detection (frozen / live / lone clock), click-recovery streaks, bar detector (100 synthetic frames).

## Not tested here
Not compiled and not run on a phone (no Android SDK or internet in the sandbox). The GitHub build is the first compile; send the error line if it fails.
The result-panel and freeze thresholds are tuned on synthetic frames, not on real Sol's RNG.
