# Store artwork

Sources of the Google Play images in `fastlane/metadata/android/fr-FR/images/`, drawn from the launcher icon
(`app/src/main/res/drawable/ic_launcher_foreground.xml`) and the website fonts. Render them with headless Chrome:

```bash
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
"$CHROME" --headless=new --hide-scrollbars --force-device-scale-factor=1 --window-size=512,512 \
  --screenshot="$PWD/fastlane/metadata/android/fr-FR/images/icon.png" "file://$PWD/fastlane/artwork/icon.html"
"$CHROME" --headless=new --hide-scrollbars --force-device-scale-factor=1 --virtual-time-budget=3000 \
  --window-size=1024,500 \
  --screenshot="$PWD/fastlane/metadata/android/fr-FR/images/featureGraphic.png" \
  "file://$PWD/fastlane/artwork/feature-graphic.html"
for n in 1 2 3 4 5 6; do
  "$CHROME" --headless=new --hide-scrollbars --force-device-scale-factor=1 --virtual-time-budget=3000 \
    --window-size=1080,1920 \
    --screenshot="$PWD/fastlane/metadata/android/fr-FR/images/phoneScreenshots/$n.png" \
    "file://$PWD/fastlane/artwork/screenshots.html?n=$n"
done
```

The phone screenshots put the captures in `screens/` (1080 × 2424 JPEG) in a phone outline under a French caption;
the captions are in `screenshots.html`. Captures: debug build on an emulator in French, location shared once (so the
map shows « Arbre le plus proche »), demo-mode status bar (`adb shell am broadcast -a com.android.systemui.demo -e
command clock -e hhmm 0941`, and `notifications -e visible false`), a few favourites added by hand, dark mode with
`adb shell cmd uimode night yes`.

| Image | Size | Play Console |
|-------|------|--------------|
| `icon.png` | 512 × 512, full square (Play rounds the corners) | App icon |
| `featureGraphic.png` | 1024 × 500 | Feature graphic |
| `phoneScreenshots/1.png` … `6.png` | 1080 × 1920 | Phone screenshots: map, tree sheet, Herbier, genera, favourites, night |
