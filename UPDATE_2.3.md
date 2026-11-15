# LuminFish 2.3 - picture recognition of the fishing buttons, any zone colour, tap circles

Built on 2.2: nothing was removed or replaced. The tap ledger engine (Tapper.kt) is unchanged.

## 1. The fishing buttons are recognised on the screen (new Scene.kt, pictures in assets/scene)
- **"Fish" button** seen -> it is tapped where it really is (no fixed Cast point needed).
- **"Exit" button** seen -> fishing is running, the macro waits for the bar and does NOT tap there (that would leave the fishing).
  If the bar never came (Bar not found), it taps the real Exit button once to leave, then casts again.
- **Bar mini-game** -> read by the existing OpenCV scan + tap ledger.
- **"Fishing Failed" / "Fish Caught!"** -> the X in the top-right corner of that box is found and tapped.
- If a button is not recognised, the old saved Cast / Close points are used, so nothing gets worse.
- Settings > Detection > "Find the Fish / Exit / X buttons on the screen" (panel: "Find buttons on screen"). Needs fast capture.
- Matching is done at many sizes and remembers the size that worked. Check Debug log lines: "Fish button seen (NN%)".

## 2. Any zone colour (Cv.kt)
The zone can change colour, and in the pictures it is made of two shades (teal + green). With one picked colour only one
shade was found, so the zone centre was off. Now any bright coloured area that is not the blue fill and not white text counts
as zone (green, teal, yellow, orange, red, pink, purple; two shades merge). Whole-screen search tries your picked colour first,
then any colour, and only accepts a blob whose row really looks like the bar (blue fill / dark track / zone along its width).
Setting: "Any zone colour" (on by default). A zone that is blue or violet (hue 95-140) is still not covered.

## 3. Small circles where it taps
- **Auto clickers**: every draggable circle now has a small black-and-white dot in its exact middle = the spot that gets tapped.
- **While running**: a short-lived small circle appears at every tap (Settings: "Show small circles where the macro taps").

## Tapping accuracy
The 2.2 tap ledger already compensates delay, lift and drain and follows a moving zone, so it was not replaced. What improves its
accuracy in 2.3 is the correct zone centre (both shades, any colour).

## Tested here
tests/scene_test.py (needs python3 + opencv-python; images in tests/fixtures): Fish / Exit / mini-game / Failed / Caught screenshots are
all classified correctly, and the zone is found at the same place after recolouring it orange, yellow, red, pink, purple and cyan.
NOT compiled and NOT run on a phone (no Android SDK here). The GitHub build is the first compile: send the error line if it fails.
Template matching sizes were not measured on a real phone screen; the screenshots you sent are crops, so the first run needs a look at the Debug log.
