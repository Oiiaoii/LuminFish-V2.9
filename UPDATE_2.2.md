# LuminFish 2.2 - precision tapping

## What changed in the tapping
- **Tap ledger (new, on by default: Settings > Precision taps).** Every tap is entered in a ledger and counts as "in flight"
  until a picture of the screen shows its lift. The v2.0 engine assumed a tap had landed after a fixed 35 ms; on a slower
  phone the lift was still missing from the picture, the marker looked too low and an extra tap went out.
- The tap-to-game delay, the lift of one tap and the drain speed are measured live from the pictures and remembered for the
  next reel (Stats > Tapping shows them).
- A tap that Android cancels or refuses leaves the ledger immediately, so it is not counted as a lift.
- The zone is followed ahead of where it was seen (speed over the last 100-260 ms), so a moving zone is tracked.
- Needs fast capture. Without it (screenshots) the older timed engine is used. The old engine is still there (Timed tapping).

## Detection
- OpenCV box scan: once the bar is found, only a small box around it is read. The box goes from the ImageReader buffer
  straight into an OpenCV Mat (one copy per row), is converted to HSV and matched by colour (zone and blue fill) with
  OpenCV, then the columns are counted. No per-pixel Kotlin loop. The built-in scan is only a fallback.
- The marker position has decimals (the two columns at the edge of the blue fill are counted partly).
- Capture is unchanged: MediaProjection, 3-buffer ImageReader on GPU-friendly hardware buffers, frames taken the moment they
  arrive with Android's time stamp.
- GPU note: the screen is composited and captured by the GPU. The OpenCV step runs on the CPU (NEON) because the box is tiny;
  uploading it to a GPU would take longer than the scan.

## Tested here (desktop simulator, tests/reel_sim.py + sim6.py; 80 px zone, 5 runs x 40 s, % of time the marker is in the zone)
| case                                   | v2.0 timed | v2.2 ledger |
|----------------------------------------|-----------:|------------:|
| exact numbers                          | 98.8       | 99.9        |
| zone moving 120 px/s                   | 92.1       | 98.8        |
| tap delay 80 ms instead of 35 ms       | 91.6       | 99.9        |
| lift +18 %, drain -15 % off            | 88.4       | 99.8        |
| 20 fps capture                         | 96.8       | 99.7        |
| delay, lift/drain off and moving zone  | 68.6       | 99.5        |
| 15 % of taps cancelled by Android      | 90.5       | 98.6        |
The simulator is my model of the bar (one tap = +58 px after a delay, drain 215 px/s), not the real game. It shows the
logic is sound and much less sensitive to wrong guesses; it does not prove the numbers on your phone.

Not compiled or run on a phone here (no Android SDK). The GitHub build is the first compile: send the error line if it fails.
