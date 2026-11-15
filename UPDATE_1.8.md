# Sol Macro 1.8

## Fixed
- **Auto click did nothing after "Tap to Start".** The control panel stayed open in the middle of the screen and caught
  the taps meant for Roblox (and covered the reeling bar in the capture). Start now closes the panel by itself.
- **OpenCV detection was switched off** (Cv.kt was an empty stub, the "OpenCV detect" toggle did nothing). Cv.kt is now a real
  OpenCV detector (HSV colour match, shape check, bar memory). If OpenCV cannot load or finds nothing, the old scanner still runs.
  Panel > Stats shows "Detect  OpenCV / Built-in".
- **Blind clicking.** The tap engine kept tapping up to 0.8 s after it stopped seeing the bar. It now taps only while the bar was seen in the last 0.3 s.

## New
- **Show auto clickers** (Macro page button, Settings, Macro Settings switch, and the app's Button positions card).
  Circles appear for Cast, Reel, Inv, Sell, OK, Move, Cam, Shift and X. Drag them onto your buttons, tap DONE.
  Positions save automatically. They hide when the macro starts so they never block a tap.
- **Result panel timed by the screen, not by a timer.** After the bar is gone the macro compares the screen with the picture taken
  while the bar showed, waits until the caught/failed panel has slid in and stopped, taps X, then waits until the panel is really gone
  (and taps X again only if it is still there) before it casts. If no panel is ever seen it falls back to the old waits.

## Tested here
- Detector (same algorithm as Cv.kt) on 100 synthetic frames with brightness changes, noise and green/blue decoys: 0 missed, tip error <= 4 px,
  0 false bars. See tests/run_tests.py.
- Result-panel timing simulated: X tapped about 100 ms after the panel settles, cast again about 165 ms later.

## Not tested here
- Not compiled and not run on a phone (no Android SDK or internet in the sandbox). The GitHub build is the first compile.
  If it fails, send the error line. Real Sol's RNG colours and the real result panel were not available, only synthetic frames.
