# LuminFish 2.6 - app design and new features

Built on 2.5: nothing was removed, same look (dark cards, drawer, tiles). New on the Home page and in Detection:

1. **Fishing order** card: four pills "1 Fish, 2 Exit, 3 Bar, 4 X". The step the macro is on is lit green, finished steps are dim green, next steps are grey.
   It follows the state text (Casting, Waiting for Exit button / Waiting for the bar, Reeling, Checking result).
2. **Setup check** card: live check marks for Service on, Fast capture running, OpenCV ready, Roblox installed (green tick / red cross).
3. **Success rate** and **Catches per hour** under the other numbers in the State card.
4. **Test screen recognition**:
   - Overlay panel (Settings list): "Test screen recognition" - shows what the Fish / Exit / X recognition sees right now, pixel search and pictures separately, with percent, position and milliseconds.
   - Detection page: same test with a 6 second delay so you can switch to Roblox first.
   - The result is also written to the Debug log ("Recognition test: ...").

Needs fast capture. NOT compiled and NOT run on a phone (no Android SDK here). The GitHub build is the first compile: send the error line if it fails.
